package com.etsy.android.reactsy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by mhorowitz on 8/21/14.
 */
public class ReactsyTestResultsFragment extends Fragment {
    protected static final String ARG_TEST_PARAMETERS = "test_parameters";
    protected static final String ARG_TEST_RESULTS = "test_results";

    public ReactsyTestParameters mTestParameters;
    public ReactsyTestResults mTestResults;

    public LinearLayout mReactsyTestResults;
    public ListView mReactsyTestResultsList;
    public Button mViewParametersButton;

    protected class TestResultsAdapter extends BaseAdapter {
        protected LayoutInflater mInflater;

        public TestResultsAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mTestResults.size();
        }

        @Override
        public Object getItem(int position) {
            return mTestResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // inflate a new view
                convertView = mInflater.inflate(R.layout.reactsy_trial_result, parent, false);
            }

            TextView trialNumber = (TextView) convertView.findViewById(R.id.trial_number);
            TextView trialSuccess = (TextView) convertView.findViewById(R.id.trial_success);
            TextView trialReactionTime =
                (TextView) convertView.findViewById(R.id.trial_reaction_time);

            ReactsyTestResult trialResult = mTestResults.get(position);

            trialNumber.setText(Long.toString(trialResult.trial) + ':');
            trialSuccess.setText(ReactsyTestState.STATES[trialResult.resultState]);
            trialReactionTime.setText(Long.toString(trialResult.reactionMs) + "ms");

            return convertView;
        }
    }

    public static ReactsyTestResultsFragment newInstance(ReactsyTestParameters testParameters,
                                                         ReactsyTestResults testResults) {

        ReactsyTestResultsFragment reactsyTestResultsFragment = new ReactsyTestResultsFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_TEST_PARAMETERS, testParameters);
        args.putSerializable(ARG_TEST_RESULTS, testResults);
        reactsyTestResultsFragment.setArguments(args);

        return reactsyTestResultsFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Bundle args = this.getArguments();
        this.mTestParameters = (ReactsyTestParameters) args.getSerializable(ARG_TEST_PARAMETERS);
        this.mTestResults = (ReactsyTestResults) args.getSerializable(ARG_TEST_RESULTS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.reactsy_test_results, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mViewParametersButton = (Button) view.findViewById(R.id.view_parameters_button);
        this.mReactsyTestResults = (LinearLayout) view.findViewById(R.id.reactsy_test_results);
        this.mReactsyTestResultsList = (ListView) view.findViewById(R.id.test_results_list);
        this.mReactsyTestResultsList.setAdapter(new TestResultsAdapter(getActivity()));

        TextView summaryStat = (TextView) view.findViewById(R.id.num_trials);
        summaryStat.setText(Long.toString(this.mTestResults.getNumValidTrials())
                               + "/"
                               + Long.toString(this.mTestResults.size())
                               + " ("
                               + String.format("%.2f",
                                               this.mTestResults.getValidTrialsPercentage() * 100.0)
                               + "%)");

        summaryStat = (TextView) view.findViewById(R.id.mean);
        summaryStat.setText(String.format("%.2f", this.mTestResults.getValidTrialsMean()) + "ms");

        summaryStat = (TextView) view.findViewById(R.id.variance);
        summaryStat.setText(String.format("%.2f", this.mTestResults.getValidTrialsVariance()) + "ms");

        summaryStat = (TextView) view.findViewById(R.id.std_dev);
        summaryStat.setText(String.format("%.2f", this.mTestResults.getValidTrialsStdDeviation()) + "ms");

        this.mViewParametersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowParametersDialog.show(getFragmentManager(), mTestParameters);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
