package src.android;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import libs.org.apache.cordova.PluginResult;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundAudioPlayerService extends Service
        implements OnAudioFocusChangeListener,
        AudioPlayer.StateChangeListener {

    // todo : detect wifi connection and stop playing if preference is set to play only on wifi -- add preference
    // todo : audio becomes noisy
    // todo : hardware button integration
    private static WifiManager.WifiLock wifiLock;
    private static PowerManager.WakeLock wakeLock;
    private static String currentlyPlayingUrl;
    private static Timer stopTimer;
    private static AudioPlayer audioPlayer;
    private static boolean isTransientAudioFocusLoss = false;
    private static boolean isDucked = false;
    private static String currentRadioName;

    private static final float DUCKING_VOLUME = 0.1f;

    public static boolean isPlaying = false;
    public static float currentVolume = 0.5f;
    public static int currentRadio = 0;
    public static Date closeTime = null;

    public final int NOTIFICATION_ID = 12745;


    public BackgroundAudioPlayerService() {
        super();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Destroying the background service");
        stopForeground(true);
        actionStop();
        teardownPlayer();

        // reset states
        isTransientAudioFocusLoss = false;
        isDucked = false;
        isPlaying = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Creating the background service");
        audioPlayer = AudioPlayer.getInstance(getApplicationContext(), this);
        setupPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return super.onStartCommand(null, flags, startId); // just call super when intent is null
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Starting command");
        Runnable task = createIntentTask(intent);
        new Thread(task).start();
        return START_STICKY;
    }

    private Runnable createIntentTask(final Intent intent) {
        return new Runnable() {
            @Override
            public void run() {
                handleIntent(intent);
            }
        };
    }

    private void handleIntent(Intent intent) {

        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Handling intent");

        String action = intent.getStringExtra(BackgroundAudioPlayerPlugin.EXTRA_ACTION);
        try {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Processing action: " + action);
            if (action.equals(BackgroundAudioPlayerPlugin.ACTION_PLAY)) {
                currentlyPlayingUrl = intent.getStringExtra(BackgroundAudioPlayerPlugin.EXTRA_AUDIO_URL);
                currentRadio = intent.getIntExtra(BackgroundAudioPlayerPlugin.EXTRA_RADIO_ID, 0);
                currentRadioName = intent.getStringExtra(BackgroundAudioPlayerPlugin.EXTRA_RADIO_NAME);
                actionPlay();
                setupAsForeground();
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_STOP)) {
                actionStop();
                stopForeground(true);
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_SET_VOLUME)) {
                currentVolume = Float.parseFloat(intent.getStringExtra(BackgroundAudioPlayerPlugin.EXTRA_VOLUME));
                actionSetVolume();
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_SCHEDULE_CLOSE)) {
                int closeTimeInMinutes = intent.getIntExtra(BackgroundAudioPlayerPlugin.EXTRA_CLOSE_TIME_MINS, 0);
                actionScheduleClose(closeTimeInMinutes);
            } else if (action.equals(BackgroundAudioPlayerPlugin.ACTION_CANCEL_SCHEDULED_CLOSE)) {
                actionCancelScheduledClose();
            }
        } catch (Exception ex) {
            // todo: change the radio status
            Log.e(BackgroundAudioPlayerPlugin.LOG_TAG, "Error when handling the intent...", ex);
        }
    }

    /*******************************************************************************************************************
     *************************************Intent action methods ********************************************************
     ******************************************************************************************************************/
    private void actionPlay() {
        if (currentlyPlayingUrl != null) {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Playing beginning");
            audioPlayer.play(currentlyPlayingUrl);
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Playing begun successfully");
        }
    }

    private void actionStop() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Stopping...");
        audioPlayer.stop();
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Stopped successfully");
    }

    private void actionSetVolume() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Adjusting volume...");
        audioPlayer.setVolume(currentVolume);
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Volume adjusted successfully");
    }

    private void actionScheduleClose(int minutes) {
        actionCancelScheduledClose(); // cancel any previously scheduled close
        int durationInMillis = minutes * 60 * 1000;
        stopTimer = new Timer();
        stopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                actionStop();
                closeTime = null;
                stopForeground(true);
                fireActionCallback();
            }
        }, durationInMillis);

        closeTime = new Date(System.currentTimeMillis() + durationInMillis);
    }

    private void actionCancelScheduledClose() {
        if (stopTimer != null) {
            stopTimer.cancel();
            closeTime = null;
        }
    }

    /*******************************************************************************************************************
     ****************************************Internal private methods***************************************************
     ******************************************************************************************************************/
    private void setupPlayer() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Setting up the player...");
        audioPlayer.initPlayer();
        acquireWakeLock();
        acquireWifiLock();
        setupAudioFocus();
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Player setup successfully");

    }

    private void teardownPlayer() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Tearing down the player");
        if (audioPlayer == null) return;
        audioPlayer.releasePlayer();
        audioPlayer = null;
        releaseWakeLock();
        releaseWifiLock();
    }

    private void setupAudioFocus() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Trying to gain stream music audio focus.");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Unable to gain audio focus");
        } else {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Gained stream music audio focus successfully.");
        }
    }


    private void streamDuck() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Ducking audio...");
        audioPlayer.setVolume(DUCKING_VOLUME);
        isDucked = true;
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Ducking completed successfully");
    }

    private void acquireWakeLock() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Acquiring partial wake lock");
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BackgroundAudioPlayerPlugin.LOG_TAG);
            wakeLock.acquire();
        } else if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Partial wake lock acquired");
    }

    private void releaseWakeLock() {
        if (wakeLock == null) return;
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Releasing partial wake lock");
        wakeLock.release();

        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Partial wake lock released successfully");
    }

    private void acquireWifiLock() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Acquiring wifi lock");
        if (wifiLock == null) {
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, BackgroundAudioPlayerPlugin.LOG_TAG);
            wifiLock.acquire();
        } else if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Wifi lock acquired");
    }

    private void releaseWifiLock() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Releasing wifi lock");
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Wifi lock released");
    }

    private void fireActionCallback() {
        PluginResult result = new PluginResult(PluginResult.Status.OK, "callback.offtimer.success");
        result.setKeepCallback(true);
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "About to call off timer success callback.");
        if (BackgroundAudioPlayerPlugin.OffTimerCallbackContext != null) {
            BackgroundAudioPlayerPlugin.OffTimerCallbackContext.sendPluginResult(result);
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Off timer success callback called.");
        }
    }

    private void setupAsForeground() {
        String contentText = "Tap to open";
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), BackgroundAudioPlayerPlugin.mainActivity.getClass()), PendingIntent.FLAG_UPDATE_CURRENT);
        int largeIconId = getApplicationContext().getResources().getIdentifier("icon", "drawable", getApplicationContext().getPackageName());
        Bitmap largeIcon = BitmapFactory.decodeResource(getApplicationContext().getResources(), largeIconId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setLargeIcon(largeIcon)
                .setContentTitle("Playing " + currentRadioName)
                .setContentText(contentText)
                .setContentIntent(pi);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    /*********************************************************************
     * *****************AudioFocusChangeListener methods*******************
     *********************************************************************/
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Audio focus gained");
                if (isTransientAudioFocusLoss) {
                    Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Recovering from transient audio focus loss");
                    actionPlay();
                    isTransientAudioFocusLoss = false;
                    Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Recovered from transient audio focus loss");
                }

                if (isDucked) {
                    Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Recovering from duckabke transient audio focus loss");
                    actionSetVolume();
                    isDucked = false;
                    Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Recovered from duckable transient audio focus loss");
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Audio focus lost");
                actionStop();
                stopForeground(true);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Audio focus transient lost");
                if(isPlaying){
                    isTransientAudioFocusLoss = true;
                }
                actionStop();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Audio focus loss transient can duck");
                if(isPlaying) {
                    streamDuck();
                }
                break;
        }
    }

    /*********************************************************************
     * *****************StageChangeListener methods*******************
     *********************************************************************/
    @Override
    public void onPlaying() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Playing event fired");
        isPlaying = true;
    }

    @Override
    public void onStopped() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Stopped event fired");
        isPlaying = false;
    }

    @Override
    public void onBuffering(int percent) {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Buffering event fired " + percent);
    }

    @Override
    public void onComplete() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Completed event fired");
    }

    @Override
    public void onError(Exception ex) {
        Log.e(BackgroundAudioPlayerPlugin.LOG_TAG, "Error reported", ex);
    }
}