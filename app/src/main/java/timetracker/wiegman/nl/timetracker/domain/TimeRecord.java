package timetracker.wiegman.nl.timetracker.domain;

import com.orm.SugarRecord;

import java.util.Calendar;

public class TimeRecord extends SugarRecord<TimeRecord> {

    private Calendar checkIn;

    private Calendar checkOut;

    private Long breakInMilliseconds;

    public Calendar getCheckIn() {
        return checkIn;
    }

    public TimeRecord setCheckIn(Calendar checkIn) {
        this.checkIn = checkIn;
        return this;
    }

    public Calendar getCheckOut() {
        return checkOut;
    }

    public TimeRecord setCheckOut(Calendar checkOut) {
        this.checkOut = checkOut;
        return this;
    }

    public Long getBreakInMilliseconds() {
        long result = 0;
        if (breakInMilliseconds != null) {
            result = breakInMilliseconds;
        }
        return result;
    }

    public TimeRecord setBreakInMilliseconds(long breakInMilliseconds) {
        this.breakInMilliseconds = breakInMilliseconds;
        return this;
    }

    public long getDuration() {
        return checkOut.getTimeInMillis() - checkIn.getTimeInMillis();
    }

    public long getBillableDuration() {
        long result = getDuration();
        if (breakInMilliseconds != null) {
            result = result - breakInMilliseconds;
        }
        return result;
    }
}
