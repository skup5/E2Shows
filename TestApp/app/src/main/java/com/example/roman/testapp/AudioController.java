package com.example.roman.testapp;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Roman on 2.5.2015.
 */
public class AudioController {

    ImageButton next, play, previous;
    SeekBar seekBar;
    TextView curTime, totalTime;
    Date cur, total;
    SimpleDateFormat dateFormater;
    AudioPlayerControl controller;
    Context context;
    View view;
    Runnable run;
    Handler seekHandler = new Handler();
    boolean enabled;

    public AudioController(Context context, View view, AudioPlayerControl controller){
        this.enabled = false;
        this.context = context;
        this.controller = controller;
        this.run = new Runnable() {
            @Override
            public void run() {
                seekBarUpdation();
            }
        };
        this.dateFormater = new SimpleDateFormat("mm:ss");
        this.cur = new Date();
        this.total = new Date();

        setView(view);
    }

    public void clickOnPlay(){
        if(enabled) {
            if (controller.isPlaying()) {
                if (controller.canPause()) {
                    controller.pause();
                    play.setImageResource(R.drawable.play);
                }
            } else {
                controller.start();
                play.setImageResource(R.drawable.pause);
                seekBarUpdation();
            }
        }
    }

    public void onCompletion(){
        seekEnd();
        play.setImageResource(R.drawable.play);
    }

    private void seekEnd(){
        if(seekBar.isEnabled()) {
            cur.setTime(total.getTime());
            seekBar.setProgress(controller.getDuration());
            curTime.setText(totalTime.getText());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setView(View view) {
        this.view = view;
        initComponents();
    }

    private void initComponents(){
        next = (ImageButton) view.findViewById(R.id.audio_controller_next_button);
        play = (ImageButton) view.findViewById(R.id.audio_controller_play_button);
        previous = (ImageButton) view.findViewById(R.id.audio_controller_previous_button);
        seekBar = (SeekBar) view.findViewById(R.id.audio_controller_seekBar);
        curTime = (TextView) view.findViewById(R.id.audio_controller_cur_time);
        totalTime = (TextView) view.findViewById(R.id.audio_controller_total_time);

        //seekBar.setVisibility(View.INVISIBLE);
        seekBar.setEnabled(false);
        initListeners();
    }

    private void initListeners() {
        Log.i("AudioController", "init listeners");
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enabled) { controller.next(); }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, play.getWidth()+"x"+play.getHeight(), Toast.LENGTH_SHORT).show();
                clickOnPlay();
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(context, previous.getWidth()+"x"+previous.getHeight(), Toast.LENGTH_SHORT).show();
                if(enabled) { controller.previous(); }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(enabled && fromUser){
                    controller.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void seekBarUpdation() {
        int currentPosition = controller.getCurrentPosition();
        cur.setTime(currentPosition * 1000);
        seekBar.setProgress(currentPosition);
        curTime.setText(dateFormater.format(cur));
        if(controller.isPlaying()) {
            seekHandler.postDelayed(run, 1000);
        }
    }

    public void setUpSeekBar() {
        int duration = controller.getDuration();
        seekBar.setMax(duration);
        total.setTime(duration * 1000);
        totalTime.setText(dateFormater.format(total));
        //seekBar.setVisibility(View.VISIBLE);
        seekBar.setEnabled(true);
    }

    static abstract class AudioPlayerControl implements MediaController.MediaPlayerControl {
        abstract void next();
        abstract void previous();
        abstract void stop();

        @Override
        public int getAudioSessionId() {
            return 0;
        }

        @Override
        public int getBufferPercentage() { return 0; }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }
    }
}
