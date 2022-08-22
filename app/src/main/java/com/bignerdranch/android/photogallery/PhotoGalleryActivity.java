package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;


public class PhotoGalleryActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryFragment.class);
    }

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}