package timetracker.wiegman.nl.timetracker.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MonthPeriod extends AbstractPeriod {

    private final int monthNumber;
    private final int year;

    public MonthPeriod(Calendar dayInMonth) {
        this.monthNumber = dayInMonth.get(Calendar.MONTH);
        this.year = dayInMonth.get(Calendar.YEAR);

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
    public Period getNext() {
        Calendar next = (Calendar) getFrom().clone();
        next.add(Calendar.MONTH, 1);
        return new MonthPeriod(next);
    }
}
