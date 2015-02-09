package timetracker.wiegman.nl.timetracker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import timetracker.wiegman.nl.timetracker.domain.CheckIn;
import timetracker.wiegman.nl.timetracker.domain.TimeRecord;

import static android.view.View.OnClickListener;

/**
 * Shows the details of the given TimeRecord.
 * Each details can be edited.
 */
// TODO: https://github.com/IvanKovac/TimePickerWithSeconds for timepicker with possibility to set seconds
public class EditTimeRecordFragment extends Fragment {

    private final String LOG_TAG = this.getClass().getName();

    private static final String ARG_PARAM_TIMERECORD_ID = "timeRecordId";

    private long timeRecordId;

    private View rootView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditTimeRecord.
     */
    public static EditTimeRecordFragment newInstance(long timeRecordId) {
        EditTimeRecordFragment fragment = new EditTimeRecordFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARAM_TIMERECORD_ID, timeRecordId);
        fragment.setArguments(args);
        return fragment;
    }

    public EditTimeRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            timeRecordId = getArguments().getLong(ARG_PARAM_TIMERECORD_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_edit_time_record, container, false);
        refresh();
        return rootView;
    }

    private void refresh() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MM");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        TimeRecord timeRecord = TimeRecord.findById(TimeRecord.class, timeRecordId);
        Calendar checkIn = timeRecord.getCheckIn();
        Calendar checkOut = timeRecord.getCheckOut();

        TextView fromDateTextView = (TextView) rootView.findViewById(R.id.fromDateValueTextView);
        String formattedFromDate = dateFormat.format(checkIn.getTime());
        fromDateTextView.setText(formattedFromDate);

        TextView fromTimeTextView = (TextView) rootView.findViewById(R.id.fromTimeValueTextView);
        String formattedFromTime = timeFormat.format(checkIn.getTime());
        fromTimeTextView.setText(formattedFromTime);
        EditTimeClickListener fromTimeClickListener = new EditTimeClickListener(CalendarField.From, checkIn.get(Calendar.HOUR_OF_DAY), checkIn.get(Calendar.MINUTE), checkIn.get(Calendar.SECOND));
        fromTimeTextView.setOnClickListener(fromTimeClickListener);

        TextView toDateTextView = (TextView) rootView.findViewById(R.id.toDateValueTextView);
        String formattedToDate = dateFormat.format(checkOut.getTime());
        toDateTextView.setText(formattedToDate);

        TextView toTimeTextView = (TextView) rootView.findViewById(R.id.toTimeValueTextView);
        String formattedToTime = timeFormat.format(checkOut.getTime());
        toTimeTextView.setText(formattedToTime);
        EditTimeClickListener toTimeClickListener = new EditTimeClickListener(CalendarField.To, checkOut.get(Calendar.HOUR_OF_DAY), checkOut.get(Calendar.MINUTE), checkOut.get(Calendar.SECOND));
        toTimeTextView.setOnClickListener(toTimeClickListener);

        TextView breakTextView = (TextView) rootView.findViewById(R.id.breakValueTextView);
        long breakInMinutes = TimeUnit.MILLISECONDS.toMinutes(timeRecord.getBreakInMilliseconds());
        breakTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editBreak();
            }
        });
        breakTextView.setText(breakInMinutes + " minutes");
    }

    /**
     * Opens a dialog in which a time can be selected.
     * The TimeRecord is updated with the selected time.
     */
    private class EditTimeClickListener implements OnClickListener {
        private final CalendarField calendarField;
        private final int hourOfDay;
        private final int minutes;
        private final int seconds;

        public EditTimeClickListener(CalendarField calendarField, int hourOfDay, int minutes, int seconds) {
            this.calendarField = calendarField;
            this.hourOfDay = hourOfDay;
            this.minutes = minutes;
            this.seconds = seconds;
        }

        @Override
        public void onClick(View view) {
            TimePickerWithSecondsDialog.OnTimeSetListener timeSetListener = new TimeSetListener(calendarField);
            TimePickerWithSecondsDialog timePickerDialog = new TimePickerWithSecondsDialog(getActivity(), timeSetListener, hourOfDay, minutes, seconds, true);
            timePickerDialog.show();
        }
    }

    private class TimeSetListener implements TimePickerWithSecondsDialog.OnTimeSetListener {
        private final CalendarField calendarField;

        public TimeSetListener(CalendarField calendarField) {
            this.calendarField = calendarField;
        }

        @Override
        public void onTimeSet(timetracker.wiegman.nl.timetracker.TimePicker view, int hourOfDay, int minute, int seconds) {
            TimeRecord timeRecord = CheckIn.findById(TimeRecord.class, timeRecordId);

            Calendar calendar = Calendar.getInstance();
            if (CalendarField.From == calendarField) {
                calendar = timeRecord.getCheckIn();
            } else if (CalendarField.To == calendarField) {
                calendar = timeRecord.getCheckOut();
            }
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, seconds);
            timeRecord.save();

            refresh();
        }
    }

    public enum CalendarField {
        From,
        To
    }

    private void editBreak() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle("Edit break");

        Map<String, Long> values = new TreeMap<>();
        values.put("0 minutes", 0l);
        values.put("5 minutes", TimeUnit.MINUTES.toMillis(5));
        values.put("10 minutes", TimeUnit.MINUTES.toMillis(10));
        values.put("15 minutes", TimeUnit.MINUTES.toMillis(15));
        values.put("30 minutes", TimeUnit.MINUTES.toMillis(30));
        values.put("45 minutes", TimeUnit.MINUTES.toMillis(45));
        values.put("60 minutes", TimeUnit.MINUTES.toMillis(60));
        values.put("75 minutes", TimeUnit.MINUTES.toMillis(75));
        values.put("90 minutes", TimeUnit.MINUTES.toMillis(90));

        String[] strings = values.keySet().toArray(new String[]{});
        Long[] durations = values.values().toArray(new Long[]{});

        builder.setItems(strings, new BreakSelectionListener(durations));
        builder.show();
    }

    private class BreakSelectionListener implements DialogInterface.OnClickListener {
        private final Long[] durations;

        public BreakSelectionListener(Long[] durations) {
            this.durations = durations;
        }

        @Override
        public void onClick(DialogInterface dialog, int selectedIndex) {
            dialog.dismiss();
            Long breakInMillis = durations[selectedIndex];
            TimeRecord.findById(TimeRecord.class, timeRecordId).setBreakInMilliseconds(breakInMillis).save();
            refresh();
        }
    }
}
