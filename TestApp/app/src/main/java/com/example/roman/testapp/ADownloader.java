package com.example.roman.testapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.roman.testapp.jweb.HtmlParser;

/**
 * Created by Roman on 28.3.2015.
 */
public abstract class ADownloader<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected ProgressDialog mProgressDialog;
    protected Context context;
//    protected Type type;
    protected HtmlParser htmlParser;
    protected boolean useProgressDialog = false;


    public ADownloader(Context context, String dialogTitle){
//        this.type = type;
        this.context = context;
        this.htmlParser = new HtmlParser();
        if (context != null) {
            useProgressDialog = true;
            this.mProgressDialog = new ProgressDialog(context);
            this.mProgressDialog.setTitle(dialogTitle);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(useProgressDialog) {
            // mProgressDialog.setTitle("Mp3 archiv Evropy 2");
            mProgressDialog.setMessage("Stahuji...");
            // mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//          mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }
        //Toast.makeText(context, "Stahuji kategorie...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        //Toast.makeText(context, "Stahování dokončeno", Toast.LENGTH_SHORT).show();
        if (useProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}

