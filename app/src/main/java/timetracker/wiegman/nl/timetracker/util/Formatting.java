package timetracker.wiegman.nl.timetracker.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Formatting {

    public static String getWeekOverViewTitle(Calendar dayInWeek) {
        return String.format("Week %d - %4d", dayInWeek.get(Calendar.WEEK_OF_YEAR), dayInWeek.get(Calendar.YEAR));
    }

    public static String getMonthOverviewTitle(Calendar dayInMonth) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
        return StringUtils.capitalize(sdf.format(dayInMonth.getTime())) + " " + dayInMonth.get(Calendar.YEAR);
    }

    public static String formatDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, "H:mm:ss");
    }
}
