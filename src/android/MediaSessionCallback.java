package src.android;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    @Override
    public void onPlay() {
        super.onPlay();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - Play");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - Pause");
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - Next");
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - Previous");
    }

    @Override
    public void onFastForward() {
        super.onFastForward();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - FF");
    }

    @Override
    public void onRewind() {
        super.onRewind();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - Rewind");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media Session Callback - Stop");
    }
}
