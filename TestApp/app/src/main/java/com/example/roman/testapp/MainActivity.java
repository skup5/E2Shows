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
import android.widget.Toast;

import com.example.roman.testapp.jweb.Category;
import com.example.roman.testapp.jweb.Record;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Roman Zelenik
 */
public class MainActivity extends AppCompatActivity {

    public static final String
            CATEGORIES_ARE_READY = "Kategorie jsou připraveny",
            DOWNLOADING_CATEGORIES = "Stahuji kategorie...",
            ERROR_DOWNLOADING = "Chyba při stahování",
            ERROR_DOWNLOADING_CATEGORIES = "Chyba při stahování kategorií",
            ERROR_DOWNLOADING_RECORDS = "Chyba při stahování záznamů",
            ERROR_NO_CONNECTION = "Nejsi připojen k síti",
            STILL_DOWNLOADING = "Stahování probíhá...",
            SUB_URL_ARCHIV = "/mp3-archiv/",
            URL_E2 = "http://evropa2.cz";

    private MediaPlayer mediaPlayer;
    private AudioController audioController;
    private AudioController.AudioPlayerControl audioPlayerControl;

    private RecyclerView recordsList;
    private RecordsAdapter recordsAdapter;

    private ExpandableListView categoriesList;
    private CategoriesAdapter categoriesAdapter;

    private boolean actualCategoriesAreDownloading = false;
    private boolean archivedCategoriesAreDownloading = false;
    private DrawerLayout mDrawerLayout;
    private ActionBar actionBar;
    private Category chosenCategory;
    private int chosenCategoryPosition = -1;
    private View loadingBar;
    private int crossfadeAnimDuration;
    private View refreshCategoryButton;
    private Animation refreshCategoryAnim;
    private int selectedRecordIndex = -1;
    private Map<String, Integer> selectedRecords;

    /*#######################################################
      ###               STATIC METHODS                    ###
      #######################################################*/

    public static Animation createRotateAnim(View animatedView, int toDegrees, int duration, boolean infinite) {
        Animation anim = new RotateAnimation(0, toDegrees,
                animatedView.getWidth() / 2, animatedView.getHeight() / 2);
        anim.setDuration(duration);
        if (infinite) {
            anim.setRepeatMode(Animation.INFINITE);
        }
        anim.setInterpolator(new LinearInterpolator());
        return anim;
    }

    public static void errorReportsDialog(Context context, List<String> reports) {
        String msg = "Došlo k ";
        msg += reports.size() > 1 ? "několika chybám." : "chybě.";
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {
            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        adapter.addAll(reports);
        new AlertDialog.Builder(context)
                .setTitle(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setAdapter(adapter, null)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /*#######################################################
      ###               OVERRIDE METHODS                  ###
      #######################################################*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main_layout);
        init();
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

        switch (item.getItemId()) {
//            case R.id.action_settings :
//                return true;

            case android.R.id.home:
                if (categoriesAdapter != null && categoriesAdapter.isEmpty()) {
                    toast("Seznam je prázdný", Toast.LENGTH_SHORT);
                    return true;
                }
                if (mDrawerLayout.isDrawerOpen(categoriesList)) {
                    mDrawerLayout.closeDrawer(categoriesList);
                } else {
                    mDrawerLayout.openDrawer(categoriesList);
                }
                return true;

            case R.id.action_refresh_category:
                downloadCategories();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*#######################################################
      ###               PUBLIC METHODS                    ###
      #######################################################*/

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
     *
     * @return true pokud je připojen
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    public void hideLoading() {
        loadingBar.setVisibility(View.GONE);
    }

    public void showLoading() {
        loadingBar.setVisibility(View.VISIBLE);
        loadingBar.setAlpha(1f);
        if (recordsList != null && recordsList.getVisibility() != View.GONE) {
            recordsList.setVisibility(View.INVISIBLE);
        }
    }

    public void toast(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    /*#######################################################
      ###              PRIVATE METHODS                    ###
      #######################################################*/

    private void crossfadeAnimation() {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        recordsList.setAlpha(0f);
        recordsList.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        recordsList.animate()
                .alpha(1f)
                .setDuration(crossfadeAnimDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        loadingBar.animate()
                .alpha(0f)
                .setDuration(crossfadeAnimDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingBar.setVisibility(View.GONE);
                    }
                });
    }

    private void downloadActualCategories() {
        if (!isNetworkConnected()) {
            toast(ERROR_NO_CONNECTION, Toast.LENGTH_LONG);
        } else if (!actualCategoriesAreDownloading) {
            startDownloadCategoriesToast();
            runCategoryRefreshAnim();
            DownloaderFactory.CategoriesDownloader downloader = (DownloaderFactory.CategoriesDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Categories);
            downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    actualCategoriesAreDownloading = false;
                    finishDownloadCategoriesToast();
                    if (result instanceof Set) {
                        setNavActualCategories((Set<Category>) result);
                    }
                    stopCategoryRefreshAnim();
                    if (!archivedCategoriesAreDownloading) {
                        unlockNavigationDrawer();
                    }
                }
            });
            downloader.setOnErrorListener(new DownloaderFactory.OnErrorListener() {
                @Override
                public void onError(List<String> errors) {
                    errorReportsDialog(errors);
                }
            });
            downloader.execute(URL_E2 + SUB_URL_ARCHIV);
            actualCategoriesAreDownloading = true;
        } else {
            toast(STILL_DOWNLOADING, Toast.LENGTH_SHORT);
        }
    }

    private void downloadArchivedCategories() {
        //Set<Category> categorySet = null;
        //Downloader downloader = null;

        if (!isNetworkConnected()) {
            toast(ERROR_NO_CONNECTION, Toast.LENGTH_LONG);
        } else if (!archivedCategoriesAreDownloading) {
            startDownloadCategoriesToast();
            runCategoryRefreshAnim();
//            downloader = new Downloader(this, Downloader.Type.Category, null);
            DownloaderFactory.ArchivedCategoriesDownloader downloader = (DownloaderFactory.ArchivedCategoriesDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.ArchivedCategories);
            downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    archivedCategoriesAreDownloading = false;
                    finishDownloadCategoriesToast();
                    if (result instanceof Set) {
                        //fillNavigation((Set<Category>) result);
                        setNavArchivedCategories((Set<Category>) result);
                    }
                    stopCategoryRefreshAnim();
                    if (!actualCategoriesAreDownloading) {
                        unlockNavigationDrawer();
                    }
                }
            });
            downloader.setOnErrorListener(new DownloaderFactory.OnErrorListener() {
                @Override
                public void onError(List<String> errors) {
                    errorReportsDialog(errors);
                }
            });
            downloader.execute(URL_E2 + SUB_URL_ARCHIV);
            archivedCategoriesAreDownloading = true;
        } else {
            toast(STILL_DOWNLOADING, Toast.LENGTH_SHORT);
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
        downloader.setOnErrorListener(new DownloaderFactory.OnErrorListener() {
            @Override
            public void onError(List<String> errors) {
                errorReportsDialog(errors);
            }
        });
        downloader.execute(category);
    }

    private void errorReportsDialog(List<String> reports) {
        errorReportsDialog(this, reports);
    }

    private void fillRecList(Category category) {
        if (category != null) {
            recordsAdapter.setSource(category);
        }
    }

    private void finishDownloadCategoriesToast() {
        if (!actualCategoriesAreDownloading && !archivedCategoriesAreDownloading) {
            toast(CATEGORIES_ARE_READY, Toast.LENGTH_SHORT);
        }
    }

    private void init() {
        // Toast.makeText(this, "Initializace...", Toast.LENGTH_SHORT).show();

        downloadCategories();
        loadingBar = findViewById(R.id.loadingPanel);
        loadingBar.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        crossfadeAnimDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        initCategoriesList();
        initRecordsList();

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
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // ActionBarDrawerToggle : This class provides a handy way to tie
        // together the functionality of DrawerLayout and
        // the framework ActionBar to implement the recommended design for
        // navigation drawers.
        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(R.string.category_title);
                actionBar.setSubtitle("");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionBar.setTitle(R.string.app_name);
                refreshActionBarSubtitle();
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
                Record nextRecord = recordsAdapter.getItem(selectedRecordIndex + 1);
                if (nextRecord != null) {
                    onRecordItemClick(nextRecord, selectedRecordIndex + 1);
                }
            }

            @Override
            public void previous() {
                if (getCurrentPosition() > 3) {
                    seekTo(0);
                } else {
                    Record previousRecord = recordsAdapter.getItem(selectedRecordIndex - 1);
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
                if (!mDrawerLayout.isDrawerOpen(categoriesList)) {
                    refreshActionBarSubtitle();
                }

            }
        });
    }

    private void initCategoriesList() {
        categoriesList = (ExpandableListView) findViewById(R.id.left_drawer);
        categoriesList.setGroupIndicator(null);
        categoriesList.setSmoothScrollbarEnabled(true);
        initCategoriesAdapter();
        categoriesList.setAdapter(categoriesAdapter);
        categoriesList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (categoriesAdapter.isActualCategories(groupPosition)) {
                    Category item = (Category) categoriesAdapter.getGroup(groupPosition);
                    onNavigationItemClick(item, groupPosition, -1);
                    return true;
                }
                return false;
            }
        });
        categoriesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Category item = (Category) categoriesAdapter.getChild(groupPosition, childPosition);
                onNavigationItemClick(item, groupPosition, childPosition);
                return true;
            }
        });
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

    private void initRecordsList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recordsList = (RecyclerView) findViewById(R.id.recycler_view);
        recordsAdapter = new RecordsAdapter(this, new RecordsAdapter.OnRecordClickListener() {
            @Override
            public void onRecordClick(Record record, int index) {
                onRecordItemClick(record, index);
            }
        });
        recordsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                categoriesAdapter.notifyDataSetChanged();
            }
        });
        recordsList.setAdapter(recordsAdapter);
        recordsList.setLayoutManager(linearLayoutManager);
        recordsList.addOnScrollListener(new MyRecyclerScrollListener(
                new EndlessScrollListener.LoadNextItems() {
                    @Override
                    public void loadNextItems() {
                        recordsAdapter.downloadNext();
                    }
                }, 3
        ));
        recordsList.addItemDecoration(new SpacesItemDecoration(20));
//        recordsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recordsList.setHasFixedSize(true);
        //recordsList.setItemAnimator(some animator);
        recordsList.setVisibility(View.GONE);
    }

    /**
     * Zjistí, jestli je telefon připojen k internetu.
     *
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

    private void lockNavigationDrawer(int lockMode) {
        mDrawerLayout.setDrawerLockMode(lockMode);
    }

    private void onNavigationItemClick(Category item, int groupPosition, int childPosition) {
        // Toast.makeText(this, id+":"+item.getName(), Toast.LENGTH_SHORT).show();
        boolean isGroup = childPosition == -1;
        int position = isGroup ? groupPosition : groupPosition + childPosition;

        if (chosenCategoryPosition == position) {
            mDrawerLayout.closeDrawer(categoriesList);
            return;
        }

        if (isGroup) {
            categoriesAdapter.setGroupSelected(groupPosition);
        } else {
            categoriesAdapter.setChildSelected(groupPosition, childPosition);
        }
        chosenCategoryPosition = position;
        chosenCategory = item;
        mDrawerLayout.closeDrawer(categoriesList);
        showLoading();
        if (item.getRecords().isEmpty()) {
            //Toast.makeText(MainActivity.this, "recordsSet is empty", Toast.LENGTH_LONG).show();
            DownloaderFactory.RecordsDownloader downloader = (DownloaderFactory.RecordsDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Records);
            downloader.setOnCompleteListener(new DownloaderFactory.OnCompleteListener() {
                @Override
                public void onComplete(Object result) {
                    if (result instanceof Category) {
                        fillRecList((Category) result);
                    }
                    crossfadeAnimation();
                }
            });
            downloader.setOnErrorListener(new DownloaderFactory.OnErrorListener() {
                @Override
                public void onError(List<String> errors) {
                    errorReportsDialog(errors);
                }
            });
            downloader.execute(item);
        } else {
            fillRecList(item);
            crossfadeAnimation();
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
        RecyclerView.ViewHolder viewHolder = recordsList.findViewHolderForAdapterPosition(index);
        if (viewHolder != null) {
            recordsAdapter.markViewHolder((RecordsAdapter.MyViewHolder) viewHolder);
        }
        viewHolder = recordsList.findViewHolderForAdapterPosition(selectedRecordIndex);
        if (viewHolder != null) {
            recordsAdapter.unmarkViewHolder((RecordsAdapter.MyViewHolder) viewHolder);
        }
        selectedRecordIndex = index;
        recordsAdapter.setSelected(index);
    }

    private void refreshActionBarSubtitle() {
        if (chosenCategory != null) {
            actionBar.setSubtitle(chosenCategory.toString());
        }
    }

    private void runCategoryRefreshAnim() {
        if (refreshCategoryButton != null && refreshCategoryAnim != null) {
            if (actualCategoriesAreDownloading || archivedCategoriesAreDownloading) {
                refreshCategoryButton.startAnimation(refreshCategoryAnim);
            }
        }
    }

    private void startDownloadCategoriesToast() {
        if (!actualCategoriesAreDownloading && !archivedCategoriesAreDownloading) {
            toast(DOWNLOADING_CATEGORIES, Toast.LENGTH_SHORT);
        }
    }

    private void stopCategoryRefreshAnim() {
        if (refreshCategoryButton != null) {
            if (!actualCategoriesAreDownloading && !archivedCategoriesAreDownloading) {
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

    private void unlockNavigationDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}
