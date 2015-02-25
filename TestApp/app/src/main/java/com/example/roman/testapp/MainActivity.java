package com.example.roman.testapp;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    ProgressDialog mProgressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class E2 extends AsyncTask<Void, Void, Void> {
        String title;
        String url = "http://evropa2.cz/mp3-archiv/";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Mp3 archiv Evropy 2");
            mProgressDialog.setMessage("Načítání...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to the web site
                Document document = Jsoup.connect(url).get();
                // Get the html document title
                title = document.title();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Set title into TextView
            TextView txttitle = (TextView) findViewById(R.id.textView);
            txttitle.setText(title);
            mProgressDialog.dismiss();
        }
    }

    public void refresh(View v){
        /*TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getData1("http://evropa2.cz/mp3-archiv/"));
        */
        new E2().execute();
    }

    public String getData1(String url){
        String text = "none";
        try {
            Document doc = Jsoup.connect(url).get();
            text = "Title: " + doc.title();

        } catch (IOException e) {
            //e.printStackTrace();
        }
        return text;
    }

    public String getData2(String url){
        return "none";
    }
}
