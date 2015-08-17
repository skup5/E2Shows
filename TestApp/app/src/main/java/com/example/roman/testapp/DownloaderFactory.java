package com.example.roman.testapp;
 
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.example.roman.testapp.jweb.Category;
import com.example.roman.testapp.jweb.Extractor;
import com.example.roman.testapp.jweb.HtmlParser;
import com.example.roman.testapp.jweb.HttpRequests;
import com.example.roman.testapp.jweb.Record;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This factory class creates AsyncTasks for downloading data (Downloaders).
 *
 * @author Roman Zelenik
 */
public class DownloaderFactory {

    private static final String DOWNLOADING = "Stahuji...";

    public enum Type {ArchivedCategories, Categories, Records, NextRecords, CoverImage}

    private DownloaderFactory() {
    }

    public static AbstractDownloader getDownloader(Type type) {
        return getDownloader(type, null, null);
    }

    public static AbstractDownloader getDownloader(Type type, Context context, String dialogTitle) {
        switch (type) {
            case ArchivedCategories:
                return new ArchivedCategoriesDownloader(context, dialogTitle);
            case Categories:
                return new CategoriesDownloader(context, dialogTitle);
            case CoverImage:
                return new CoverImageDownloader(context, dialogTitle);
            case NextRecords:
                return new NextRecordsDownloader(context, dialogTitle);
            case Records:
                return new RecordsDownloader(context, dialogTitle);
            default:
                return null;
        }
    }

    abstract static class AbstractDownloader<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

        protected ProgressDialog mProgressDialog;
        protected Context context;
        protected HtmlParser htmlParser;
        protected boolean useProgressDialog = false;
        protected OnCompleteListener onCompleteListener;
        protected OnErrorListener onErrorListener;
        protected List<String> errors;

        public AbstractDownloader(Context context, String dialogTitle){
            this.context = context;
            this.htmlParser = new HtmlParser();
            this.errors = new ArrayList();
            this.onCompleteListener = null;
            this.onErrorListener = null;
            if (context != null) {
                useProgressDialog = true;
                this.mProgressDialog = new ProgressDialog(context);
                this.mProgressDialog.setTitle(dialogTitle);
            }
        }

        public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
            this.onCompleteListener = onCompleteListener;
        }

        public void setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
        }

        protected abstract Result download(Params... params);

        @Override
        protected Result doInBackground(Params... params) {
            if (params != null && params.length > 0) {
                return download(params);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(useProgressDialog) {
                mProgressDialog.setMessage(DOWNLOADING);
                mProgressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            if(onErrorListener != null && !errors.isEmpty()){
                onErrorListener.onError(errors);
            }
            if(onCompleteListener != null){
                onCompleteListener.onComplete(result);
            }

            if (useProgressDialog && mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    static class ArchivedCategoriesDownloader extends AbstractDownloader<String, Void, Set<Category>> {

        public ArchivedCategoriesDownloader(Context context, String dialogTitle) {
            super(context, dialogTitle);
        }

        @Override
        protected Set<Category> download(String... url) {
            Set<Category> archiveCategories = null;
            Document site;
            try {
                site = HttpRequests.httpGetSite(url[0]);
                Elements categories = Extractor.getArchiveCategory(site);
                if (!categories.isEmpty()) {
                    archiveCategories = htmlParser.parseCategoryItems(categories);
                }
            } catch (IOException e) {
                e.printStackTrace();
                errors.add("Chyba při stahování seznamu kategorií z archivu.");
            }
            return archiveCategories;
        }
    }

    static class CategoriesDownloader extends AbstractDownloader<String, Void, Set<Category>> {

        public CategoriesDownloader(Context context, String dialogTitle) {
            super(context, dialogTitle);
        }

        @Override
        protected Set<Category> download(String... url) {
            Set<Category> actualCategories = null;
            Document site;
            try {
                site = HttpRequests.httpGetSite(url[0]);
                Elements categories = Extractor.getCategoryList(site);
                if (!categories.isEmpty()) {
                    actualCategories = htmlParser.parseCategoryItems(categories);
                }
            } catch (IOException e) {
                e.printStackTrace();
                errors.add("Chyba při stahování seznamu kategorií.");
            }
            return actualCategories;
        }

    }

    static class RecordsDownloader extends AbstractDownloader<Category, Void, Category> {

        public RecordsDownloader(Context context, String dialogTitle) {
            super(context, dialogTitle);
        }

        @Override
        protected Category download(Category... categories) {
            Category category = categories[0];
            URL site = category.getWebSite();
            if (site != null) {
                try {
                    Set<Record> set;
                    Document doc = HttpRequests.httpGetSite(site.toString());
                    Elements records = Extractor.getRecords(doc);
                    if (!records.isEmpty()) {
                        set = this.htmlParser.parseRecords(records, category);
                        if (!set.isEmpty()) {
                            category.addRecords(set);
                        }
                        Element nextRecord = Extractor.getNextRecord(doc);
                        if (nextRecord != null) {
                            String urlE2 = site.getProtocol() + "://" + site.getHost();
                            try {
                                category.update(this.htmlParser.parseCategory(records.first(), nextRecord, urlE2));
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                                errors.add("Chyba při aktualizaci údajů kategorie '" + category.getName() + "'.");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    errors.add("Chyba při stahování seznamu záznamů.");
                }
            }
            return category;
        }
    }

    static class NextRecordsDownloader extends AbstractDownloader<Category, Void, Category> {

        public NextRecordsDownloader(Context context, String dialogTitle) {
            super(context, dialogTitle);
        }

        @Override
        protected Category download(Category... categories) {
            Category category = categories[0];
            URL site = category.getNextRecords();
            int page = category.getPage();
            if (site != null && category.getRecordsCount() < category.getTotalRecordsCount()){
                try {
                    Set<Record> set;
                    page++;
                    Document doc = HttpRequests.httpPostNextRecords(site.toString(), category.getId() + "", page + "");
                    Elements records = Extractor.getRecords(doc);
                    if (!records.isEmpty()) {
                        set = this.htmlParser.parseRecords(records, category);
                        category.addRecords(set);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                    page--;
                    errors.add("Chyba při stahování dalších záznamů.");
                }
            }
            category.setPage(page);
            return category;
        }
    }

    static class CoverImageDownloader extends AbstractDownloader<Category, Void, Category> {

        public CoverImageDownloader(Context context, String dialogTitle) {
            super(context, dialogTitle);
        }

        @Override
        protected Category download(Category... categories) {
            Category category = categories[0];
            URL site = category.getImageUrl();
            if (site != null) {
                try {
                    InputStream stream = null;
                    stream = site.openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    if (bitmap != null) {
                        category.setCover(bitmap);
                    }
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    errors.add("Chyba při stahování obrázku kategorie.");
                }
            }
            return category;
        }
    }

    interface OnCompleteListener{
        void onComplete(Object result);
    }

    interface OnErrorListener{
        void onError(List<String> errors);
    }
}
