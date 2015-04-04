package nl.wiegman.timetracker.period;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.wiegman.timetracker.util.TimeAndDurationService;

public class Day extends AbstractPeriod {

    public Day(Calendar day) {
        setFrom(TimeAndDurationService.getStartOfDay(day));
        setTo(TimeAndDurationService.getEndOfDay(day));
    }

    @Override
    public String getTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd-MM-yyyy");
        return sdf.format(getFrom().getTime());
    }

    @Override
    public Period getPrevious() {
        Calendar previous = (Calendar) getFrom().clone();
        previous.add(Calendar.DAY_OF_MONTH, -1);
        return new Day(previous);
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.DAY_OF_MONTH, 1);
        return new Day(next);
    }
}
