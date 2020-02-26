package cz.skup5.e2shows.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import cz.skup5.e2shows.ui.listener.OnErrorListener;

/**
 * AsyncTask preparing source for MediaPlayer from url.
 *
 * @author Roman Zelenik
 */
public class PrepareStream extends AsyncTask<String, Void, Boolean> {
  private static final String LOADING = "Načítání...";

  private ProgressDialog progress;
  private MediaPlayer mediaPlayer;
  private OnErrorListener listener;
  private Exception error;

  public PrepareStream(Context context, MediaPlayer mediaPlayer) {
    this.mediaPlayer = mediaPlayer;
    progress = new ProgressDialog(context);
    listener = errors -> {
    };
  }

  public void setOnErrorListener(OnErrorListener listener) {
    this.listener = listener;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    this.error = null;
    this.progress.setMessage(LOADING);
    this.progress.show();
  }

  @Override
  protected Boolean doInBackground(String... params) {
    Boolean prepared = false;
    try {
      mediaPlayer.setDataSource(params[0]);
      mediaPlayer.prepare();
      prepared = true;
    } catch (IllegalArgumentException | IllegalStateException | IOException | SecurityException e) {
      e.printStackTrace();
      error = e;
    }
    return prepared;
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);
    if (progress.isShowing()) {
      progress.cancel();
    }
    if (!result) {
      listener.onError(Collections.singletonList(error.getLocalizedMessage()));
    }
  }
}

