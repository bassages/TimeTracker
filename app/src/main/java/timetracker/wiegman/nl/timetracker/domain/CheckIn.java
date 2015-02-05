package timetracker.wiegman.nl.timetracker.domain;

import com.orm.SugarRecord;

import java.util.Calendar;

public class CheckIn extends SugarRecord<CheckIn> {

    private Calendar timestamp;

    public Calendar getTimestamp() {
        return timestamp;
    }

    public CheckIn setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
