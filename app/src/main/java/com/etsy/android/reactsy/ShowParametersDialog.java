package com.etsy.android.reactsy;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by mhorowitz on 8/21/14.
 */
public class ShowParametersDialog extends DialogFragment {
    private static final String TAG = ShowParametersDialog.class.getName();

    protected ReactsyTestParameters mParameters;

    public static ShowParametersDialog show(FragmentManager manager, ReactsyTestParameters parameters) {
        ShowParametersDialog dialog = new ShowParametersDialog();
        dialog.mParameters = parameters;
        dialog.show(manager, TAG);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reactsy_show_parameters, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().setTitle(R.string.test_parameters_title);

        TextView parameter = (TextView) view.findViewById(R.id.test_duration);
        parameter.setText(Long.toString(this.mParameters.testDurationSec) + "sec");

        parameter = (TextView) view.findViewById(R.id.min_delay);
        parameter.setText(Long.toString(this.mParameters.minDelayMs) + "ms");

        parameter = (TextView) view.findViewById(R.id.max_delay);
        parameter.setText(Long.toString(this.mParameters.maxDelayMs) + "ms");

        parameter = (TextView) view.findViewById(R.id.too_soon);
        parameter.setText(Long.toString(this.mParameters.tooSoonMs) + "ms");

        parameter = (TextView) view.findViewById(R.id.too_late);
        parameter.setText(Long.toString(this.mParameters.tooLateMs) + "ms");

        parameter = (TextView) view.findViewById(R.id.recovery_delay);
        parameter.setText(Long.toString(this.mParameters.recoveryDelayMs) + "ms");

        parameter = (TextView) view.findViewById(R.id.min_valid_trials_pct);
        parameter.setText(String.format("%.2f", this.mParameters.minValidTrialsPct) + "%");

        parameter = (TextView) view.findViewById(R.id.test_mechanism);
        parameter.setText("finger " + this.mParameters.testMechanism);
    }
}
