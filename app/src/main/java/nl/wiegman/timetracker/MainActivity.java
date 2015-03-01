package nl.wiegman.timetracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import nl.wiegman.timetracker.util.FragmentHelper;

public class MainActivity extends Activity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        StrictMode.enableDefaults();

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
//            Backup.restore();
            showCheckInCheckOutFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            showAddTimeRecordFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCheckInCheckOutFragment() {
        CheckInCheckoutFragment fragment = new CheckInCheckoutFragment();
        FragmentHelper.showFragment(this, fragment);
    }

    private void showAddTimeRecordFragment() {
        EditTimeRecordFragment fragment = EditTimeRecordFragment.newInstance(null);
        FragmentHelper.showFragment(this, fragment);
    }
}
