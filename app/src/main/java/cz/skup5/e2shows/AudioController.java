package cz.skup5.e2shows;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.skup5.e2shows.exception.NonPlaylistException;
import cz.skup5.e2shows.playlist.Playlist;
import cz.skup5.e2shows.playlist.PlaylistItem;

/**
 * A view containing controls for MediaPlayer.
 * Play/Pause, Forward and Previous buttons and progress slider.
 *
 * @author Roman Zelenik
 */
public class AudioController<Item extends PlaylistItem> {

  private ImageButton next, play, previous;
  private ImageView coverImage;
  private SeekBar seekBar;
  private TextView curTime, totalTime;
  private TextView infoLine;
  private Date cur, total;
  private SimpleDateFormat dateFormatter;
  private AudioPlayerControl controller;
  private View view;
  private Runnable run;
  private Handler seekHandler = new Handler();
  private Animation infoLineAnim, coverImageAnim;
  private boolean enabled;

  private Playlist<Item> playlist;

  public AudioController(View view, AudioPlayerControl controller) {
    this.enabled = false;
    this.controller = controller;
    this.run = new Runnable() {
      @Override
      public void run() {
        seekBarUpdate();
      }
    };
    this.dateFormatter = new SimpleDateFormat("mm:ss");
    this.cur = new Date();
    this.total = new Date();

    setView(view);
  }

    /*#######################################################
      ###               PUBLIC METHODS                    ###
      #######################################################*/

  public void clickOnPlayPause() {
    if (enabled) {
      if (controller.isPlaying()) {
        if (controller.canPause()) {
          controller.pause();
          play.setImageResource(R.drawable.play);
        }
      } else {
        controller.start();
        play.setImageResource(R.drawable.pause);
        seekBarUpdate();
      }
    }
  }

  public Playlist<Item> getPlaylist() {
    return playlist;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isPlaying() {
    return controller.isPlaying();
  }

  public void onCompletion() {
    seekEnd();
    play.setImageResource(R.drawable.play);
  }

  public void resetCoverImage() {
    coverImage.setImageResource(android.R.drawable.ic_menu_report_image);
  }

  public void setCoverImage(Bitmap cover) {
    coverImage.setImageBitmap(cover);
    coverImage.startAnimation(coverImageAnim);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setInfoLineText(String text) {
    infoLine.clearAnimation();
    infoLine.setText(text);
    prepareInfoLineAnim();
//        infoLine.setSelected(true);
    infoLine.startAnimation(infoLineAnim);
  }

  public void setPlaylist(Playlist<Item> playlist) {
    this.playlist = playlist;
  }

  public void setView(View view) {
    this.view = view;
    initComponents();
  }

  public void setUpSeekBar() {
    int duration = controller.getDuration();
    seekBar.setMax(duration);
    total.setTime(duration * 1000);
    totalTime.setText(dateFormatter.format(total));
    seekBar.setEnabled(true);
  }

    /*#######################################################
      ###              PRIVATE METHODS                    ###
      #######################################################*/

  private void prepareInfoLineAnim() {
    int textWidth = infoLine.getWidth();
    infoLineAnim = new TranslateAnimation(textWidth, -textWidth, 0, 0);
    infoLineAnim.setDuration(15000);
    infoLineAnim.setRepeatMode(Animation.RESTART);
    infoLineAnim.setRepeatCount(Animation.INFINITE);
    infoLineAnim.setInterpolator(new LinearInterpolator());
  }

  private void prepareCoverImageAnim() {
    coverImageAnim = new AlphaAnimation(0.1f, 1);
    coverImageAnim.setDuration(1500);
    coverImageAnim.setInterpolator(new LinearInterpolator());
  }

  private void initComponents() {
    next = (ImageButton) view.findViewById(R.id.audio_controller_next_button);
    play = (ImageButton) view.findViewById(R.id.audio_controller_play_button);
    previous = (ImageButton) view.findViewById(R.id.audio_controller_previous_button);
    seekBar = (SeekBar) view.findViewById(R.id.audio_controller_seekBar);
    curTime = (TextView) view.findViewById(R.id.audio_controller_cur_time);
    totalTime = (TextView) view.findViewById(R.id.audio_controller_total_time);
    infoLine = (TextView) view.findViewById(R.id.audio_controller_info_line);
    coverImage = (ImageView) view.findViewById(R.id.audio_controller_image);

    seekBar.setEnabled(false);
    initListeners();
    prepareCoverImageAnim();
  }

  private void initListeners() {
    next.setOnClickListener(v -> {
      if (enabled) {
        if (playlist == null) {
          throw new NonPlaylistException();
        }
       // controller.onNext(playlist.actual(), playlist.next());
        controller.next();
      }
    });
    play.setOnClickListener(v -> {
      //Toast.makeText(context, play.getWidth()+"x"+play.getHeight(), Toast.LENGTH_SHORT).show();
      clickOnPlayPause();
    });
    previous.setOnClickListener(v -> {
      // Toast.makeText(context, previous.getWidth()+"x"+previous.getHeight(), Toast.LENGTH_SHORT).show();
      if (enabled) {
        if (playlist == null) {
          throw new NonPlaylistException();
        }
       // controller.onPrevious(playlist.actual(), playlist.previous());
        controller.previous();
      }
    });
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (enabled && fromUser) {
          controller.seekTo(progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });
  }

  private void seekBarUpdate() {
    int currentPosition = controller.getCurrentPosition();
    cur.setTime(currentPosition * 1000);
    seekBar.setProgress(currentPosition);
    curTime.setText(dateFormatter.format(cur));
    if (controller.isPlaying()) {
      seekHandler.postDelayed(run, 1000);
    }
  }

  private void seekEnd() {
    if (seekBar.isEnabled()) {
      cur.setTime(total.getTime());
      seekBar.setProgress(controller.getDuration());
      curTime.setText(totalTime.getText());
    }
  }

  public static abstract class AudioPlayerControl implements MediaController.MediaPlayerControl {
    public abstract void next();

//    /**
//     * Invoked when user click "next".
//     *
//     * @param actualItem the last selected (played) item or null if none item wasn't selected yet
//     * @param nextItem   the next item in actual {@link Playlist}
//     */
   // abstract void onNext(Item actualItem, Item nextItem);

//    /**
//     * Invoked when user click "previous".
//     *
//     * @param actualItem   the last selected (played) item or null if none item wasn't selected yet
//     * @param previousItem the previous item in actual {@link Playlist}
//     */
    //abstract void onPrevious(Item actualItem, Item previousItem);

    public abstract void previous();

    public abstract void stop();

    @Override
    public int getAudioSessionId() {
      return 0;
    }

    @Override
    public int getBufferPercentage() {
      return 0;
    }

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
