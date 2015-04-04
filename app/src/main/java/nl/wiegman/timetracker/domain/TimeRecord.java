package nl.wiegman.timetracker.domain;

import com.orm.SugarRecord;

import java.util.Calendar;
import java.util.Date;

public class TimeRecord extends SugarRecord<TimeRecord> {

    public static final long NULL_DATE_MILLIS = Long.MAX_VALUE;
    public static final Calendar NULL_DATE;
    static {
        NULL_DATE = Calendar.getInstance();
        NULL_DATE.setTime(new Date(NULL_DATE_MILLIS));
    }

    private Calendar checkIn = NULL_DATE;
    private Calendar checkOut = NULL_DATE;
    private Long breakInMilliseconds;
    private String note;

    public String getNote() {
        return note;
    }

    public TimeRecord setNote(String note) {
        this.note = note;
        return this;
    }

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
        long result;

        if (checkOut.getTimeInMillis() == NULL_DATE_MILLIS) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0);
            result = calendar.getTimeInMillis() - checkIn.getTimeInMillis();
        } else {
            result = checkOut.getTimeInMillis() - checkIn.getTimeInMillis();
        }

        return result;
    }

    public long getBillableDuration() {
        long result = getDuration();
        if (breakInMilliseconds != null) {
            result = result - breakInMilliseconds;
        }
        return result;
    }

    public boolean isCheckIn() {
        return checkOut.getTimeInMillis() == NULL_DATE_MILLIS;
    }

    @Override
    public String toString() {
        return "From: " + checkIn.getTime() + " to: " + checkOut.getTime();
    }
}
