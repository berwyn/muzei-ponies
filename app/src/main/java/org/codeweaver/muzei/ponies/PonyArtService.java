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

import retrofit.RestAdapter;

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
        Config config = new Config();
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;
        MyLittleWallpaperService.Wallpaper wall = mlw.getRandom();

        if(wall.result.length < 1) {
            Log.w("Muzei Poniz", "No wall returned");
            throw new RetryException();
        }

        DeviantArtService.Deviation deviation = deviantart.getDeviation(wall.result[0].url);

        publishArtwork(new Artwork.Builder()
                .title(deviation.title)
                .byline(deviation.author)
                .imageUri(Uri.parse(deviation.url))
                .token(wall.result[0].imageID)
                .viewIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(wall.result[0].url)))
                .build());
        scheduleUpdate(10*1000);
    }
}
