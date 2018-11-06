package so.codeweaver.muzei.ponies.derpi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import so.codeweaver.muzei.ponies.BuildConfig;
import so.codeweaver.muzei.ponies.derpi.DerpibooruResult;

/**
 * Created by berwyn on 09/04/15.
 */
public interface DerpibooruService {
    String PREF_TAGS  = "derpibooru.tagString";
    String PREF_DELAY = "derpibooru.delay";
    String PREF_KEY = "derpibooru.userKey";

    String SORT_FORMAT_RANDOM = "random";
    String SORT_FORMAT_RELEVANCE = "relevance";
    String SORT_FORMAT_SCORE = "score";
    String SORT_FORMAT_COMMENTS = "comments";
    String SORT_FORMAT_HEIGHT = "height";
    String SORT_FORMAT_CREATED_AT = "created_at";

    String SORT_DIRECTION_DESC = "desc";
    String SORT_DIRECTION_ASC = "asc";

    String DEFAULT_TAGS = "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080";

    @GET("/search.json")
    @Headers({"User-Agent: muzei-ponies/" + BuildConfig.VERSION_NAME})
    Call<DerpibooruResult> search(
            @NonNull @Query("q") String tagString,
            @NonNull @Query("sf") String searchFilter,
            @NonNull @Query("sd") String searchOrder,
            @Nullable @Query("key") String key,
            @Nullable @Query("perpage") Integer count
    );
}
