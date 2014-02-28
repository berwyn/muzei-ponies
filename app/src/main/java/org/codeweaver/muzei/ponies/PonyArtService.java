package org.codeweaver.muzei.ponies;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.squareup.okhttp.OkHttpClient;

import java.net.HttpURLConnection;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class PonyArtService extends RemoteMuzeiArtSource {

    Config config;
    MyLittleWallpaperService mlw;
    DeviantArtService deviantart;

    public PonyArtService() {
        super("PonyArtService");

        mlw = new RestAdapter.Builder()
                .setEndpoint("http://www.mylittlewallpaper.com/")
                .build()
                .create(MyLittleWallpaperService.class);
        deviantart = new RestAdapter.Builder()
                .setEndpoint("http://backend.deviantart.com")
                .build()
                .create(DeviantArtService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int i) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;
        MyLittleWallpaperService.Wallpaper wall = null;
        try {
            wall = mlw.getRandom(null);
        } catch(RetrofitError e) {
            throw new RetryException();
        }

        if(wall.amount < 1) throw new RetryException();

        Artwork art = null;
        for(MyLittleWallpaperService.WallpaperResult wallpaper : wall.result) {
            if(wallpaper.url.contains("deviantart")) {
                DeviantArtService.Deviation deviation = null;
                try {
                    deviation = deviantart.getDeviation(wallpaper.url);
                } catch(RetrofitError e) {
                    throw new RetryException();
                }
                art = new Artwork.Builder()
                        .title(deviation.title)
                        .byline(deviation.author)
                        .imageUri(Uri.parse(deviation.url))
                        .token(wall.result[0].imageID)
                        .viewIntent(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(wall.result[0].url)))
                        .build();
                break;
            }
        }

        if(art == null) throw new RetryException();
        publishArtwork(art);
        scheduleUpdate(System.currentTimeMillis() + (10*1000));
    }

    enum Source {
        DEVIANTART;
    }
}
