package timetracker.wiegman.nl.timetracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

import timetracker.wiegman.nl.timetracker.util.Formatting;
import timetracker.wiegman.nl.timetracker.util.Period;
import timetracker.wiegman.nl.timetracker.util.PeriodicRunnableExecutor;
import timetracker.wiegman.nl.timetracker.util.TimeAndDurationService;

public class CheckInCheckoutFragment extends Fragment {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private TextView todaysTotalTextView;
    private TextView thisWeeksTotalTextView;
    private ImageView pausePlayImageView;

    private PeriodicRunnableExecutor checkedInTimeUpdaterExecutor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
        View rootView = inflater.inflate(R.layout.fragment_checkin_checkout, container, false);

        todaysTotalTextView = (TextView) rootView.findViewById(R.id.todaysTotalTextView);
        thisWeeksTotalTextView = (TextView) rootView.findViewById(R.id.thisWeeksTotalTextView);

        pausePlayImageView = (ImageView) rootView.findViewById(R.id.pausePlayImageView);
        pausePlayImageView.setOnClickListener(new CheckInCheckOutButtonOnClickListener());
        setPausePlayImage();

        todaysTotalTextView.setOnClickListener(new ShowTodaysDetails());
        thisWeeksTotalTextView.setOnClickListener(new ShowThisWeeksTimeRecords());

        new CheckedInTimeUpdater().run();

        return rootView;
    }

    private void setPausePlayImage() {
        if (TimeAndDurationService.isCheckedIn()) {
            pausePlayImageView.setImageResource(R.drawable.ic_av_pause_circle_outline);
        } else {
            pausePlayImageView.setImageResource(R.drawable.ic_av_play_circle_outline);
        }
    }

    private class ShowThisWeeksTimeRecords implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Calendar today = Calendar.getInstance();

            Period period = TimeAndDurationService.getWeek(today);

            DaysInPeriodFragment fragment = DaysInPeriodFragment.newInstance(period);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
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
        pausePlayImageView.setImageResource(R.drawable.ic_av_pause_circle_outline);
    }

    private void checkOut() {
        TimeAndDurationService.checkOut();
        pausePlayImageView.setImageResource(R.drawable.ic_av_play_circle_outline);
    }

    private class CheckInCheckOutButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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

}
