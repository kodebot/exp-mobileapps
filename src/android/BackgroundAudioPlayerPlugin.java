package src.android;

import android.app.Activity;
import android.content.Intent;
import org.apache.cordova.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.Override;
import java.lang.String;

public class BackgroundAudioPlayerPlugin extends CordovaPlugin{

    public static final String ACTION_PLAY = "action.play";
    public static final String ACTION_STOP = "action.stop";
    public static final String ACTION_SCHEDULE_CLOSE="action.schedule.close";
    public static final String ACTION_CANCEL_SCHEDULED_CLOSE="action.cancel.scheduled.close";
    public static final String ACTION_GET_TIME_TO_SCHEDULED_CLOSE= "action.get.time.to.scheduled.close";
    public static final String ACTION_SET_VOLUME = "action.set.volume";
    public static final String ACTION_GET_VOLUME = "action.get.volume";
    public static final String ACTION_GET_STATUS = "action.get.status";
    public static final String ACTION_GET_CURRENT_RADIO = "action.get.current.radio";

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_AUDIO_URL = "audio.url";
    public static final String EXTRA_RADIO_ID = "radio.id";
    public static final String EXTRA_RADIO_NAME = "radio.name";
    public static final String EXTRA_VOLUME = "volume";
    public static final String EXTRA_CLOSE_TIME_MINS = "close.time.in.minutes";

    public static final String LOG_TAG = "BackgroundAudioPlayerPlugin";

    public static CallbackContext OffTimerCallbackContext = null;

    public static Activity MainActivity = null;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        MainActivity = cordova.getActivity();
        Intent intent = new Intent(cordova.getActivity(), BackgroundAudioPlayerService.class);
        intent.putExtra(EXTRA_ACTION, action);

        try {
            if (action.equals(BackgroundAudioPlayerPlugin.ACTION_PLAY)) {
                intent.putExtra(EXTRA_AUDIO_URL, args.getString(0));
                intent.putExtra(EXTRA_RADIO_ID, args.getInt(1));
                cordova.getActivity().startService(intent);
                callbackContext.success();
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_STOP)) {
                cordova.getActivity().startService(intent);
                callbackContext.success();
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_SET_VOLUME)) {
                intent.putExtra(EXTRA_VOLUME, args.getString(0));
                cordova.getActivity().startService(intent);
                callbackContext.success();
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_GET_STATUS)) {
                if (BackgroundAudioPlayerService.isPlaying) {
                    callbackContext.success(1);
                } else {
                    callbackContext.success(0);
                }
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_GET_VOLUME)) {
                callbackContext.success(Float.toString(BackgroundAudioPlayerService.currentVolume));
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_GET_CURRENT_RADIO)) {
                callbackContext.success(BackgroundAudioPlayerService.currentRadio);
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_SCHEDULE_CLOSE)){
                intent.putExtra(EXTRA_CLOSE_TIME_MINS, args.getInt(0));
                OffTimerCallbackContext = callbackContext;
                cordova.getActivity().startService(intent);
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                OffTimerCallbackContext.sendPluginResult(result);
            } else if(action.equals(BackgroundAudioPlayerPlugin.ACTION_CANCEL_SCHEDULED_CLOSE)){
                cordova.getActivity().startService(intent);
                OffTimerCallbackContext = null;
                callbackContext.success();
            } else if(action.equals(BackgroundAudioPlayerPlugin.ACTION_GET_TIME_TO_SCHEDULED_CLOSE)){
                if(BackgroundAudioPlayerService.closeTime != null){
                    long diff = BackgroundAudioPlayerService.closeTime.getTime() - System.currentTimeMillis();
                    callbackContext.success(String.valueOf(diff));
                }else {
                    callbackContext.success("0");
                }
            }
        }catch(Exception ex){
            callbackContext.error(ex.getMessage());
        }
        return true;
    }



}