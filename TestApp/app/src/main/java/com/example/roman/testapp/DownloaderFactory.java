package com.example.roman.testapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.roman.testapp.jweb.Category;
import com.example.roman.testapp.jweb.E2Data;
import com.example.roman.testapp.jweb.Extractor;
import com.example.roman.testapp.jweb.HtmlParser;
import com.example.roman.testapp.jweb.JWeb;
import com.example.roman.testapp.jweb.Record;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Created by Roman on 28.3.2015.
 */
public class DownloaderFactory {

    public static enum Type {Category, Records}

    private DownloaderFactory(){}

    public static ADownloader getDownloader(Type type) {
        return getDownloader(type, null, null);
    }

    public static ADownloader getDownloader(Type type, Context context, String dialogTitle){
        switch (type) {
            case Category: return new CategoryDownloader(context, dialogTitle);
            case Records: return new RecordsDownloader(context, dialogTitle);
            default: return null;
        }
    }

    private ADownloader createCategoryDownloader(Context context, String dialogTitle) {
        return null;
    }

    static class CategoryDownloader extends ADownloader<String, Void, Set<Category>> {

        public CategoryDownloader(Context context, String dialogTitle){
            super(context, dialogTitle);
        }

        @Override
        protected Set<Category> doInBackground(String... params) {
            Set set = null;
            if (params != null && params.length > 0) {
                try {
                    set = downloadCategory(params[0]);
                    //publishProgress();
                }
                catch (IOException e) {  }
            }
            return set;
        }


        private Set<Category> downloadCategory(String url) throws IOException {
            Set<Category> category;
            Document site;
            site = JWeb.httpGetSite(url);
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

            category = htmlParser.parseCategoryItems(Extractor.getCategoryList(site));
            return category;

        }

    }

    static class RecordsDownloader extends ADownloader<Category, Void, Set<Record>> {

        public RecordsDownloader(Context context, String dialogTitle) {
            super(context, dialogTitle);
        }

        @Override
        protected Set<Record> doInBackground(Category... params) {
            Set set = null;
            if (params != null && params.length > 0) {
                try {
                    set = downloadRecords(params[0]);
                    //publishProgress();
                } catch (IOException e) {
                }
            }
            return set;
        }

        private Set<Record> downloadRecords(Category category) throws IOException {
            URL site = category.getWebSite();
            Document doc = JWeb.httpGetSite(site.toString());
            Elements records = Extractor.getRecords(doc);
            String urlE2 = site.getProtocol() + "://" + site.getHost();
            boolean successful = category.update(this.htmlParser.parseCategory(records.first(), urlE2));
            return this.htmlParser.parseRecords(records, urlE2, category);
        }
    }

}
