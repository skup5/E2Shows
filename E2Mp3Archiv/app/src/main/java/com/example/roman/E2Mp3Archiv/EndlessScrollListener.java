package com.example.roman.E2Mp3Archiv;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 * A Listener of endless scroll list for RecyclerView.
 *
 * @author Roman Zelenik
 */
public class EndlessScrollListener extends OnScrollListener {

    private int visibleThreshold,
                visibleItemCount,
                totalItemCount,
                pastVisibleItems;
    private LoadNextItems loader;

    public EndlessScrollListener(LoadNextItems loader, int visibleThreshold) {
        this.loader = loader;
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        visibleItemCount = layoutManager.getChildCount();
        totalItemCount = layoutManager.getItemCount();
        pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

        if ((visibleItemCount + pastVisibleItems) >= (totalItemCount - visibleThreshold)) {
            loader.loadNextItems();
        }
    }

    interface LoadNextItems{
        void loadNextItems();
    }
}
