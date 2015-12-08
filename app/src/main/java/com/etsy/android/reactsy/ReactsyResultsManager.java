package com.etsy.android.reactsy;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mhorowitz on 8/26/14.
 */
public class ReactsyResultsManager {
    protected static final int NO_TEST_INSTANCE_ID = -1;

    public class SendSavedResultsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            fetchAndSendResults();
            return null;
        }
    }

    protected Context mContext;
    protected ContentResolver mContentProvider;

    protected class SendResultsSuccessListener implements Response.Listener<JSONObject> {
        protected String testInstanceId;

        public SendResultsSuccessListener(long testInstanceId) {
            if (testInstanceId != NO_TEST_INSTANCE_ID) {
                this.testInstanceId = Long.toString(testInstanceId);
            }
            else {
                this.testInstanceId = null;
            }
        }

        @Override
        public void onResponse(JSONObject jsonObject) {
            if (this.testInstanceId != null) {
                mContentProvider.delete(ReactsyResultsProvider.UriHelper.URI_TEST_INSTANCE_BASE,
                                        ReactsyTables.TestInstance._ID + " = ?",
                                        new String[]{ this.testInstanceId });
                mContentProvider.delete(ReactsyResultsProvider.UriHelper.URI_TRIAL_RESULTS_BASE,
                                        ReactsyTables.TrialResults.COL_TEST_INSTANCE_ID + " = ?",
                                        new String[]{ this.testInstanceId });
            }
        }
    }

    protected class SendResultsErrorListener implements Response.ErrorListener {
        protected String testInstanceId;
        public ReactsyTestParameters mTestParameters;
        public ReactsyTestResults mTestResults;

        public SendResultsErrorListener(ReactsyTestParameters testParameters,
                                        ReactsyTestResults testResults,
                                        long testInstanceId) {

            this.mTestParameters = testParameters;
            this.mTestResults = testResults;

            if (testInstanceId != NO_TEST_INSTANCE_ID) {
                this.testInstanceId = Long.toString(testInstanceId);
            }
            else {
                this.testInstanceId = null;
            }
        }

        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Log.e(this.getClass().getName(), volleyError.getMessage());
            if (this.testInstanceId == null) {
                recordResultsLocally(this.mTestParameters, this.mTestResults);
            }
        }
    }

    public ReactsyResultsManager(Context context) {
        this.mContext = context;
        this.mContentProvider = context.getContentResolver();
    }

    protected void addVolleyRequest(Request request) {
        request.setTag(this.mContext);
        VolleyApplication.get(this.mContext).getRequestQueue().add(request);
    }

    public void sendResultsToServer(ReactsyTestParameters testParameters,
                                    ReactsyTestResults testResults)
        throws JSONException
    {
        sendResultsToServer(testParameters, testResults, NO_TEST_INSTANCE_ID);
    }

    public void sendResultsToServer(ReactsyTestParameters testParameters,
                                    ReactsyTestResults testResults,
                                    long testInstanceId)
        throws JSONException
    {
        SendResultsSuccessListener successListener =
            new SendResultsSuccessListener(testInstanceId);
        SendResultsErrorListener errorListener =
            new SendResultsErrorListener(testParameters, testResults, testInstanceId);

        // TODO: need a real URL
        addVolleyRequest(ReactsySendResultsRequest.buildResultsRequest("abc",
                                                                       testParameters,
                                                                       testResults,
                                                                       successListener,
                                                                       errorListener));
    }

    public void recordResultsLocally(ReactsyTestParameters testParameters,
                                     ReactsyTestResults testResults) {

        ContentValues testInstance = new ContentValues();

        testInstance.put(ReactsyTables.TestInstance.COL_TEST_DURATION_SEC,
                         testParameters.testDurationSec);
        testInstance.put(ReactsyTables.TestInstance.COL_MIN_DELAY_MS,
                         testParameters.minDelayMs);
        testInstance.put(ReactsyTables.TestInstance.COL_MAX_DELAY_MS,
                         testParameters.maxDelayMs);
        testInstance.put(ReactsyTables.TestInstance.COL_TOO_SOON_MS,
                         testParameters.tooSoonMs);
        testInstance.put(ReactsyTables.TestInstance.COL_TOO_LATE_MS,
                         testParameters.tooLateMs);
        testInstance.put(ReactsyTables.TestInstance.COL_RECOVERY_DELAY_MS,
                         testParameters.recoveryDelayMs);
        testInstance.put(ReactsyTables.TestInstance.COL_MIN_VALID_TRIALS_PCT,
                         testParameters.minValidTrialsPct);
        testInstance.put(ReactsyTables.TestInstance.COL_TEST_MECHANISM,
                         testParameters.testMechanism);
        testInstance.put(ReactsyTables.TestInstance.COL_LDAP,
                         testParameters.ldap);
        testInstance.put(ReactsyTables.TestInstance.COL_TEST_IDENTITY,
                         testResults.testIdentity);
        testInstance.put(ReactsyTables.TestInstance.COL_SUM_VALID_REACTIONS_MS,
                         testResults.sumValidTrialReactionsMs);
        testInstance.put(ReactsyTables.TestInstance.COL_NUM_TRIALS_TOO_LATE,
                         testResults.numTrialsTooLate);
        testInstance.put(ReactsyTables.TestInstance.COL_NUM_TRIALS_TOO_SOON,
                         testResults.numTrialsTooSoon);

        Uri testInstanceUri =
            this.mContentProvider.insert(ReactsyResultsProvider.UriHelper.URI_TEST_INSTANCE_BASE,
                                         testInstance);

        long testInstanceId = ReactsyResultsProvider.UriHelper.getTestInstanceId(testInstanceUri);

        ContentValues trialResult = new ContentValues();
        trialResult.put(ReactsyTables.TrialResults.COL_TEST_INSTANCE_ID, testInstanceId);

        for (ReactsyTestResult trial: testResults) {
            trialResult.put(ReactsyTables.TrialResults.COL_TRIAL, trial.trial);
            trialResult.put(ReactsyTables.TrialResults.COL_RESULT_STATE, trial.resultState);
            trialResult.put(ReactsyTables.TrialResults.COL_FROM_STATE, trial.fromState);
            trialResult.put(ReactsyTables.TrialResults.COL_WAIT_MS, trial.waitMs);
            trialResult.put(ReactsyTables.TrialResults.COL_REACTION_MS, trial.reactionMs);

            // Ignore returned Uri
            this.mContentProvider.insert(ReactsyResultsProvider.UriHelper.URI_TRIAL_RESULTS_BASE,
                                         trialResult);
        }
    }

    protected static String getString(Cursor c, String column) {
        return c.getString(c.getColumnIndex(column));
    }

    protected static long getLong(Cursor c, String column) {
        return c.getLong(c.getColumnIndex(column));
    }

    protected static int getInt(Cursor c, String column) {
        return c.getInt(c.getColumnIndex(column));
    }

    protected static double getDouble(Cursor c, String column) {
        return c.getDouble(c.getColumnIndex(column));
    }

    // TODO: need a bkg thread to do this; when?
    public void fetchAndSendResults() {
        Cursor testInstances =
            this.mContentProvider.query(ReactsyResultsProvider.UriHelper.URI_TEST_INSTANCE_BASE,
                                        ReactsyTables.ALL_COLUMNS,
                                        ReactsyTables.ALL_ROWS,
                                        null,   // no query parameters needed
                                        null);  // no sort order needed

        ReactsyTestParameters parameters = new ReactsyTestParameters();
        ReactsyTestResults testResults = new ReactsyTestResults(null);

        while (! testInstances.isAfterLast()) {
            parameters.testDurationSec =
                getInt(testInstances, ReactsyTables.TestInstance.COL_TEST_DURATION_SEC);
            parameters.minDelayMs =
                getInt(testInstances, ReactsyTables.TestInstance.COL_MIN_DELAY_MS);
            parameters.maxDelayMs =
                getInt(testInstances, ReactsyTables.TestInstance.COL_MAX_DELAY_MS);
            parameters.tooSoonMs =
                getInt(testInstances, ReactsyTables.TestInstance.COL_TOO_SOON_MS);
            parameters.tooLateMs =
                getInt(testInstances, ReactsyTables.TestInstance.COL_TOO_LATE_MS);
            parameters.recoveryDelayMs =
                getInt(testInstances, ReactsyTables.TestInstance.COL_RECOVERY_DELAY_MS);
            parameters.minValidTrialsPct =
                getDouble(testInstances, ReactsyTables.TestInstance.COL_MIN_VALID_TRIALS_PCT);
            parameters.testMechanism =
                getString(testInstances, ReactsyTables.TestInstance.COL_TEST_MECHANISM);
            parameters.ldap =
                getString(testInstances, ReactsyTables.TestInstance.COL_LDAP);

            testResults.testIdentity =
                getString(testInstances, ReactsyTables.TestInstance.COL_TEST_IDENTITY);
            testResults.sumValidTrialReactionsMs =
                getInt(testInstances, ReactsyTables.TestInstance.COL_SUM_VALID_REACTIONS_MS);
            testResults.numTrialsTooLate =
                getInt(testInstances, ReactsyTables.TestInstance.COL_NUM_TRIALS_TOO_LATE);
            testResults.numTrialsTooSoon =
                getInt(testInstances, ReactsyTables.TestInstance.COL_NUM_TRIALS_TOO_SOON);

            long testInstanceId = getLong(testInstances, ReactsyTables.TestInstance._ID);
            Cursor trialResults =
                this.mContentProvider.query(ReactsyResultsProvider.UriHelper.URI_TRIAL_RESULTS_BASE,
                                            ReactsyTables.ALL_COLUMNS,
                                            ReactsyTables.TrialResults.COL_TEST_INSTANCE_ID + " = ?",
                                            new String[] { Long.toString(testInstanceId) },
                                            ReactsyTables.TrialResults.COL_TRIAL);

            while (! trialResults.isAfterLast()) {
                ReactsyTestResult trialResult = new ReactsyTestResult();

                trialResult.trial =
                    getInt(trialResults, ReactsyTables.TrialResults.COL_TRIAL);
                trialResult.resultState =
                    getInt(trialResults, ReactsyTables.TrialResults.COL_RESULT_STATE);
                trialResult.fromState =
                    getInt(trialResults, ReactsyTables.TrialResults.COL_FROM_STATE);
                trialResult.waitMs =
                    getInt(trialResults, ReactsyTables.TrialResults.COL_WAIT_MS);
                trialResult.reactionMs =
                    getInt(trialResults, ReactsyTables.TrialResults.COL_REACTION_MS);

                testResults.add(trialResult);

                trialResults.moveToNext();
            }

            // Send fetched data; reset parameters/testResults before reuse TODO try
            try {
                sendResultsToServer(parameters, testResults, testInstanceId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            testResults.clear();

            testInstances.moveToNext();
        }
    }

    public void sendSavedResults() {
        new SendSavedResultsTask().execute();
    }

    public void sendOrRecordResults(ReactsyTestParameters testParameters,
                                    ReactsyTestResults testResults) {
        try {
            sendResultsToServer(testParameters, testResults);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
