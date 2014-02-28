package org.codeweaver.muzei.ponies;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

public class PonyArtService extends RemoteMuzeiArtSource {

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
        MyLittleWallpaperService.Wallpaper wall;
        try {
            wall = mlw.getRandom(null);
        } catch(RetrofitError e) {
            throw new RetryException();
        }

        if(wall.amount < 1) throw new RetryException();

        Artwork art = null;
        for(MyLittleWallpaperService.WallpaperResult wallpaper : wall.result) {
            if(currentToken.equals(wallpaper.imageID)) continue;
            if(wallpaper.url.contains("deviantart")) {
                DeviantArtService.Deviation deviation;
                try {
                    deviation = deviantart.getDeviation(wallpaper.url);
                } catch(RetrofitError e) {
                    throw new RetryException();
                }
                art = new Artwork.Builder()
                        .title(deviation.title)
                        .byline(deviation.author)
                        .imageUri(Uri.parse(deviation.url))
                        .token(wallpaper.imageID)
                        .viewIntent(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(wallpaper.url)))
                        .build();
                break;
            }
        }

        if(art == null) throw new RetryException();
        int delayInMillis = 60 * 1000; // One minute!
        publishArtwork(art);
        scheduleUpdate(System.currentTimeMillis() + delayInMillis);
    }
}
