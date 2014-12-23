package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static java.lang.String.format;

public class Loader {

    static String PDA_GEOCACHING = "http://pda.geocaching.su";
    static String INFO_URL = PDA_GEOCACHING + "/cache.php?cid=%s&mode=0";
    static String COMMENTS_URL = PDA_GEOCACHING + "/note.php?cid=%s&mode=0";
    static String IMAGES_URL = PDA_GEOCACHING + "/pict.php?cid=%s&mode=0";

    public static JSONObject loadInfo(String cacheId) {
        return Parser.info(load(format(INFO_URL, cacheId)));
    }

    public static JSONArray loadComments(String cacheId) {
        return Parser.comments(load(format(COMMENTS_URL, cacheId)));
    }

    public static JSONArray loadImages(String cacheId) {
        return Parser.images(load(format(IMAGES_URL, cacheId)));
    }

    public static JSONArray loadFullData(String[] rect, String[] excludedCaches) throws JSONException {
        List<String> excludedIds = Arrays.asList(excludedCaches);
        String url = "http://www.geocaching.su/pages/1031.ajax.php?lngmax=" + rect[0] + "&lngmin=" + rect[1] + "&latmax=" + rect[2] + "&latmin=" + rect[3] + "&id=12345678&geocaching=5767e405a17c4b0e1cbaecffdb93475d&exactly=1";
        JSONArray caches = XML.toJSONObject(load(url)).getJSONObject("data").getJSONArray("c");
        JSONArray result = new JSONArray();
        for (int i = 0; i < caches.length(); i++) {
            JSONObject obj = caches.getJSONObject(i);
            String cacheId = obj.getString("id");
            if (!excludedIds.contains(cacheId)) {
                obj.put("comments", loadComments(cacheId));
                obj.put("images", loadImages(cacheId));
                obj.put("info", loadInfo(cacheId));
                result.put(obj);
            }
        }
        return result;
    }

    public static String load(String url) {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader in = null;
        try {
            InputStreamReader inputStreamReader = getInputSteamReader(new URL(url));
            in = new BufferedReader(inputStreamReader);
            int size;
            while ((size = in.read(buffer)) != -1) {
                html.append(buffer, 0, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String resultHtml = html.toString();
        resultHtml = resultHtml.replace("windows-1251", "UTF-8");
        resultHtml = resultHtml.replaceAll("\\r|\\n", "");
        return resultHtml;
    }

    private static InputStreamReader getInputSteamReader(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip;q=1.0, identity;q=0.5, *;q=0");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Can't connect to geocaching.su. Response: " + connection.getResponseCode());
        }
        InputStream inputStream = connection.getInputStream();
        if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
            inputStream = new GZIPInputStream(inputStream);
        }
        String charset = getCharsetFromContentType(connection.getContentType());

        return new InputStreamReader(inputStream, charset);
    }

    private static String getCharsetFromContentType(String contentType) {
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.toLowerCase().startsWith("charset=")) {
                    return param.split("=", 2)[1];
                }
            }
        }
        return "windows-1251";
    }
}
