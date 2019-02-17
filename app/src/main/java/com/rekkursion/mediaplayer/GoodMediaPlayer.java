package com.rekkursion.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class GoodMediaPlayer extends MediaPlayer
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private final Context mContext;
    private boolean mHasPrepared;
    private Handler mHandler;
    private Runnable mRunnable;
    private RelativeLayout mControllerContainer = null;
    private SeekBar mSeekBar = null;
    private TextView mTxvShowSongLength = null;
    private String songLengthString = "00:00";
    private static final int POST_DELAYED_INTERVAL_MSEC = 10;

    public GoodMediaPlayer(Context context) {
        mContext = context;
        mHasPrepared = false;
        mHandler = new Handler();

        setOnPreparedListener(this);
        setOnCompletionListener(this);
        setOnErrorListener(this);
    }

    // set media play bar
    public void setSeekBar(SeekBar seekBar) {
        mSeekBar = seekBar;
    }

    // set media controller container
    public void setControllerContainerAndChildren(RelativeLayout controllerContainer) {
        mControllerContainer = controllerContainer;
        mSeekBar = controllerContainer.findViewById(R.id.skb_media_play_bar);
        mTxvShowSongLength = controllerContainer.findViewById(R.id.txv_show_song_length);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
                if(input) {
                    seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // set data source then prepare async
    public void setDataSourceThenPrepareAsync(Uri fileUri) throws IOException, IllegalStateException {
        if(mHasPrepared)
            this.reset();
        setDataSource(fileUri.toString());
        prepareAsync();
    }

    // play the audio
    public void play() {
        mSeekBar.setProgress(getCurrentPosition());

        mRunnable = new Runnable() {
            @Override
            public void run() {
                // show the current position and the total length of the playing song
                int sec = mSeekBar.getProgress() / 1000;
                String currentPositionString = String.format("%02d:%02d", sec / 60, sec % 60);
                mTxvShowSongLength.setText(currentPositionString + "/" + songLengthString);

                play();
            }
        };
        mHandler.postDelayed(mRunnable, POST_DELAYED_INTERVAL_MSEC);
    }

    // release resources when destroying
    public void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        release();
    }

    @Override
    public void start() throws IllegalStateException {
        play();
        super.start();
    }

    @Override
    public void reset() {
        super.reset();
        mHasPrepared = false;
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        if(mHasPrepared)
            super.seekTo(msec);
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
        mSeekBar.setMax(mediaPlayer.getDuration());
        songLengthString = String.format("%02d:%02d", (mSeekBar.getMax() / 1000) / 60, (mSeekBar.getMax() / 1000) % 60);
        start();
    }
}
