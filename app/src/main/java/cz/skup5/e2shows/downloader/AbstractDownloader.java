package cz.skup5.e2shows.downloader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import cz.skup5.jEvropa2.HtmlParser;

/**
 * This abstract class simplifies creating AsyncTask for downloading data (Downloaders).
 * <p/>
 * Created on 5.1.2019
 *
 * @author Skup5
 */
abstract class AbstractDownloader<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private static final String DOWNLOADING = "Stahuji...";

    protected ProgressDialog mProgressDialog;
    protected Context context;
    protected HtmlParser htmlParser;
    protected boolean useProgressDialog = false;
    protected OnCompleteListener<Result> onCompleteListener;
    protected OnErrorListener onErrorListener;
    protected List<String> errors;

    AbstractDownloader() {
        this(null, "");
    }

    AbstractDownloader(Context context, String dialogTitle) {
        this.context = context;
        this.htmlParser = new HtmlParser();
        this.errors = new ArrayList<>();
        this.onCompleteListener = null;
        this.onErrorListener = null;
        if (context != null) {
            useProgressDialog = true;
            this.mProgressDialog = new ProgressDialog(context);
            this.mProgressDialog.setTitle(dialogTitle);
        }
    }

    public void setOnCompleteListener(OnCompleteListener<Result> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    protected abstract Result download(@NonNull Params... params);

    @Override
    protected Result doInBackground(Params... params) {
        if (((params != null) && (params.length > 0)) || (params instanceof Void[])) {
            try {
                return download(params);
            } catch (Exception e) {
                e.printStackTrace();
                errors.add(e.getLocalizedMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (useProgressDialog) {
            mProgressDialog.setMessage(DOWNLOADING);
            mProgressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (onErrorListener != null && !errors.isEmpty()) {
            onErrorListener.onError(errors);
        }
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(result);
        }

        if (useProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    public interface OnCompleteListener<Result> {
        void onComplete(Result result);
    }

    public interface OnErrorListener {
        void onError(List<String> errors);
    }
}
