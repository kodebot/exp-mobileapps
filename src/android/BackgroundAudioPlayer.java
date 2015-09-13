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

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Intent intent = new Intent(cordova.getActivity(), BackgroundAudioPlayerService.class);
        intent.putExtra("action", action);
        if(action.equals(BackgroundAudioPlayerService.ACTION_PLAY)) {
            intent.putExtra("audioUrl", args.getString(0));
        }

        if(action.equals(BackgroundAudioPlayerService.ACTION_SET_VOLUME)){
            intent.putExtra("volume", args.getString(0));
        }
        cordova.getActivity().startService(intent);
        callbackContext.success(); // Todo : change this appropriately
        return true;
    }


    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

    @Override
    public void onReset() {
        super.onReset();
    }
}