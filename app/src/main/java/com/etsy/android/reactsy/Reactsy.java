package com.etsy.android.reactsy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Reactsy extends FragmentActivity {
    // All times measured in milliseconds (ms)
    public static final String PARAM_TEST_DURATION = "test_duration_ms";
    public static final String PARAM_MIN_DELAY = "min_delay_ms";
    public static final String PARAM_MAX_DELAY = "max_delay_ms";
    public static final String PARAM_TOO_SOON = "too_soon_ms";
    public static final String PARAM_TOO_LATE = "too_late_ms";
    public static final String PARAM_RECOVERY_DELAY = "recovery_delay_ms";  // when too soon/late occurs
    public static final String PARAM_MIN_VALID_TRIALS = "min_valid_trials"; // for test to be considered valid
    public static final String PARAM_TEST_MECHANISM = "test_mechanism";
    public static final String PARAM_LDAP = "ldap";

    protected static final int DEFAULT_TEST_DURATION_SEC = 3 * 60;       // 3 minutes
    protected static final int DEFAULT_MIN_DELAY_MS = 2 * 1000;          // 2 seconds
    protected static final int DEFAULT_MAX_DELAY_MS = 10 * 1000;         // 10 seconds
    protected static final int DEFAULT_TOO_SOON_MS = 100;                // too lucky; 0.1 second
    protected static final int DEFAULT_TOO_LATE_MS = 1000;               // asleep? 1 second
    protected static final int DEFAULT_RECOVERY_DELAY_MS = 2000;         // 2 seconds
    protected static final double DEFAULT_MIN_VALID_TRIALS_PCT = 90.0;   // 90% of trials w/o error
    protected static final String DEFAULT_TEST_MECHANISM = ReactsyTestParameters.MECHANISM_PRESS_DOWN;

    public ReactsyTestParameters mTestParameters = new ReactsyTestParameters();
    public String mTestIdentity;    // "YYYY-MM-DD HH:MI:SS"

    protected static final String FRAGMENT_REACTSY_TEST = "reactsy_test";

    private FrameLayout mDrawerRootView;
    private ReactsyTestFragment mReactsyTestFragment;

    protected String getTestIdentity() {
        Date now = new Date();
        return this.mTestParameters.ldap + ": "
                                         + new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ").format(now);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reactsy);

        Bundle initialState = null;

        Intent intent = this.getIntent();
        if (intent != null) {
            initialState = intent.getExtras();
        }
        // Extract parameters
        if (initialState != null) {
            this.mTestParameters.testDurationSec =
                initialState.getInt(PARAM_TEST_DURATION, DEFAULT_TEST_DURATION_SEC);
            this.mTestParameters.minDelayMs =
                initialState.getInt(PARAM_MIN_DELAY, DEFAULT_MIN_DELAY_MS);
            this.mTestParameters.maxDelayMs =
                initialState.getInt(PARAM_MAX_DELAY, DEFAULT_MAX_DELAY_MS);
            this.mTestParameters.tooSoonMs =
                initialState.getInt(PARAM_TOO_SOON, DEFAULT_TOO_SOON_MS);
            this.mTestParameters.tooLateMs =
                initialState.getInt(PARAM_TOO_LATE, DEFAULT_TOO_LATE_MS);
            this.mTestParameters.testMechanism =
                initialState.getString(PARAM_TEST_MECHANISM, DEFAULT_TEST_MECHANISM);
            this.mTestParameters.recoveryDelayMs =
                initialState.getInt(PARAM_RECOVERY_DELAY, DEFAULT_RECOVERY_DELAY_MS);
            this.mTestParameters.minValidTrialsPct =
                initialState.getDouble(PARAM_MIN_VALID_TRIALS, DEFAULT_MIN_VALID_TRIALS_PCT);
            this.mTestParameters.ldap =
                initialState.getString(PARAM_LDAP, "");
        }
        else {
            this.mTestParameters.testDurationSec = DEFAULT_TEST_DURATION_SEC;
            this.mTestParameters.minDelayMs = DEFAULT_MIN_DELAY_MS;
            this.mTestParameters.maxDelayMs = DEFAULT_MAX_DELAY_MS;
            this.mTestParameters.tooSoonMs = DEFAULT_TOO_SOON_MS;
            this.mTestParameters.tooLateMs = DEFAULT_TOO_LATE_MS;
            this.mTestParameters.testMechanism = DEFAULT_TEST_MECHANISM;
            this.mTestParameters.recoveryDelayMs = DEFAULT_RECOVERY_DELAY_MS;
            this.mTestParameters.minValidTrialsPct = DEFAULT_MIN_VALID_TRIALS_PCT;
            this.mTestParameters.ldap = "";
        }
        this.mTestIdentity = getTestIdentity();

        this.mDrawerRootView = (FrameLayout) this.findViewById(R.id.reactsy_navigation);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ldap = preferences.getString(ReactsySettingsActivity.PREFERENCE_LDAP_ID_KEY, "");

        Toast.makeText(this, "LDAP is: " + ldap, Toast.LENGTH_SHORT).show();

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(FRAGMENT_REACTSY_TEST) == null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();

            // ft.add(R.id.etsearch_home_content, SearchResultFragment.newInstance());
            this.mReactsyTestFragment =
                ReactsyTestFragment.newInstance(this.mTestIdentity, this.mTestParameters);
            ft.add(R.id.reactsy_main_view, mReactsyTestFragment, FRAGMENT_REACTSY_TEST);

            ft.commit();
        }
        if (this.mDrawerRootView != null) {
            // this.mDrawerRootView.openDrawer(this.findViewById(R.id.etsearch_home_navigation));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reactsy, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            /*
            getSupportFragmentManager().beginTransaction()
                                       .replace(R.id.reactsy_main_view,
                                                new ReactsySettingsActivity())
                                       .addToBackStack(null)
                                       .commit();
            */
            startActivity(new Intent(this, ReactsySettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
