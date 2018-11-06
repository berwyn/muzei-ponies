package so.codeweaver.muzei.ponies.util;

/**
 * Created by berwyn on 09/04/15.
 */
public class StringUtils {

    public static String buildDerpibooruTagString(String... tags) {
        if(tags.length == 0) {
            return "";
        } else if(tags.length == 1) {
            return adjustTag(tags[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for(String tag : tags) {
                sb.append(adjustTag(tag));
                sb.append(",");
            }
            return sb.substring(0, sb.lastIndexOf(","));
        }
    }

    private static String adjustTag(String tag) {
        return tag.replace(" ", "+");
    }

}
