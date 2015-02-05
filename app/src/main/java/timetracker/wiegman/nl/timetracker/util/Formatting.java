package timetracker.wiegman.nl.timetracker.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Calendar;

public class Formatting {

    public static String getWeekOverViewTitle(Calendar dayInWeek) {
        return String.format("Week %d | Year %4d", dayInWeek.get(Calendar.WEEK_OF_YEAR), dayInWeek.get(Calendar.YEAR));
    }

    public static String getMonthOverviewTitle(Calendar dayInMonth) {
        return String.format("Month %d | Year %4d", dayInMonth.get(Calendar.MONTH) + 1, dayInMonth.get(Calendar.YEAR));
    }

    public static String formatDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, "H:mm:ss");
    }
}
