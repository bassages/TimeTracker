package nl.wiegman.timetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.List;

import nl.wiegman.timetracker.domain.TimeRecord;
import nl.wiegman.timetracker.period.Day;
import nl.wiegman.timetracker.util.FragmentHelper;
import nl.wiegman.timetracker.util.TimeAndDurationService;

public class DayDetailsHelper {

    private final FragmentActivity activity;

    /**
     * Constructor
     */
    public DayDetailsHelper(FragmentActivity activity) {
        this.activity = activity;
    }

    public void showDetailsOrTimeRecordsOfDay(Calendar day) {
        Calendar startOfDay = TimeAndDurationService.getStartOfDay(day);
        Calendar endOfDay = TimeAndDurationService.getEndOfDay(day);

        List<TimeRecord> timeRecordsOnDay = TimeAndDurationService.getTimeRecordsBetween(startOfDay, endOfDay);
        int nrOfTimeRecordsOnDay = timeRecordsOnDay.size();

        boolean isCheckedInOnDay = false;
        TimeRecord checkIn = TimeAndDurationService.getCheckIn();
        if (checkIn != null && DateUtils.isSameDay(day, checkIn.getCheckIn())) {
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
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                dialog.dismiss();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.no_details_available_because_no_checkin)
                .setPositiveButton(android.R.string.ok, dialogClickListener)
                .show();
    }

    private void showEditTimeRecordFragment(List<TimeRecord> timeRecordsOnDay) {
        EditTimeRecordFragment fragment = EditTimeRecordFragment.newInstance(timeRecordsOnDay.get(0).getId());
        FragmentHelper.showFragment(activity, fragment);
    }

    private void showTimeRecordsOnDayFragment(Calendar day) {
        Day period = new Day(day);
        TimeRecordsInPeriodFragment fragment = TimeRecordsInPeriodFragment.newInstance(period);
        FragmentHelper.showFragment(activity, fragment);
    }
}
