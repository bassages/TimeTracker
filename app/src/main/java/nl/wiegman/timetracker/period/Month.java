package nl.wiegman.timetracker.period;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.wiegman.timetracker.util.TimeAndDurationService;

public class Month extends AbstractPeriod {

    public Month(Calendar dayInMonth) {
        Calendar start = TimeAndDurationService.getStartOfMonth(dayInMonth);
        setFrom(start);

        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.MILLISECOND, -1);

        setTo(end);
    }

    @Override
    public String getTitle() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
        return StringUtils.capitalize(sdf.format(getFrom().getTime())) + " " + getFrom().get(Calendar.YEAR);
    }

    @Override
    public Period getPrevious() {
        Calendar previous = (Calendar) getFrom().clone();
        previous.add(Calendar.MONTH, -1);
        return new Month(previous);
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.MONTH, 1);
        return new Month(next);
    }
}
