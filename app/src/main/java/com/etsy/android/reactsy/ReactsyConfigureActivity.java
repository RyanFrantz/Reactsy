package com.etsy.android.reactsy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

/**
 * Created by mhorowitz on 8/23/14.
 */
public class ReactsyConfigureActivity extends Activity {

    protected void setParameterDefaults() {
        Switch switcher = (Switch) findViewById(R.id.config_test_mechanism);
        switcher.setChecked(true);

        EditText input = null;

        input = (EditText) findViewById(R.id.config_test_duration);
        input.setText(Integer.toString(Reactsy.DEFAULT_TEST_DURATION_SEC));
        input = (EditText) findViewById(R.id.config_min_delay);
        input.setText(Integer.toString(Reactsy.DEFAULT_MIN_DELAY_MS));
        input = (EditText) findViewById(R.id.config_max_delay);
        input.setText(Integer.toString(Reactsy.DEFAULT_MAX_DELAY_MS));
        input = (EditText) findViewById(R.id.config_too_soon);
        input.setText(Integer.toString(Reactsy.DEFAULT_TOO_SOON_MS));
        input = (EditText) findViewById(R.id.config_too_late);
        input.setText(Integer.toString(Reactsy.DEFAULT_TOO_LATE_MS));
        input = (EditText) findViewById(R.id.config_recovery_delay);
        input.setText(Integer.toString(Reactsy.DEFAULT_RECOVERY_DELAY_MS));
        input = (EditText) findViewById(R.id.config_min_valid_trials_pct);
        input.setText(Double.toString(Reactsy.DEFAULT_MIN_VALID_TRIALS_PCT));
    }

    protected ReactsyTestParameters fetchParameters() {
        ReactsyTestParameters parameters = new ReactsyTestParameters();

        Switch switcher = (Switch) findViewById(R.id.config_test_mechanism);
        parameters.testMechanism = switcher.isChecked() ? ReactsyTestParameters.MECHANISM_PRESS_DOWN
                                                        : ReactsyTestParameters.MECHANISM_LIFT_UP;

        EditText input = null;

        input = (EditText) findViewById(R.id.config_test_duration);
        parameters.testDurationSec = Integer.parseInt(input.getText().toString());
        input = (EditText) findViewById(R.id.config_min_delay);
        parameters.minDelayMs = Integer.parseInt(input.getText().toString());
        input = (EditText) findViewById(R.id.config_max_delay);
        parameters.maxDelayMs = Integer.parseInt(input.getText().toString());
        input = (EditText) findViewById(R.id.config_too_soon);
        parameters.tooSoonMs = Integer.parseInt(input.getText().toString());
        input = (EditText) findViewById(R.id.config_too_late);
        parameters.tooLateMs = Integer.parseInt(input.getText().toString());
        input = (EditText) findViewById(R.id.config_recovery_delay);
        parameters.recoveryDelayMs = Integer.parseInt(input.getText().toString());
        input = (EditText) findViewById(R.id.config_min_valid_trials_pct);
        parameters.minValidTrialsPct = Double.parseDouble(input.getText().toString());

        parameters.ldap = "";

        return parameters;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reactsy_configure);

        setParameterDefaults();

        findViewById(R.id.create_notification_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReactsyTestParameters parameters = fetchParameters();
                ReactsyNotification.addReactsyNotification(ReactsyConfigureActivity.this,
                                                           parameters);
            }
        });

        findViewById(R.id.run_reactsy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReactsyTestParameters parameters = fetchParameters();
                startActivity(ReactsyNotification.buildIntentWithArguments(ReactsyConfigureActivity.this,
                                                                           parameters));
            }
        });
    }
}
