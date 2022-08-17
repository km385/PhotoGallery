package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    public static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int mCurrentPage = 1;
    private int mColumnCount = 3;
    private PhotoAdapter mAdapter;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static Fragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute(String.valueOf(mCurrentPage));

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                (target, thumbnail) -> {
                    Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                    target.bindDrawable(drawable);
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);

        mPhotoRecyclerView.addOnScrollListener(new MyOnScrollListener());
        GridLayoutManager manager = new GridLayoutManager(getActivity(), mColumnCount);
        ViewTreeObserver observer = mPhotoRecyclerView.getViewTreeObserver();
        if (observer.isAlive()){
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPhotoRecyclerView.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                    mColumnCount = mPhotoRecyclerView.getWidth() / 360;
                    manager.setSpanCount(mColumnCount);
                }
            });
        }
        mPhotoRecyclerView.setLayoutManager(manager);
        setupAdapter();

        return v;

    }

    private void updateUI(){
        if (isAdded()){
            mAdapter.setGalleryItems(mItems);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private void setupAdapter() {
        if (isAdded()){
            mAdapter = new PhotoAdapter(mItems);
            mPhotoRecyclerView.setAdapter(mAdapter);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
        }

        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }

    public class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        public void setGalleryItems(List<GalleryItem> items){
            mGalleryItems = items;
        }
    }

    public class MyOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(!recyclerView.canScrollVertically(1)){
                Log.d(TAG,"cannot be scrolled down any more");
                mCurrentPage++;
                new FetchItemsTask().execute(String.valueOf(mCurrentPage));
            }
        }
    }

    public class FetchItemsTask extends AsyncTask<String, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(String... strings) {
            return new FlickrFetchr().fetchItems(strings[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems.addAll(items);

            updateUI();
        }
    }
}
