package nl.wiegman.timetracker.period;

import java.util.Calendar;

import nl.wiegman.timetracker.util.TimeAndDurationService;

public abstract class AbstractPeriod implements Period {

    private Calendar from;
    private Calendar to;

    @Override
    public Calendar getFrom() {
        return from;
    }

    void setFrom(Calendar from) {
        this.from = from;
    }

    @Override
    public Calendar getTo() {
        return to;
    }

    protected void setTo(Calendar to) {
        this.to = to;
    }

    @Override
    public long getBillableDuration() {
        return TimeAndDurationService.getBillableDurationInPeriod(this);
    }

}
