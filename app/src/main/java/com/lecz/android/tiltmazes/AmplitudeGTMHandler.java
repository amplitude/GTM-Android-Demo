package com.lecz.android.tiltmazes;

import android.util.Log;

import com.amplitude.api.Amplitude;
import com.google.android.gms.tagmanager.Container.FunctionCallTagCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by danieljih on 5/6/16.
 */

public class AmplitudeGTMHandler implements FunctionCallTagCallback
{

    public static final String AmplitudeGTMLogEventTag = "logEvent";
    public static final String AmplitudeGTMSetUserIdTag = "setUserId";
    public static final String AmplitudeGTMSetUserPropertiesTag = "setUserProperties";

    private static final String TAG = "AmplitudeGTMHandler";

    private AmplitudeGTMHandler(){};
    protected static AmplitudeGTMHandler instance = new AmplitudeGTMHandler();

    public static AmplitudeGTMHandler getInstance() {
        return instance;
    }

    @Override
    public void execute(String functionName, java.util.Map<String, Object> parameters) {
        Log.i("AmplitudeGTMHandler", "GTM HANDLER");

        switch(functionName) {
            case AmplitudeGTMLogEventTag:
                Log.i(TAG, AmplitudeGTMLogEventTag + ": " + parameters.toString());
                String eventType = (String) parameters.get("eventType");
                JSONObject eventProperties = null;
                try {
                    eventProperties = new JSONObject((String)parameters.get("eventProperties"));
                } catch (JSONException e) {
                    Log.e(TAG, "Could not parse event properties JSONObject");
                }
                Amplitude.getInstance().logEvent(eventType, eventProperties);
                break;

            case AmplitudeGTMSetUserIdTag:
                Log.i(TAG, AmplitudeGTMSetUserIdTag + ": " + parameters.toString());
                String userId = (String) parameters.get("userId");
                Amplitude.getInstance().setUserId(userId);
                break;

            case AmplitudeGTMSetUserPropertiesTag:
                Log.i(TAG, AmplitudeGTMSetUserPropertiesTag + ": " + parameters.toString());
                JSONObject userProperties = null;
                try {
                    userProperties = new JSONObject((String) parameters.get("userProperties"));
                } catch (JSONException e) {
                    Log.e(TAG, "Could not parse user properties JSONObject");
                }
                Amplitude.getInstance().setUserProperties(userProperties);
                break;
        }
    }
}
