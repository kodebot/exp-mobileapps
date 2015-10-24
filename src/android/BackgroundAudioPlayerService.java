package com.qubits.cordova.plugin;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.text.style.ImageSpan;
import android.util.Log;
import org.apache.cordova.PluginResult;

import java.io.IOException;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundAudioPlayerService extends IntentService
        implements OnAudioFocusChangeListener {

    // todo : detect wifi connection and stop playing if preference is set to play only on wifi -- add preference
    // todo : audio becomes noisy
    // todo : hardware button integration
    private static MediaPlayer mMediaPlayer;
    private static WifiManager.WifiLock mWifiLock;
    private static String mCurrentlyPlayingUrl;
    private static boolean mHasSetupAudioFocus = false;
    private static Timer mStopTimer;

    public static boolean IsPlaying = false;
    public static float CurrentVolume = 0.5f;
    public static int CurrentRadio = 0;
    public static Date CloseTime = null;

    public final String LOG_TAG = "BackgroundAudioPlayerService";

    public BackgroundAudioPlayerService() {
        super("com.qubits.backgroundaudioplayer");
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "destroying...");
        // actionStop();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(LOG_TAG, "on handle intent");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        Log.i(LOG_TAG, "handling intent");

        String action = intent.getExtras().getString("action");
        try {
            Log.i(LOG_TAG, "passed in action " + action);
            if (action.equals(BackgroundAudioPlayer.ACTION_PLAY)) {
                mCurrentlyPlayingUrl = intent.getExtras().getString("audioUrl");
                CurrentRadio = intent.getIntExtra("radioId", 0);
                actionPlay();
            } else if (action.equals(BackgroundAudioPlayer.ACTION_STOP)) {
                actionStop();
            } else if (action.equals(BackgroundAudioPlayer.ACTION_SET_VOLUME)) {
                CurrentVolume = Float.parseFloat(intent.getStringExtra("volume"));
                actionSetVolume();
            } else if (action.equals(BackgroundAudioPlayer.ACTION_SCHEDULE_CLOSE)) {
                int closeTimeInMinutes = intent.getIntExtra("closeTimeInMinutes", 0);
                actionScheduleClose(closeTimeInMinutes);
            } else if (action.equals(BackgroundAudioPlayer.ACTION_CANCEL_SCHEDULED_CLOSE)) {
                actionCancelScheduledClose();
            }
        } catch (Exception ex) {
            // change the radio status
            Log.e(LOG_TAG, "error when handling the intent...");
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void setupPlayer() {
        Log.i(LOG_TAG, "setting up the player...");
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // to keep cpu running
            acquireWifiLock();
            setupAudioFocus();
        }
    }

    private void teardownPlayer() {
        Log.i(LOG_TAG, "tearing down the player...");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            releaseWifiLock();
        }
    }

    private void setupAudioFocus() {
        if (mHasSetupAudioFocus == false) {
            Log.i(LOG_TAG, "trying to gain stream music audio focus.");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.i(LOG_TAG, "Unable to gain audio focus");
            } else {
                Log.i(LOG_TAG, "Gained stream music audio focus successfully.");
                mHasSetupAudioFocus = true;
            }
        }

    }

    private void actionPlay() {
        try {
            if (mCurrentlyPlayingUrl != null) {
                actionStop(); // stop first if already playing
                setupPlayer();
                Log.i(LOG_TAG, "playing");
                mMediaPlayer.setDataSource(mCurrentlyPlayingUrl);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                if (mMediaPlayer.isPlaying()) {
                    actionSetVolume();
                    IsPlaying = true;
                }
            }
        } catch (IOException ex) {
            // to do add exception handling
            Log.e(LOG_TAG, "unexpected error when playing audio...");
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void actionStop() {
        Log.i(LOG_TAG, "stopping music...");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            if (!mMediaPlayer.isPlaying()) {
                IsPlaying = false;
            }
        }
        teardownPlayer();
    }

    private void actionSetVolume() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            Log.i(LOG_TAG, "adjusting volume...");
            mMediaPlayer.setVolume(CurrentVolume, CurrentVolume);
        }
    }

    private void actionScheduleClose(int minutes) {
        actionCancelScheduledClose(); // cancel any previously scheduled close
        int durationInMillis = minutes * 60 * 1000;
        mStopTimer = new Timer();
        mStopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                actionStop();
                fireActionCallback();
            }
        }, durationInMillis);

        CloseTime = new Date(System.currentTimeMillis() + durationInMillis);
    }

    private void actionCancelScheduledClose() {
        if (mStopTimer != null) {
            mStopTimer.cancel();
            CloseTime = null;
        }
    }

    private void streamDuck() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            Log.i(LOG_TAG, "ducking audio...");
            mMediaPlayer.setVolume(0.1f, 0.1f);
        }
    }

    private void acquireWifiLock() {
        Log.i(LOG_TAG, "acquiring wifi lock...");
        if (mWifiLock == null) {
            mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "qubits_wifi_lock");
            mWifiLock.acquire();
        } else if (!mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    private void releaseWifiLock() {
        Log.i(LOG_TAG, "releasing wifi lock...");
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void fireActionCallback() {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "callback.offtimer.success");
        result.setKeepCallback(true);
        Log.i(LOG_TAG, "about to call offtimer success callback...");
        if (BackgroundAudioPlayer.OffTimerCallbackContext != null) {
            BackgroundAudioPlayer.OffTimerCallbackContext.sendPluginResult(result);
            Log.i(LOG_TAG, "offtimer success callback called...");
        }

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                actionSetVolume(); // only handle ducking
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                actionStop();
                teardownPlayer();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                actionStop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                streamDuck();
                break;
        }
    }
}