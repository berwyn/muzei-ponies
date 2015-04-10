package so.codeweaver.muzei.ponies;

import com.google.gson.annotations.SerializedName;

/**
 * Created by berwyn on 09/04/15.
 */
public class DerpibooruResult {

    public int     total;
    public Image[] search;

    public class Image {
        public String id;
        public String image;
        @SerializedName("id_number")
        public long   idNumber;
        public String uploader;
        public String tags;
    }

}
