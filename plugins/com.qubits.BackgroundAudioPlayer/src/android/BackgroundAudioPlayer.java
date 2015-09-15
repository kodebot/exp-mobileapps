package com.qubits.cordova.plugin;

import android.content.Intent;
import org.apache.cordova.*;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.Override;
import java.lang.String;

public class BackgroundAudioPlayer extends CordovaPlugin{

    public static final String ACTION_PLAY = "action.play";
    public static final String ACTION_STOP = "action.stop";
    public static final String ACTION_SET_VOLUME = "action.set.volume";
    public static final String ACTION_GET_VOLUME = "action.get.volume";
    public static final String ACTION_GET_STATUS = "action.get.status";
    public static final String ACTION_GET_CURRENT_RADIO = "action.get.current.radio";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Intent intent = new Intent(cordova.getActivity(), BackgroundAudioPlayerService.class);
        intent.putExtra("action", action);

        try {
            if (action.equals(BackgroundAudioPlayer.ACTION_PLAY)) {
                intent.putExtra("audioUrl", args.getString(0));
                intent.putExtra("radioId", args.getInt(1));
                cordova.getActivity().startService(intent);
                callbackContext.success();
            } else if (action.equals(BackgroundAudioPlayer.ACTION_STOP)) {
                cordova.getActivity().startService(intent);
                callbackContext.success();
            } else if (action.equals(BackgroundAudioPlayer.ACTION_SET_VOLUME)) {
                intent.putExtra("volume", args.getString(0));
                cordova.getActivity().startService(intent);
                callbackContext.success();
            } else if (action.equals(BackgroundAudioPlayer.ACTION_GET_STATUS)) {
                if (BackgroundAudioPlayerService.IsPlaying) {
                    callbackContext.success(1);
                } else {
                    callbackContext.success(0);
                }
            } else if (action.equals(BackgroundAudioPlayer.ACTION_GET_VOLUME)) {
                callbackContext.success(Float.toString(BackgroundAudioPlayerService.CurrentVolume));
            } else if (action.equals(BackgroundAudioPlayer.ACTION_GET_CURRENT_RADIO)) {
                callbackContext.success(BackgroundAudioPlayerService.CurrentRadio);
            }
        }catch(Exception ex){
            callbackContext.error(ex.getMessage());
        }
        return true;
    }



}