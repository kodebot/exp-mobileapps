package src.android;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import java.io.IOException;

public class FroyoAudioPlayer extends AudioPlayer
        implements OnBufferingUpdateListener,
        OnPreparedListener,
        OnErrorListener,
        OnCompletionListener {

    private MediaPlayer mediaPlayer;
    private Context context;
    private StateChangeListener stateChangeListener;

    public FroyoAudioPlayer(Context context, StateChangeListener stateChangeListener) {
        if (context == null) throw new IllegalArgumentException("Context must be non null value");
        this.context = context;
        this.stateChangeListener = stateChangeListener;
    }

    @Override
    public void initPlayer() {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Initialising player");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Player initialised successfully");
    }

    @Override
    public void releasePlayer() {
        if (mediaPlayer == null) return;
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Releasing player");
        mediaPlayer.release();
        mediaPlayer = null;
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Player released successfully");
    }

    @Override
    public void play(String url) {
        if (mediaPlayer == null) throw new IllegalStateException("Player must be initialised to play");
        if (url == null || url.length() == 0) throw new IllegalStateException("Invalid url is specified");
        try {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Setting data source to play");
            mediaPlayer.setDataSource(url);
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Data source set successfully");
            mediaPlayer.prepareAsync();
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Preparing player");
        } catch (IOException ex) {
            Log.e(BackgroundAudioPlayerPlugin.LOG_TAG, "Error while setting data source", ex);
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer == null) throw new IllegalStateException("Player must be initialised to stop");
        if (mediaPlayer.isPlaying()) {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Stopping player");
            mediaPlayer.stop();
            if (stateChangeListener != null) {
                stateChangeListener.onStopped();
            }
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Player stopped successfully");
        }
    }

    @Override
    public void setVolume(float volume) {
        if (mediaPlayer == null) throw new IllegalStateException("Player must be initialised to set volume");
        if (mediaPlayer.isPlaying()) {
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Setting volume");
            mediaPlayer.setVolume(volume, volume);
            Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Volume set successfully");
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, String.format("Buffering %d", percent));
        if (stateChangeListener != null) {
            stateChangeListener.onBuffering(percent);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        if (stateChangeListener != null) {
            stateChangeListener.onError(new Exception(
                    String.format("Exception while playing. What %d, Extra %d", what, extra)));
            Log.e(BackgroundAudioPlayerPlugin.LOG_TAG, String.format("Error during playback. What %d Extra %d", what, extra));
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Prepared successfully, starting the player");
        mediaPlayer.start();
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Playback started successfully");
        if (stateChangeListener != null) {
            stateChangeListener.onPlaying();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.v(BackgroundAudioPlayerPlugin.LOG_TAG, "Playback completed");
        if (stateChangeListener != null) {
            stateChangeListener.onComplete();
        }
    }
}
