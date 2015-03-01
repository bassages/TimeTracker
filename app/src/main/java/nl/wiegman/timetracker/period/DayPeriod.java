package nl.wiegman.timetracker.period;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.wiegman.timetracker.util.TimeAndDurationService;

public class DayPeriod extends AbstractPeriod {

    public DayPeriod(Calendar day) {
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
        return new DayPeriod(previous);
    }

    @Override
    public long getBillableDuration() {
        return TimeAndDurationService.getBillableDurationOnDay(getFrom());
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.DAY_OF_MONTH, 1);
        return new DayPeriod(next);
    }
}
