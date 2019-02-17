package com.rekkursion.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

public class GoodMediaPlayer extends MediaPlayer
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private final Context mContext;
    private boolean mHasPrepared;

    public GoodMediaPlayer(Context context) {
        mContext = context;
        mHasPrepared = false;
        setOnPreparedListener(this);
        setOnCompletionListener(this);
        setOnErrorListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Toast.makeText(mContext, "Error happened when playing", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mHasPrepared = true;
        start();
    }
}
