package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlickrFetchr {

    public static final String TAG = "FlickrFetchr";
    // method and page gone
    public static final String API_KEY = "ecc2d0069ba8b6cfba5011e31f354906";
    public static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    public static final String SEARCH_METHOD = "flickr.photos.search";
    public static final Uri ENDPOINT = Uri
            .parse("https://www.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(String page){
        String url = buildUrl(FETCH_RECENTS_METHOD, page,null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String page, String query){
        String url = buildUrl(SEARCH_METHOD, page, query);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            Gson gson = new Gson();
            Gallery gallery = gson.fromJson(jsonString, Gallery.class);
            items = gallery.getGalleryItemList();
            checkForNull(items);

        } catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }


    private String buildUrl(String method, String page, String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("page", page);

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }

        return uriBuilder.build().toString();
    }

    private void checkForNull(List<GalleryItem> items) {
        Iterator<GalleryItem> i = items.iterator();
        while(i.hasNext()){
            GalleryItem s = i.next();
            if (s.getUrl() == null){
                i.remove();
            }
        }
    }
}
