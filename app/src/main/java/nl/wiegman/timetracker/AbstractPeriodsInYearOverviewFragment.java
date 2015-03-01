package nl.wiegman.timetracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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

import java.util.List;

import nl.wiegman.timetracker.util.Formatting;
import nl.wiegman.timetracker.period.Period;
import nl.wiegman.timetracker.util.PeriodicRunnableExecutor;

public abstract class AbstractPeriodsInYearOverviewFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    protected static final String ARG_YEAR = "year";

    public static final String INSTANCE_STATE_YEAR = "YEAR";

    private TextView yearTextView;
    private ListView periodsListView;

    private TimeRecordsInPeriodAdapter listViewAdapter;

    private PeriodicRunnableExecutor checkedInTimeUpdaterExecutor;

    private int year;

    public AbstractPeriodsInYearOverviewFragment() {
        // Required empty public constructor
    }

    /**
     * To be overridden by subclasses. Must return the
     * @return
     */
    protected abstract long getActualPeriodBillableDuration();

    protected abstract List<PeriodOverviewItem> getOverviewItems(int year);

    protected abstract Period getPeriod(long periodId, int year);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            year = getArguments().getInt(ARG_YEAR);
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(INSTANCE_STATE_YEAR)) {
                year = savedInstanceState.getInt(INSTANCE_STATE_YEAR);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_period_overview, container, false);

        yearTextView = (TextView) rootView.findViewById(R.id.title);

        periodsListView = (ListView) rootView.findViewById(R.id.periodListView);
        periodsListView.setOnItemClickListener(new PeriodItemClickListener());
        periodsListView.setOnItemLongClickListener(new PeriodItemLongClickListener());

        View previous = rootView.findViewById(R.id.previousImageView);
        previous.setOnClickListener(new PreviousOnClickListener());

        View next = rootView.findViewById(R.id.nextImageView);
        next.setOnClickListener(new NextOnClickListener());

        refreshData();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INSTANCE_STATE_YEAR, year);
    }

    private void refreshData() {
        yearTextView.setText(Integer.toString(year));
        new RefreshData().execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        activateRecalculateCurrentPeriodUpdater();
    }

    @Override
    public void onStop() {
        super.onStop();
        deactivateCheckedInTimeUpdater();
    }

    private void activateRecalculateCurrentPeriodUpdater() {
        checkedInTimeUpdaterExecutor = new PeriodicRunnableExecutor(1000, new CheckedInTimeUpdater());
        checkedInTimeUpdaterExecutor.start();
    }

    private void deactivateCheckedInTimeUpdater() {
        if (checkedInTimeUpdaterExecutor != null) {
            checkedInTimeUpdaterExecutor.stop();
            checkedInTimeUpdaterExecutor = null;
        }
    }

    private class NextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            year += 1;
            refreshData();
        }
    }

    private class PreviousOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            year -= 1;
            refreshData();
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
            long currentPeriodBillableDuration = getActualPeriodBillableDuration();
            return Formatting.formatDuration(currentPeriodBillableDuration);
        }

        @Override
        protected void onPostExecute(String formattedBillableDuration) {
            super.onPostExecute(formattedBillableDuration);

            Integer positionOfCurrentItem = getPositionOfCurrentItem(listViewAdapter.getOverviewItems());
            if (positionOfCurrentItem != null) {
                int index = positionOfCurrentItem - periodsListView.getFirstVisiblePosition();
                if (index >= 0) {
                    View periodOverviewItem = periodsListView.getChildAt(index);
                    if (periodOverviewItem != null) {
                        TextView billableDurationColumnTextView = (TextView) periodOverviewItem.findViewById(R.id.totalBillableDurationColumn);
                        billableDurationColumnTextView.setText(formattedBillableDuration);
                    }
                }
            }
        }
    }

    private Integer getPositionOfCurrentItem(List<PeriodOverviewItem> periodOverviewItems) {
        Integer result = null;
        for (int position = 0; position < periodOverviewItems.size(); position++) {
            PeriodOverviewItem period = periodOverviewItems.get(position);
            if (period.isCurrentPeriod()) {
                result = position;
                break;
            }
        }
        return result;
    }

    private void showTimeRecordsInPeriod(int periodId, int year) {
        Period period = getPeriod(periodId, year);
        DaysInPeriodFragment fragment = DaysInPeriodFragment.newInstance(period);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private class TimeRecordsInPeriodAdapter extends BaseAdapter {
        private List<PeriodOverviewItem> overviewItems;

        /**
         * Constructor
         */
        public TimeRecordsInPeriodAdapter(List<PeriodOverviewItem> overviewItems) {
            this.overviewItems = overviewItems;
        }

        @Override
        public int getCount() {
            return overviewItems.size();
        }

        @Override
        public Object getItem(int position) {
            return overviewItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return overviewItems.get(position).getPeriodId();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            PeriodOverviewItem periodOverviewItem = overviewItems.get(position);

            View row = View.inflate(getActivity(), R.layout.period_overview_item, null);

            TextView periodNumberTextView = (TextView) row.findViewById(R.id.periodNumberColumn);
            periodNumberTextView.setText(periodOverviewItem.getPeriodName());

            TextView billableDurationColumn = (TextView) row.findViewById(R.id.totalBillableDurationColumn);
            long billableDuration = periodOverviewItem.getBillableDuration();

            String formattedBillableDuration = Formatting.formatDuration(billableDuration);
            billableDurationColumn.setText(formattedBillableDuration);

            if (periodOverviewItem.isCurrentPeriod()) {
                billableDurationColumn.setTypeface(Typeface.DEFAULT_BOLD);
                periodNumberTextView.setTypeface(Typeface.DEFAULT_BOLD);
            } else if (billableDuration == 0) {
                billableDurationColumn.setEnabled(false);
                periodNumberTextView.setEnabled(false);
            }

            return row;
        }

        public List<PeriodOverviewItem> getOverviewItems() {
            return overviewItems;
        }
    }

    protected class PeriodOverviewItem {
        private long periodId;
        private boolean isCurrentPeriod;
        private String periodName;
        private long billableDuration;

        public String getPeriodName() {
            return periodName;
        }

        public void setPeriodName(String periodName) {
            this.periodName = periodName;
        }

        public long getBillableDuration() {
            return billableDuration;
        }

        public void setBillableDuration(long billableDuration) {
            this.billableDuration = billableDuration;
        }

        public long getPeriodId() {
            return periodId;
        }

        public void setPeriodId(long periodId) {
            this.periodId = periodId;
        }

        public boolean isCurrentPeriod() {
            return isCurrentPeriod;
        }

        public void setCurrentPeriod(boolean isCurrentPeriod) {
            this.isCurrentPeriod = isCurrentPeriod;
        }
    }

    private class PeriodItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            showTimeRecordsInPeriod((int) id, year);
        }
    }

    private class PeriodItemLongClickListener implements AdapterView.OnItemLongClickListener {
        public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long periodId) {
            Period period = getPeriod(periodId, year);

            DeleteTimeRecordsInPeriod.TimeRecordsDeletedListener timeRecordsDeletedListener = new DeleteTimeRecordsInPeriod.TimeRecordsDeletedListener() {
                @Override
                public void recordDeleted() {
                    refreshData();
                }
            };
            DeleteTimeRecordsInPeriod deleteTimeRecordsInPeriod = new DeleteTimeRecordsInPeriod(getActivity(), period.getFrom(), period.getTo(), timeRecordsDeletedListener);
            deleteTimeRecordsInPeriod.handleUserRequestToDeleteRecordsInPeriod();
            return true;
        }
    }

    private class RefreshData extends AsyncTask<Void, Void, List<PeriodOverviewItem>> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            openProgressDialog();
        }

        @Override
        protected List<PeriodOverviewItem> doInBackground(Void... voids) {
            return getOverviewItems(year);
        }

        @Override
        protected void onPostExecute(List<PeriodOverviewItem> periodOverviewItems) {
            listViewAdapter = new TimeRecordsInPeriodAdapter(periodOverviewItems);
            periodsListView.setAdapter(listViewAdapter);

            Integer positionOfCurrentItem = getPositionOfCurrentItem(periodOverviewItems);
            if (positionOfCurrentItem != null) {
                periodsListView.setSelection(positionOfCurrentItem);
            }
            closeProgressDialog();
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
