package src.android;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.drm.DrmManagerClient;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundAudioPlayerService extends Service
        implements OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener{

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
        super();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "destroying...");
        stopForeground(true);
        actionStop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "on handle intent");
        Runnable task = createIntentTask(intent);
        new Thread(task).start();
        return START_STICKY;
    }

    private Runnable createIntentTask(final Intent intent){
        return new Runnable() {
            @Override
            public void run() {
                handleIntent(intent);
            }
        };
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
                setupAsForeground();
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
            mMediaPlayer.setOnBufferingUpdateListener(BackgroundAudioPlayerService.this);
            mMediaPlayer.setOnCompletionListener(BackgroundAudioPlayerService.this);
            mMediaPlayer.setOnErrorListener(BackgroundAudioPlayerService.this);
            mMediaPlayer.setOnInfoListener(BackgroundAudioPlayerService.this);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
                mMediaPlayer.prepareAsync();
               // mMediaPlayer.start();
//                if (mMediaPlayer.isPlaying()) {
//                    actionSetVolume();
//                    IsPlaying = true;
//                }
            }
        } catch (IOException ex) {
            // to do add exception handling
            Log.e(LOG_TAG, "unexpected error when playing audio...");
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void actionPlayStoppedPlayer() {
        if (mCurrentlyPlayingUrl != null && mMediaPlayer != null) {
            Log.v(LOG_TAG, "playing stopped player");
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
                IsPlaying = true;
            }
        }
        actionSetVolume();
    }

    private void actionStop() {
        Log.i(LOG_TAG, "stopping music...");
        stopForeground(true);
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

    private void setupAsForeground() {
        String radioName = "Tap to open";
        // assign the song name to songName
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), BackgroundAudioPlayer.MainActivity.getClass()), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(getApplicationContext().getResources().getIdentifier("icon", "drawable", getApplicationContext().getPackageName()))
                .setContentTitle("Vaanoli")
                .setContentText(radioName)
                .setContentIntent(pi);

        startForeground(12345, builder.build());
    }

    /*********************************************************************
     ******************AudioFocusChangeListener methods*******************
     *********************************************************************/
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v(LOG_TAG, "audio focus gain");
                actionPlayStoppedPlayer(); // play only on transient losses
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.v(LOG_TAG, "audio focus loss");
                actionStop();
                teardownPlayer();
                stopForeground(true);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v(LOG_TAG, "audio focus loss transient");
                actionStop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.v(LOG_TAG, "audio focus loss transient can duck");
                streamDuck();
                break;
        }
    }

    /*********************************************************************
     ******************OnBufferingUpdateListener methods******************
     *********************************************************************/
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        Log.v(LOG_TAG, "Buffer status : " + String.valueOf(i));
    }

    /*********************************************************************
     ******************OnCompletionListener methods*******************
     *********************************************************************/
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.v(LOG_TAG, "MediaPlayer completion");
        actionPlayStoppedPlayer();
    }

    /*********************************************************************
     ******************OnErrorListener methods****************************
     *********************************************************************/
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.v(LOG_TAG, "Error: what: " + i + " extra: " + i1);
        return false;
    }

    /*********************************************************************
     ******************OnInfoListener methods****************************
     *********************************************************************/

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        Log.v(LOG_TAG, "Info: what: " + i + " extra: " + i1);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }
}