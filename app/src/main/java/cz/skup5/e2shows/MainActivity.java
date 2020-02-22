package cz.skup5.e2shows;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.skup5.e2shows.downloader.CoverImageDownloader;
import cz.skup5.e2shows.downloader.MediaUrlDownloader;
import cz.skup5.e2shows.downloader.RecordsDownloader;
import cz.skup5.e2shows.dto.ShowDto;
import cz.skup5.e2shows.manager.BasicShowManager;
import cz.skup5.e2shows.manager.ShowManager;
import cz.skup5.e2shows.record.RecordItem;
import cz.skup5.e2shows.record.RecordItemViewHolder;
import cz.skup5.e2shows.record.RecordType;
import cz.skup5.e2shows.record.RecordsAdapter;
import cz.skup5.e2shows.show.ShowsAdapter;
import cz.skup5.e2shows.utils.NetworkUtils;

//import cz.skup5.e2shows.manager.BasicPlaylistManager;
//import cz.skup5.e2shows.playlist.PlaylistManager;

/**
 * Main class of application and the only activity.
 *
 * @author Skup5
 */
public class MainActivity extends AppCompatActivity {

    public static final String TRY_NEXT_RECORD = "Zkus další";

    private static final int
        ITEM_OFFSET = 6,
        VISIBLE_TRESHOLD = 5;

    private static Context CONTEXT;
    private static final ShowManager showManager = BasicShowManager.getInstance();
//  private static final PlaylistManager playlistManager = BasicPlaylistManager.getInstance();

    private MediaPlayer mediaPlayer;
    private AudioController<RecordItem> audioController;
    private AudioController.AudioPlayerControl audioPlayerControl;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recordsList;
    //  private RecordsAdapter recordsAdapter;

    private ListView showsList;
    //private ShowsAdapter showsAdapter;

    private boolean
        recordsAreDownloading = false,
        showsAreDownloading = false;

    private Menu menu;
    private DrawerLayout mDrawerLayout;
    private ActionBar actionBar;
    private RecordItem chosenRecord;
    private ShowDto playShow;
    private int chosenShowPosition = -1;
    private View loadingBar;
    private int crossfadeAnimDuration;
    private View refreshShowButton;
    private Animation refreshShowAnim;
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

    public static Context getContext() {
        return CONTEXT;
    }

    /*#######################################################
      ###               OVERRIDE METHODS                  ###
      #######################################################*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONTEXT = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.main_layout);
//        setContentView(R.layout.testing_layout);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                refreshShowButton = findViewById(R.id.action_refresh_shows);
                refreshShowAnim = createRotateAnim(refreshShowButton, 360, 1000, true);
                runShowRefreshAnim();
            }
        });
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//            case R.id.action_settings :
//                return true;

            case android.R.id.home:
                ShowsAdapter showsAdapter = getShowsListAdapter();
                if (showsAdapter != null && showsAdapter.isEmpty()) {
                    toast(R.string.shows_empty_list, Toast.LENGTH_SHORT);
                    return true;
                }
                if (mDrawerLayout.isDrawerOpen(showsList)) {
                    mDrawerLayout.closeDrawer(showsList);
                } else {
                    mDrawerLayout.openDrawer(showsList);
                }
                return true;

            case R.id.action_refresh_shows:
                onRefreshShows();
                return true;

            case R.id.action_filter_all:
                onAllFilterClick();
                return true;

            case R.id.action_filter_audio:
                onAudioFilterClick();
                return true;

            case R.id.action_filter_video:
                onVideoFilterClick();
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
        if (!NetworkUtils.isNetworkConnected()) {
            noConnectionToast();
            return;
        }

        PrepareStream ps = new PrepareStream(this, mediaPlayer);
        ps.setOnErrorListener(new PrepareStream.OnErrorListener() {
            @Override
            public void onError() {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.loading)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.error_on_loading)
                    .setPositiveButton(TRY_NEXT_RECORD, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            audioPlayerControl.next();
                            dialog.dismiss();
                        }
                    }).setCancelable(true)
//                .setNegativeButton(STORNO, new DialogInterface.OnClickListener() {
//                  @Override
//                  public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                  }
//                })
                    .show();
            }
        });
        ps.execute(url);
    }

    /**
     * Checks network connection
     *
     * @return <code>true</code> if and only if device is connected,
     * <code>false</code> otherwise
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

    public void toast(int resourceId, int duration) {
        Toast.makeText(this, resourceId, duration).show();
    }

    public void toast(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
    }

    /*#######################################################
      ###              PRIVATE METHODS                    ###
      #######################################################*/

    private RecordsAdapter createRecordsAdapter() {
        RecordsAdapter recordsAdapter = new RecordsAdapter(this);
        recordsAdapter.setOnRecordClickListener(this::onRecordItemClick);
        recordsAdapter.setOnMenuClickListener((item, source) -> {
            switch (item.getItemId()) {
                case R.id.context_action_detail:
                    onRecordItemDetail(source.getActualRecord());
                    return true;
//      case R.id.context_action_play:
//        onRecordItemClick();
//        return true;
                default:
                    return false;
            }
        });
        recordsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                getShowsListAdapter().notifyDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                onChanged();
            }
        });

        return recordsAdapter;
    }

    private ShowsAdapter createShowsAdapter() {
        ShowsAdapter showsAdapter = new ShowsAdapter(this);
        showsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (!mDrawerLayout.isDrawerOpen(showsList)) {
                    refreshActionBarSubtitle();
                }
            }
        });
        return showsAdapter;
    }

    private void crossfadeAnimation() {
        recordsList.setAlpha(0f);
        recordsList.setVisibility(View.VISIBLE);

        recordsList.animate()
            .alpha(1f)
            .setDuration(crossfadeAnimDuration)
            .setListener(null);

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

    private void downloadCoverImage(RecordItem record) {
        final CoverImageDownloader downloader = new CoverImageDownloader();
        downloader.setOnCompleteListener(bitmap -> {
            audioController.setCoverImage(bitmap);
            record.setCover(bitmap);
        });
        downloader.setOnErrorListener(this::errorReportsDialog);
        if (record.getRecord().hasImgUri()) {
            try {
                downloader.execute(record.getRecord().getImgUri().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadNextRecords(ShowDto item) {
        Log.d(getClass().getSimpleName(), "downloadNextRecords: for show " + item.getShow().getName());

        final RecordsDownloader downloader = new RecordsDownloader(item.incAudioPage(), ShowDto.ITEMS_PER_PAGE, item.getShow());
        downloader.setOnCompleteListener(result -> {
            onRecordsDownloaded(item, result);
            getRecordsListAdapter().update();
        });
        downloader.setOnErrorListener(errors -> {
            item.decAudioPage();
            errorReportsDialog(errors);
        });

        downloader.execute();
        recordsAreDownloading = true;
    }

    private void errorReportsDialog(List<String> reports) {
        errorReportsDialog(this, reports);
    }

    private void fillRecordsList(ShowDto show) {
        RecordsAdapter recordsAdapter = getRecordsListAdapter();
        audioController.setPlaylist(recordsAdapter);
        recordsAdapter.setSource(show);
        int selected = getSelectedRecordIndex();
        if (chosenRecord != null && chosenRecord.equals(recordsAdapter.getItem(selected))) {
            recordsAdapter.setSelected(selected);
        } else {
            recordsAdapter.setSelected(-1);
        }
        recordsList.scrollToPosition(getSelectedRecordIndex());
    }

    private int getSelectedRecordIndex() {
        Integer selected = selectedRecords.get(playShow.getShow().getName());
        return selected == null ? -1 : selected;
    }

    /**
     * The last chosen {@link ShowDto} from navigation. Record items this show are actual in {@code recordsAdapter}.
     *
     * @return actual {@link ShowDto} or null
     */
    private ShowDto getChosenShow() {
        RecordsAdapter recordsAdapter = getRecordsListAdapter();
        if (recordsAdapter != null) {
            return recordsAdapter.getSource();
        }
        return null;
    }

    private RecordsAdapter getRecordsListAdapter() {
        return (RecordsAdapter) recordsList.getAdapter();
    }

    private ShowsAdapter getShowsListAdapter() {
        return (ShowsAdapter) showsList.getAdapter();
    }

    private void init() {
        onRefreshShows();

        loadingBar = findViewById(R.id.loadingPanel);
        loadingBar.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        crossfadeAnimDuration = getResources().getInteger(
            android.R.integer.config_shortAnimTime);

        initShowsList();
        initRecordsList();

        initActionBar();

        initMediaPlayer();
        initAudioController();

        /*EditText textView = findViewById(R.id.testingTextFied);
        textView.setText("https://m.static.lagardere.cz/evropa2/2018/12/20181224-viki-vanoce.mp3");
//    textView.setText("https://m.static.lagardere.cz/evropa2/image/2016/01/Leos_Patrik-3-660x336.jpg");
        findViewById(R.id.testingButton).setOnClickListener(v -> {
            prepareMediaPlayerSource(textView.getText().toString());

            CoverImageDownloader downloader = new CoverImageDownloader();
            downloader.setOnCompleteListener(result -> {
                ImageView iv = (ImageView) findViewById(R.id.testingImage);
                iv.setImageBitmap((Bitmap) result);
                toast("cover was downloaded", Toast.LENGTH_LONG);
            });
            try {
                downloader.execute(new URL(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });*/

    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        lockNavigationDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
            mDrawerLayout, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(R.string.navigation_title);
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
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //Hide the default Actionbar
        //getSupportActionBar().hide();
        // Call syncState() from your Activity's onPostCreate to synchronize the
        // indicator
        // with the state of the linked DrawerLayout after
        // onRestoreInstanceState has occurred
        mDrawerToggle.syncState();
    }

    private void initAudioController() {
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
                return mediaPlayer.getDuration() / 1000;
            }

            @Override
            public int getCurrentPosition() {
                return mediaPlayer.getCurrentPosition() / 1000;
            }

            @Override
            public void seekTo(int pos) {
                mediaPlayer.seekTo(pos * 1000);
            }

            @Override
            public boolean isPlaying() {
                if (mediaPlayer != null) {
                    return mediaPlayer.isPlaying();
                }
                return false;
            }

            @Override
            void next() {
                RecordItem nextItem = audioController.getPlaylist().next();
                if (nextItem != null) {
                    onRecordItemClick(nextItem, audioController.getPlaylist().indexOf(nextItem));
                }
            }

            @Override
            void previous() {
                if (getCurrentPosition() > 3) {
                    seekTo(0);
                } else {
                    RecordItem previousItem = audioController.getPlaylist().previous();
                    if (previousItem == null) {
                        seekTo(0);
                    } else {
                        onRecordItemClick(previousItem, audioController.getPlaylist().indexOf(previousItem));
                    }
                }
            }

            @Override
            public boolean canPause() {
                return true;
            }

        };
        controllerView.setOnClickListener(v -> {
            if (audioController.isEnabled() && chosenRecord != null) {
                if (playShow.equals(getChosenShow())) {
                    recordsList.smoothScrollToPosition(getSelectedRecordIndex());
                }
            }
        });
        audioController = new AudioController(controllerView, audioPlayerControl);
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
                audioController.clickOnPlayPause();
            }

        });
        mediaPlayer.setOnCompletionListener(mp -> audioController.onCompletion());
    }

    private void initRecordsList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recordsList = findViewById(R.id.recycler_view);

        RecordsAdapter recordsAdapter = createRecordsAdapter();
        recordsList.setAdapter(recordsAdapter);
        recordsList.setLayoutManager(linearLayoutManager);
        recordsList.addOnScrollListener(new EndlessScrollListener(() -> {
            if (!recordsAreDownloading && getChosenShow() != null) {
                downloadNextRecords(getChosenShow());
            }
        }, VISIBLE_TRESHOLD
        ));
        recordsList.addItemDecoration(new SpacesItemDecoration(ITEM_OFFSET));
//        recordsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recordsList.setHasFixedSize(true);
        recordsList.setVisibility(View.GONE);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::onRefreshRecords);
        swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.RED, Color.WHITE);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary_material_dark);
        selectedRecords = new HashMap<>();
    }

    private void initShowsList() {
        ShowsAdapter showsAdapter = createShowsAdapter();
        showsList = findViewById(R.id.left_drawer);
        showsList.setSmoothScrollbarEnabled(true);
        showsList.setAdapter(showsAdapter);
        showsList.setOnItemClickListener((adapterView, view, i, l) -> {
            ShowDto s = (ShowDto) showsAdapter.getItem(i);
            onNavigationItemClick(s, i);
        });
    }

    private void setShowsNavigation(List<ShowDto> shows) {
        getShowsListAdapter().setShows(shows.toArray(new ShowDto[0]));
        getShowsListAdapter().notifyDataSetChanged();
    }


    /*### Event handlers ###*/

    private void onAllFilterClick() {
        filterRecords(RecordType.All);
        updateFilterMenuItem(menu.findItem(R.id.action_filter_all));
    }

    private void onAudioFilterClick() {
        filterRecords(RecordType.Audio);
        updateFilterMenuItem(menu.findItem(R.id.action_filter_audio));
    }

    private void onVideoFilterClick() {
        filterRecords(RecordType.Video);
        updateFilterMenuItem(menu.findItem(R.id.action_filter_video));
    }

    private void onAudioItemClick(RecordItem record) {
        if (record.getRecord().hasMediaUri()) {
            prepareMediaPlayerSource(record.getRecord().getMediaUri().toString());
        } else {
            final MediaUrlDownloader downloader = new MediaUrlDownloader();
            downloader.setOnCompleteListener(uri -> {
                if (uri != null) {
                    record.getRecord().setMediaUri(uri);
                    prepareMediaPlayerSource(record.getRecord().getMediaUri().toString());
                }
            });
            downloader.setOnErrorListener(this::errorReportsDialog);
            downloader.execute(record.getRecord().getWebSiteUri());
        }
    }

    private void onNavigationItemClick(ShowDto item, int position) {
        if (chosenShowPosition == position) {
            mDrawerLayout.closeDrawer(showsList);
            return;
        }

        getShowsListAdapter().setSelectedItem(position);
        chosenShowPosition = position;
        //chosenShow = item;
        if (playShow == null) playShow = item;

        mDrawerLayout.closeDrawer(showsList);
        showLoading();

        if (item.isEmpty()) {
            final RecordsDownloader downloader = new RecordsDownloader(item.incAudioPage(), ShowDto.ITEMS_PER_PAGE, item.getShow());
            downloader.setOnCompleteListener(result -> {
                onRecordsDownloaded(item, result);
                fillRecordsList(item);
                crossfadeAnimation();
            });
            downloader.setOnErrorListener(errors -> {
                item.decAudioPage();
                errorReportsDialog(errors);
            });
            downloader.execute();
        } else {
            fillRecordsList(item);
            crossfadeAnimation();
        }
    }

    private void onRecordItemClick(RecordItem record, int index) {
        toast(record.toString(), Toast.LENGTH_SHORT);
        int selected = getSelectedRecordIndex();
        if (selected == index) {
            audioController.clickOnPlayPause();
            return;
        }

        audioPlayerControl.stop();
        chosenRecord = record;
        playShow = getChosenShow();

        switch (record.getType()) {
            case Audio:
                onAudioItemClick(record);
                break;
            case Video:
                onVideoItemClick(record);
                break;
            default:
                break;
        }

        if (record.hasCover()) {
            audioController.setCoverImage(record.getCover());
        } else {
            audioController.resetCoverImage();
            downloadCoverImage(record);
        }

        audioController.setInfoLineText(record.getRecord().getName());
        RecordsAdapter recordsAdapter = getRecordsListAdapter();

        RecyclerView.ViewHolder viewHolder = recordsList.findViewHolderForAdapterPosition(index);
        if (viewHolder != null) {
            recordsAdapter.markViewHolder((RecordItemViewHolder) viewHolder);
        }
        viewHolder = recordsList.findViewHolderForAdapterPosition(selected);
        if (viewHolder != null) {
            recordsAdapter.unmarkViewHolder((RecordItemViewHolder) viewHolder);
        }

        selectedRecords.put(playShow.getShow().getName(), index);
        recordsAdapter.setSelected(index);
    }

    private void onRecordItemDetail(RecordItem recordItem) {
        new AlertDialog.Builder(this)
            .setTitle("Detail")
            .setMessage(recordItem.getRecord().info())
            .setIcon(android.R.drawable.ic_dialog_info)
            .setNeutralButton("OK", (dialog1, which) -> dialog1.dismiss())
            .show();
    }

    private void onRecordsDownloaded(ShowDto item, Set<RecordItem> records) {
        Log.d(getClass().getSimpleName(), "onRecordsDownloaded: for show " + item.getInfo());
//    List<RecordItem> audioList = new ArrayList<>(),
//            videoList = new ArrayList<>();
//    Set<RecordItem> audioSet = new LinkedHashSet<>();

        if (!records.isEmpty()) {
            item.addRecordItems(records);
        }

        recordsAreDownloading = false;
        Log.d(getClass().getSimpleName(), "onRecordsDownloaded: done");
    }

    private void onRefreshRecords() {
        final RecordsDownloader downloader = new RecordsDownloader(1, ShowDto.ITEMS_PER_PAGE, playShow.getShow());
        downloader.setOnCompleteListener(result -> {
            onRecordsDownloaded(playShow, result);
            getRecordsListAdapter().update();
            swipeRefreshLayout.setRefreshing(false);
            toast(R.string.refresh_done, Toast.LENGTH_LONG);
        });
        downloader.setOnErrorListener(this::errorReportsDialog);
        Log.d(getClass().getSimpleName(), "onRefreshRecords: from " + playShow.getShow().getName());
        downloader.execute();
        recordsAreDownloading = true;
    }

    private void onRefreshShows() {
        if (!NetworkUtils.isNetworkConnected()) {
            noConnectionToast();
        } else if (!showsAreDownloading) {
            showsAreDownloading = true;
            startDownloadShowsToast();
            runShowRefreshAnim();

            showManager.loadAllShowsAsync(
                shows -> {
                    showsAreDownloading = false;
                    finishDownloadShowsToast();
                    if (shows != null && !shows.isEmpty()) {
                        setShowsNavigation(shows);
                    }
                    stopShowRefreshAnim();
                    unlockNavigationDrawer();
                },
                (this::errorReportsDialog)
            );

        } else {
            toast(R.string.still_downloading, Toast.LENGTH_SHORT);
        }
    }

    private void onVideoItemClick(RecordItem record) {
//        if (record.getRecord().hasMediaUrl()) {
//            playVideo(record.getRecord().getMediaUrl());
//        } else {
//            MediaUrlDownloader downloader = (MediaUrlDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.MediaUrl);
//            downloader.setOnCompleteListener(url -> {
//                if (url != null) {
//                    record.getRecord().setMediaUrl(url);
//                    playVideo(record.getRecord().getMediaUrl());
//                }
//            });
//            downloader.setOnErrorListener(errors -> errorReportsDialog(errors));
//            downloader.execute(new MediaUrlDownloader.Params(MediaUrlDownloader.Params.TYPE_VIDEO, record.getRecord().getWebSiteUri()));
//        }
        Log.e(getClass().getSimpleName(), "onVideoItemClick(record): Not implemented yet.");
    }


    /*### Actions ###*/

    private void filterRecords(RecordType type) {
        RecordsAdapter recordsAdapter = getRecordsListAdapter();
        if (recordsAdapter == null) return;
        recordsAdapter.filter(type);
    }

    private void playVideo(URL url) {
   /* if (recordItem.getType().compareTo(Type.Video) != 0) return;
    if (audioController.isPlaying()) audioController.clickOnPlayPause();
*/
        Uri path = Uri.parse(url.toExternalForm());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(path, "video/*");
//    intent.setType("text/plain");

// Verify that the intent will resolve to an activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            toast(R.string.error_none_video_app, Toast.LENGTH_LONG);
        }
    }

    private void refreshActionBarSubtitle() {
        if (getChosenShow() != null) {
            actionBar.setSubtitle(getChosenShow().getShow().getName() + " (" + getRecordsListAdapter().getItemCount() + ")");
        }
    }

    private void runShowRefreshAnim() {
        if (refreshShowButton != null && refreshShowAnim != null) {
            if (showsAreDownloading) {
                refreshShowButton.startAnimation(refreshShowAnim);
            }
        }
    }

    private void stopShowRefreshAnim() {
        if (refreshShowButton != null) {
            if (!showsAreDownloading) {
                refreshShowButton.clearAnimation();
            }
        }
    }

    private void lockNavigationDrawer(int lockMode) {
        mDrawerLayout.setDrawerLockMode(lockMode);
    }

    private void unlockNavigationDrawer() {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void updateFilterMenuItem(MenuItem item) {
        MenuItem filterItem = menu.findItem(R.id.filter_records_list);
        filterItem.setTitle(item.getTitle());
        filterItem.setIcon(item.getIcon());
    }


    /*### Toasts ###*/

    private void noConnectionToast() {
        toast(R.string.error_no_connection, Toast.LENGTH_LONG);
    }

    private void finishDownloadShowsToast() {
        if (!showsAreDownloading) {
            toast(R.string.shows_are_ready, Toast.LENGTH_SHORT);
        }
    }

    private void startDownloadShowsToast() {
        if (!showsAreDownloading) toast(R.string.downloading_shows, Toast.LENGTH_SHORT);
    }

}
