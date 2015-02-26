package com.example.roman.testapp;


import android.app.ProgressDialog;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    ProgressDialog mProgressDialog;
    AsyncPlayer ap;
    boolean play, playingStream;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ap = new AsyncPlayer("MyTest");
        playingStream = false;
        play = false;
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
            String url = "http://evropa2.cz";
            url += "/file/edee/tym-a-porady/mp3-archiv/18058/20150225_odhaleni.mp3";
            TextView mp3url = (TextView) findViewById(R.id.mp3Url);
            mp3url.setText(url);
            mProgressDialog.dismiss();
        }
    }

    public void refresh(View v){
        /*TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getData1("http://evropa2.cz/mp3-archiv/"));
        */
        new E2().execute();
    }

    public void playStopStream(View v){
        if(!playingStream) {
            String url = ((TextView) findViewById(R.id.mp3Url)).getText().toString();
            ap.play(this, Uri.parse(url), false, AudioManager.STREAM_MUSIC);
            ((Button)findViewById(R.id.playBt)).setText(R.string.stop);
            Toast.makeText(this, "Přehrávám...", Toast.LENGTH_LONG).show();
            playingStream = true;
        } else {
            ap.stop();
            ((Button)findViewById(R.id.playBt)).setText(R.string.play);
            Toast.makeText(this, "Stop", Toast.LENGTH_LONG).show();
            playingStream = false;
        }
    }
}
