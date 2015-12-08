package com.etsy.android.reactsy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by mhorowitz on 8/19/14.
 */
public class ReactsyTestFragment extends Fragment implements ReactsyTestState.StateChangeObserver {
    protected static final String ARG_TEST_IDENTITY = "test_identity";
    protected static final String ARG_TEST_PARAMETERS = "test_parameters";

    protected static final String KEY_CURRENT_STATE = "current_state";

    protected static final String FRAGMENT_REACTSY_TEST_RESULTS = "reactsy_test_results";

    protected LinearLayout mReactsyTestZone;
    protected TextView mTestInstructions;
    protected FrameLayout mTestArea;
    protected Button mReadyButton;
    protected ImageView mTestImage;
    protected LinearLayout mTestComplete;
    protected Button mViewResultsButton;

    public ReactsyTestState mTestState;
    public ReactsyTestParameters mTestParameters;
    public ReactsyTestResults mTestResults;

    @Override
    public void handleStateChange() {
        reactToState(this.mTestState.getCurrentState());
    }

    protected class PressDownListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTestState.handleTrialEvent();
                    break;
            }
            return true;
        }
    }

    protected class LiftUpListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    mTestState.handleTrialEvent();
                    break;
            }
            return true;
        }
    }

    public static ReactsyTestFragment newInstance(String testIdentity, ReactsyTestParameters parameters) {
        ReactsyTestFragment reactsyTestFragment = new ReactsyTestFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TEST_IDENTITY, testIdentity);
        args.putSerializable(ARG_TEST_PARAMETERS, parameters);
        reactsyTestFragment.setArguments(args);

        return reactsyTestFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle args = this.getArguments();
        this.mTestParameters = (ReactsyTestParameters) args.getSerializable(ARG_TEST_PARAMETERS);
        this.mTestResults = new ReactsyTestResults(args.getString(ARG_TEST_IDENTITY));

        this.mTestState = new ReactsyTestState(this, this.mTestParameters, this.mTestResults);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reactsy_test, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mReactsyTestZone = (LinearLayout) view.findViewById(R.id.reactsy_test);
        this.mTestInstructions = (TextView) view.findViewById(R.id.response_instructions);
        this.mTestArea = (FrameLayout) view.findViewById(R.id.test_area);
        this.mReadyButton = (Button) view.findViewById(R.id.ready_button);
        this.mTestImage = (ImageView) view.findViewById(R.id.test_image);
        this.mTestComplete = (LinearLayout) view.findViewById(R.id.test_complete);
        this.mViewResultsButton = (Button) view.findViewById(R.id.view_results_button);

        if (this.mTestParameters.testMechanism.equals(ReactsyTestParameters.MECHANISM_LIFT_UP)) {
            this.mTestInstructions.setText(getString(R.string.finger_up));
            this.mReactsyTestZone.setOnTouchListener(new LiftUpListener());
        }
        else {
            this.mTestInstructions.setText(getString(R.string.finger_down));
            this.mReactsyTestZone.setOnTouchListener(new PressDownListener());
        }

        this.mReadyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestState.setCurrentState(ReactsyTestState.STATE_USER_WAITING);
            }
        });

        this.mViewResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                if (fragmentManager.findFragmentByTag(FRAGMENT_REACTSY_TEST_RESULTS) == null) {
                    ReactsyTestResultsFragment testResultsFragment =
                        ReactsyTestResultsFragment.newInstance(mTestParameters, mTestResults);
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.reactsy_main_view,
                               testResultsFragment,
                               FRAGMENT_REACTSY_TEST_RESULTS);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        });
    }

    public void reactToState(int currentState) {
        switch (currentState) {
            case ReactsyTestState.STATE_BEFORE_TEST:
                this.mReadyButton.setVisibility(View.VISIBLE);
                this.mTestImage.setVisibility(View.GONE);
                this.mTestComplete.setVisibility(View.GONE);
                break;

            case ReactsyTestState.STATE_TRIAL_STARTED:
            case ReactsyTestState.STATE_TRIAL_VALID:
                this.mReadyButton.setVisibility(View.GONE);
                this.mTestImage.setVisibility(View.VISIBLE);
                this.mTestComplete.setVisibility(View.GONE);
                break;

            case ReactsyTestState.STATE_AFTER_TEST:
                this.mReadyButton.setVisibility(View.GONE);
                this.mTestImage.setVisibility(View.GONE);
                this.mTestComplete.setVisibility(View.VISIBLE);
                ReactsyNotification.cancelNotification(getActivity());
                recordAndSendTestResults();
                break;

            default:
                this.mReadyButton.setVisibility(View.GONE);
                this.mTestImage.setVisibility(View.GONE);
                this.mTestComplete.setVisibility(View.GONE);

                String toastText = null;
                switch (currentState) {
                    case ReactsyTestState.STATE_USER_WAITING:
                        // This is ok
                        break;
                    case ReactsyTestState.STATE_TOO_SOON:
                        toastText = getString(R.string.too_fast);
                        break;
                    case ReactsyTestState.STATE_TOO_LATE:
                        Vibrator vibrator =
                            (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(1000);

                        toastText = getString(R.string.too_slow);
                        break;
                }
                if (toastText != null) {
                    Toast.makeText(this.getActivity(), toastText, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            this.mTestState.resetTest();
        }
        else {
            this.mTestState.restoreTestState(savedInstanceState.getInt(KEY_CURRENT_STATE));
        }

        reactToState(mTestState.getCurrentState());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Not cool; reset test to beginning state
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CURRENT_STATE, this.mTestState.getCurrentState());
    }

    protected void recordAndSendTestResults() {
        new ReactsyResultsManager(getActivity()).sendOrRecordResults(this.mTestParameters,
                                                                     this.mTestResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        VolleyApplication.get(getActivity()).getRequestQueue().cancelAll(this);
    }
}
