package com.example.roman.testapp;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Adapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.roman.testapp.jweb.Category;

import java.util.List;

/**
 * Created by Roman on 29.5.2015.
 */
public class CategoriesAdapter extends BaseExpandableListAdapter {

    private Category[] actualCategories;
    private Category[] archivedCategories;
    private Context context;
    private Category archived;
    private LayoutInflater inflater;

    public CategoriesAdapter(Context context) {
        this.context = context;
        this.archived = new Category(Integer.MAX_VALUE, "Archiv", Category.NO_URL_SITE, Category.NO_URL_SITE){
            @Override
            public String toString() {
                return getName();
            }
        };
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setActualCategories(new Category[0]);
        setArchivedCategories(new Category[0]);
    }

    public void setActualCategories(Category[] actualCategories) {
        this.actualCategories = actualCategories;
    }

    public void setArchivedCategories(Category[] archivedCategories) {
        this.archivedCategories = archivedCategories;
    }

    public boolean isActualCategories(int groupPosition) {
        return groupPosition < actualCategories.length;
    }

    public void prepareToCollapseGroup(int groupPosition) {

    }

    /**
     * Gets the number of groups.
     *
     * @return the number of groups
     */
    @Override
    public int getGroupCount() {
        return archivedCategories.length > 0 ? actualCategories.length + 1 : actualCategories.length;
    }

    /**
     * Gets the number of children in a specified group.
     *
     * @param groupPosition the position of the group for which the children
     *                      count should be returned
     * @return the children count in the specified group
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return isActualCategories(groupPosition) ? 0 : archivedCategories.length;
    }

    /**
     * Gets the data associated with the given group.
     *
     * @param groupPosition the position of the group
     * @return the data child for the specified group
     */
    @Override
    public Object getGroup(int groupPosition) {
        return isActualCategories(groupPosition) ? actualCategories[groupPosition] : archived;
    }

    /**
     * Gets the data associated with the given child within the given group.
     *
     * @param groupPosition the position of the group that the child resides in
     * @param childPosition the position of the child with respect to other
     *                      children in the group
     * @return the data of the child
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return isActualCategories(groupPosition) ? null : archivedCategories[childPosition];
    }

    /**
     * Gets the ID for the group at the given position. This group ID must be
     * unique across groups. The combined ID (see
     * {@link #getCombinedGroupId(long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group for which the ID is wanted
     * @return the ID associated with the group
     */
    @Override
    public long getGroupId(int groupPosition) {
        return isActualCategories(groupPosition) ? actualCategories[groupPosition].getId() : archived.getId();
    }

    /**
     * Gets the ID for the given child within the given group. This ID must be
     * unique across all children within the group. The combined ID (see
     * {@link #getCombinedChildId(long, long)}) must be unique across ALL items
     * (groups and all children).
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group for which
     *                      the ID is wanted
     * @return the ID associated with the child
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return isActualCategories(groupPosition) ? actualCategories[groupPosition].getId() : archivedCategories[childPosition].getId() ;
    }

    /**
     * Indicates whether the child and group IDs are stable across changes to the
     * underlying data.
     *
     * @return whether or not the same ID always refers to the same object
     * @see Adapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * Gets a View that displays the given group. This View is only for the
     * group--the Views for the group's children will be fetched using
     * {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     *
     * @param groupPosition the position of the group for which the View is
     *                      returned
     * @param isExpanded    whether the group is expanded or collapsed
     * @param convertView   the old view to reuse, if possible. You should check
     *                      that this view is non-null and of an appropriate type before
     *                      using. If it is not possible to convert this view to display
     *                      the correct data, this method can create a new view. It is not
     *                      guaranteed that the convertView will have been previously
     *                      created by
     *                      {@link #getGroupView(int, boolean, View, ViewGroup)}.
     * @param parent        the parent that this view will eventually be attached to
     * @return the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
        final TextView textView;
        final ImageView imageView;
        final ExpandableListView par = (ExpandableListView) parent;
        convertView = inflater.inflate(R.layout.categories_list_group, null);
        final View conView = convertView;
        textView = (TextView) convertView.findViewById(R.id.categories_group_header);
        imageView = (ImageView) conView.findViewById(R.id.categories_group_indicator);
        if (!isActualCategories(groupPosition)) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ImageView view = imageView;
                            Log.i("ImageView", "X=" + view.getWidth() + " Y=" + view.getHeight());
                            Animation anim = MainActivity.createRotateAnim(view, 180, 200, false);
                            anim.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    if (isExpanded) {
                                        view.setImageResource(R.drawable.ic_action_expand);
                                    } else {
                                        view.setImageResource(R.drawable.ic_action_collapse);
                                    }
                                    par.performItemClick(textView, groupPosition, getGroupId(groupPosition));
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                            view.startAnimation(anim);
                }
            });
            if(isExpanded){
                imageView.setImageResource(R.drawable.ic_action_collapse);
            } else {
                imageView.setImageResource(R.drawable.ic_action_expand);
            }
        }
        textView.setText(getGroup(groupPosition).toString());
        return convertView;
    }

    /**
     * Gets a View that displays the data for the given child within the given
     * group.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child (for which the View is
     *                      returned) within the group
     * @param isLastChild   Whether the child is the last child within the group
     * @param convertView   the old view to reuse, if possible. You should check
     *                      that this view is non-null and of an appropriate type before
     *                      using. If it is not possible to convert this view to display
     *                      the correct data, this method can create a new view. It is not
     *                      guaranteed that the convertView will have been previously
     *                      created by
     *                      {@link #getChildView(int, int, boolean, View, ViewGroup)}.
     * @param parent        the parent that this view will eventually be attached to
     * @return the View corresponding to the child at the specified position
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView textView;
        convertView = inflater.inflate(R.layout.categories_list_item, null);
        textView = (TextView) convertView.findViewById(R.id.categories_list_item);
        textView.setText(getChild(groupPosition, childPosition).toString());
        Animation animation = AnimationUtils.makeInChildBottomAnimation(context);
//        Interpolator interpolator = new LinearInterpolator();
//        animation = new AlphaAnimation(0,1);
////        animation.setInterpolator(interpolator);
        animation.setDuration(100);
        //animation.scaleCurrentDuration(5*childPosition);
        convertView.setAnimation(animation);
        return convertView;
    }

    /**
     * Whether the child at the specified position is selectable.
     *
     * @param groupPosition the position of the group that contains the child
     * @param childPosition the position of the child within the group
     * @return whether the child is selectable.
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}