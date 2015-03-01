package nl.wiegman.timetracker.period;

import java.util.Calendar;

public abstract class AbstractPeriod implements Period {

    private Calendar from;
    private Calendar to;

    @Override
    public Calendar getFrom() {
        return from;
    }

    protected void setFrom(Calendar from) {
        this.from = from;
    }

    @Override
    public Calendar getTo() {
        return to;
    }

    protected void setTo(Calendar to) {
        this.to = to;
    }

}
