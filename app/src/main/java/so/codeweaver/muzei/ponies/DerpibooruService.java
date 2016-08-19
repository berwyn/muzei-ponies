package so.codeweaver.muzei.ponies;

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

    String SEARCH_FILTER_RANDOM = "random";
    String SEARCH_ORDER_DESC    = "desc";

    @GET("/search.json")
    @Headers({"User-Agent: muzei-ponies/" + BuildConfig.VERSION_NAME})
    Call<DerpibooruResult> search(@Query("q") String tagString,
                                  @Query("sf") String searchFilter,
                                  @Query("sd") String searchOrder);

}
