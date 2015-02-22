package nl.wiegman.timetracker;

import android.content.DialogInterface;

public class DismissOnClickListener implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        dialogInterface.dismiss();
    }
}