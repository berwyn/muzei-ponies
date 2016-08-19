package so.codeweaver.muzei.ponies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.io.IOException;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class PonyArtService extends RemoteMuzeiArtSource {

    DerpibooruService service;
    Random rand;

    public PonyArtService() {
        super("PonyArtService");

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Log.d(this.getClass().getSimpleName(), chain.request().header("User-Agent"));
                    Log.d(this.getClass().getSimpleName(), chain.request().url().toString());
                    return chain.proceed(chain.request());
                })
                .build();

        service = new Retrofit.Builder()
                .baseUrl("http://derpibooru.org")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
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
        String tagString = prefs.getString(DerpibooruService.PREF_TAGS, "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080");

        Call<DerpibooruResult> call = service.search(tagString, DerpibooruService.SEARCH_FILTER_RANDOM, DerpibooruService.SEARCH_ORDER_DESC);
        Response<DerpibooruResult> resp;

        try {
            resp = call.execute();
        } catch (IOException e) {
            throw new RetryException();
        }

        DerpibooruResult res = resp.body();
        if(res.total < 1) throw new RetryException();

        Artwork art = null;
        do {
            int idx = rand.nextInt(res.search.length);
            DerpibooruResult.Image image = res.search[idx];
            if(currentToken.equals(image.id)) continue;
            art = new Artwork.Builder()
                    .title("#" + image.id)
                    .byline(getString(R.string.uploaderName, image.uploader))
                    .imageUri(Uri.parse("https:" + image.image))
                    .token(image.id)
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://derpibooru.org/" + image.id)))
                    .build();
        } while(art == null);

        String delayString = prefs.getString(DerpibooruService.PREF_DELAY, "86400000"); // Defaults to one day
        int delayInMillis = Integer.parseInt(delayString, 10);
        publishArtwork(art);
        scheduleUpdate(System.currentTimeMillis() + delayInMillis);
    }
}
