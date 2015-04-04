package nl.wiegman.timetracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import nl.wiegman.timetracker.domain.TimeRecord;

public class Backup {

    public static void restore() {
        TimeRecord.deleteAll(TimeRecord.class);

        // 9-2015
        saveTimeRecord("27-02-2015 06:49:57", "27-02-2015 14:55:53", 30);
        saveTimeRecord("26-02-2015 07:01:12", "26-02-2015 15:40:04", 30);
        saveTimeRecord("25-02-2015 06:58:28", "25-02-2015 14:16:44", 30);
        saveTimeRecord("24-02-2015 06:54:40", "24-02-2015 15:37:43", 30);
        saveTimeRecord("23-02-2015 07:00:00", "23-02-2015 16:04:02", 30);

        // 8-2015
        saveTimeRecord("20-02-2015 06:57:10", "20-02-2015 13:36:16", 30);
        saveTimeRecord("19-02-2015 07:00:33", "19-02-2015 16:17:27", 30);
        saveTimeRecord("18-02-2015 06:28:58", "18-02-2015 15:54:09", 30);
        saveTimeRecord("17-02-2015 07:00:00", "17-02-2015 16:48:46", 30);
        saveTimeRecord("16-02-2015 07:00:01", "16-02-2015 16:38:25", 30);

        // 7-2015
        saveTimeRecord("13-02-2015 07:03:00", "13-02-2015 15:17:02", 30);
        saveTimeRecord("12-02-2015 06:56:29", "12-02-2015 16:01:07", 30);
        saveTimeRecord("11-02-2015 07:00:00", "11-02-2015 15:26:03", 30);
        saveTimeRecord("10-02-2015 06:59:31", "10-02-2015 15:35:57", 30);
        saveTimeRecord("09-02-2015 06:58:37", "09-02-2015 15:51:26", 30);

        // 6-2015
        saveTimeRecord("06-02-2015 07:03:54", "06-02-2015 15:06:45", 30);
        saveTimeRecord("05-02-2015 07:01:08", "05-02-2015 15:54:13", 30);
        saveTimeRecord("04-02-2015 06:54:50", "04-02-2015 14:44:42", 30);
        saveTimeRecord("03-02-2015 06:59:43", "03-02-2015 16:31:40", 30);
        saveTimeRecord("02-02-2015 07:05:42", "02-02-2015 15:33:37", 30);

        // 5-2015
        saveTimeRecord("30-01-2015 07:00:00", "30-01-2015 15:34:00", 30);
        saveTimeRecord("29-01-2015 06:58:00", "29-01-2015 15:31:00", 30);
        saveTimeRecord("28-01-2015 06:58:00", "28-01-2015 15:52:00", 30);
        saveTimeRecord("27-01-2015 06:57:00", "27-01-2015 14:22:00", 30);
        saveTimeRecord("26-01-2015 06:57:00", "26-01-2015 16:20:00", 30);

        // 4-2015
        saveTimeRecord("23-01-2015 07:00:00", "23-01-2015 15:50:00", 30);
        saveTimeRecord("22-01-2015 06:58:00", "22-01-2015 16:23:00", 30);
        saveTimeRecord("21-01-2015 06:56:00", "21-01-2015 16:47:00", 30);
        saveTimeRecord("20-01-2015 06:48:00", "20-01-2015 16:11:00", 30);
        saveTimeRecord("19-01-2015 07:00:00", "19-01-2015 16:18:00", 30);

        // 3-2015
        saveTimeRecord("16-01-2015 06:42:00", "16-01-2015 11:50:00", 0);
        saveTimeRecord("15-01-2015 06:56:00", "15-01-2015 16:45:00", 30);
        saveTimeRecord("14-01-2015 06:58:00", "14-01-2015 15:34:00", 30);
        saveTimeRecord("13-01-2015 06:58:00", "13-01-2015 15:46:00", 30);
        saveTimeRecord("12-01-2015 06:56:00", "12-01-2015 15:37:00", 30);

        // 2-2015
        saveTimeRecord("09-01-2015 07:12:00", "09-01-2015 14:03:00", 30);
        saveTimeRecord("08-01-2015 06:56:00", "08-01-2015 15:57:00", 30);
        saveTimeRecord("07-01-2015 07:01:00", "07-01-2015 15:55:00", 30);
        saveTimeRecord("06-01-2015 06:58:00", "06-01-2015 15:56:00", 30);
        saveTimeRecord("05-01-2015 07:00:00", "05-01-2015 16:40:00", 30);
    }

    private static void saveTimeRecord(String in, String out, int breakInMinutes) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            Calendar inCalendar = Calendar.getInstance();
            inCalendar.set(Calendar.MILLISECOND, 0);
            inCalendar.setTime(sdf.parse(in));

            Calendar outCalendar = Calendar.getInstance();
            outCalendar.set(Calendar.MILLISECOND, 0);
            outCalendar.setTime(sdf.parse(out));

            new TimeRecord().setCheckIn(inCalendar).setCheckOut(outCalendar).setBreakInMilliseconds(TimeUnit.MINUTES.toMillis(breakInMinutes)).save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
