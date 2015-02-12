package timetracker.wiegman.nl.timetracker;

import android.content.DialogInterface;

public class DismissOnClickListener implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        dialogInterface.dismiss();
    }
}