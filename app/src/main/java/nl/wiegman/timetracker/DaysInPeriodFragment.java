package nl.wiegman.timetracker;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import nl.wiegman.timetracker.util.Formatting;
import nl.wiegman.timetracker.period.Period;
import nl.wiegman.timetracker.util.PeriodicRunnableExecutor;
import nl.wiegman.timetracker.util.TimeAndDurationService;

/**
 * Shows a list of days within a given period.
 * For each dat the billable duration is shown.
 *
 * The timerecords on a day can be deleted (long press) or edited (on click).
 */
public class DaysInPeriodFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    public static final int MENU_ITEM_EXPORT_TO_PDF_ID = 0;

    private static final String ARG_PERIOD = "periodTitle";

    private Period period;

    private TextView titleTextView;
    private ListView billableDurationOnDayListView;
    private TextView footerTotalTextView;

    private SwipeDetector listViewSwipeDetector;

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
        args.putSerializable(ARG_PERIOD, period);
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
            period = (Period) getArguments().getSerializable(ARG_PERIOD);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        listViewSwipeDetector = new SwipeDetector();

        View rootView = inflater.inflate(R.layout.fragment_time_records_in_period, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.title);
        footerTotalTextView = (TextView) rootView.findViewById(R.id.totalBillableDurationColumn);

        billableDurationOnDayListView = (ListView) rootView.findViewById(R.id.timeRecordsInPeriodListView);
        billableDurationOnDayListView.setOnItemClickListener(new BillableDurationOnDayItemClickListener());
        billableDurationOnDayListView.setOnItemLongClickListener(new BillableDurationLongClickListener());
        billableDurationOnDayListView.setOnTouchListener(listViewSwipeDetector);

        View previous = rootView.findViewById(R.id.previousImageView);
        previous.setOnClickListener(new PreviousOnClickListener());

        View next = rootView.findViewById(R.id.nextImageView);
        next.setOnClickListener(new NextOnClickListener());

        refreshData();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, MENU_ITEM_EXPORT_TO_PDF_ID, 0, R.string.export_to_pdf);
    }

    @Override
    public void onStart() {
        super.onStart();
        activateRecalculateCurrentDayUpdater();
    }

    @Override
    public void onStop() {
        super.onStop();
        deactivateCheckedInTimeUpdater();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == MENU_ITEM_EXPORT_TO_PDF_ID) {
            new PdfExport(getActivity(), period).execute();
        }
        return super.onOptionsItemSelected(item);
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
            new PeriodicUpdate().execute();
        }
    }

    private class PeriodicUpdate extends AsyncTask<Void, Void, CurrentItemUpdateData> {

        @Override
        protected CurrentItemUpdateData doInBackground(Void... voids) {
            CurrentItemUpdateData data = new CurrentItemUpdateData();
            long currentPeriodBillableDuration = TimeAndDurationService.getBillableDurationOnDay(Calendar.getInstance());
            data.current = Formatting.formatDuration(currentPeriodBillableDuration);
            data.total = Formatting.formatDuration(period.getBillableDuration());
            return data;
        }

        @Override
        protected void onPostExecute(CurrentItemUpdateData updateData) {
            List<BillableDurationOnDay> billableDurationOnDays = listViewAdapter.getBillableDurationOnDays();
            Integer positionOfCurrentItem = getPositionOfCurrentItem(billableDurationOnDays);
            if (positionOfCurrentItem != null) {
                int index = positionOfCurrentItem - billableDurationOnDayListView.getFirstVisiblePosition();
                if (index >= 0) {
                    View dayItemView = billableDurationOnDayListView.getChildAt(index);
                    if (dayItemView != null) {
                        TextView billableDurationColumnTextView = (TextView) dayItemView.findViewById(R.id.totalBillableDurationColumn);
                        billableDurationColumnTextView.setText(updateData.current);
                    }
                }
            }
            footerTotalTextView.setText(updateData.total);
        }
    }

    private class CurrentItemUpdateData {
        private String total;
        private String current;
    }

    private Integer getPositionOfCurrentItem(List<BillableDurationOnDay> billableHoursOnDays) {
        Integer result = null;
        for (int position = 0; position < billableHoursOnDays.size(); position++) {
            BillableDurationOnDay billableHoursOnDay = billableHoursOnDays.get(position);
            if (isCurrentDay(billableHoursOnDay)) {
                result = position;
                break;
            }
        }
        return result;
    }

    private boolean isCurrentDay(BillableDurationOnDay billableHoursOnDay) {
        Calendar today = Calendar.getInstance();
        return DateUtils.isSameDay(billableHoursOnDay.getDay(), today);
    }

    private void refreshData() {
        titleTextView.setText(period.getTitle());
        new RefreshData().execute();
    }

    private List<BillableDurationOnDay> getBillableHoursOnDays() {
        List<BillableDurationOnDay> days = new ArrayList<>();

        Calendar day = (Calendar) period.getFrom().clone();
        while (day.getTimeInMillis() < period.getTo().getTimeInMillis()) {
            BillableDurationOnDay billableHoursOnDay = new BillableDurationOnDay();
            billableHoursOnDay.setDay((Calendar)day.clone());
            long billableDurationOnDay = TimeAndDurationService.getBillableDurationOnDay(day);
            billableHoursOnDay.setBillableDuration(billableDurationOnDay);
            days.add(billableHoursOnDay);
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    private class BillableHoursPerDayAdapter extends BaseAdapter {
        private List<BillableDurationOnDay> billableHoursOnDays;

        /**
         * Constructor
         */
        public BillableHoursPerDayAdapter(List<BillableDurationOnDay> billableHoursOnDays) {
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
            BillableDurationOnDay billableHoursOnDay = billableHoursOnDays.get(position);

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
            TextView billableHoursColumn = (TextView) row.findViewById(R.id.totalBillableDurationColumn);
            String formattedBillableDuration = Formatting.formatDuration(billableDuration);
            billableHoursColumn.setText(formattedBillableDuration);
            return billableHoursColumn;
        }

        public List<BillableDurationOnDay> getBillableDurationOnDays() {
            return billableHoursOnDays;
        }
    }

    private class BillableDurationOnDay {
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
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long timestamp) {
            if(listViewSwipeDetector.swipeDetected()) {
                handleSwipe();
            } else {
                handleClick(timestamp);
            }
        }

        private void handleClick(long timestamp) {
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(timestamp);
            DayDetailsHelper dayDetailsHelper = new DayDetailsHelper(getActivity());
            dayDetailsHelper.showDetailsOrTimeRecordsOfDay(day);
        }
    }

    private class BillableDurationLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long timestamp) {
            boolean result;
            if(listViewSwipeDetector.swipeDetected()) {
                result = false;
                handleSwipe();
            } else {
                result = true;
                handleLongClick(timestamp);
            }

            return result;
        }

        private void handleLongClick(long timestamp) {
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
        }
    }

    private void handleSwipe() {
        if(listViewSwipeDetector.getAction() == SwipeDetector.Action.RL) {
            nextPeriod();
        } else if (listViewSwipeDetector.getAction() == SwipeDetector.Action.LR) {
            previousPeriod();
        }
    }

    private class PreviousOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            previousPeriod();
        }
    }

    private class NextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            nextPeriod();
        }
    }

    private void previousPeriod() {
        period = period.getPrevious();
        refreshData();
    }

    private void nextPeriod() {
        period = period.getNext();
        refreshData();
    }

    private class RefreshData extends AsyncTask<Void, Void, List<BillableDurationOnDay>> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            openProgressDialog();
        }

        @Override
        protected List<BillableDurationOnDay> doInBackground(Void... voids) {
            return getBillableHoursOnDays();
        }

        @Override
        protected void onPostExecute(List<BillableDurationOnDay> billableHoursOnDays) {
            listViewAdapter = new BillableHoursPerDayAdapter(billableHoursOnDays);
            billableDurationOnDayListView.setAdapter(listViewAdapter);

            Integer positionOfCurrentItem = getPositionOfCurrentItem(billableHoursOnDays);
            if (positionOfCurrentItem != null) {
                billableDurationOnDayListView.setSelection(positionOfCurrentItem);
            }

            setTotalInFooter(billableHoursOnDays);
            closeProgressDialog();
        }

        private void setTotalInFooter(List<BillableDurationOnDay> billableHoursOnDays) {
            long total = 0;
            for (BillableDurationOnDay billableHoursOnDay : billableHoursOnDays) {
                total += billableHoursOnDay.getBillableDuration();
            }
            footerTotalTextView.setText(Formatting.formatDuration(total));
        }

        private void openProgressDialog() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getActivity().getString(R.string.loading_data));
            dialog.show();
        }

        private void closeProgressDialog() {
            if (dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        }
    }
}
