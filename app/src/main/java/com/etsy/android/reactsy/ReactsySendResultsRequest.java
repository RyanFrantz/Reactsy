package com.etsy.android.reactsy;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mhorowitz on 8/25/14.
 */
public class ReactsySendResultsRequest extends JsonObjectRequest {
    public static final String POSTVAR_TEST_PARMS = "test_parms";
    public static final String POSTVAR_TEST_RESULTS = "test_results";

    public ReactsySendResultsRequest(String url,
                                     JSONObject jsonRequest,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        super(Method.POST, url, jsonRequest, listener, errorListener);
    }

    public static ReactsySendResultsRequest buildResultsRequest(String url,
                                                                ReactsyTestParameters parameters,
                                                                ReactsyTestResults testResults,
                                                                Response.Listener<JSONObject> listener,
                                                                Response.ErrorListener errorListener)
            throws JSONException {

        JSONObject requestData = new JSONObject();

        requestData.put(POSTVAR_TEST_PARMS, parameters.toJSON());
        requestData.put(POSTVAR_TEST_RESULTS, testResults.toJSON());

        return new ReactsySendResultsRequest(url, requestData, listener, errorListener);
    }
}
