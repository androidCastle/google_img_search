package com.imagegrid.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.imagegrid.R;
import com.imagegrid.helpers.ZoomableImageView;

public class DisplayImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_img);
        ZoomableImageView fullImgVw = findViewById(R.id.activityDisplayImgIV);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String url = bundle.getString("IMAGE_URL");
            if (url != null) {
                Glide.with(DisplayImageActivity.this)
                        .load(url)
                        .into(fullImgVw);
            }
        }
        fullImgVw.setMaxZoom(4f);
    }
}