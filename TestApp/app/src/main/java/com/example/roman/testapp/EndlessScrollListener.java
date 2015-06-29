package com.example.roman.testapp;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 *
 * @author Roman Zelenik
 */
public class EndlessScrollListener extends OnScrollListener {

    private int visibleThreshold,
                visibleItemCount,
                totalItemCount,
                pastVisiblesItems;
    private LoadNextItems loader;

    public EndlessScrollListener(LoadNextItems loader) {
        this(loader, 1);
    }

    public EndlessScrollListener(LoadNextItems loader, int visibleThreshold) {
        this.loader = loader;
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        visibleItemCount = layoutManager.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

        if ((visibleItemCount + pastVisiblesItems) >= (totalItemCount - visibleThreshold)) {
            loader.loadNextItems();
        }
    }

    interface LoadNextItems{
        void loadNextItems();
    }
}
