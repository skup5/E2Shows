package com.example.roman.testapp;

import android.widget.AbsListView;

/**
 * Created by Roman on 14.4.2015.
 */
public class EndlessScrollListener implements AbsListView.OnScrollListener {

    private int visibleThreshold;
    private int currentPage = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private LoadNextItems loader;

    public EndlessScrollListener(LoadNextItems loader) {
        this(loader, 1);
    }

    public EndlessScrollListener(LoadNextItems loader ,int visibleThreshold) {
        this.loader = loader;
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        if (loading) {
//            if (totalItemCount > previousTotal) {
//                loading = false;
//                previousTotal = totalItemCount;
//                currentPage++;
//            }
//        }
//        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
//            // I load the next page of gigs using a background task,
//            // but you can call any function here.
//            loading = true;
//            loader.loadNextItems();
//        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if (view.getLastVisiblePosition() >= view.getCount() - 1 - visibleThreshold) {
                currentPage++;
                //load more list items:
                loader.loadNextItems();
            }
        }
    }

    interface LoadNextItems{
        public void loadNextItems();
    }
}