package com.zy.videocoverseletor;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zy.videocoverselector.view.ShortVideoSelectCover;

public class CoverSelectorActivity extends AppCompatActivity {

    private ShortVideoSelectCover mUGCKitVideoCut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cover_selector);
        initDataObserver();
    }

    public void initDataObserver() {
        mUGCKitVideoCut = findViewById(R.id.video_cutter_layout);
        final ImageView mCoverImg = findViewById(R.id.iv_cover);
        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUGCKitVideoCut.getCover();
            }
        });
        mUGCKitVideoCut.setOnGetSampleImageListener(new ShortVideoSelectCover.OnGetSampleImageListener() {
            @Override
            public void start() {

            }

            @Override
            public void complete(Bitmap img) {
                mCoverImg.setImageBitmap(img);
            }
        });
    }

}
