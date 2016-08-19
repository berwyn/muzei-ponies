package so.codeweaver.muzei.ponies;

/**
 * Created by berwyn on 09/04/15.
 */
public final class DerpibooruResult {

    public int     total;
    public Image[] search;

    public static final class Image {
        public String id;
        public String image;
        public String uploader;
        public String tags;
    }

}
