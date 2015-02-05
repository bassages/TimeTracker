package timetracker.wiegman.nl.timetracker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timetracker.wiegman.nl.timetracker.domain.TimeRecord;
import timetracker.wiegman.nl.timetracker.util.Formatting;
import timetracker.wiegman.nl.timetracker.util.Period;
import timetracker.wiegman.nl.timetracker.util.PeriodicRunnableExecutor;
import timetracker.wiegman.nl.timetracker.util.TimeAndDurationService;

/**
 * Shows a list of timerecords within a given period.
 * The timerecords can be deleted or edited (which is actually processed in EditTimeRecordFragemnt)
 */
public class DaysInPeriodFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final String ARG_PERIOD_TITLE = "periodTitle";
    private static final String ARG_PERIOD_FROM = "periodFrom";
    private static final String ARG_PERIOD_TO = "periodTo";

    private String periodTitle;
    private Calendar periodFrom;
    private Calendar periodTo;

    private TextView titleTextView;
    private ListView billableHoursOnDayListView;

    private BillableHoursPerDayAdapter listViewAdapter;

    private PeriodicRunnableExecutor checkedInTimeUpdaterExecutor;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimeRecordsInPeriodFragment.
     */
    public static DaysInPeriodFragment newInstance(Period period) {
        DaysInPeriodFragment fragment = new DaysInPeriodFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PERIOD_TITLE, period.getTitle());
        args.putSerializable(ARG_PERIOD_FROM, period.getFrom());
        args.putSerializable(ARG_PERIOD_TO, period.getTo());
        fragment.setArguments(args);
        return fragment;
    }

    public DaysInPeriodFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            periodTitle = getArguments().getString(ARG_PERIOD_TITLE);
            periodFrom = (Calendar) getArguments().getSerializable(ARG_PERIOD_FROM);
            periodTo = (Calendar) getArguments().getSerializable(ARG_PERIOD_TO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time_records_in_period, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.timeRecordsInPeriodDetailsTitle);
        titleTextView.setText(periodTitle);

        billableHoursOnDayListView = (ListView) rootView.findViewById(R.id.timeRecordsInPeriodListView);

        billableHoursOnDayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long timestamp) {
                Calendar day = Calendar.getInstance();
                day.setTimeInMillis(timestamp);
                showDetailsOrTimeRecordsOfDay(day);
            }
        });
        billableHoursOnDayListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long timestamp) {
                Calendar day = Calendar.getInstance();
                day.setTimeInMillis(timestamp);
                deleteTimeRecordsOfDayAfterConfirmedByUser(day);
                return true;
            }
        });

        refreshData();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isCurrentPeriodInList() && TimeAndDurationService.isCheckedIn()) {
            activateRecalculateCurrentDayUpdater();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        deactivateCheckedInTimeUpdater();
    }

    private boolean isCurrentPeriodInList() {
        return getPositionOfCurrentItem(listViewAdapter.getBillableHoursOnDays()) != null;
    }

    private void activateRecalculateCurrentDayUpdater() {
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

    private class Updater extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            long currentPeriodBillableDuration = TimeAndDurationService.getBillableDurationOnDay(Calendar.getInstance());
            return Formatting.formatDuration(currentPeriodBillableDuration);
        }

        @Override
        protected void onPostExecute(String formattedBillableDuration) {
            super.onPostExecute(formattedBillableDuration);

            Integer positionOfCurrentItem = getPositionOfCurrentItem(listViewAdapter.getBillableHoursOnDays());
            if (positionOfCurrentItem != null) {
                int index = positionOfCurrentItem - billableHoursOnDayListView.getFirstVisiblePosition();
                if (index >= 0) {
                    View dayItemView = billableHoursOnDayListView.getChildAt(index);
                    TextView billableDurationColumnTextView = (TextView) dayItemView.findViewById(R.id.billableDurationColumn);
                    billableDurationColumnTextView.setText(formattedBillableDuration);
                }
            }
        }
    }

    private Integer getPositionOfCurrentItem(List<BillableHoursOnDay> billableHoursOnDays) {
        Integer result = null;
        for (int position = 0; position < billableHoursOnDays.size(); position++) {
            BillableHoursOnDay billableHoursOnDay = billableHoursOnDays.get(position);
            if (isCurrentDay(billableHoursOnDay)) {
                result = position;
                break;
            }
        }
        return result;
    }

    private boolean isCurrentDay(BillableHoursOnDay billableHoursOnDay) {
        Calendar today = Calendar.getInstance();
        return DateUtils.isSameDay(billableHoursOnDay.getDay(), today);
    }

    private void deleteTimeRecordsOfDayAfterConfirmedByUser(final Calendar day) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        Calendar startOfDay = TimeAndDurationService.getStartOfDay(day);
                        Calendar endOfDay = TimeAndDurationService.getEndOfDay(day);

                        List<TimeRecord> timeRecordsOnDay = TimeAndDurationService.getTimeRecordsBetween(startOfDay.getTimeInMillis(), endOfDay.getTimeInMillis());
                        for (TimeRecord recordToDelete : timeRecordsOnDay) {
                            recordToDelete.delete();
                        }
                        refreshData();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked, do nothing
                        break;
                }
            }
        };
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Delete all records of " + sdf.format(day.getTime()) + "?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private void showDetailsOrTimeRecordsOfDay(Calendar day) {
        Calendar startOfDay = TimeAndDurationService.getStartOfDay(day);
        Calendar endOfDay = TimeAndDurationService.getEndOfDay(day);

        List<TimeRecord> timeRecordsOnDay = TimeAndDurationService.getTimeRecordsBetween(startOfDay.getTimeInMillis(), endOfDay.getTimeInMillis());
        int nrOfTimeRecordOnDay = timeRecordsOnDay.size();

        if (nrOfTimeRecordOnDay == 0) {
            showDialogThatThereIsNothingToDelete();
        } else if (nrOfTimeRecordOnDay == 1) {
            showEditTimeRecordFragment(timeRecordsOnDay);
        } else {
            showTimeRecordsOnDayFragment(day, startOfDay, endOfDay);
        }
    }

    private void showDialogThatThereIsNothingToDelete() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("No details available because you have not checked in on the selected date")
                .setPositiveButton("OK", dialogClickListener)
                .show();
    }

    private void showEditTimeRecordFragment(List<TimeRecord> timeRecordsOnDay) {
        EditTimeRecordFragment fragment = EditTimeRecordFragment.newInstance(timeRecordsOnDay.get(0).getId());
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showTimeRecordsOnDayFragment(Calendar day, Calendar startOfDay, Calendar endOfDay) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd-MM-yyyy");

        Period period = new Period();
        period.setFrom(startOfDay);
        period.setTo(endOfDay);
        period.setTitle(sdf.format(day.getTime()));

        TimeRecordsInPeriodFragment fragment = TimeRecordsInPeriodFragment.newInstance(period);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void refreshData() {
        List<BillableHoursOnDay> days = new ArrayList<>();

        Calendar day = (Calendar) periodFrom.clone();
        while (day.getTimeInMillis() < periodTo.getTimeInMillis()) {
            BillableHoursOnDay billableHoursOnDay = new BillableHoursOnDay();
            billableHoursOnDay.setDay((Calendar)day.clone());
            long billableDurationOnDay = TimeAndDurationService.getBillableDurationOnDay(day);
            billableHoursOnDay.setBillableDuration(billableDurationOnDay);
            days.add(billableHoursOnDay);

            day.add(Calendar.DAY_OF_MONTH, 1);
        }
        listViewAdapter = new BillableHoursPerDayAdapter(days);
        billableHoursOnDayListView.setAdapter(listViewAdapter);
    }

    private class BillableHoursPerDayAdapter extends BaseAdapter {
        private List<BillableHoursOnDay> billableHoursOnDays;

        /**
         * Constructor
         */
        public BillableHoursPerDayAdapter(List<BillableHoursOnDay> billableHoursOnDays) {
            this.billableHoursOnDays = billableHoursOnDays;
        }

        @Override
        public int getCount() {
            return billableHoursOnDays.size();
        }

        @Override
        public Object getItem(int position) {
            return billableHoursOnDays.get(position);
        }

        @Override
        public long getItemId(int position) {
            return billableHoursOnDays.get(position).getDay().getTimeInMillis();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            BillableHoursOnDay billableHoursOnDay = billableHoursOnDays.get(position);
            Date day = billableHoursOnDay.getDay().getTime();

            View row = View.inflate(getActivity(), R.layout.time_records_in_period_item, null);

            TextView dayColumn = (TextView) row.findViewById(R.id.dayColumn);
            SimpleDateFormat dayInWeekFormat = new SimpleDateFormat("EEE");
            String formattedDay = dayInWeekFormat.format(day);
            dayColumn.setText(formattedDay);

            TextView dateColumn = (TextView) row.findViewById(R.id.dateColumn);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM");
            String formattedDate = simpleDateFormat.format(day);
            dateColumn.setText(formattedDate);

            TextView billableHoursColumn = (TextView) row.findViewById(R.id.billableDurationColumn);
            long billableDuration = billableHoursOnDay.getBillableDuration();
            String formattedBillableDuration = Formatting.formatDuration(billableDuration);
            billableHoursColumn.setText(formattedBillableDuration);

            if (isCurrentDay(billableHoursOnDay)) {
                dayColumn.setTypeface(Typeface.DEFAULT_BOLD);
                dateColumn.setTypeface(Typeface.DEFAULT_BOLD);
                billableHoursColumn.setTypeface(Typeface.DEFAULT_BOLD);
            } else if (billableDuration == 0) {
                dayColumn.setEnabled(false);
                dateColumn.setEnabled(false);
                billableHoursColumn.setEnabled(false);
            }

            return row;
        }

        public List<BillableHoursOnDay> getBillableHoursOnDays() {
            return billableHoursOnDays;
        }
    }

}
