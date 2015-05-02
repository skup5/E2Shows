package com.example.roman.testapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.example.roman.testapp.jweb.Category;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;

public class MainActivity extends ActionBarActivity{

    private ProgressDialog mProgressDialog;
    private AsyncPlayer ap;
    private MediaPlayer mediaPlayer;
    private Button playBt;
    private ListView navList;
    private TextView navHeader;

    private ArrayAdapter navListAdapter;
    //private String url;
    final String[] categoryItems = new String[] {
            "Hudební ceny Evropy 2 (2014)", "To nejlepší z Ranní show",
            "Zpátky do minulosti", "Bombucman", "Odpolední odhalení - Ekl Zástěra",
            "To nejlepší z Odpolední show", "To nejlepší z Víkendové ranní show",
            "Emoce na Evropě 2", "Dance Exxtravaganza", "Novinky na DVD",
            "Internet", "Hosté Evropy 2", "Songy Evropy 2", "Fake"};

    /**
     * remain false till media is not completed, inside OnCompletionListener make it true.
     */
    private boolean initialStage = true, categoryIsDownloading = false;
    private boolean playing, playingStream;
    private DrawerLayout mDrawerLayout;
    
    private ListView recList;
    private RecordsAdapter recAdapter;
    private final String urlE2 = "http://evropa2.cz";
    private final String urlArchiv = "/mp3-archiv/";
    private ActionBar actionBar;
    private String choosenCategory;
    private int lastChoosenCategoryId = -1;
    private View loadingBar;
    private int mAnimationDuration;
    private Category defaultCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
       // setContentView(R.layout.activity_main);
        setContentView(R.layout.main_layout);

        init();

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
//            case R.id.action_settings :
//                return true;
            case android.R.id.home :
                //Toast.makeText(this, "Home click", Toast.LENGTH_LONG).show();
                //View navigation = findViewById(R.id.left_drawer);
                if(mDrawerLayout.isDrawerOpen(navList)){
                    mDrawerLayout.closeDrawer(navList);
                } else {
                    mDrawerLayout.openDrawer(navList);
                }
                return true;
            case R.id.action_refresh_category :
                downloadCategory();
                return true;
            default : return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
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

    public void stopMedia(View v) {
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

    public void hideLoading() { loadingBar.setVisibility(View.GONE); }

    public void showLoading(){
        loadingBar.setVisibility(View.VISIBLE);
        loadingBar.setAlpha(1f);
    }

    private void crossfade() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        recList.setAlpha(0f);
        recList.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        recList.animate()
                .alpha(1f)
                .setDuration(mAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        loadingBar.animate()
                .alpha(0f)
                .setDuration(mAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingBar.setVisibility(View.GONE);
                    }
                });
    }

    private void init() {
       // Toast.makeText(this, "Initializace...", Toast.LENGTH_SHORT).show();

        downloadCategory();
        loadingBar = findViewById(R.id.loadingPanel);
        loadingBar.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        mAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        initNavigation();
        initRecList();

        actionBar = getSupportActionBar();
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
                R.string.navigation_drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(R.string.categoryTitle);
                super.onDrawerOpened(drawerView);
                //Animation animation = AnimationUtils.makeInAnimation(MainActivity.this, false);


                //invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                actionBar.setTitle(R.string.app_name);
                actionBar.setSubtitle(choosenCategory);
                super.onDrawerClosed(drawerView);

                //invalidateOptionsMenu();
            }
        };

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

        //ap = new AsyncPlayer("MyTest");
        ap = null;
        mediaPlayer = null;
        playingStream = false;
        playing = false;
        //playBt = (Button) findViewById(R.id.playBt);
        playBt = new Button(this);

//        if (downloader != null) {
//            try {
//                categorySet = downloader.get();
//                fillNavigation(categorySet);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private void initNavigation() {
        //navHeader = (TextView) findViewById(R.id.navHeader);
        // Find the ListView resource.
        navList = (ListView) findViewById(R.id.left_drawer);

//        if (categorySet == null) {
//            Toast.makeText(this, "Chyba při stahování kategorií", Toast.LENGTH_LONG).show();
//            return;
//        }

        Log.d("onCreate", "Kategorie byly stazeny, pridavam do navListu");
        //navListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, categorySet.toArray());
        navListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        // Set the ArrayAdapter as the ListView's adapter.
        navList.setAdapter(navListAdapter);
        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //navHeader.setText(categoryItems[position].toString());
                // actionBar.setTitle(categoryItems[position].toString());
                //choosenCategory = categoryItems[position].toString();
                Category item = (Category) parent.getAdapter().getItem(position);
                if (lastChoosenCategoryId == position) {
                    mDrawerLayout.closeDrawer(navList);
                    return;
                }
                lastChoosenCategoryId = position;
                choosenCategory = item.toString();
                mDrawerLayout.closeDrawer(navList);
                //recList.setVisibility(View.GONE);

                //Toast.makeText(MainActivity.this, ""+parent.getAdapter().getItem(position), Toast.LENGTH_LONG).show();
                // Set<Record> recordsSet = item.getRecords();
                if (item.getRecords().isEmpty()) {
                    showLoading();
                    //Toast.makeText(MainActivity.this, "recordsSet is empty", Toast.LENGTH_LONG).show();
                    DownloaderFactory.RecordsDownloader downloader = (DownloaderFactory.RecordsDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Records);
                    downloader.setOnCompleteListener(new ADownloader.OnCompleteListener() {
                        @Override
                        public void onComplete(Object result) {
                            if (result instanceof Category) {
                                fillRecList((Category) result);
                                crossfade();
                            }
                        }
                    });
                    downloader.execute(item);
                    /*try {
                        recordsSet = downloader.get();
                        item.addRecords(recordsSet);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }*/
                    //crossfade();
                } else {
                    fillRecList(item);
                }
                /*hideLoading();
                recList.setVisibility(View.VISIBLE);*/

            }
        });
    }

    private void initRecList() {
        recList = (ListView) findViewById(R.id.listView_records);
        recAdapter = new RecordsAdapter(this);
        recList.setAdapter(recAdapter);
        recList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "" + parent.getAdapter().getItem(position), Toast.LENGTH_SHORT).show();
            }
        });
        recList.setOnScrollListener(new EndlessScrollListener(new EndlessScrollListener.LoadNextItems() {
            @Override
            public void loadNextItems() {
                recAdapter.downloadNext();
            }
        }));
        recList.setVisibility(View.GONE);
    }

    private void fillNavigation(Set<Category> categorySet) {
        if (categorySet == null) {
            Toast.makeText(this, "Chyba při stahování kategorií", Toast.LENGTH_LONG).show();
            return;
        }
        navListAdapter.addAll(categorySet);
    }

    private void fillRecList(Category category){
        if (category == null) {
            Toast.makeText(this, "Chyba při stahování záznamů", Toast.LENGTH_LONG).show();
            return;
        }
        recAdapter.setSource(category);
    }

    private void downloadCategory(){
        //Set<Category> categorySet = null;
        //Downloader downloader = null;
        DownloaderFactory.CategoriesDownloader downloader = null;
        if (!isNetworkConnected()) {
            Toast.makeText(this, "Nejsi připojen k síti", Toast.LENGTH_LONG).show();
        }
        else if(!categoryIsDownloading){
            Toast.makeText(this, "Stahuji kategorie", Toast.LENGTH_SHORT).show();
//            downloader = new Downloader(this, Downloader.Type.Category, null);
            downloader = (DownloaderFactory.CategoriesDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Categories);
            downloader.setOnCompleteListener(new ADownloader.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    if (result instanceof Set) {
                        fillNavigation((Set<Category>) result);
                        categoryIsDownloading = false;
                    }
                }
            });
            downloader.execute(urlE2 + urlArchiv);
            categoryIsDownloading = true;
        }

    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    public boolean isInternetAvailable() {
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
