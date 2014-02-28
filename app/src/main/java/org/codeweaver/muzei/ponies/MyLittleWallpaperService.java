package org.codeweaver.muzei.ponies;

import com.google.gson.annotations.SerializedName;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by jamisongreeley on 12/2/14.
 */
public interface MyLittleWallpaperService {

    @GET("/api/v1/random.json")
    Wallpaper getRandom(@Query("search")String tags) throws RetrofitError;

    public static class Wallpaper {
        @SerializedName("search_tags")
        public String[] searchTags;
        public int amount;
        public WallpaperResult[] result;
    }

    public static class WallpaperResult {
        public String title;
        @SerializedName("imageid")
        public String imageID;
        @SerializedName("downloadurl")
        public String url;
    }
}
