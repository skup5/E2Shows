package com.example.roman.testapp;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

/**
 * Created by Roman on 27.5.2015.
 */
public class MyRecyclerScrollListener extends OnScrollListener {

    private int visibleThreshold,
                visibleItemCount,
                totalItemCount,
                pastVisiblesItems;
    private EndlessScrollListener.LoadNextItems loader;

    public MyRecyclerScrollListener(EndlessScrollListener.LoadNextItems loader) {
        this(loader, 1);
    }

    public MyRecyclerScrollListener(EndlessScrollListener.LoadNextItems loader, int visibleThreshold) {
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
                //load more list items:
                loader.loadNextItems();
            }

    }
}
