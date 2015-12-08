package com.etsy.android.reactsy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mhorowitz on 8/21/14.
 */
public class ReactsyTestResults extends ArrayList<ReactsyTestResult> {
    public String testIdentity;     // "YYYY-MM-DD HH:MI:SS"
    public long sumValidTrialReactionsMs = 0;
    public int numTrialsTooLate = 0;
    public int numTrialsTooSoon = 0;

    public ReactsyTestResults(String testIdentity) {
        this.testIdentity = testIdentity;
    }

    @Override
    public void clear() {
        super.clear();
        this.sumValidTrialReactionsMs = 0;
        this.numTrialsTooLate = 0;
        this.numTrialsTooSoon = 0;
    }

    public void addTrial(int resultState, int fromState, long waitMs, long reactionMs) {
        ReactsyTestResult trialResult = new ReactsyTestResult();
        trialResult.trial = size();
        trialResult.resultState = resultState;
        trialResult.fromState = fromState;
        trialResult.waitMs = waitMs;
        trialResult.reactionMs = reactionMs;
        add(trialResult);

        switch (resultState) {
            case ReactsyTestState.STATE_TRIAL_VALID:
                this.sumValidTrialReactionsMs += reactionMs;
                break;
            case ReactsyTestState.STATE_TOO_SOON:
                this.numTrialsTooSoon++;
                break;
            case ReactsyTestState.STATE_TOO_LATE:
                this.numTrialsTooLate++;
                break;
        }
    }

    public long getNumValidTrials() {
        return size() - this.numTrialsTooSoon - this.numTrialsTooLate;
    }

    public double getValidTrialsPercentage() {
        int numTrials = size();
        if (numTrials > 0) {
            return ((double) getNumValidTrials()) / ((double) size());
        }
        return 0.0;
    }

    public double getValidTrialsMean() {
        long numValidTrials = getNumValidTrials();
        if (numValidTrials > 0) {
            return ((double) this.sumValidTrialReactionsMs) / ((double) numValidTrials);
        }
        return 0.0;
    }

    public double getValidTrialsVariance() {
        long numValidTrials = getNumValidTrials();

        if (numValidTrials > 0) {
            double validTrialsMean = getValidTrialsMean();
            double varianceSum = 0.0;

            for (ReactsyTestResult trialResult : this) {
                if (trialResult.resultState == ReactsyTestState.STATE_TRIAL_VALID) {
                    double variance = validTrialsMean - (double) trialResult.reactionMs;
                    varianceSum += variance * variance;
                }
            }

            return varianceSum / ((double) getNumValidTrials());
        }

        return 0.0;
    }

    public double getValidTrialsStdDeviation() {
        return Math.sqrt(getValidTrialsVariance());
    }

    protected JSONArray trialsToJSON() throws JSONException {
        JSONArray result = new JSONArray();

        for (int i = 0; i < this.size(); i++) {
            result.put(i, this.get(i).toJSON());
        }

        return result;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("testIdentity", this.testIdentity);
        result.put("sumValidTrialReactionsMs", this.sumValidTrialReactionsMs);
        result.put("numTrialsTooLate", this.numTrialsTooLate);
        result.put("numTrialsTooSoon", this.numTrialsTooSoon);
        result.put("trials", trialsToJSON());

        return result;
    }
}
