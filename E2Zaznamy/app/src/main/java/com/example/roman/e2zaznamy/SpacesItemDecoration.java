package com.example.roman.e2zaznamy;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * A ItemDecoration for RecyclerView that use spaces like divider between items.
 *
 * @author Roman Zelenik
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = space;
        outRect.bottom = space;
    }
}
