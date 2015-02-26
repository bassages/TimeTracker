package nl.wiegman.timetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.wiegman.timetracker.domain.CheckIn;
import nl.wiegman.timetracker.util.TimeAndDurationService;

public class CheckedInOnDayDialog {

    private static final SimpleDateFormat checkDateFormat = new SimpleDateFormat("EEEE dd-MM");
    private static final SimpleDateFormat checkTimeFormat = new SimpleDateFormat("HH:mm:ss");

    private final Context context;

    public CheckedInOnDayDialog(Context context) {
        this.context = context;
    }

    public void showCheckedInDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEUTRAL:
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        TimeAndDurationService.checkOut();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        Date checkInTimestamp = TimeAndDurationService.getCheckIn().getTimestamp().getTime();
        String[] params = new String[] {checkDateFormat.format(checkInTimestamp), checkTimeFormat.format(checkInTimestamp)};
        String message = context.getString(R.string.checked_in_at, params);
        builder.setMessage(message)
                .setNeutralButton(android.R.string.ok, dialogClickListener)
                .setNegativeButton(R.string.checkout, dialogClickListener)
                .show();
    }

}
