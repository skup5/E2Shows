package com.example.roman.testapp;

import android.content.Context;
import android.widget.Adapter;
import android.widget.ListView;


/**
 * Created by Roman on 14.4.2015.
 */
public class RecordsListView extends ListView implements EndlessScrollListener.LoadNextItems{

    //private Category currentCategory;

    public RecordsListView(Context context) {
        super(context);
      //  this.currentCategory = null;
    }

//    public void setCurrentCategory(Category currentCategory) {
//        this.currentCategory = currentCategory;
//    }

    @Override
    public void loadNextItems(){
//        if(currentCategory == null) {
//            return;
//        }

        Adapter adapter = getAdapter();
        if(adapter instanceof RecordsAdapter){
            ((RecordsAdapter) adapter).downloadNext();
        }

    }

}
