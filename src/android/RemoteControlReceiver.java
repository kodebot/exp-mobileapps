package src.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - PLAY");
            } else if (KeyEvent.KEYCODE_MEDIA_STOP == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - STOP");
            } else if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - PLAY/PAUSE");
            } else if (KeyEvent.KEYCODE_MEDIA_FAST_FORWARD == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - FF");
            } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - NEXT");
            } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - PAUSE");
            } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - PREVIOUS");
            } else if (KeyEvent.KEYCODE_MEDIA_REWIND == event.getKeyCode()) {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - REWIND");
            } else {
                Log.i(BackgroundAudioPlayerPlugin.LOG_TAG, "Media button - " + event.getKeyCode());
            }
        }
    }
}
