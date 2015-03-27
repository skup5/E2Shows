package com.example.roman.testapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    private ProgressDialog mProgressDialog;
    private AsyncPlayer ap;
    private MediaPlayer mediaPlayer;
    private Button playBt;
    private ListView navList;
    private TextView navHeader;

    private ArrayAdapter<String> navListAdapter;
    //private String url;
    final String[] categoryItems = new String[] {
            "Hudební ceny Evropy 2 (2014)", "To nejlepší z Ranní show",
            "Zpátky do minulosti", "Bombucman", "Odpolední odhalení - Ekl Zástěra",
            "To nejlepší z Odpolední show", "To nejlepší z Víkendové ranní show",
            "Emoce na Evropě 2", "Dance Exxtravaganza", "Novinky na DVD",
            "Internet", "Hosté Evropy 2", "Songy Evropy 2"};

    /**
     * remain false till media is not completed, inside OnCompletionListener make it true.
     */
    private boolean initialStage = true;
    private boolean playing, playingStream;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_main);
        setContentView(R.layout.main_layout);
        initNavigation();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Toolbar :it is a generalization of action bars for use within
        // application layouts.
        //Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // DrawerLayout : it acts as a top-level container for window content
        // that allows for interactive "drawer" views to be
        // pulled out from the edge of the window.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle : This class provides a handy way to tie
        // together the functionality of DrawerLayout and
        // the framework ActionBar to implement the recommended design for
        // navigation drawers.
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        //Set the ActionBarDrawerToggle in the layout
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //Hide the default Actionbar
        //getSupportActionBar().hide();
        // Call syncState() from your Activity's onPostCreate to synchronize the
        // indicator
        // with the state of the linked DrawerLayout after
        // onRestoreInstanceState has occurred
        mDrawerToggle.syncState();

        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                //navHeader.setText(categoryItems[position].toString());
                actionBar.setTitle(categoryItems[position].toString());
                mDrawerLayout.closeDrawers();
            }
        });

        ap = new AsyncPlayer("MyTest");
        mediaPlayer = null;
        playingStream = false;
        playing = false;
        playBt = (Button) findViewById(R.id.playBt);
       // Log.d("onCreate", "\n***************\n* VYTVORIL JSEM APPKU\n***************");
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
        //Toast.makeText(this, "Click on " + item.getItemId(), Toast.LENGTH_LONG).show();

        switch(item.getItemId()) {
            case R.id.action_settings :
                return true;
            case android.R.id.home :
                //Toast.makeText(this, "Home click", Toast.LENGTH_LONG).show();
                View navigation = findViewById(R.id.left_drawer);
                if(mDrawerLayout.isDrawerOpen(navigation)){
                    mDrawerLayout.closeDrawer(navigation);
                } else {
                    mDrawerLayout.openDrawer(navigation);
                }
                return true;

            default : return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
       /* if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }*/
    }
    
    public void refresh(View v) {
        /*TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getData1("http://evropa2.cz/mp3-archiv/"));
        */
        new E2().execute();
    }

    public void playStopStream(View v) {
        if (!playingStream) {
            String url = ((TextView) findViewById(R.id.mp3Url)).getText().toString();
            ap.play(this, Uri.parse(url), false, AudioManager.STREAM_MUSIC);
            playBt.setText(R.string.stop);
            Toast.makeText(this, "Přehrávám...", Toast.LENGTH_LONG).show();
            playingStream = true;
        } else {
            ap.stop();
            playBt.setText(R.string.play);
            Toast.makeText(this, "Stop", Toast.LENGTH_LONG).show();
            playingStream = false;
        }
    }

    public void playPauseMedia(View v) {
        if (!playing) {
            //btn.setBackgroundResource(R.drawable.button_pause);
            playBt.setText(R.string.pause);
            if (initialStage) {
                String url = ((TextView) findViewById(R.id.mp3Url)).getText().toString();
                if(url == null || url.isEmpty()){
                    Log.d("playPauseMedia", "empty url");
                    playing = false;
                    initialStage = true;
                    playBt.setText(R.string.play);
                    return;
                }
                new Player().execute(url);
            } else {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
            playing = true;
        } else {
            //btn.setBackgroundResource(R.drawable.button_play);
            playBt.setText(R.string.play);
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            playing = false;
        }
    }

    public void stopMedia(View v){
        if(mediaPlayer == null){
            return;
        }
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        playing = false;
        initialStage = true;
        playBt.setText(R.string.play);
    }
    
    private void initNavigation() {
        // TODO Auto-generated method stub
        //navHeader = (TextView) findViewById(R.id.navHeader);
        // Find the ListView resource.
        navList = (ListView) findViewById(R.id.left_drawer);

        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll(Arrays.asList(categoryItems));

        // Create ArrayAdapter using the planet navList.
        navListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, planetList);

        // Set the ArrayAdapter as the ListView's adapter.
        navList.setAdapter(navListAdapter);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else {
            return true;
        }
    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }

    private class E2 extends AsyncTask<Void, Void, Void> {
        String title;
        String url = "http://evropa2.cz/mp3-archiv/";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // mProgressDialog.setTitle("Mp3 archiv Evropy 2");
            mProgressDialog.setMessage("Načítání...");
            //  mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to the web site
                if (isInternetAvailable()) {
                    Document document = Jsoup.connect(url).get();
                    // Get the html document title
                    title = document.title();
                    url = "http://evropa2.cz";
                    url += "/file/edee/tym-a-porady/mp3-archiv/18058/20150225_odhaleni.mp3";

                } else {
                    title = "Chyba připojení";
                    url = "";
                }
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
            TextView mp3url = (TextView) findViewById(R.id.mp3Url);
            mp3url.setText(url);
            mProgressDialog.dismiss();
        }
    }

    /**
     * preparing mediaplayer will take sometime to buffer the content so prepare it inside the background thread and starting it on UI thread.
     *
     * @author piyush
     */
    private class Player extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            if(mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.progress.setMessage("Načítání...");
            this.progress.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            Boolean prepared;
            try {
                Log.d("class Player", "params[0]=" + params[0]);
                mediaPlayer.setDataSource(params[0]);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        initialStage = true;
                        playing = false;
                        playBt.setText(R.string.pause);
                        //btn.setBackgroundResource(R.drawable.);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.d("IllegarArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (progress.isShowing()) {
                progress.cancel();
            }
            Log.d("Prepared", "//" + result);
            mediaPlayer.start();
            initialStage = false;
        }

        public Player() {
            progress = new ProgressDialog(MainActivity.this);
        }

    }

}
