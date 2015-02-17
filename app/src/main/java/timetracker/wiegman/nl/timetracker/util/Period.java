package timetracker.wiegman.nl.timetracker.util;

import java.io.Serializable;
import java.util.Calendar;

public interface Period extends Serializable {
    Calendar getFrom();

    Calendar getTo();

    String getTitle();

    Period getNext();

    Period getPrevious();
}
