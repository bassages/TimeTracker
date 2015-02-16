package timetracker.wiegman.nl.timetracker.util;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;

public class WeekPeriod extends AbstractPeriod implements Period{

    private final int weekNumber;
    private final int year;

    public WeekPeriod(Calendar dayInWeek) {
        this.weekNumber = dayInWeek.get(Calendar.WEEK_OF_YEAR);
        this.year = dayInWeek.get(Calendar.YEAR);

        Calendar startOfMonday = DateUtils.iterator(dayInWeek, DateUtils.RANGE_WEEK_MONDAY).next();
        startOfMonday = TimeAndDurationService.getStartOfDay(startOfMonday);

        Calendar endOfSunday = (Calendar) startOfMonday.clone();
        endOfSunday.add(Calendar.WEEK_OF_YEAR, 1);
        endOfSunday.add(Calendar.MILLISECOND, -1);

        setFrom(startOfMonday);
        setTo(endOfSunday);
    }

    @Override
    public String getTitle() {
        return String.format("Week %d - %4d", getFrom().get(Calendar.WEEK_OF_YEAR), getFrom().get(Calendar.YEAR));
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.WEEK_OF_YEAR, 1);
        return new WeekPeriod(next);
    }
}
