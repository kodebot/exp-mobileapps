package com.qubits.cordova.plugin;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;

public class BackgroundAudioPlayerService extends IntentService
        implements OnAudioFocusChangeListener {

    // todo : detect wifi connection and stop playing if preference is set to play only on wifi -- add preference
    // todo : audio becomes noisy
    // todo : hardware button integration
    private static MediaPlayer mediaPlayer;
    private static WifiManager.WifiLock wifiLock;
    private static String currentlyPlayingUrl;

    public static final String ACTION_PLAY = "action.play";
    public static final String ACTION_STOP = "action.stop";
    public static final String ACTION_SET_VOLUME = "action.set.volume";

    public static boolean STATE_PLAYING = false;
    public static float STATE_VOLUME = 0.5f;

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
            if (action.equals(ACTION_PLAY)) {
                currentlyPlayingUrl = intent.getExtras().getString("audioUrl");
                actionPlay();
            } else if (action.equals(ACTION_STOP)) {
                actionStop();
            } else if (action.equals(ACTION_SET_VOLUME)) {
                STATE_VOLUME = intent.getFloatExtra("volume", STATE_VOLUME);
            }
        } catch (Exception ex) {
            // change the radio status
            Log.e(LOG_TAG, "error when handling the intent...");
        }
    }

    private void setupPlayer() {
        Log.i(LOG_TAG, "setting up the player...");
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK); // to keep cpu running
            acquireWifiLock();
            setupAudioFocus();
        }
    }

    private void teardownPlayer() {
        Log.i(LOG_TAG, "tearing down the player...");
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            releaseWifiLock();
        }
    }

    private void setupAudioFocus() {
        Log.i(LOG_TAG, "trying to gain stream music audio focus.");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(LOG_TAG, "Unable to gain audio focus");
        } else {
            Log.i(LOG_TAG, "Gained stream music audio focus successfully.");
        }

    }

    private void actionPlay() {
        try {
            if (currentlyPlayingUrl != null) {
                actionStop(); // stop first if already playing
                setupPlayer();
                Log.i(LOG_TAG, "playing");
                mediaPlayer.setDataSource(currentlyPlayingUrl);
                mediaPlayer.prepare();
                mediaPlayer.start();
                if (mediaPlayer.isPlaying()) {
                    actionSetVolume();
                    STATE_PLAYING = true;
                }
            }
        }catch(IOException ex) {
            // to do add exception handling
            Log.e(LOG_TAG, "unexpected error when playing audio...");
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void actionStop() {
        Log.i(LOG_TAG, "stopping music...");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            if (!mediaPlayer.isPlaying()) {
                STATE_PLAYING = false;
            }
        }
        teardownPlayer();
    }

    private void actionSetVolume() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.i(LOG_TAG, "adjusting volume...");
            mediaPlayer.setVolume(STATE_VOLUME, STATE_VOLUME);
        }
    }

    private void streamDuck() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Log.i(LOG_TAG, "ducking audio...");
            mediaPlayer.setVolume(0.1f, 0.1f);
        }
    }

    private void acquireWifiLock() {
        Log.i(LOG_TAG, "acquiring wifi lock...");
        if (wifiLock == null) {
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "qubits_wifi_lock");
            wifiLock.acquire();
        } else if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
    }

    private void releaseWifiLock() {
        Log.i(LOG_TAG, "releasing wifi lock...");
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                setupPlayer();
                actionPlay();
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