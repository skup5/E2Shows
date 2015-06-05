package com.example.roman.testapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.roman.testapp.jweb.Category;
import com.example.roman.testapp.jweb.Extractor;
import com.example.roman.testapp.jweb.HtmlParser;
import com.example.roman.testapp.jweb.JWeb;
import com.example.roman.testapp.jweb.Record;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Created by Roman on 28.3.2015.
 */
public class DownloaderFactory {

    public static enum Type {ArchivedCategories, Categories, Records, NextRecords}

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

        public AbstractDownloader(Context context, String dialogTitle){
            this.context = context;
            this.htmlParser = new HtmlParser();
            this.onCompleteListener = null;
            if (context != null) {
                useProgressDialog = true;
                this.mProgressDialog = new ProgressDialog(context);
                this.mProgressDialog.setTitle(dialogTitle);
            }
        }

        public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
            this.onCompleteListener = onCompleteListener;
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
            //Toast.makeText(context, "Stahovani dokonceno", Toast.LENGTH_SHORT).show();
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
                site = JWeb.httpGetSite(url[0]);
                archiveCategories = htmlParser.parseCategoryItems(Extractor.getArchiveCategory(site));
            } catch (IOException e) {
                e.printStackTrace();
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
            Set<Category> categories = null;
            Document site;
            try {
                site = JWeb.httpGetSite(url[0]);
                categories = htmlParser.parseCategoryItems(Extractor.getCategoryList(site));
            } catch (IOException e) {
                e.printStackTrace();
            }
//    Elements categoryList = Extractor.getCategory(site);
//    category = new LinkedHashSet<>(categoryList.size());
//    for (Element categoryItem : categoryList) {
//      urlCat = null;
//      urlCat = categoryItem.attr("href");
//      if (urlCat != null) {
//        System.out.println(urlCat);
//        doc = JWeb.httpGetSite(urlE2 + urlCat);
//        category.add(this.htmlParser.parseCategory(doc, urlE2));
//      }
//    }
            return categories;
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
                    Document doc = JWeb.httpGetSite(site.toString());
                    Elements records = Extractor.getRecords(doc);
                    Element nextRecord = Extractor.getNextRecord(doc);
                    String urlE2 = site.getProtocol() + "://" + site.getHost();
                    boolean successful = category.update(this.htmlParser.parseCategory(records.first(), nextRecord, urlE2));
                    set = this.htmlParser.parseRecords(records, urlE2, category);
                    category.addRecords(set);
                } catch (IOException e) {
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
            if (site != null){
                try {
                    Set<Record> set;
                    int page = category.getPage();
                    page++;
                    category.setPage(page);
                    Document doc = JWeb.httpPostNextRecords(site.toString(), category.getId() + "", page + "");
                    Elements records = Extractor.getRecords(doc);
                    set = this.htmlParser.parseRecords(records, site.getProtocol()+"://"+site.getHost(), category);
                    category.addRecords(set);
                } catch (IOException e){}
            }
            return category;
        }
    }

    interface OnCompleteListener{
        void onComplete(Object result);
    }
}
