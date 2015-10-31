package src.android;

import android.content.Context;
import android.os.Build;

import java.util.Vector;

public abstract class AudioPlayer {
    private static AudioPlayer instance;

    public abstract void initPlayer();

    public abstract void releasePlayer();

    public abstract void play(String url);

    public abstract void stop();

    public abstract void setVolume(float volume);

    public interface StateChangeListener {
        void onPlaying();

        void onStopped();

        void onBuffering(int percent);

        void onComplete();

        void onError(Exception ex);
    }

    public static AudioPlayer getInstance(Context context, StateChangeListener stateChangeListener) {
       // return new FroyoAudioPlayer(context, stateChangeListener); // for testing
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= Build.VERSION_CODES.FROYO && sdkVersion < Build.VERSION_CODES.JELLY_BEAN) {
            return new FroyoAudioPlayer(context, stateChangeListener);
        } else if (sdkVersion >= Build.VERSION_CODES.JELLY_BEAN) { // jelly bean and above
            return new JellyBeanAudioPlayer(context, stateChangeListener);
        } else {
            throw new UnsupportedOperationException("Audio playback is not supported in the selected platform");
        }
    }
}
