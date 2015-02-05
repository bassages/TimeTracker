package timetracker.wiegman.nl.timetracker;

import java.util.Calendar;

public class BillableHoursOnDay {

    private Calendar day;

    private long billableDuration;

    public Calendar getDay() {
        return day;
    }

    public void setDay(Calendar day) {
        this.day = day;
    }

    public long getBillableDuration() {
        return billableDuration;
    }

    public void setBillableDuration(long billableDuration) {
        this.billableDuration = billableDuration;
    }
}
