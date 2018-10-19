package so.codeweaver.muzei.ponies;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by berwyn on 09/04/15.
 */
public interface DerpibooruService {
    String PREF_TAGS  = "derpibooru.tagString";
    String PREF_DELAY = "derpibooru.delay";
    String PREF_KEY = "derpibooru.userKey";

    String SEARCH_FILTER_RANDOM = "random";
    String SEARCH_ORDER_DESC    = "desc";

    String DEFAULT_TAGS = "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080";

    @GET("/search.json")
    @Headers({"User-Agent: muzei-ponies/" + BuildConfig.VERSION_NAME})
    Call<DerpibooruResult> search(
            @NonNull @Query("q") String tagString,
            @NonNull @Query("sf") String searchFilter,
            @NonNull @Query("sd") String searchOrder,
            @Nullable @Query("key") String key
    );
}
