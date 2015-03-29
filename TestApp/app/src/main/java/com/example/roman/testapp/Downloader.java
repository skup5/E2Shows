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
 * Created by Roman on 27.3.2015.
 */
public class Downloader extends AsyncTask<String, Void, Set<E2Data>> {

    public static enum Type {Category, Records}

    protected ProgressDialog mProgressDialog;
    protected Context context;
    protected Type type;
    protected HtmlParser htmlParser;
    protected boolean useProgressDialog = false;


    public Downloader(Context context, Type type, String dialogTitle){
        this.context = context;

        this.type = type;
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
    protected Set<E2Data> doInBackground(String... params) {
        Set set = null;
        if (params != null && params.length > 0) {
            switch (type) {
                case Category:
                    try {
                        set = downloadCategory(params[0]);
                        //publishProgress();
                    }
                    catch (IOException e) {

                    }
                    break;
                case Records:
                    break;
                default:
                    break;
            }
        }
        return set;
    }

    @Override
    protected void onPostExecute(Set<E2Data> result) {
        super.onPostExecute(result);
        Toast.makeText(context, "Stahování dokončeno", Toast.LENGTH_SHORT).show();
        if (useProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
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

    private Set<Record> downloadRecords(Category category) throws IOException {
//        URL site = category.getWebSite();
//        Document doc = JWeb.httpGetSite(site.toString());
//        Elements records = Extractor.getRecords(doc);
//        boolean successful = category.update(this.htmlParser.parseCategory(records.first(), urlE2));
//        return this.htmlParser.parseRecords(records, site.getProtocol() + "://" + site.getHost(), category);

        return null;
    }
}
