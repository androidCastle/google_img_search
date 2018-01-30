package com.imagegrid.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.imagegrid.R;
import com.imagegrid.pojos.SearchResultPOJO;

import java.util.List;

public class GridImageAdapter extends ArrayAdapter<SearchResultPOJO> {

    private int columns;

    public GridImageAdapter(Context context, List<SearchResultPOJO> searchResultPOJOList, int columns) {
        super(context, android.R.layout.simple_list_item_1, searchResultPOJOList);
        this.columns = columns;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        SearchResultPOJO searchResultPOJO = getItem(position);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_grid_image, parent, false);
        }
        ImageView imgGridItem = convertView.findViewById(R.id.itemGridImgVw);
        if (searchResultPOJO != null) {
            try {
                Glide.with(getContext())
                        .asBitmap()
                        .load(searchResultPOJO.getImage().getThumbnailLink())
                        .into(imgGridItem);
                DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int dimen = (screenWidth / columns);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimen, dimen);
                layoutParams.gravity = Gravity.CENTER;
                imgGridItem.setLayoutParams(layoutParams);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return convertView;
    }

    public void updateColumns(int num) {
        this.columns = num;
    }
}