package com.example.roman.e2zaznamy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.roman.e2zaznamy.record.RecordItem;
import com.example.roman.e2zaznamy.record.RecordsAdapter;
import com.example.roman.e2zaznamy.show.ShowItem;
import com.example.roman.e2zaznamy.show.ShowsAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jEvropa2.data.Show;

/**
 * Main class of application and the only activity.
 *
 * @author Roman Zelenik
 */
public class MainActivity extends AppCompatActivity {

  public static final String
      SHOWS_ARE_READY = "Show jsou připraveny",
      DOWNLOADING_SHOWS = "Stahuji seznam Show...",
      ERROR_ON_LOADING = "Při načítání došlo k chybě :-(",
      ERROR_NO_CONNECTION = "Nejsi připojen k síti",
      LOADING = "Načítání",
      STILL_DOWNLOADING = "Stahování probíhá...",
      SUB_URL_ARCHIV = "/mp3-archiv/",
      SUB_URL_SHOWS = "/shows/",
      URL_E2 = "https://evropa2.cz";

  private static final int
      ITEM_OFFSET = 6,
      VISIBLE_TRESHOLD = 5;

  private MediaPlayer mediaPlayer;
  private AudioController audioController;
  private AudioController.AudioPlayerControl audioPlayerControl;

  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView recordsList;
  private RecordsAdapter recordsAdapter;

  private ListView showsList;
  private ShowsAdapter showsAdapter;

  private boolean
      recordsAreDownloading = false,
      showsAreDownloading = false;
  private DrawerLayout mDrawerLayout;
  private ActionBar actionBar;
  private RecordItem chosenRecord;
  private ShowItem chosenShow, playShow;
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

    /*#######################################################
      ###               OVERRIDE METHODS                  ###
      #######################################################*/

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    setContentView(R.layout.main_layout);
//    setContentView(R.layout.testing_layout);
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
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
//            case R.id.action_settings :
//                return true;

      case android.R.id.home:
        if (showsAdapter != null && showsAdapter.isEmpty()) {
          toast("Seznam je prázdný", Toast.LENGTH_SHORT);
          return true;
        }
        if (mDrawerLayout.isDrawerOpen(showsList)) {
          mDrawerLayout.closeDrawer(showsList);
        } else {
          mDrawerLayout.openDrawer(showsList);
        }
        return true;

      case R.id.action_refresh_shows:
        try {
          downloadShows();
        } catch (MalformedURLException e) {
          List list = new ArrayList<>();
          list.add(e.getLocalizedMessage());
          errorReportsDialog(list);
        }
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

    PrepareStream ps = new PrepareStream(this, mediaPlayer);
    ps.setOnErrorListener(new PrepareStream.OnErrorListener() {
      @Override
      public void onError() {
        new AlertDialog.Builder(MainActivity.this)
            .setTitle(LOADING)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setMessage(ERROR_ON_LOADING)
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

  public void toast(String msg, int duration) {
    Toast.makeText(this, msg, duration).show();
  }

    /*#######################################################
      ###              PRIVATE METHODS                    ###
      #######################################################*/

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
    DownloaderFactory.CoverImageDownloader downloader = (DownloaderFactory.CoverImageDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.CoverImage);
    downloader.setOnCompleteListener(bitmap -> {
      audioController.setCoverImage(bitmap);
      record.setCover(bitmap);
    });
    downloader.setOnErrorListener(errors -> errorReportsDialog(errors));
    downloader.execute(record.getRecord().getImgUrl());
  }

  private void downloadNextRecords(ShowItem item) {
    DownloaderFactory.RecordsDownloader downloader = (DownloaderFactory.RecordsDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Records);
    downloader.setOnCompleteListener(result -> {
      onRecordsDownloaded(item, result);
      recordsAdapter.update();
    });
    downloader.setOnErrorListener(errors -> errorReportsDialog(errors));
    if (item.hasNextPageUrl()) {
      downloader.execute(item.getNextPageUrl());
      recordsAreDownloading = true;
    }
  }

  private void downloadShows() throws MalformedURLException {
    if (!isNetworkConnected()) {
      toast(ERROR_NO_CONNECTION, Toast.LENGTH_LONG);
    } else if (!showsAreDownloading) {
      startDownloadShowsToast();
      runShowRefreshAnim();
      DownloaderFactory.ShowsDownloader downloader = (DownloaderFactory.ShowsDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Shows);
      downloader.setOnCompleteListener(set -> {
        showsAreDownloading = false;
        finishDownloadShowsToast();
        ArrayList<ShowItem> shows = new ArrayList(set.size());
        for (Show s : set) {
          shows.add(new ShowItem(s));
        }
        setShowsNavigation(shows.toArray(new ShowItem[shows.size()]));
        stopShowRefreshAnim();
        unlockNavigationDrawer();
      });
      downloader.setOnErrorListener(errors -> errorReportsDialog(errors));
      downloader.execute(new URL(URL_E2 + SUB_URL_SHOWS));
      showsAreDownloading = true;
    } else {
      toast(STILL_DOWNLOADING, Toast.LENGTH_SHORT);
    }
  }

  private void errorReportsDialog(List<String> reports) {
    errorReportsDialog(this, reports);
  }

  private void fillRecList(ShowItem show) {
    recordsAdapter.setSource(show);
    int selected = getSelectedRecordIndex();
    if (chosenRecord != null && chosenRecord.equals(recordsAdapter.getItem(selected))) {
      recordsAdapter.setSelected(selected);
    } else {
      recordsAdapter.setSelected(-1);
    }
    recordsList.scrollToPosition(getSelectedRecordIndex());
  }

  private void finishDownloadShowsToast() {
    if (!showsAreDownloading) {
      toast(SHOWS_ARE_READY, Toast.LENGTH_SHORT);
    }
  }

  private int getSelectedRecordIndex() {
    Integer selected = selectedRecords.get(playShow.getShow().getName());
    return selected == null ? -1 : selected.intValue();
  }

  private void init() {
    try {
      downloadShows();
    } catch (MalformedURLException e) {
      List list = new ArrayList<>();
      list.add(e.getLocalizedMessage());
      errorReportsDialog(list);
    }
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
/*
    EditText textView = (EditText) findViewById(R.id.testingTextFied);
    textView.setText("https://m.static.lagardere.cz/evropa2/audio/2016/02/20160225-Meteorit.mp3");
    textView.setText("https:\\/\\/m.static.lagardere.cz\\/evropa2\\/audio\\/2016\\/06\\/20160617-\u010cesk\u00e1-volba-Ktery-druh-sportu-maji-cesi-nejradeji.mp3");
    textView.setText("https://m.static.lagardere.cz/evropa2/image/2016/01/Leos_Patrik-3-660x336.jpg");
    ((Button) findViewById(R.id.testingButton)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //prepareMediaPlayerSource(textView.getText().toString());
        String url = textView.getText().toString();
        url = org.apache.commons.lang.StringEscapeUtils.unescapeJava(url);
        toast(url, Toast.LENGTH_LONG);
        //  MediaPlayer mp = MediaPlayer.create(getApplicationContext(), Uri.parse(url));
        //  mp.start();
        DownloaderFactory.CoverImageDownloader downloader = (DownloaderFactory.CoverImageDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.CoverImage);
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
      }
    });
*/
  }

  private void initActionBar() {
    actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
      public void next() {
        int selected = getSelectedRecordIndex();
        RecordItem nextRecord = recordsAdapter.getItem(selected + 1);
        if (nextRecord != null) {
          onRecordItemClick(nextRecord, selected + 1);
        }
      }

      @Override
      public void previous() {
        if (getCurrentPosition() > 3) {
          seekTo(0);
        } else {
          int selected = getSelectedRecordIndex();
          RecordItem previousRecord = recordsAdapter.getItem(selected - 1);
          if (previousRecord == null) {
            seekTo(0);
          } else {
            onRecordItemClick(previousRecord, selected - 1);
          }
        }
      }

      @Override
      public boolean canPause() {
        return true;
      }

    };
    controllerView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (audioController.isEnabled() && chosenRecord != null) {
          if (playShow.equals(chosenShow)) {
            recordsList.smoothScrollToPosition(getSelectedRecordIndex());
          }
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
    recordsAdapter = new RecordsAdapter(this, (record, index) -> onRecordItemClick(record, index));
    recordsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        showsAdapter.notifyDataSetChanged();
      }
    });
    recordsList.setAdapter(recordsAdapter);
    recordsList.setLayoutManager(linearLayoutManager);
    recordsList.addOnScrollListener(new EndlessScrollListener(
        new EndlessScrollListener.LoadNextItems() {
          @Override
          public void loadNextItems() {
            if (!recordsAreDownloading) {
              downloadNextRecords(playShow);
            }
          }
        }, VISIBLE_TRESHOLD
    ));
    recordsList.addItemDecoration(new SpacesItemDecoration(ITEM_OFFSET));
//        recordsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    recordsList.setHasFixedSize(true);
    recordsList.setVisibility(View.GONE);
    swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        onRefreshRecords();
      }
    });
    swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.RED, Color.WHITE);
    swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary_material_dark);
    selectedRecords = new HashMap<>();
  }

  private void initShowsAdapter() {
    showsAdapter = new ShowsAdapter(this);
    showsAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        if (!mDrawerLayout.isDrawerOpen(showsList)) {
          refreshActionBarSubtitle();
        }
      }
    });
  }

  private void initShowsList() {
    showsList = (ListView) findViewById(R.id.left_drawer);
    showsList.setSmoothScrollbarEnabled(true);
    initShowsAdapter();
    showsList.setAdapter(showsAdapter);
    showsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ShowItem s = (ShowItem) showsAdapter.getItem(i);
        onNavigationItemClick(s, i);
      }
    });
  }

  private void lockNavigationDrawer(int lockMode) {
    mDrawerLayout.setDrawerLockMode(lockMode);
  }

  private void onNavigationItemClick(ShowItem item, int position) {
    if (chosenShowPosition == position) {
      mDrawerLayout.closeDrawer(showsList);
      return;
    }

    showsAdapter.setSelectedItem(position);
    chosenShowPosition = position;
    chosenShow = item;
    if (playShow == null) playShow = item;

    mDrawerLayout.closeDrawer(showsList);
    showLoading();

    if (item.getAudioRecords().isEmpty() && item.getVideoRecords().isEmpty()) {
      DownloaderFactory.RecordsDownloader downloader = (DownloaderFactory.RecordsDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Records);
      downloader.setOnCompleteListener(result -> {
        onRecordsDownloaded(item, result);
        fillRecList(item);
      });
      downloader.setOnErrorListener(errors -> errorReportsDialog(errors));
      downloader.execute(item.getShow().getWebSiteUrl());
    } else {
      fillRecList(item);
      crossfadeAnimation();
    }
  }

  private void onRecordItemClick(RecordItem record, int index) {
    toast(record.toString(), Toast.LENGTH_SHORT);
    int selected = getSelectedRecordIndex();
    if (selected == index) {
      audioController.clickOnPlay();
      return;
    }

    audioPlayerControl.stop();
    chosenRecord = record;
    playShow = chosenShow;

    if (record.getRecord().hasMp3Url()) {
      prepareMediaPlayerSource(record.getRecord().getMp3Url().toString());
    } else {
      DownloaderFactory.Mp3UrlDownloader downloader = (DownloaderFactory.Mp3UrlDownloader) DownloaderFactory.getDownloader(DownloaderFactory.Type.Mp3Url);
      downloader.setOnCompleteListener(url -> {
        if (url != null) {
          record.getRecord().setMp3Url(url);
          prepareMediaPlayerSource(record.getRecord().getMp3Url().toString());
        }
      });
      downloader.setOnErrorListener(errors -> errorReportsDialog(errors));
      downloader.execute(record.getRecord().getWebSiteUrl());
    }

    if (record.hasCover()) {
      audioController.setCoverImage(record.getCover());
    } else {
      audioController.resetCoverImage();
      downloadCoverImage(record);
    }

    audioController.setInfoLineText(record.getRecord().getName());

    RecyclerView.ViewHolder viewHolder = recordsList.findViewHolderForAdapterPosition(index);
    if (viewHolder != null) {
      recordsAdapter.markViewHolder((RecordsAdapter.MyViewHolder) viewHolder);
    }
    viewHolder = recordsList.findViewHolderForAdapterPosition(selected);
    if (viewHolder != null) {
      recordsAdapter.unmarkViewHolder((RecordsAdapter.MyViewHolder) viewHolder);
    }

    selectedRecords.put(playShow.getShow().getName(), index);
    recordsAdapter.setSelected(index);
  }

  private void onRecordsDownloaded(ShowItem item, Map<String, Object> result) {
    ArrayList<RecordItem> audioList = new ArrayList<>(),
        videoList = new ArrayList<>();
    Set<RecordItem> records = (Set<RecordItem>) result.get("records");
    if (!records.isEmpty()) {
      for (RecordItem i : records) {
        if (i.getType() == RecordItem.Type.Audio) {
          audioList.add(i);
        } else if (i.getType() == RecordItem.Type.Video) {
          videoList.add(i);
        }
      }
      item.getAudioRecords().addAll(audioList);
      item.getVideoRecords().addAll(videoList);
    }
    URL nextPage = (URL) result.get("nextPage");
    if (nextPage != null) item.setNextPageUrl(nextPage);
    crossfadeAnimation();
    recordsAreDownloading = false;
  }

  private void onRefreshRecords() {
    //swipeRefreshLayout.setRefreshing(true);

    new Handler().postDelayed(() -> {
      swipeRefreshLayout.setRefreshing(false);
      toast("Refresh done", Toast.LENGTH_LONG);
    },5000);

  }

  private void refreshActionBarSubtitle() {
    if (chosenShow != null) {
      actionBar.setSubtitle(chosenShow.getShow().getName() + " (" + recordsAdapter.getItemCount() + ")");
    }
  }

  private void runShowRefreshAnim() {
    if (refreshShowButton != null && refreshShowAnim != null) {
      if (showsAreDownloading) {
        refreshShowButton.startAnimation(refreshShowAnim);
      }
    }
  }

  private void startDownloadShowsToast() {
    if (!showsAreDownloading) toast(DOWNLOADING_SHOWS, Toast.LENGTH_SHORT);
  }

  private void stopShowRefreshAnim() {
    if (refreshShowButton != null) {
      if (!showsAreDownloading) {
        refreshShowButton.clearAnimation();
      }
    }
  }

  private void setShowsNavigation(ShowItem[] shows) {
    if (showsAdapter == null) {
      initShowsAdapter();
      showsList.setAdapter(showsAdapter);
    }
    showsAdapter.setShows(shows);
    showsAdapter.notifyDataSetChanged();
  }

  private void unlockNavigationDrawer() {
    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
  }
}
