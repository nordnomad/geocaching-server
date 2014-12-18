import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static spark.Spark.get;

public class Main {
    public static void main(String[] args) {

        get("/hello", "application/json", (request, response) -> {
            StringBuilder html = new StringBuilder();
            char[] buffer = new char[1024];
            BufferedReader in = null;
            try {
                InputStreamReader inputStreamReader = getInputSteamReader(new URL("http://pda.geocaching.su/note.php?cid=8817&mode=0"));
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

            JSONArray jsonArray = new JSONArray();

            Document doc = Jsoup.parse(resultHtml);
            Elements dateElements = doc.select("b + i");
            Elements userElements = doc.select("b > u");
            try {
                for (int i = 0; i < dateElements.size(); i++) {
                    Element node = dateElements.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("date", node.text().trim().replace("от ", ""));
                    jsonObject.put("message", node.nextSibling().nextSibling().toString().trim());
                    jsonObject.put("user", userElements.get(i).text().trim());
                    jsonArray.put(jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonArray;
        });
    }

    public static InputStreamReader getInputSteamReader(URL url) throws IOException {
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

    public static String getCharsetFromContentType(String contentType) {
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
