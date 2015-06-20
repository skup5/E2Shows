package com.example.roman.testapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.roman.testapp.jweb.Category;
import com.example.roman.testapp.jweb.Record;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    public static final String URL_E2 = "http://evropa2.cz",
                                SUB_URL_ARCHIV = "/mp3-archiv/",
                                DOWNLOADING_CATEGORIES = "Stahuji kategorie...",
                                CATEGORIES_ARE_READY = "Kategorie jsou připraveny",
                                ERROR_NO_CONNECTION = "Nejsi připojen k síti",
                                ERROR_DOWNLOADING = "Chyba při stahování",
                                ERROR_DOWNLOADING_CATEGORIES = "Chyba při stahování kategorií",
                                ERROR_DOWNLOADING_RECORDS = "Chyba při stahování záznamů";

    private MediaPlayer mediaPlayer;
    private AudioController audioController;
    private AudioController.AudioPlayerControl audioPlayerControl;
    private ListView navList;
    private ArrayAdapter navListAdapter;

    private RecyclerView recyclerView;
    private MyRecyclerAdapter recyclerAdapter;

    private ExpandableListView categoriesList;
    private CategoriesAdapter categoriesAdapter;

    /**
     * remain false till media is not completed, inside OnCompletionListener make it true.
     */

    private boolean actualCategoriesIsDownloading = false;
    private boolean archivedCategoriesIsDownloading = false;
    private DrawerLayout mDrawerLayout;
    private ListView recList;
    private RecordsAdapter recAdapter;
    private ActionBar actionBar;
    private String chosenCategory;
    private int lastChosenCategoryId = -1;
    private View loadingBar;
    private int mAnimationDuration;
    private Category defaultCategory;
    private View refreshCategoryButton;
    private Animation refreshCategoryAnim;
    private int selectedRecordIndex = -1;

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
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                refreshCategoryButton = findViewById(R.id.action_refresh_category);
                refreshCategoryAnim = createRotateAnim(refreshCategoryButton, 360, 1000, true);
                runCategoryRefreshAnim();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //Toast.makeText(this, "Click on " + item.getItemId(), Toast.LENGTH_LONG).show();

        switch(item.getItemId()){
//            case R.id.action_settings :
//                return true;

            case android.R.id.home :
                //Toast.makeText(this, "Home click", Toast.LENGTH_LONG).show();
                //View navigation = findViewById(R.id.left_drawer);
//                if(mDrawerLayout.isDrawerOpen(navList)){
//                    mDrawerLayout.closeDrawer(navList);
//                } else {
//                    mDrawerLayout.openDrawer(navList);
//                }
                if(mDrawerLayout.isDrawerOpen(categoriesList)){
                    mDrawerLayout.closeDrawer(categoriesList);
                } else {
                    mDrawerLayout.openDrawer(categoriesList);
                }
                return true;

            case R.id.action_refresh_category :
                downloadCategories();
                return true;

            default : return super.onOptionsItemSelected(item);
        }
    }

    public void prepareMediaPlayerSource(String url) {
        if (mediaPlayer == null) {
            initMediaPlayer();
        }
        if (!isNetworkConnected()) {
            toast(ERROR_NO_CONNECTION, Toast.LENGTH_LONG);
            return;
        }
        if (!isInternetAvailable()) {
            //Toast.makeText(this, "Nejsi připojen k internetu", Toast.LENGTH_LONG).show();
            //return;
        }
        PrepareStream ps = new PrepareStream(this, mediaPlayer);
        ps.setOnErrorListener(new PrepareStream.OnErrorListener() {
            @Override
            public void onError() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Načítání")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Při načítání došlo k chybě :-(")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                audioPlayerControl.next();
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        ps.execute(url);
    }

    /**
     * Zjistí, jestli je telefon připojen k síti.
     * @return true pokud je připojen
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    public void hideLoading() { loadingBar.setVisibility(View.GONE); }

    public void showLoading(){
        loadingBar.setVisibility(View.VISIBLE);
        loadingBar.setAlpha(1f);
    }

    public void toast(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }


    private void init() {
       // Toast.makeText(this, "Initializace...", Toast.LENGTH_SHORT).show();

        downloadCategories();
        loadingBar = findViewById(R.id.loadingPanel);
        loadingBar.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        mAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        //initNavigation();
        initCategoriesList();
        //initRecList();
        initRecyclerView();

        initActionBar();

        initMediaPlayer();
        initAudioController();

    }

    private void initActionBar() {
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
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // ActionBarDrawerToggle : This class provides a handy way to tie
        // together the functionality of DrawerLayout and
        // the framework ActionBar to implement the recommended design for
        // navigation drawers.
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close){

            @Override
            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(R.string.category_title);
                super.onDrawerOpened(drawerView);
                //Animation animation = AnimationUtils.makeInAnimation(MainActivity.this, false);

                //invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                actionBar.setTitle(R.string.app_name);
                actionBar.setSubtitle(chosenCategory);
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
    }

    private void initCategoriesList() {
        categoriesList = (ExpandableListView) findViewById(R.id.left_drawer);
        categoriesList.setGroupIndicator(null);
        initCategoriesAdapter();
        categoriesList.setAdapter(categoriesAdapter);
        categoriesList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (categoriesAdapter.isActualCategories(groupPosition)) {
                    Category item = (Category) categoriesAdapter.getGroup(groupPosition);
                    onNavigationItemClick(item, groupPosition);
                    return true;
                }
                return false;
            }
        });
        categoriesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Category item = (Category) categoriesAdapter.getChild(groupPosition, childPosition);
                onNavigationItemClick(item, groupPosition + childPosition);
                return true;
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerAdapter = new MyRecyclerAdapter(this, new MyRecyclerAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(Record record, int index) {
                onRecordItemClick(record, index);
            }
        });
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new MyRecyclerScrollListener(
                new EndlessScrollListener.LoadNextItems() {
                    @Override
                    public void loadNextItems() {
                        recyclerAdapter.downloadNext();
                    }
                }, 3
        ));
//        recyclerView.addItemDecoration(new SpacesItemDecoration(5));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);
        //recyclerView.setItemAnimator(some animator);
        recyclerView.setVisibility(View.GONE);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            /**
             * Called when the media file is ready for playback.
             *
             * @param mp the MediaPlayer that is ready for playback
             */
            @Override
            public void onPrepared(MediaPlayer mp) {
                //        mediaController.setMediaPlayer(this);
//        mediaController.setAnchorView(findViewById(R.id.audio_controller));
//        //mediaController.setAnchorView(findViewById(R.id.listview_records));
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                mediaController.setEnabled(true);
//                mediaController.show(0);
//            }
//        });

                audioController.setEnabled(true);
                audioController.setUpSeekBar();
                /* play mp3 */
                audioController.clickOnPlay();
            }

        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioController.onCompletion();
            }
        });
    }

    private void initAudioController() {
//        LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View infView = li.inflate(R.layout.audio_controller, new LinearLayout(getApplicationContext()));
        View controllerView = findViewById(R.id.audio_controller);
        audioPlayerControl = new AudioController.AudioPlayerControl() {

            @Override
            public void start() {
                mediaPlayer.start();
            }

            @Override
            public void stop() {
                if (mediaPlayer != null) {
                    if (isPlaying()) {
                        mediaPlayer.pause();
                    }
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
            }

            @Override
            public void pause() {
                mediaPlayer.pause();
            }

            @Override
            public int getDuration() {
                // if(mediaPlayer != null){
                return mediaPlayer.getDuration() / 1000;
                //  }
                //  return 0;
            }

            @Override
            public int getCurrentPosition() {
                // if(mediaPlayer != null){
                return mediaPlayer.getCurrentPosition() / 1000;
                //}
                //return 0;
            }

            @Override
            public void seekTo(int pos) {
                // if(mediaPlayer != null){
                mediaPlayer.seekTo(pos * 1000);
                //}
            }

            @Override
            public boolean isPlaying() {
                if (mediaPlayer != null) {
                    return mediaPlayer.isPlaying();
                }
                return false;
            }

            @Override
            public void next() {
                Record nextRecord = recyclerAdapter.getItem(selectedRecordIndex + 1);
                if (nextRecord != null) {
                    onRecordItemClick(nextRecord, selectedRecordIndex + 1);
                }
            }

            @Override
            public void previous() {
                if (getCurrentPosition() > 3) {
                    seekTo(0);
                } else {
                    Record previousRecord = recyclerAdapter.getItem(selectedRecordIndex - 1);
                    if (previousRecord == null) {
                        seekTo(0);
                    } else {
                        onRecordItemClick(previousRecord, selectedRecordIndex - 1);
                    }
                }
            }

            @Override
            public boolean canPause() {
                return true;
            }

        };
        audioController = new AudioController(getApplicationContext(), controllerView, audioPlayerControl);
    }

    private void initCategoriesAdapter() {
        categoriesAdapter = new CategoriesAdapter(this);
        categoriesAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                invalidateOptionsMenu();
                Log.println(Log.ASSERT, "MainActivity", "refresh action bar");
            }
        });
    }

    private void initNavigation() {
//        //navHeader = (TextView) findViewById(R.id.navHeader);
//        // Find the ListView resource.
//        navList = (ListView) findViewById(R.id.left_drawer);
//
//        //        if (categorySet == null) {
//        //            Toast.makeText(this, "Chyba při stahování kategorií", Toast.LENGTH_LONG).show();
//        //            return;
//        //        }
//
//        Log.d("onCreate", "Kategorie byly stazeny, pridavam do navListu");
//        //navListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, categorySet.toArray());
//        navListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
//        // Set the ArrayAdapter as the ListView's adapter.
//        navList.setAdapter(navListAdapter);
//        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                Category item = (Category) parent.getAdapter().getItem(position);
//                onNavigationItemClick(item);
//            }
//        });
    }

    private void initRecList() {
//        recList = (ListView) findViewById(R.id.listView_records);
//        recAdapter = new RecordsAdapter(this);
//        recList.setAdapter(recAdapter);
//        recList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Record item = (Record) parent.getAdapter().getItem(position);
//                Toast.makeText(MainActivity.this, "" + item, Toast.LENGTH_SHORT).show();
//                audioPlayerControl.stop();
//                prepareMediaPlayerSource(item.getMp3().toString());
//            }
//        });
//        recList.setOnScrollListener(new EndlessScrollListener(new EndlessScrollListener.LoadNextItems() {
//            @Override
//            public void loadNextItems() {
//                recAdapter.downloadNext();
//            }
//        }));
//        recList.setVisibility(View.GONE);
    }

    @Deprecated
    private void fillNavigation(Set<Category> actualCategories, Set<Category> archiveCategories) {
        if (actualCategories == null) {
           toast(ERROR_DOWNLOADING_CATEGORIES, Toast.LENGTH_LONG);
            return;
        }
        //navListAdapter.addAll(categorySet);
        categoriesAdapter = new CategoriesAdapter(this);
        categoriesAdapter.setActualCategories(actualCategories.toArray(new Category[actualCategories.size()]));
//        Category[] archived = new Category[5];
//        for (int i = 0; i < archived.length; i++) {
//            archived[i] = new Category(Integer.MAX_VALUE - i - 1, "Historie"+i+1, Category.NO_URL, i+1, Category.NO_URL, Category.NO_URL); }
        if (archiveCategories != null) {
            categoriesAdapter.setArchivedCategories(archiveCategories.toArray(new Category[archiveCategories.size()]));
        }
        categoriesList.setGroupIndicator(null);
        categoriesList.setAdapter(categoriesAdapter);
    }

    private void fillRecList(Category category) {
        if (category == null) {
//            toast(ERROR_DOWNLOADING_RECORDS, Toast.LENGTH_LONG);
            return;
        }
        //recAdapter.setSource(category);
        recyclerAdapter.setSource(category);
    }

    private void finishDownloadCategoriesToast() {
        if (!actualCategoriesIsDownloading && !archivedCategoriesIsDownloading) {
            toast(CATEGORIES_ARE_READY, Toast.LENGTH_SHORT);
        }
    }

    private void downloadActualCategories() {
        //Set<Category> categorySet = null;
        //Downloader downloader = null;

        if (!isNetworkConnected()) {
            toast(ERROR_NO_CONNECTION, Toast.LENGTH_LONG);
        }
        else if(!actualCategoriesIsDownloading){
            startDownloadCategoriesToast();
            runCategoryRefreshAnim();
//            downloader = new Downloader(this, Downloader.Type.Category, null);
            DownloaderFactory.CategoriesDownloader downloader = (DownloaderFactory.CategoriesDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Categories);
            downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    if (result instanceof Set) {
                        //fillNavigation((Set<Category>) result);
                        setNavActualCategories((Set<Category>) result);
                        actualCategoriesIsDownloading = false;
                        finishDownloadCategoriesToast();
                        stopCategoryRefreshAnim();
                    }
                }
            });
            downloader.execute(URL_E2 + SUB_URL_ARCHIV);
            actualCategoriesIsDownloading = true;
        }
    }

    private void downloadArchivedCategories() {
        //Set<Category> categorySet = null;
        //Downloader downloader = null;

        if (!isNetworkConnected()) {
            toast(ERROR_NO_CONNECTION, Toast.LENGTH_LONG);
        }
        else if(!archivedCategoriesIsDownloading){
            startDownloadCategoriesToast();
            runCategoryRefreshAnim();
//            downloader = new Downloader(this, Downloader.Type.Category, null);
            DownloaderFactory.ArchivedCategoriesDownloader downloader = (DownloaderFactory.ArchivedCategoriesDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.ArchivedCategories);
            downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    if (result instanceof Set) {
                        //fillNavigation((Set<Category>) result);
                        setNavArchivedCategories((Set<Category>) result);
                        archivedCategoriesIsDownloading = false;
                        finishDownloadCategoriesToast();
                        stopCategoryRefreshAnim();
                    }
                }
            });
            downloader.execute(URL_E2 + SUB_URL_ARCHIV);
            archivedCategoriesIsDownloading = true;
        }
    }

    private void downloadCategories() {
        downloadActualCategories();
        downloadArchivedCategories();
    }

    private void downloadCategoryCoverImage(Category category) {
        DownloaderFactory.CoverImageDownloader downloader = (DownloaderFactory.CoverImageDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.CoverImage);
        downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
            @Override
            public void onComplete(Object result) {
                if (result instanceof Category) {
                    if (((Category) result).hasCover()) {
                        audioController.setCoverImage(((Category) result).getCover());
                    }
                }
            }
        });
        downloader.execute(category);
    }

    private void onNavigationItemClick(Category item, int position) {
        int id = position;
       // Toast.makeText(this, id+":"+item.getName(), Toast.LENGTH_SHORT).show();
        if (lastChosenCategoryId == id) {
            mDrawerLayout.closeDrawer(categoriesList);
            return;
        }
        lastChosenCategoryId = id;
        chosenCategory = item.toString();
        mDrawerLayout.closeDrawer(categoriesList);
        if (item.getRecords().isEmpty()) {
            showLoading();
            //Toast.makeText(MainActivity.this, "recordsSet is empty", Toast.LENGTH_LONG).show();
            DownloaderFactory.RecordsDownloader downloader = (DownloaderFactory.RecordsDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Records);
            downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    if (result instanceof Category) {
                        fillRecList((Category) result);
                        crossfadeAnimation();
                    }
                }
            });
            downloader.execute(item);
        } else {
            fillRecList(item);
        }
    }

    private void onRecordItemClick(Record record, int index) {
        toast("" + record, Toast.LENGTH_SHORT);
        if (selectedRecordIndex == index) {
            return;
        }
        audioPlayerControl.stop();
        prepareMediaPlayerSource(record.getMp3().toString());
        if (record.getCategory().hasCover()) {
            audioController.setCoverImage(record.getCategory().getCover());
        } else {
            audioController.resetCoverImage();
            downloadCategoryCoverImage(record.getCategory());
        }
        audioController.setInfoLineText(record.getName());
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(index);
        if (viewHolder != null) {
            recyclerAdapter.markViewHolder((MyRecyclerAdapter.MyViewHolder) viewHolder);
        }
        viewHolder = recyclerView.findViewHolderForAdapterPosition(selectedRecordIndex);
        if (viewHolder != null) {
            recyclerAdapter.unmarkViewHolder((MyRecyclerAdapter.MyViewHolder) viewHolder);
        }
        selectedRecordIndex = index;
        recyclerAdapter.setSelected(index);
    }

    /**
     * Zjistí, jestli je telefon připojen k internetu.
     * @return true pokud je připojen
     */
    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }

    }

    private void crossfadeAnimation() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
//        recList.setAlpha(0f);
//        recList.setVisibility(View.VISIBLE);
        recyclerView.setAlpha(0f);
        recyclerView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
//        recList.animate()
//                .alpha(1f)
//                .setDuration(mAnimationDuration)
//                .setListener(null);
        recyclerView.animate()
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

    private void runCategoryRefreshAnim() {
        if (refreshCategoryButton != null && refreshCategoryAnim != null) {
            if (actualCategoriesIsDownloading || archivedCategoriesIsDownloading) {
                refreshCategoryButton.startAnimation(refreshCategoryAnim);
            }
        }
    }

    private void startDownloadCategoriesToast() {
        if (!actualCategoriesIsDownloading && !archivedCategoriesIsDownloading) {
            toast(DOWNLOADING_CATEGORIES, Toast.LENGTH_SHORT);
        }
    }

    private void stopCategoryRefreshAnim() {
        if (refreshCategoryButton != null) {
            if (!actualCategoriesIsDownloading && !archivedCategoriesIsDownloading) {
                refreshCategoryButton.clearAnimation();
            }
        }
    }

    private void setNavActualCategories(Set<Category> actualCategories) {
        if (categoriesAdapter == null) {
            initCategoriesAdapter();
            categoriesList.setAdapter(categoriesAdapter);
        }
        categoriesAdapter.setActualCategories(actualCategories.toArray(new Category[actualCategories.size()]));
        categoriesAdapter.notifyDataSetChanged();
    }

    private void setNavArchivedCategories(Set<Category> archivedCategories) {
        if (categoriesAdapter == null) {
            initCategoriesAdapter();
            categoriesList.setAdapter(categoriesAdapter);
        }
        categoriesAdapter.setArchivedCategories(archivedCategories.toArray(new Category[archivedCategories.size()]));
        categoriesAdapter.notifyDataSetChanged();
    }

    public static Animation createRotateAnim(View animatedView, int toDegrees, int duration, boolean infinite) {
        Animation anim = new RotateAnimation(0, toDegrees,
                animatedView.getPivotX() + animatedView.getWidth() / 2,
                animatedView.getPivotY() + animatedView.getHeight() / 2);
        anim.setDuration(duration);
        if(infinite) {
            anim.setRepeatMode(Animation.INFINITE);
        }
        anim.setInterpolator(new LinearInterpolator());
        return anim;
    }

}
