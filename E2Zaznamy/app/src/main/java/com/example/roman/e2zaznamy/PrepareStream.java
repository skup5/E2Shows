package com.example.roman.e2zaznamy;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import java.io.IOException;

/**
 * AsyncTask preparing source for MediaPlayer from url.
 *
 * @author Roman Zelenik
 */
public class PrepareStream extends AsyncTask<String, Void, Boolean> {
    private static final String LOADING = "Načítání...";

    private ProgressDialog progress;
    private MediaPlayer mediaPlayer;
    private Context context;
    private OnErrorListener listener;

    public PrepareStream(Context context, MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        this.context = context;
        progress = new ProgressDialog(context);
        listener = new OnErrorListener() {
            @Override
            public void onError() {}
        };
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

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
            listener.onError();
        }
    }

    interface OnErrorListener {
        void onError();
    }
}

