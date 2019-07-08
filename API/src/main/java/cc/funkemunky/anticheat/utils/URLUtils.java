package cc.funkemunky.anticheat.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author ieb
 * @version $Rev: 28982 $
 * @since Sakai 2.4
 */

public class URLUtils {

    public static String addParameter(String URL, String name, String value) {
        int qpos = URL.indexOf('?');
        int hpos = URL.indexOf('#');
        char sep = qpos == -1 ? '?' : '&';
        String seg = sep + encodeUrl(name) + '=' + encodeUrl(value);
        return hpos == -1 ? URL + seg : URL.substring(0, hpos) + seg
                + URL.substring(hpos);
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException(uee);
        }
    }

}