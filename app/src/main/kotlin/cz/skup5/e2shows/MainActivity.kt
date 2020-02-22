package cz.skup5.e2shows

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.DataSetObserver
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cz.skup5.e2shows.AudioController.AudioPlayerControl
import cz.skup5.e2shows.EndlessScrollListener.LoadNextItems
import cz.skup5.e2shows.downloader.CoverImageDownloader
import cz.skup5.e2shows.downloader.MediaUrlDownloader
import cz.skup5.e2shows.downloader.RecordsDownloader
import cz.skup5.e2shows.dto.ShowDto
import cz.skup5.e2shows.listener.OnCompleteListener
import cz.skup5.e2shows.listener.OnErrorListener
import cz.skup5.e2shows.manager.BasicShowManager
import cz.skup5.e2shows.manager.ShowManager
import cz.skup5.e2shows.record.RecordItem
import cz.skup5.e2shows.record.RecordItemViewHolder
import cz.skup5.e2shows.record.RecordType
import cz.skup5.e2shows.record.RecordsAdapter
import cz.skup5.e2shows.show.ShowsAdapter
import cz.skup5.e2shows.utils.NetworkUtils
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.*

//import cz.skup5.e2shows.manager.BasicPlaylistManager;
//import cz.skup5.e2shows.playlist.PlaylistManager;
/**
 * Main class of application and the only activity.
 *
 * @author Skup5
 */
class MainActivity : AppCompatActivity() {
    //  private static final PlaylistManager playlistManager = BasicPlaylistManager.getInstance();
    private var mediaPlayer: MediaPlayer? = null
    private var audioController: AudioController<RecordItem?>? = null
    private var audioPlayerControl: AudioPlayerControl? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var recordsList: RecyclerView? = null
    //  private RecordsAdapter recordsAdapter;
    private var showsList: ListView? = null
    //private ShowsAdapter showsAdapter;
    private var recordsAreDownloading = false
    private var showsAreDownloading = false
    private var menu: Menu? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var actionBar: ActionBar? = null
    private var chosenRecord: RecordItem? = null
    private var playShow: ShowDto? = null
    private var chosenShowPosition = -1
    private var loadingBar: View? = null
    private var crossfadeAnimDuration = 0
    private var refreshShowButton: View? = null
    private var refreshShowAnim: Animation? = null
    private var selectedRecords: MutableMap<String, Int>? = null

    /*#######################################################
      ###               OVERRIDE METHODS                  ###
      #######################################################*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.main_layout)
        //        setContentView(R.layout.testing_layout);
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        Handler().post {
            refreshShowButton = findViewById(R.id.action_refresh_shows)
            refreshShowAnim = createRotateAnim(refreshShowButton, 360, 1000, true)
            runShowRefreshAnim()
        }
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val showsAdapter: ShowsAdapter = showsListAdapter
                if (showsAdapter.isEmpty) {
                    toast(R.string.shows_empty_list, Toast.LENGTH_SHORT)
                    return true
                }
                if (mDrawerLayout!!.isDrawerOpen((showsList)!!)) {
                    mDrawerLayout!!.closeDrawer((showsList)!!)
                } else {
                    mDrawerLayout!!.openDrawer((showsList)!!)
                }
                return true
            }
            R.id.action_refresh_shows -> {
                onRefreshShows()
                return true
            }
            R.id.action_filter_all -> {
                onAllFilterClick()
                return true
            }
            R.id.action_filter_audio -> {
                onAudioFilterClick()
                return true
            }
            R.id.action_filter_video -> {
                onVideoFilterClick()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*#######################################################
      ###               PUBLIC METHODS                    ###
      #######################################################*/

    fun prepareMediaPlayerSource(url: String?) {
        if (mediaPlayer == null) {
            initMediaPlayer()
        }
        if (!NetworkUtils.isNetworkConnected()) {
            noConnectionToast()
            return
        }
        val ps = PrepareStream(this, mediaPlayer)
        ps.setOnErrorListener {
            AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.loading)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.error_on_loading)
                    .setPositiveButton(TRY_NEXT_RECORD) { dialog, _ ->
                        audioPlayerControl!!.next()
                        dialog.dismiss()
                    }.setCancelable(true)
//                .setNegativeButton(STORNO, new DialogInterface.OnClickListener() {
//                  @Override
//                  public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                  }
//                })
                    .show()
        }
        ps.execute(url)
    }

    fun hideLoading() {
        loadingBar!!.visibility = View.GONE
    }

    fun showLoading() {
        loadingBar!!.visibility = View.VISIBLE
        loadingBar!!.alpha = 1f
        if (recordsList != null && recordsList!!.visibility != View.GONE) {
            recordsList!!.visibility = View.INVISIBLE
        }
    }

    fun toast(resourceId: Int, duration: Int) {
        Toast.makeText(this, resourceId, duration).show()
    }

    fun toast(msg: String?, duration: Int) {
        Toast.makeText(this, msg, duration).show()
    }

    /*#######################################################
      ###              PRIVATE METHODS                    ###
      #######################################################*/

    private fun createRecordsAdapter(): RecordsAdapter {
        val recordsAdapter = RecordsAdapter(this)
        recordsAdapter.setOnRecordClickListener { record: RecordItem, index: Int -> onRecordItemClick(record, index) }
        recordsAdapter.setOnMenuClickListener { item: MenuItem, source: RecordItemViewHolder ->
            when (item.itemId) {
                R.id.context_action_detail -> {
                    onRecordItemDetail(source.actualRecord)
                    return@setOnMenuClickListener true
                }
                else -> return@setOnMenuClickListener false
            }
        }
        recordsAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                showsListAdapter.notifyDataSetChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                onChanged()
            }
        })
        return recordsAdapter
    }

    private fun createShowsAdapter(): ShowsAdapter {
        val showsAdapter = ShowsAdapter(this)
        showsAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                if (!mDrawerLayout!!.isDrawerOpen((showsList)!!)) {
                    refreshActionBarSubtitle()
                }
            }
        })
        return showsAdapter
    }

    private fun crossfadeAnimation() {
        recordsList!!.alpha = 0f
        recordsList!!.visibility = View.VISIBLE
        recordsList!!.animate()
                .alpha(1f)
                .setDuration(crossfadeAnimDuration.toLong())
                .setListener(null)
        loadingBar!!.animate()
                .alpha(0f)
                .setDuration(crossfadeAnimDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        loadingBar!!.visibility = View.GONE
                    }
                })
    }

    private fun downloadCoverImage(record: RecordItem) {
        val downloader = CoverImageDownloader()
        downloader.setOnCompleteListener({ bitmap: Bitmap? ->
            audioController!!.setCoverImage(bitmap)
            record.setCover(bitmap)
        })
        downloader.setOnErrorListener({ reports: List<String> -> this.errorReportsDialog(reports) })
        if (record.record.hasImgUri()) {
            try {
                downloader.execute(record.record.imgUri.toURL())
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadNextRecords(item: ShowDto?) {
        Log.d(javaClass.simpleName, "downloadNextRecords: for show " + item!!.show.name)
        val downloader = RecordsDownloader(item.incAudioPage(), ShowDto.ITEMS_PER_PAGE, item.show)
        downloader.setOnCompleteListener({ result: Set<RecordItem> ->
            onRecordsDownloaded(item, result)
            recordsListAdapter!!.update()
        })
        downloader.setOnErrorListener({ errors: List<String> ->
            item.decAudioPage()
            errorReportsDialog(errors)
        })
        downloader.execute()
        recordsAreDownloading = true
    }

    private fun errorReportsDialog(reports: List<String>) {
        errorReportsDialog(this, reports)
    }

    private fun fillRecordsList(show: ShowDto) {
        val recordsAdapter = recordsListAdapter
        audioController!!.playlist = recordsAdapter
        recordsAdapter!!.source = show
        val selected = selectedRecordIndex
        if (chosenRecord != null && (chosenRecord == recordsAdapter.getItem(selected))) {
            recordsAdapter.setSelected(selected)
        } else {
            recordsAdapter.setSelected(-1)
        }
        recordsList!!.scrollToPosition(selectedRecordIndex)
    }

    private val selectedRecordIndex: Int
        get() {
            val selected = selectedRecords!![playShow!!.show.name]
            return selected ?: -1
        }

    /**
     * The last chosen [ShowDto] from navigation. Record items this show are actual in `recordsAdapter`.
     *
     * @return actual [ShowDto] or null
     */
    private val chosenShow: ShowDto?
        get() = recordsListAdapter?.source

    private val recordsListAdapter: RecordsAdapter?
        get() = recordsList!!.adapter as RecordsAdapter?

    private val showsListAdapter: ShowsAdapter
        get() = showsList!!.adapter as ShowsAdapter

    private fun init() {
        onRefreshShows()
        loadingBar = findViewById(R.id.loadingPanel)
        loadingBar?.visibility = View.GONE
        // Retrieve and cache the system's default "short" animation time.
        crossfadeAnimDuration = resources.getInteger(
                android.R.integer.config_shortAnimTime)
        initShowsList()
        initRecordsList()
        initActionBar()
        initMediaPlayer()
        initAudioController()
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

    private fun initActionBar() {
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        mDrawerLayout = findViewById(R.id.drawer)
        mDrawerLayout?.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)
        lockNavigationDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val mDrawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this,
                mDrawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                actionBar!!.setTitle(R.string.navigation_title)
                actionBar!!.subtitle = ""
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                actionBar!!.setTitle(R.string.app_name)
                refreshActionBarSubtitle()
            }
        }
        mDrawerToggle.isDrawerIndicatorEnabled = true
        //Set the ActionBarDrawerToggle in the layout
        mDrawerLayout?.addDrawerListener(mDrawerToggle)
        //Hide the default Actionbar
//getSupportActionBar().hide();
// Call syncState() from your Activity's onPostCreate to synchronize the
// indicator
// with the state of the linked DrawerLayout after
// onRestoreInstanceState has occurred
        mDrawerToggle.syncState()
    }

    private fun initAudioController() {
        val controllerView = findViewById<View>(R.id.audio_controller)
        audioPlayerControl = object : AudioPlayerControl() {
            override fun start() {
                mediaPlayer!!.start()
            }

            public override fun stop() {
                if (mediaPlayer != null) {
                    if (isPlaying) {
                        mediaPlayer!!.pause()
                    }
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                }
            }

            override fun pause() {
                mediaPlayer!!.pause()
            }

            override fun getDuration(): Int {
                return mediaPlayer!!.duration / 1000
            }

            override fun getCurrentPosition(): Int {
                return mediaPlayer!!.currentPosition / 1000
            }

            override fun seekTo(pos: Int) {
                mediaPlayer!!.seekTo(pos * 1000)
            }

            override fun isPlaying(): Boolean {
                return if (mediaPlayer != null) {
                    mediaPlayer!!.isPlaying
                } else false
            }

            public override fun next() {
                val nextItem = audioController!!.playlist.next()
                if (nextItem != null) {
                    onRecordItemClick(nextItem, audioController!!.playlist.indexOf(nextItem))
                }
            }

            public override fun previous() {
                if (currentPosition > 3) {
                    seekTo(0)
                } else {
                    val previousItem = audioController!!.playlist.previous()
                    if (previousItem == null) {
                        seekTo(0)
                    } else {
                        onRecordItemClick(previousItem, audioController!!.playlist.indexOf(previousItem))
                    }
                }
            }

            override fun canPause(): Boolean {
                return true
            }
        }
        controllerView.setOnClickListener {
            if (audioController!!.isEnabled && chosenRecord != null) {
                if ((playShow == chosenShow)) {
                    recordsList!!.smoothScrollToPosition(selectedRecordIndex)
                }
            }
        }
        audioController = AudioController(controllerView, audioPlayerControl)
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setOnPreparedListener(MediaPlayer.OnPreparedListener
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the MediaPlayer that is ready for playback
         */
        {
            audioController!!.isEnabled = true
            audioController!!.setUpSeekBar()
            /* play mp3 */
            audioController!!.clickOnPlayPause()
        })
        mediaPlayer!!.setOnCompletionListener { audioController!!.onCompletion() }
    }

    private fun initRecordsList() {
        val linearLayoutManager = LinearLayoutManager(this)
        val recordsAdapter = createRecordsAdapter()

        recordsList = findViewById(R.id.recycler_view)
        recordsList?.run {
            adapter = recordsAdapter
            layoutManager = linearLayoutManager
            addOnScrollListener(EndlessScrollListener(LoadNextItems {
                if (!recordsAreDownloading && chosenShow != null) {
                    downloadNextRecords(chosenShow)
                }
            }, VISIBLE_THRESHOLD
            ))
            addItemDecoration(SpacesItemDecoration(ITEM_OFFSET))
            //        recordsList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
            setHasFixedSize(true)
            visibility = View.GONE
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout?.run {
            setOnRefreshListener { onRefreshRecords() }
            setColorSchemeColors(Color.BLUE, Color.RED, Color.WHITE)
            setProgressBackgroundColorSchemeResource(R.color.primary_material_dark)
        }

        selectedRecords = HashMap()
    }

    private fun initShowsList() {
        val showsAdapter = createShowsAdapter()
        showsList = findViewById(R.id.left_drawer)
        showsList?.run {
            isSmoothScrollbarEnabled = true
            adapter = showsAdapter
            onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                val s: ShowDto = showsAdapter.getItem(i) as ShowDto
                onNavigationItemClick(s, i)
            }
        }
    }

    private fun setShowsNavigation(shows: List<ShowDto?>) {
        showsListAdapter.setShows(shows.toTypedArray())
        showsListAdapter.notifyDataSetChanged()
    }

    /*### Event handlers ###*/
    private fun onAllFilterClick() {
        filterRecords(RecordType.All)
        updateFilterMenuItem(menu!!.findItem(R.id.action_filter_all))
    }

    private fun onAudioFilterClick() {
        filterRecords(RecordType.Audio)
        updateFilterMenuItem(menu!!.findItem(R.id.action_filter_audio))
    }

    private fun onVideoFilterClick() {
        filterRecords(RecordType.Video)
        updateFilterMenuItem(menu!!.findItem(R.id.action_filter_video))
    }

    private fun onAudioItemClick(record: RecordItem) {
        if (record.record.hasMediaUri()) {
            prepareMediaPlayerSource(record.record.mediaUri.toString())
        } else {
            val downloader = MediaUrlDownloader()
            downloader.setOnCompleteListener({ uri: URI? ->
                if (uri != null) {
                    record.getRecord().mediaUri = uri
                    prepareMediaPlayerSource(record.getRecord().mediaUri.toString())
                }
            })
            downloader.setOnErrorListener({ reports: List<String> -> this.errorReportsDialog(reports) })
            downloader.execute(record.record.webSiteUri)
        }
    }

    private fun onNavigationItemClick(item: ShowDto, position: Int) {
        if (chosenShowPosition == position) {
            mDrawerLayout!!.closeDrawer((showsList)!!)
            return
        }
        showsListAdapter.setSelectedItem(position)
        chosenShowPosition = position
        //chosenShow = item;
        if (playShow == null) playShow = item
        mDrawerLayout!!.closeDrawer((showsList)!!)
        showLoading()
        if (item.isEmpty) {
            val downloader = RecordsDownloader(item.incAudioPage(), ShowDto.ITEMS_PER_PAGE, item.show)
            downloader.setOnCompleteListener({ result: Set<RecordItem> ->
                onRecordsDownloaded(item, result)
                fillRecordsList(item)
                crossfadeAnimation()
            })
            downloader.setOnErrorListener({ errors: List<String> ->
                item.decAudioPage()
                errorReportsDialog(errors)
            })
            downloader.execute()
        } else {
            fillRecordsList(item)
            crossfadeAnimation()
        }
    }

    private fun onRecordItemClick(record: RecordItem, index: Int) {
        toast(record.toString(), Toast.LENGTH_SHORT)
        val selected = selectedRecordIndex
        if (selected == index) {
            audioController!!.clickOnPlayPause()
            return
        }
        audioPlayerControl!!.stop()
        chosenRecord = record
        playShow = chosenShow
        when (record.type) {
            RecordType.Audio -> onAudioItemClick(record)
            RecordType.Video -> onVideoItemClick(record)
            else -> {
            }
        }
        if (record.hasCover()) {
            audioController!!.setCoverImage(record.cover)
        } else {
            audioController!!.resetCoverImage()
            downloadCoverImage(record)
        }
        audioController!!.setInfoLineText(record.record.name)
        val recordsAdapter = recordsListAdapter
        var viewHolder = recordsList!!.findViewHolderForAdapterPosition(index)
        if (viewHolder != null) {
            recordsAdapter!!.markViewHolder(viewHolder as RecordItemViewHolder?)
        }
        viewHolder = recordsList!!.findViewHolderForAdapterPosition(selected)
        if (viewHolder != null) {
            recordsAdapter!!.unmarkViewHolder(viewHolder as RecordItemViewHolder?)
        }
        selectedRecords!![playShow!!.show.name] = index
        recordsAdapter!!.setSelected(index)
    }

    private fun onRecordItemDetail(recordItem: RecordItem) {
        AlertDialog.Builder(this)
                .setTitle("Detail")
                .setMessage(recordItem.record.info())
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNeutralButton("OK") { dialog1: DialogInterface, _: Int -> dialog1.dismiss() }
                .show()
    }

    private fun onRecordsDownloaded(item: ShowDto?, records: Set<RecordItem>) {
        Log.d(javaClass.simpleName, "onRecordsDownloaded: for show " + item!!.info)
        //    List<RecordItem> audioList = new ArrayList<>(),
//            videoList = new ArrayList<>();
//    Set<RecordItem> audioSet = new LinkedHashSet<>();
        if (!records.isEmpty()) {
            item.addRecordItems(records)
        }
        recordsAreDownloading = false
        Log.d(javaClass.simpleName, "onRecordsDownloaded: done")
    }

    private fun onRefreshRecords() {
        val downloader = RecordsDownloader(1, ShowDto.ITEMS_PER_PAGE, playShow!!.show)
        downloader.setOnCompleteListener({ result: Set<RecordItem> ->
            onRecordsDownloaded(playShow, result)
            recordsListAdapter!!.update()
            swipeRefreshLayout!!.setRefreshing(false)
            toast(R.string.refresh_done, Toast.LENGTH_LONG)
        })
        downloader.setOnErrorListener({ reports: List<String> -> this.errorReportsDialog(reports) })
        Log.d(javaClass.simpleName, "onRefreshRecords: from " + playShow!!.show.name)
        downloader.execute()
        recordsAreDownloading = true
    }

    private fun onRefreshShows() {
        if (!NetworkUtils.isNetworkConnected()) {
            noConnectionToast()
        } else if (!showsAreDownloading) {
            showsAreDownloading = true
            startDownloadShowsToast()
            runShowRefreshAnim()
            showManager.loadAllShowsAsync(
                    OnCompleteListener { shows: List<ShowDto?>? ->
                        showsAreDownloading = false
                        finishDownloadShowsToast()
                        if (shows != null && !shows.isEmpty()) {
                            setShowsNavigation(shows)
                        }
                        stopShowRefreshAnim()
                        unlockNavigationDrawer()
                    },
                    (OnErrorListener { reports: List<String> -> this.errorReportsDialog(reports) })
            )
        } else {
            toast(R.string.still_downloading, Toast.LENGTH_SHORT)
        }
    }

    private fun onVideoItemClick(record: RecordItem) { //        if (record.getRecord().hasMediaUrl()) {
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
        Log.e(javaClass.simpleName, "onVideoItemClick(record): Not implemented yet.")
    }

    /*### Actions ###*/
    private fun filterRecords(type: RecordType) {
        val recordsAdapter = recordsListAdapter ?: return
        recordsAdapter.filter(type)
    }

    private fun playVideo(url: URL) { /* if (recordItem.getType().compareTo(Type.Video) != 0) return;
    if (audioController.isPlaying()) audioController.clickOnPlayPause();
*/
        val path = Uri.parse(url.toExternalForm())
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(path, "video/*")
        //    intent.setType("text/plain");
// Verify that the intent will resolve to an activity
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            toast(R.string.error_none_video_app, Toast.LENGTH_LONG)
        }
    }

    private fun refreshActionBarSubtitle() {
        if (chosenShow != null) {
            actionBar!!.subtitle = chosenShow!!.show.name + " (" + recordsListAdapter!!.itemCount + ")"
        }
    }

    private fun runShowRefreshAnim() {
        if (refreshShowButton != null && refreshShowAnim != null) {
            if (showsAreDownloading) {
                refreshShowButton!!.startAnimation(refreshShowAnim)
            }
        }
    }

    private fun stopShowRefreshAnim() {
        if (refreshShowButton != null) {
            if (!showsAreDownloading) {
                refreshShowButton!!.clearAnimation()
            }
        }
    }

    private fun lockNavigationDrawer(lockMode: Int) {
        mDrawerLayout!!.setDrawerLockMode(lockMode)
    }

    private fun unlockNavigationDrawer() {
        mDrawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun updateFilterMenuItem(item: MenuItem) {
        val filterItem = menu!!.findItem(R.id.filter_records_list)
        filterItem.title = item.title
        filterItem.icon = item.icon
    }

    /*### Toasts ###*/
    private fun noConnectionToast() {
        toast(R.string.error_no_connection, Toast.LENGTH_LONG)
    }

    private fun finishDownloadShowsToast() {
        if (!showsAreDownloading) {
            toast(R.string.shows_are_ready, Toast.LENGTH_SHORT)
        }
    }

    private fun startDownloadShowsToast() {
        if (!showsAreDownloading) toast(R.string.downloading_shows, Toast.LENGTH_SHORT)
    }

    companion object {
        const val TRY_NEXT_RECORD = "Zkus další"
        private const val ITEM_OFFSET = 6
        private const val VISIBLE_THRESHOLD = 5
        @JvmStatic
        var context: Context? = null
            private set
        private val showManager: ShowManager = BasicShowManager.getInstance()

        fun createRotateAnim(animatedView: View?, toDegrees: Int, duration: Int, infinite: Boolean): Animation {
            val anim: Animation = RotateAnimation(0F, toDegrees.toFloat(),
                    (animatedView!!.width / 2).toFloat(), (animatedView.height / 2).toFloat())
            anim.duration = duration.toLong()
            if (infinite) {
                anim.repeatMode = Animation.INFINITE
            }
            anim.interpolator = LinearInterpolator()
            return anim
        }

        fun errorReportsDialog(context: Context, reports: List<String>) {
            var msg = "Došlo k "
            msg += if (reports.size > 1) "několika chybám." else "chybě."
            val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1) {
                override fun isEnabled(position: Int): Boolean {
                    return false
                }
            }
            adapter.addAll(reports)
            AlertDialog.Builder(context)
                    .setTitle(msg)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setAdapter(adapter, null)
                    .setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }.show()
        }

    }
}