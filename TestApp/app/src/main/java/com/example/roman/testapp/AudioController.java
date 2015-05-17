package com.example.roman.testapp;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * Created by Roman on 2.5.2015.
 */
public class AudioController {

    ImageButton play, next;
    ImageButton previous;
    SeekBar seekBar;
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setView(View view) {
        this.view = view;
        initControllers();
    }

    private void initControllers(){
        next = (ImageButton) view.findViewById(R.id.audio_controller_next_button);
        play = (ImageButton) view.findViewById(R.id.audio_controller_play_button);
        previous = (ImageButton) view.findViewById(R.id.audio_controller_previous_button);
        seekBar = (SeekBar) view.findViewById(R.id.audio_controller_seekBar);

        seekBar.setVisibility(View.INVISIBLE);
        initListeners();
    }

    private void initListeners() {
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
                if(fromUser){
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
        seekBar.setProgress(controller.getCurrentPosition());
        if(controller.isPlaying()) {
            seekHandler.postDelayed(run, 1000);
        }
    }

    public void setUpSeekBar() {
        seekBar.setMax(controller.getDuration());
        seekBar.setVisibility(View.VISIBLE);
    }

    interface AudioPlayerControl extends android.widget.MediaController.MediaPlayerControl {
        void next();
        void previous();
        void stop();
    }
}
