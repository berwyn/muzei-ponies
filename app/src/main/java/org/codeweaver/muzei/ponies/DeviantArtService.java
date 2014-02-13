package org.codeweaver.muzei.ponies;

import com.google.gson.annotations.SerializedName;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by jamisongreeley on 12/2/14.
 */
public interface DeviantArtService {

    @GET("/oembed")
    Deviation getDeviation(@Query("url") String url);

    public static class Deviation {
        public String title;
        public String url;
        @SerializedName("author_name")
        public String author;
    }
}
