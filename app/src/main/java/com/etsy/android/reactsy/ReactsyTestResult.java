package com.etsy.android.reactsy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by mhorowitz on 8/21/14.
 */
public class ReactsyTestResult implements Serializable {
    int trial;      // 0-based
    int resultState;
    int fromState;
    long waitMs;
    long reactionMs;

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("trial", this.trial);
        result.put("resultState", this.resultState);
        result.put("fromState", this.fromState);
        result.put("waitMs", this.waitMs);
        result.put("reactionMs", this.reactionMs);

        return result;
    }
}
