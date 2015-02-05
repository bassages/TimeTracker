package timetracker.wiegman.nl.timetracker.util;

import android.util.Log;

import com.orm.query.Condition;
import com.orm.query.Select;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timetracker.wiegman.nl.timetracker.domain.CheckIn;
import timetracker.wiegman.nl.timetracker.domain.TimeRecord;

public class TimeAndDurationService {

    private static final String LOG_TAG = TimeAndDurationService.class.getName();

    private static final long DEFAULT_BREAKDURATION = TimeUnit.MINUTES.toMillis(30);

    public static boolean isCheckedIn() {
        return getCheckIn() != null;
    }

    public static CheckIn getCheckIn() {
        CheckIn result = null;
        List<CheckIn> checkIns = CheckIn.listAll(CheckIn.class);
        if (!checkIns.isEmpty()) {
            result = checkIns.get(0);
        }
        return result;
    }

    public static long getTotalDurationCheckedIn() {
        long totalDurationCheckedIn = 0;
        CheckIn checkIn = getCheckIn();
        if (checkIn != null) {
            totalDurationCheckedIn = Calendar.getInstance().getTimeInMillis() - checkIn.getTimestamp().getTimeInMillis();
            totalDurationCheckedIn -= getBreakDuration(totalDurationCheckedIn);
        }
        return totalDurationCheckedIn;
    }

    private static long getBreakDuration(long totalCheckedInDuration) {
        long result = 0;

        long breakFrom = TimeUnit.HOURS.toMillis(5);

        if (totalCheckedInDuration > breakFrom) {
            long i = totalCheckedInDuration - breakFrom;
            if (i > DEFAULT_BREAKDURATION) {
                result = DEFAULT_BREAKDURATION;
            } else {
                result = i;
            }
        }
        return result;
    }

    /**
     * Returns the total time on a given date.
     *
     * If the date is the current date and the user is checked in
     * then the duration of this checkIn will be included in the result.
     */
    public static long getBillableDurationOnDay(Calendar day) {
        long dayTotal = 0;

        long startOfDay = DateUtils.truncate(day, Calendar.DAY_OF_MONTH).getTimeInMillis();
        long endOfDay = startOfDay + DateUtils.MILLIS_PER_DAY;

        List<TimeRecord> timeRecordsOnDay = getTimeRecordsBetween(startOfDay, endOfDay);

        for (TimeRecord timeRecord : timeRecordsOnDay) {
            dayTotal += timeRecord.getBillableDuration();
        }

        Calendar today = Calendar.getInstance();
        if (DateUtils.isSameDay(day, today) && isCheckedIn()) {
            long totalDurationCheckedIn = getTotalDurationCheckedIn();
            dayTotal += totalDurationCheckedIn;
        }
        return dayTotal;
    }

    public static long getBillableDurationInWeekOfDay(Calendar dayInWeek) {
        long weekTotal = 0;

        Iterator<Calendar> iterator = DateUtils.iterator(dayInWeek, DateUtils.RANGE_WEEK_MONDAY);
        while(iterator.hasNext()) {
            Calendar next = iterator.next();
            weekTotal += getBillableDurationOnDay(next);
        }
        return weekTotal;
    }

    public static List<TimeRecord> getTimeRecordsBetween(long start, long end) {
        return Select.from(TimeRecord.class)
                .where(Condition.prop("check_in").gt(start))
                .and(Condition.prop("check_out").lt(end))
                .orderBy("check_in")
                .list();
    }

    public static void checkIn() {
        new CheckIn().setTimestamp(Calendar.getInstance()).save();
    }

    public static void checkOut() {
        CheckIn checkIn = TimeAndDurationService.getCheckIn();
        Calendar checkInTimestamp = checkIn.getTimestamp();
        Calendar checkOutTimestamp = Calendar.getInstance();

        TimeRecord timeRecord = new TimeRecord();
        timeRecord.setCheckIn(checkInTimestamp).setCheckOut(checkOutTimestamp);
        timeRecord.setBreakInMilliseconds(getBreakDuration(timeRecord.getDuration()));
        timeRecord.save();

        checkIn.delete();
    }

    public static Period getWeek(Calendar dayInWeek) {
        Period result = new Period();
        result.setTitle(Formatting.getWeekOverViewTitle(dayInWeek));

        Calendar monday = DateUtils.iterator(dayInWeek, DateUtils.RANGE_WEEK_MONDAY).next();
        Calendar startOfMonday = DateUtils.truncate(monday, Calendar.DAY_OF_MONTH);
        result.setFrom(startOfMonday);

        Calendar endOfSunday = (Calendar) startOfMonday.clone();
        endOfSunday.add(Calendar.WEEK_OF_YEAR, 1);
        endOfSunday.add(Calendar.MILLISECOND, -1);

        result.setTo(endOfSunday);

        return result;
    }

    public static long getBillableDurationInMonthOfDay(Calendar dayInMonth) {
        long monthTotal = 0;

        Calendar start = getStartOfMonth(dayInMonth);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);

        Log.d(LOG_TAG, "From: " + start.getTime().toString() + " to " + end.getTime().toString());

        while (start.getTimeInMillis() < end.getTimeInMillis()) {
            monthTotal += getBillableDurationOnDay(start);
            start.add(Calendar.DAY_OF_MONTH, 1);
        }

        return monthTotal;
    }

    public static Period getMonth(Calendar dayInMonth) {
        Period result = new Period();
        result.setTitle(Formatting.getMonthOverviewTitle(dayInMonth));

        Calendar start = getStartOfMonth(dayInMonth);
        result.setFrom(start);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);

        result.setTo(end);

        return result;
    }

    private static Calendar getStartOfMonth(Calendar dayInMonth) {
        Calendar start = ((Calendar)dayInMonth.clone());
        start = DateUtils.truncate(start, Calendar.MONTH);
        return start;
    }

    public static Calendar getStartOfDay(Calendar day) {
        Calendar startOfDay = (Calendar) day.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        return startOfDay;
    }

    public static Calendar getEndOfDay(Calendar day) {
        Calendar endOfDay = (Calendar) day.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 0);
        return endOfDay;
    }

    public static Calendar getFirstDayOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.YEAR, year);
        return calendar;
    }
}
