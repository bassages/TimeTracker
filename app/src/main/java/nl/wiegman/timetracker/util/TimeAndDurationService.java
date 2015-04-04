package nl.wiegman.timetracker.util;

import android.text.format.Time;

import com.orm.query.Condition;
import com.orm.query.Select;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.wiegman.timetracker.domain.TimeRecord;
import nl.wiegman.timetracker.period.Period;

public class TimeAndDurationService {
    private static final String LOG_TAG = TimeAndDurationService.class.getSimpleName();

    private static final long DEFAULT_BREAK_DURATION = TimeUnit.MINUTES.toMillis(30);

    public static boolean isCheckedIn() {
        return getCheckIn() != null;
    }

    public static TimeRecord getCheckIn() {
        String nullDateMillis = Long.toString(TimeRecord.NULL_DATE_MILLIS);
        List<TimeRecord> checkIn = TimeRecord.find(TimeRecord.class, "check_out = ?", nullDateMillis);
        TimeRecord result = null;
        if (!checkIn.isEmpty()) {
            result = checkIn.get(0);
        }
        return result;
    }

    private static long getBreakDuration(long totalCheckedInDuration) {
        long result = 0;

        long breakFrom = TimeUnit.HOURS.toMillis(5);

        if (totalCheckedInDuration > breakFrom) {
            long i = totalCheckedInDuration - breakFrom;
            if (i > DEFAULT_BREAK_DURATION) {
                result = DEFAULT_BREAK_DURATION;
            } else {
                result = i;
            }
        }
        return result;
    }

    public static long getBillableDurationInPeriod(Period period) {
        long result = 0;

        List<TimeRecord> timeRecordsInPeriod = getTimeRecordsBetween(period.getFrom(), period.getTo());
        for (TimeRecord timeRecord : timeRecordsInPeriod) {
            result += timeRecord.getBillableDuration();
        }

        return result;
    }

    public static List<TimeRecord> getTimeRecordsBetween(Calendar from, Calendar to) {
        String fromTimeInMillis = Long.toString(from.getTimeInMillis());
        String toTimeInMillis = Long.toString(to.getTimeInMillis());
        return TimeRecord.find(TimeRecord.class, "check_in > ? AND check_in < ? order by check_in", fromTimeInMillis, toTimeInMillis);
    }

    public static TimeRecord checkIn() {
        TimeRecord timeRecord = new TimeRecord();
        Calendar checkIn = Calendar.getInstance();
        checkIn.set(Calendar.MILLISECOND, 0);
        timeRecord.setCheckIn(checkIn);
        timeRecord.save();
        return timeRecord;
    }

    public static TimeRecord checkOut() {
        TimeRecord timeRecord = TimeAndDurationService.getCheckIn();

        Calendar checkOutTimestamp = Calendar.getInstance();
        checkOutTimestamp.set(Calendar.MILLISECOND, 0);

        timeRecord.setCheckOut(checkOutTimestamp);

        if (timeRecord.getBreakInMilliseconds() == null) {
            timeRecord.setBreakInMilliseconds(getBreakDuration(timeRecord.getDuration()));
        }

        timeRecord.save();

        return timeRecord;
    }

    public static Calendar getStartOfMonth(Calendar dayInMonth) {
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
