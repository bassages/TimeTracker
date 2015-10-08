package nl.wiegman.timetracker.period;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;

import nl.wiegman.timetracker.util.TimeAndDurationService;

public class Week extends AbstractPeriod implements Period {

    public Week(Calendar dayInWeek) {
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
    public Period getPrevious() {
        Calendar previous = (Calendar) getFrom().clone();
        previous.add(Calendar.WEEK_OF_YEAR, -1);
        return new Week(previous);
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.WEEK_OF_YEAR, 1);
        return new Week(next);
    }
}