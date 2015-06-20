package com.example.roman.testapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by SKUP on 25.3.2015.
 */
public class PrepareStream extends AsyncTask<String, Void, Boolean> {
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

        this.progress.setMessage("Načítání...");
        this.progress.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Boolean prepared = false;
        try {
            //Log.d("class Player", "params[0]=" + params[0]);
            mediaPlayer.setDataSource(params[0]);
            mediaPlayer.prepare();
            prepared = true;
        } catch (IllegalArgumentException e) {
            Log.d("IllegalArgument", e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        Log.d("Prepared", "//" + result);
    }

    interface OnErrorListener {
        void onError();
    }
}

