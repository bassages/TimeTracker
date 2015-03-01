package nl.wiegman.timetracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

import nl.wiegman.timetracker.util.Formatting;
import nl.wiegman.timetracker.period.Period;
import nl.wiegman.timetracker.util.FragmentHelper;
import nl.wiegman.timetracker.util.PeriodicRunnableExecutor;
import nl.wiegman.timetracker.util.TimeAndDurationService;
import nl.wiegman.timetracker.period.WeekPeriod;
import nl.wiegman.timetracker.widget.CheckInCheckOutWidgetProvider;

public class CheckInCheckoutFragment extends Fragment {
    private static final int MENU_ITEM_WEEK_OVERVIEW_ID = 0;
    private static final int MENU_ITEM_MONTH_OVERVIEW_ID = 1;

    private final String LOG_TAG = this.getClass().getSimpleName();

    private TextView todaysTotalTextView;
    private TextView thisWeeksTotalTextView;
    private ImageView pausePlayImageView;

    private PeriodicRunnableExecutor checkedInTimeUpdaterExecutor;

    @Override
    public void onDestroy() {
        super.onDestroy();
        deactivateCheckedInTimeUpdater();
    }

    @Override
    public void onStart() {
        super.onStart();
        activateCheckedInTimeUpdater();
    }

    @Override
    public void onPause() {
        super.onPause();
        deactivateCheckedInTimeUpdater();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_checkin_checkout, container, false);

        todaysTotalTextView = (TextView) rootView.findViewById(R.id.todaysTotalTextView);
        thisWeeksTotalTextView = (TextView) rootView.findViewById(R.id.thisWeeksTotalTextView);

        pausePlayImageView = (ImageView) rootView.findViewById(R.id.pausePlayImageView);
        pausePlayImageView.setOnClickListener(new CheckInCheckOutButtonOnClickListener());

        todaysTotalTextView.setOnClickListener(new ShowTodaysDetails());
        thisWeeksTotalTextView.setOnClickListener(new ShowThisWeeksTimeRecords());

        new IconUpdater().execute();
        new Updater().execute();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_ITEM_MONTH_OVERVIEW_ID, 0, R.string.action_month_overview);
        menu.add(0, MENU_ITEM_WEEK_OVERVIEW_ID, 0, R.string.action_week_overview);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == MENU_ITEM_WEEK_OVERVIEW_ID) {
            showWeekOverview();
        } else if (id == MENU_ITEM_MONTH_OVERVIEW_ID) {
            showMonthOverview();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWeekOverview() {
        WeeksOverviewFragment fragment = WeeksOverviewFragment.newInstance(Calendar.getInstance().get(Calendar.YEAR));
        FragmentHelper.showFragment(getActivity(), fragment);
    }

    private void showMonthOverview() {
        MonthsOverviewFragment fragment = MonthsOverviewFragment.newInstance(Calendar.getInstance().get(Calendar.YEAR));
        FragmentHelper.showFragment(getActivity(), fragment);
    }

    private class ShowThisWeeksTimeRecords implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Calendar today = Calendar.getInstance();
            Period period = new WeekPeriod(today);
            DaysInPeriodFragment fragment = DaysInPeriodFragment.newInstance(period);
            FragmentHelper.showFragment(getActivity(), fragment);
        }
    }

    private class ShowTodaysDetails implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Calendar day = Calendar.getInstance();
            DayDetailsHelper dayDetailsHelper = new DayDetailsHelper(getActivity());
            dayDetailsHelper.showDetailsOrTimeRecordsOfDay(day);
        }
    }

    private void checkIn() {
        TimeAndDurationService.checkIn();
        pausePlayImageView.setImageResource(R.drawable.ic_av_pause_circle_outline_blue);
        updateWidget();
    }

    private void checkOut() {
        TimeAndDurationService.checkOut();
        pausePlayImageView.setImageResource(R.drawable.ic_av_play_circle_outline_blue);
        updateWidget();
    }

    private void updateWidget() {
        try {
            CheckInCheckOutWidgetProvider.getUpdateWidgetIntent(getActivity()).send();
        } catch (PendingIntent.CanceledException e) {
            Log.e(LOG_TAG, "Unable to update widget: " + e.getMessage());
        }
    }

    private class CheckInCheckOutButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            if (TimeAndDurationService.isCheckedIn()) {
                checkOut();
            } else {
                checkIn();
            }
        }
    }

    private void activateCheckedInTimeUpdater() {
        checkedInTimeUpdaterExecutor = new PeriodicRunnableExecutor(1000, new CheckedInTimeUpdater());
        checkedInTimeUpdaterExecutor.start();
    }

    private void deactivateCheckedInTimeUpdater() {
        if (checkedInTimeUpdaterExecutor != null) {
            checkedInTimeUpdaterExecutor.stop();
            checkedInTimeUpdaterExecutor = null;
        }
    }

    private class CheckedInTimeUpdater implements Runnable {
        @Override
        public void run() {
            new Updater().execute();
        }
    }

    private class Updater extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... voids) {
            Calendar today = Calendar.getInstance();

            long dayTotal = TimeAndDurationService.getBillableDurationOnDay(today);
            long weekTotal = TimeAndDurationService.getBillableDurationInWeekOfDay(today);

            return new String[] {
                                    Formatting.formatDuration(dayTotal),
                                    Formatting.formatDuration(weekTotal),
                                };
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            todaysTotalTextView.setText(s[0]);
            thisWeeksTotalTextView.setText(s[1]);
        }
    }

    private class IconUpdater extends AsyncTask<Void, Void, Boolean>  {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return TimeAndDurationService.isCheckedIn();
        }

        @Override
        protected void onPostExecute(Boolean checkedIn) {
            if (checkedIn) {
                pausePlayImageView.setImageResource(R.drawable.ic_av_pause_circle_outline_blue);
            } else {
                pausePlayImageView.setImageResource(R.drawable.ic_av_play_circle_outline_blue);
            }
        }
    }

}
