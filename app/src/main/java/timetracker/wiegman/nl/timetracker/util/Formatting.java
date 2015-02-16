package timetracker.wiegman.nl.timetracker.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class Formatting {

    public static String formatDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, "H:mm:ss");
    }
}
