package so.codeweaver.muzei.ponies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.gson.Gson;

import java.util.Random;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class PonyArtService extends RemoteMuzeiArtSource {

    DerpibooruService service;
    Random rand;

    public PonyArtService() {
        super("PonyArtService");

        service = new RestAdapter.Builder()
                .setEndpoint("http://derpiboo.ru")
                .setClient(new OkClient())
                .setConverter(new GsonConverter(new Gson()))
                .build()
                .create(DerpibooruService.class);

        rand = new Random();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int i) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : "";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tagString = prefs.getString(DerpibooruService.PREF_TAGS, "safe,wallpaper,score.gte:300");

        DerpibooruResult res;
        try {
            res = service.search(tagString, DerpibooruService.SEARCH_FILTER_RANDOM, DerpibooruService.SEARCH_ORDER_DESC);
        } catch(RetrofitError e) {
            throw new RetryException();
        }

        if(res.total < 1) throw new RetryException();

        Artwork art = null;
        do {
            int idx = rand.nextInt(res.search.length);
            DerpibooruResult.Image image = res.search[idx];
            if(currentToken.equals(image.id)) continue;
            art = new Artwork.Builder()
                    .title("#" + image.idNumber)
                    .byline(getString(R.string.uploaderName, image.uploader))
                    .imageUri(Uri.parse("http:" + image.image))
                    .token(image.id)
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://derpiboo.ru/" + image.idNumber)))
                    .build();
        } while(art == null);

        String delayString = prefs.getString(DerpibooruService.PREF_DELAY, "60000"); // Defaults to one minute
        int delayInMillis = Integer.parseInt(delayString, 10);
        publishArtwork(art);
        scheduleUpdate(System.currentTimeMillis() + delayInMillis);
    }
}
