package nl.wiegman.timetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.wiegman.timetracker.domain.CheckIn;
import nl.wiegman.timetracker.domain.TimeRecord;
import nl.wiegman.timetracker.util.DayPeriod;
import nl.wiegman.timetracker.util.TimeAndDurationService;

public class DayDetailsHelper {

    private final Activity activity;

    /**
     * Constructor
     */
    public DayDetailsHelper(Activity activity) {
        this.activity = activity;
    }

    public void showDetailsOrTimeRecordsOfDay(Calendar day) {
        Calendar startOfDay = TimeAndDurationService.getStartOfDay(day);
        Calendar endOfDay = TimeAndDurationService.getEndOfDay(day);

        List<TimeRecord> timeRecordsOnDay = TimeAndDurationService.getTimeRecordsBetween(startOfDay, endOfDay);
        int nrOfTimeRecordsOnDay = timeRecordsOnDay.size();

        boolean isCheckedInOnDay = false;
        CheckIn checkIn = TimeAndDurationService.getCheckIn();
        if (checkIn != null && DateUtils.isSameDay(day, checkIn.getTimestamp())) {
            isCheckedInOnDay = true;
        }

        if (nrOfTimeRecordsOnDay == 0 && isCheckedInOnDay) {
            new CheckedInOnDayDialog(activity).showCheckedInDialog();
        } else if (nrOfTimeRecordsOnDay == 0) {
            showDialogThatThereIsNothingToDelete();
        } else if (nrOfTimeRecordsOnDay == 1) {
            showEditTimeRecordFragment(timeRecordsOnDay);
        } else {
            showTimeRecordsOnDayFragment(day);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.no_details_available_because_no_checkin)
                .setPositiveButton(android.R.string.ok, dialogClickListener)
                .show();
    }

    private void showEditTimeRecordFragment(List<TimeRecord> timeRecordsOnDay) {
        EditTimeRecordFragment fragment = EditTimeRecordFragment.newInstance(timeRecordsOnDay.get(0).getId());
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showTimeRecordsOnDayFragment(Calendar day) {
        DayPeriod period = new DayPeriod(day);

        TimeRecordsInPeriodFragment fragment = TimeRecordsInPeriodFragment.newInstance(period);
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
