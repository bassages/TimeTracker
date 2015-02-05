package timetracker.wiegman.nl.timetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import timetracker.wiegman.nl.timetracker.domain.TimeRecord;
import timetracker.wiegman.nl.timetracker.util.TimeAndDurationService;

public class DeleteTimeRecordsInPeriod {

    private final Activity activity;

    private final TimeRecordsDeletedListener timeRecordsDeletedListener;

    private final Calendar from;
    private final Calendar to;

    /**
     * Constructor
     */
    public DeleteTimeRecordsInPeriod(Activity activity, Calendar from, Calendar to, TimeRecordsDeletedListener timeRecordsDeletedListener) {
        this.activity = activity;
        this.timeRecordsDeletedListener = timeRecordsDeletedListener;
        this.from = from;
        this.to = to;
    }

    public void deleteAfterConfirmedByUser() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        List<TimeRecord> timeRecordsOnDay = TimeAndDurationService.getTimeRecordsBetween(from.getTimeInMillis(), to.getTimeInMillis());
                        for (TimeRecord recordToDelete : timeRecordsOnDay) {
                            recordToDelete.delete();
                        }
                        if (timeRecordsDeletedListener != null) {
                            timeRecordsDeletedListener.recordDeleted();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        String message;
        if (DateUtils.isSameDay(from, to)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            message = "Delete all records on " + sdf.format(from.getTime()) + "?";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            message = "Delete all records between " + sdf.format(from.getTime()) + " and " + sdf.format(to.getTime()) + "?";
        }

        builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    public interface TimeRecordsDeletedListener {
        void recordDeleted();
    }
}
