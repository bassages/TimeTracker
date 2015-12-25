package nl.wiegman.timetracker.period;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.wiegman.timetracker.util.TimeAndDurationService;

public class Year extends AbstractPeriod {

    public Year(Calendar dayInYear) {
        Calendar start = TimeAndDurationService.getFirstDayOfYear(dayInYear.get(Calendar.YEAR));
        setFrom(start);

        Calendar end = (Calendar) start.clone();
        end.set(Calendar.MONTH, Calendar.DECEMBER);
        end.set(Calendar.DAY_OF_MONTH, 31);

        setTo(end);
    }

    @Override
    public String getTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        return StringUtils.capitalize(sdf.format(getFrom().getTime())) + " " + getFrom().get(Calendar.YEAR);
    }

    @Override
    public Period getPrevious() {
        Calendar previous = (Calendar) getFrom().clone();
        previous.add(Calendar.YEAR, -1);
        return new Year(previous);
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.YEAR, 1);
        return new Year(next);
    }
}
