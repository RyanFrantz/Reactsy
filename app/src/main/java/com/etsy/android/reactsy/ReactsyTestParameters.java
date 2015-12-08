package com.etsy.android.reactsy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by mhorowitz on 8/19/14.
 */
public class ReactsyTestParameters implements Serializable {
    public static final String MECHANISM_PRESS_DOWN = "down";
    public static final String MECHANISM_LIFT_UP = "up";

    public int testDurationSec;
    public int minDelayMs;
    public int maxDelayMs;
    public int tooSoonMs;
    public int tooLateMs;
    public int recoveryDelayMs;
    public double minValidTrialsPct;
    public String testMechanism;
    public String ldap;

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put("testDurationSec", this.testDurationSec);
        result.put("minDelayMs", this.minDelayMs);
        result.put("maxDelayMs", this.maxDelayMs);
        result.put("tooSoonMs", this.tooSoonMs);
        result.put("tooLateMs", this.tooLateMs);
        result.put("recoveryDelayMs", this.recoveryDelayMs);
        result.put("minValidTrialsPct", this.minValidTrialsPct);
        result.put("testMechanism", this.testMechanism);
        result.put("ldap", this.ldap);

        return result;
    }
}
