package com.bignerdranch.android.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Gallery {
    @SerializedName("photos")
    private GalleryName mGalleryName;

    public List<GalleryItem> getGalleryItemList() {
        return mGalleryName.getItems();
    }
}

class GalleryName{
    @SerializedName("page")
    private String mPage;
    @SerializedName("photo")
    private List<GalleryItem> mItems;

    public List<GalleryItem> getItems() {
        return mItems;
    }

}
