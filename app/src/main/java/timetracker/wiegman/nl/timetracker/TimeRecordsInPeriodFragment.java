package timetracker.wiegman.nl.timetracker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import timetracker.wiegman.nl.timetracker.domain.TimeRecord;
import timetracker.wiegman.nl.timetracker.util.Formatting;
import timetracker.wiegman.nl.timetracker.util.Period;
import timetracker.wiegman.nl.timetracker.util.TimeAndDurationService;

/**
 * Shows a list of timerecords within a given period.
 * The timerecords can be deleted or edited (which is actually processed in EditTimeRecordFragemnt)
 */
public class TimeRecordsInPeriodFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getName();

    private static final String ARG_PERIOD_TITLE = "periodTitle";
    private static final String ARG_PERIOD_FROM = "periodFrom";
    private static final String ARG_PERIOD_TO = "periodTo";

    private String periodTitle;
    private Calendar periodFrom;
    private Calendar periodTo;

    private TextView titleTextView;
    private ListView timeRecordsListView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimeRecordsInPeriodFragment.
     */
    public static TimeRecordsInPeriodFragment newInstance(Period period) {
        TimeRecordsInPeriodFragment fragment = new TimeRecordsInPeriodFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PERIOD_TITLE, period.getTitle());
        args.putSerializable(ARG_PERIOD_FROM, period.getFrom());
        args.putSerializable(ARG_PERIOD_TO, period.getTo());
        fragment.setArguments(args);
        return fragment;
    }

    public TimeRecordsInPeriodFragment() {
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

        titleTextView = (TextView) rootView.findViewById(R.id.timeRecordDetailsTitle);
        titleTextView.setText(periodTitle);

        timeRecordsListView = (ListView) rootView.findViewById(R.id.timeRecordsInPeriodListView);

        timeRecordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int position, long timeRecordId) {
                editTimeRecord(timeRecordId);
            }
        });
        timeRecordsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
                deleteTimeRecordWhenConfirmed(id);
                return true;
            }
        });

        refreshData();

        return rootView;
    }

    public void deleteTimeRecordWhenConfirmed(final long timeRecordId) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        TimeRecord.findById(TimeRecord.class, timeRecordId).delete();
                        refreshData();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked, do nothing
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_this_row)
                .setTitle(R.string.confirm)
                .setPositiveButton(android.R.string.yes, dialogClickListener)
                .setNegativeButton(android.R.string.no, dialogClickListener)
                .show();
    }

    private void editTimeRecord(long timeRecordId) {
        EditTimeRecordFragment fragment = EditTimeRecordFragment.newInstance(timeRecordId);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void refreshData() {
        List<TimeRecord> timeRecordsInPeriod = TimeAndDurationService.getTimeRecordsBetween(periodFrom.getTimeInMillis(), periodTo.getTimeInMillis());
        timeRecordsListView.setAdapter(new TimeRecordsInPeriodAdapter(timeRecordsInPeriod));
    }

    private class TimeRecordsInPeriodAdapter extends BaseAdapter {

        private List<TimeRecord> timeRecordsInPeriod;

        /**
         * Constructor
         */
        public TimeRecordsInPeriodAdapter(List<TimeRecord> timeRecordsInPeriod) {
            this.timeRecordsInPeriod = timeRecordsInPeriod;
        }

        @Override
        public int getCount() {
            return timeRecordsInPeriod.size();
        }

        @Override
        public Object getItem(int position) {
            return timeRecordsInPeriod.get(position);
        }

        @Override
        public long getItemId(int position) {
            return timeRecordsInPeriod.get(position).getId();
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            TimeRecord timeRecord = timeRecordsInPeriod.get(position);

            View row = View.inflate(getActivity(), R.layout.time_records_in_period_item, null);

            TextView dayColumn = (TextView) row.findViewById(R.id.dayOfWeekColumn);
            SimpleDateFormat dayInWeekFormat = new SimpleDateFormat("EEE");
            String formattedDay = dayInWeekFormat.format(timeRecord.getCheckIn().getTime());
            dayColumn.setText(formattedDay);

            TextView dateColumn = (TextView) row.findViewById(R.id.dateColumn);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM");
            String formattedDate = simpleDateFormat.format(timeRecord.getCheckIn().getTime());
            dateColumn.setText(formattedDate);

            TextView billableHoursColumn = (TextView) row.findViewById(R.id.billableDurationColumn);
            String formattedBillableDuration = Formatting.formatDuration(timeRecord.getBillableDuration());
            billableHoursColumn.setText(formattedBillableDuration);

            return row;
        }
    }

}
