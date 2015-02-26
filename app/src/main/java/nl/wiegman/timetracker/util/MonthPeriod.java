package nl.wiegman.timetracker.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MonthPeriod extends AbstractPeriod {

    public MonthPeriod(Calendar dayInMonth) {
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
        return new MonthPeriod(previous);
    }

    @Override
    public long getBillableDuration() {
        return TimeAndDurationService.getBillableDurationInMonthOfDay(getFrom());
    }

    @Override
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.MONTH, 1);
        return new MonthPeriod(next);
    }
}
