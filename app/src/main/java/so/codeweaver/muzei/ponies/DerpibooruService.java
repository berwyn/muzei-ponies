package so.codeweaver.muzei.ponies;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by berwyn on 09/04/15.
 */
public interface DerpibooruService {

    public static final String PREF_TAGS  = "derpibooru.tagString";
    public static final String PREF_DELAY = "derpibooru.delay";

    public static final String SEARCH_FILTER_RANDOM = "random";
    public static final String SEARCH_ORDER_DESC    = "desc";

    @GET("/search.json")
    public DerpibooruResult search(@Query("q") String tagString,
                                   @Query("sf") String searchFilter,
                                   @Query("sd") String searchOrder);

}
