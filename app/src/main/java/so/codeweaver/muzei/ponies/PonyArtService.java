package so.codeweaver.muzei.ponies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.io.IOException;
import java.util.Random;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import timber.log.Timber;

public class PonyArtService extends RemoteMuzeiArtSource {

    DerpibooruService service;
    Random rand;

    public PonyArtService() {
        super("PonyArtService");

        OkHttpClient client = new OkHttpClient.Builder().build();

        service = new Retrofit.Builder()
                .baseUrl("https://derpibooru.org")
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
        switch (i) {
            case UPDATE_REASON_INITIAL:
                Timber.d("Waking for initial wallpaper");
                break;
            case UPDATE_REASON_USER_NEXT:
                Timber.d("Waking because user has requested a new wallpaper");
                break;
            case UPDATE_REASON_SCHEDULED:
                Timber.d("Waking for scheduled update");
                break;
            default:
            case UPDATE_REASON_OTHER:
                Timber.d("Waking for unknown reason");
                break;
        }

        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : "";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String tagString = prefs.getString(DerpibooruService.PREF_TAGS, "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080");
        String keyString = prefs.getString(DerpibooruService.PREF_KEY, null);

        Call<DerpibooruResult> call = service.search(
                tagString,
                DerpibooruService.SORT_FORMAT_RANDOM,
                DerpibooruService.SORT_DIRECTION_DESC,
                keyString,
                20
        );
        Response<DerpibooruResult> resp;

        try {
            resp = call.execute();
        } catch (IOException e) {
            Timber.e(e);
            throw new RetryException();
        }

        DerpibooruResult res = resp.body();
        if (res.getTotal() < 1) {
            Timber.w("Query of %1s came back with no results", tagString);
            throw new RetryException();
        } else {
            Timber.w("Query %s had %d results", tagString, res.getSearch().length);
        }

        Artwork art = null;
        do {
            int idx = rand.nextInt(res.getSearch().length);
            DerpibooruImage image = res.getSearch()[idx];
            if (currentToken.equals(image.getId())) continue;
            art = new Artwork.Builder()
                    .title("#" + image.getId())
                    .byline(getString(R.string.uploaderName, image.getUploader()))
                    .imageUri(Uri.parse("https:" + image.getImage()))
                    .token(image.getId())
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://derpibooru.org/" + image.getId())))
                    .build();
        } while (art == null);

        publishArtwork(art);
        String delayString = prefs.getString(DerpibooruService.PREF_DELAY, "86400000"); // Defaults to one day
        scheduleUpdate(System.currentTimeMillis() + Long.parseLong(delayString, 10));

    }
}
