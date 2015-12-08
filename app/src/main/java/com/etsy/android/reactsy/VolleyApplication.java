package com.etsy.android.reactsy;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by mhorowitz on 8/25/14.
 */
public class VolleyApplication extends Application {
    public static VolleyApplication get(Context context) {
        return (VolleyApplication) context.getApplicationContext();
    }

    private RequestQueue mRequestQueue;

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (this.mRequestQueue == null) {
            this.mRequestQueue = Volley.newRequestQueue(this);
        }
    }
}
