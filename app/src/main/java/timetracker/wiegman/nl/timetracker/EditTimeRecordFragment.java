package timetracker.wiegman.nl.timetracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import timetracker.wiegman.nl.timetracker.domain.CheckIn;
import timetracker.wiegman.nl.timetracker.domain.TimeRecord;

import static android.view.View.OnClickListener;

/**
 * Shows the details of the given TimeRecord.
 * Each detail can be edited.
 */
public class EditTimeRecordFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final String ARG_PARAM_TIMERECORD_ID = "timeRecordId";

    private long timeRecordId;

    private View rootView;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MM-yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

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
        TimeRecord timeRecord = TimeRecord.findById(TimeRecord.class, timeRecordId);

        Calendar checkIn = timeRecord.getCheckIn();
        refreshFromDate(dateFormat, checkIn);
        refreshFromTime(timeFormat, checkIn);

        Calendar checkOut = timeRecord.getCheckOut();
        refreshToDate(dateFormat, checkOut);
        refreshToTime(timeFormat, checkOut);

        refreshBreak(timeRecord);
    }

    private void refreshBreak(TimeRecord timeRecord) {
        TextView breakTextView = (TextView) rootView.findViewById(R.id.breakValueTextView);
        final long breakInMinutes = TimeUnit.MILLISECONDS.toMinutes(timeRecord.getBreakInMilliseconds());
        breakTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                editBreak(breakInMinutes);
            }
        });
        breakTextView.setText(breakInMinutes + " minutes");
    }

    private void refreshFromDate(SimpleDateFormat dateFormat, Calendar checkIn) {
        TextView fromDateTextView = (TextView) rootView.findViewById(R.id.fromDateValueTextView);
        String formattedFromDate = dateFormat.format(checkIn.getTime());
        fromDateTextView.setText(formattedFromDate);
        EditDateClickListener fromDateClickListener = new EditDateClickListener(CalendarField.From, checkIn);
        fromDateTextView.setOnClickListener(fromDateClickListener);
    }

    private void refreshFromTime(SimpleDateFormat timeFormat, Calendar checkIn) {
        TextView fromTimeTextView = (TextView) rootView.findViewById(R.id.fromTimeValueTextView);
        String formattedFromTime = timeFormat.format(checkIn.getTime());
        fromTimeTextView.setText(formattedFromTime);
        EditTimeClickListener fromTimeClickListener = new EditTimeClickListener(CalendarField.From, checkIn);
        fromTimeTextView.setOnClickListener(fromTimeClickListener);
    }

    private void refreshToDate(SimpleDateFormat dateFormat, Calendar checkOut) {
        TextView toDateTextView = (TextView) rootView.findViewById(R.id.toDateValueTextView);
        String formattedToDate = dateFormat.format(checkOut.getTime());
        toDateTextView.setText(formattedToDate);
        EditDateClickListener toDateClickListener = new EditDateClickListener(CalendarField.To, checkOut);
        toDateTextView.setOnClickListener(toDateClickListener);
    }

    private void refreshToTime(SimpleDateFormat timeFormat, Calendar checkOut) {
        TextView toTimeTextView = (TextView) rootView.findViewById(R.id.toTimeValueTextView);
        String formattedToTime = timeFormat.format(checkOut.getTime());
        toTimeTextView.setText(formattedToTime);
        EditTimeClickListener toTimeClickListener = new EditTimeClickListener(CalendarField.To, checkOut);
        toTimeTextView.setOnClickListener(toTimeClickListener);
    }

    /**
     * Opens a dialog in which a time can be selected.
     * The TimeRecord is updated with the selected time when the user confirms.
     */
    private class EditTimeClickListener implements OnClickListener {
        private final CalendarField calendarField;
        private final Calendar calendar;

        public EditTimeClickListener(CalendarField calendarField, Calendar calendar) {
            this.calendarField = calendarField;
            this.calendar = calendar;
        }

        @Override
        public void onClick(View view) {
            TimePickerWithSecondsDialog.OnTimeSetListener timeSetListener = new TimeSetListener(calendarField);
            TimePickerWithSecondsDialog timePickerDialog = new TimePickerWithSecondsDialog(getActivity(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), true);
            timePickerDialog.show();
        }
    }

    /**
     * Opens a dialog in which a date can be selected.
     * The TimeRecord is updated with the selected date when the user confirms.
     */
    private class EditDateClickListener implements OnClickListener {
        private final CalendarField calendarField;
        private final Calendar calendar;

        public EditDateClickListener(CalendarField calendarField, Calendar calendar) {
            this.calendarField = calendarField;
            this.calendar = calendar;
        }

        @Override
        public void onClick(View view) {
            DateSetListener dateSetListener = new DateSetListener(calendarField);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
    }

    private class TimeSetListener implements TimePickerWithSecondsDialog.OnTimeSetListener {
        private final CalendarField calendarField;

        public TimeSetListener(CalendarField calendarField) {
            this.calendarField = calendarField;
        }

        @Override
        public void onTimeSet(timetracker.wiegman.nl.timetracker.TimePicker view, int hourOfDay, int minute, int seconds) {
            TimeRecord timeRecord = TimeRecord.findById(TimeRecord.class, timeRecordId);

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

    private class DateSetListener implements DatePickerDialog.OnDateSetListener {
        private final CalendarField calendarField;

        public DateSetListener(CalendarField calendarField) {
            this.calendarField = calendarField;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            TimeRecord timeRecord = TimeRecord.findById(TimeRecord.class, timeRecordId);

            Calendar calendar = Calendar.getInstance();
            if (CalendarField.From == calendarField) {
                calendar = timeRecord.getCheckIn();
            } else if (CalendarField.To == calendarField) {
                calendar = timeRecord.getCheckOut();
            }
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.YEAR, year);
            timeRecord.save();

            refresh();
        }
    }

    public enum CalendarField {
        From,
        To
    }

    private void editBreak(long currentBreakInMinutes) {
        RelativeLayout linearLayout = new RelativeLayout(getActivity());
        final EditText editText = new EditText(getActivity());
        editText.setText(Long.toString(currentBreakInMinutes));
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(3);
        editText.setFilters(filterArray);
        editText.selectAll();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        linearLayout.setLayoutParams(params);
        linearLayout.addView(editText, numPickerParams);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(R.string.break_in_minutes);
        alertDialogBuilder.setView(linearLayout);

        Resources res = Resources.getSystem();
        int idOfPositiveButtonResource = res.getIdentifier("date_time_set", "string", "android");
        if (idOfPositiveButtonResource == 0) {
            idOfPositiveButtonResource = android.R.string.ok;
        }
        int idOfNegativeButtonResource = res.getIdentifier("cancel", "string", "android");
        if (idOfNegativeButtonResource == 0) {
            idOfNegativeButtonResource = android.R.string.cancel;
        }
        alertDialogBuilder
                .setPositiveButton(idOfPositiveButtonResource, new BreakSelectionListener(editText))
                .setNegativeButton(idOfNegativeButtonResource, new DismissOnClickListener());
        final AlertDialog alertDialog = alertDialogBuilder.create();

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        alertDialog.show();
    }

    private class BreakSelectionListener implements DialogInterface.OnClickListener {
        private final EditText editText;

        public BreakSelectionListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onClick(DialogInterface dialog, int selectedIndex) {
            String breakInMinutesInput = editText.getText().toString();
            long breakInMinutes = 0;
            if (!breakInMinutesInput.isEmpty()) {
                breakInMinutes = Long.parseLong(breakInMinutesInput);
            }
            long breakInMillis = TimeUnit.MINUTES.toMillis(breakInMinutes);
            TimeRecord.findById(TimeRecord.class, timeRecordId).setBreakInMilliseconds(breakInMillis).save();
            dialog.dismiss();
            refresh();
        }
    }
}
