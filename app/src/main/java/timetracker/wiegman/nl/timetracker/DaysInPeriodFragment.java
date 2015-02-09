package timetracker.wiegman.nl.timetracker;

import android.app.Fragment;
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

import timetracker.wiegman.nl.timetracker.util.Formatting;
import timetracker.wiegman.nl.timetracker.util.Period;
import timetracker.wiegman.nl.timetracker.util.PeriodicRunnableExecutor;
import timetracker.wiegman.nl.timetracker.util.TimeAndDurationService;

/**
 * Shows a list of days within a given period.
 * For each dat the billable duration is shown.
 *
 * The timerecords on a day can be deleted or edited.
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
    private ListView billableDurationOnDayListView;

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

        billableDurationOnDayListView = (ListView) rootView.findViewById(R.id.timeRecordsInPeriodListView);
        billableDurationOnDayListView.setOnItemClickListener(new BillableDurationOnDayItemClickListener());
        billableDurationOnDayListView.setOnItemLongClickListener(new BillableDurationLongClickListener());

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
                int index = positionOfCurrentItem - billableDurationOnDayListView.getFirstVisiblePosition();
                if (index >= 0) {
                    View dayItemView = billableDurationOnDayListView.getChildAt(index);
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
        billableDurationOnDayListView.setAdapter(listViewAdapter);
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
            long billableDuration = billableHoursOnDay.getBillableDuration();

            View row = View.inflate(getActivity(), R.layout.time_records_in_period_item, null);

            TextView dayColumn = getDayOfWeekColumnTextView(day, row);
            TextView dateColumn = getDateColumnTextView(day, row);
            TextView billableDurationColumn = getBillableDurationTextView(row, billableDuration);

            if (isCurrentDay(billableHoursOnDay)) {
                dayColumn.setTypeface(Typeface.DEFAULT_BOLD);
                dateColumn.setTypeface(Typeface.DEFAULT_BOLD);
                billableDurationColumn.setTypeface(Typeface.DEFAULT_BOLD);
            } else if (billableDuration == 0) {
                dayColumn.setEnabled(false);
                dateColumn.setEnabled(false);
                billableDurationColumn.setEnabled(false);
            }

            return row;
        }

        private TextView getDayOfWeekColumnTextView(Date day, View row) {
            TextView dayColumn = (TextView) row.findViewById(R.id.dayOfWeekColumn);
            SimpleDateFormat dayInWeekFormat = new SimpleDateFormat("EEE");
            String formattedDay = dayInWeekFormat.format(day);
            dayColumn.setText(formattedDay);
            return dayColumn;
        }

        private TextView getDateColumnTextView(Date day, View row) {
            TextView dateColumn = (TextView) row.findViewById(R.id.dateColumn);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM");
            String formattedDate = simpleDateFormat.format(day);
            dateColumn.setText(formattedDate);
            return dateColumn;
        }

        private TextView getBillableDurationTextView(View row, long billableDuration) {
            TextView billableHoursColumn = (TextView) row.findViewById(R.id.billableDurationColumn);
            String formattedBillableDuration = Formatting.formatDuration(billableDuration);
            billableHoursColumn.setText(formattedBillableDuration);
            return billableHoursColumn;
        }

        public List<BillableHoursOnDay> getBillableHoursOnDays() {
            return billableHoursOnDays;
        }
    }

    private class BillableHoursOnDay {
        private Calendar day;
        private long billableDuration;

        public Calendar getDay() {
            return day;
        }

        public void setDay(Calendar day) {
            this.day = day;
        }

        public long getBillableDuration() {
            return billableDuration;
        }

        public void setBillableDuration(long billableDuration) {
            this.billableDuration = billableDuration;
        }
    }

    private class BillableDurationOnDayItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> arg0, View view, int position, long timestamp) {
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(timestamp);
            DayDetailsHelper dayDetailsHelper = new DayDetailsHelper(getActivity());
            dayDetailsHelper.showDetailsOrTimeRecordsOfDay(day);
        }
    }

    private class BillableDurationLongClickListener implements AdapterView.OnItemLongClickListener {
        public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long timestamp) {
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(timestamp);

            Calendar startOfDay = TimeAndDurationService.getStartOfDay(day);
            Calendar endOfDay = TimeAndDurationService.getEndOfDay(day);

            DeleteTimeRecordsInPeriod.TimeRecordsDeletedListener timeRecordsDeletedListener = new DeleteTimeRecordsInPeriod.TimeRecordsDeletedListener() {
                @Override
                public void recordDeleted() {
                    refreshData();
                }
            };
            DeleteTimeRecordsInPeriod deleteTimeRecordsInPeriod = new DeleteTimeRecordsInPeriod(getActivity(), startOfDay, endOfDay, timeRecordsDeletedListener);
            deleteTimeRecordsInPeriod.handleUserRequestToDeleteRecordsInPeriod();
            return true;
        }
    }
}
