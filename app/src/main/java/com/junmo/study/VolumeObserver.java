package com.junmo.study;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import static android.content.Context.AUDIO_SERVICE;

public class VolumeObserver extends ContentObserver{
    Context context;

    public VolumeObserver(Context c, Handler handler) {
        super(handler);
        context=c;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        MainActivity.volume_val = currentVolume;
    }
}