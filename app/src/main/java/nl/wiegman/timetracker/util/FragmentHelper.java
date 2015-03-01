package nl.wiegman.timetracker.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

import nl.wiegman.timetracker.R;

public class FragmentHelper {

    public static void showFragment(Activity activity, Fragment fragment) {
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
