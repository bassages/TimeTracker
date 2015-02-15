package timetracker.wiegman.nl.timetracker;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import timetracker.wiegman.nl.timetracker.domain.CheckIn;
import timetracker.wiegman.nl.timetracker.domain.TimeRecord;

public class MainActivity extends Activity {

    private final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        StrictMode.enableDefaults();

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
//            restoreBackup();
            showCheckInCheckOut();
        }
    }

    private void restoreBackup() {
        CheckIn.deleteAll(CheckIn.class);
        TimeRecord.deleteAll(TimeRecord.class);

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

    private void saveTimeRecord(String in, String out, int breakInMinutes) {
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

    private void showCheckInCheckOut() {
        CheckInCheckoutFragment fragment = new CheckInCheckoutFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_week) {
            showWeekOverview();
        } else if (id == R.id.action_month) {
            showMonthOverview();
        } else if (id == R.id.action_add) {
            showAdd();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAdd() {
        EditTimeRecordFragment fragment = EditTimeRecordFragment.newInstance(null);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showWeekOverview() {
        WeeksOverviewFragment fragment = WeeksOverviewFragment.newInstance(Calendar.getInstance().get(Calendar.YEAR));
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void showMonthOverview() {
        MonthsOverviewFragment fragment = MonthsOverviewFragment.newInstance(Calendar.getInstance().get(Calendar.YEAR));
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
