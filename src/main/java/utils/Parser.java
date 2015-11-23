package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class Parser {
    public static JSONArray comments(String html) {
        Document doc = Jsoup.parse(html);
        Elements dateElements = doc.select("b + i");
        Elements userElements = doc.select("b > u");
        JSONArray jsonArray = new JSONArray();
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
    }

    public static JSONObject info(String html) {
        Document doc = Jsoup.parse(html);

        Elements textElements = doc.select("b u");
        JSONObject jsonObject = new JSONObject();
        JSONArray checkpoints = new JSONArray();
        try {
            for (Object textElement : textElements) {
                Node node = ((Node) textElement).parent();
                String key = "";
                switch (((Element) node).text()) {
                    case "Атрибуты":
                        key = "attributes";
                        break;
                    case "Описание тайника":
                        key = "description";
                        break;
                    case "Описание окружающей местности":
                        key = "surroundingArea";
                        break;
                    case "Содержимое тайника":
                        key = "content";
                        break;
                }
                String result = "";
                while (node != null && !node.nodeName().equalsIgnoreCase("hr")) {
                    node = node.nextSibling();
                    if (node != null) {
                        if (node instanceof TextNode)
                            result += (node).outerHtml();
                        else
                            result += (node).outerHtml();
                    }
                }
                result = result.trim();
                if (result.startsWith("<br>")) result = result.substring(4).trim();
                if (result.endsWith("<hr>")) result = result.substring(0, result.length() - 4).trim();
                if (result.endsWith("<p> <br>&nbsp;<br></p><p></p>"))
                    result = result.substring(0, result.length() - 29).trim();
                JSONArray chp = findCheckpoints(result);
                for (int j = 0; j < chp.length(); j++) {
                    checkpoints.put(chp.getJSONObject(j));
                }
                result = insertCheckpointsLink(result);
                jsonObject.put(key, result);
            }
            jsonObject.put("checkpoints", checkpoints);

            Elements elements = doc.select("p>b");

            jsonObject.put("name", getTextByIndex(elements, 0));

            JSONObject authorObject = new JSONObject();
            authorObject.put("name", getTextByIndex(elements, 1));
            authorObject.put("id", doc.select("a[href~=profile.php]").get(0).attr("href").split("=")[1]);
            jsonObject.put("author", authorObject);
            int i = 2;
            switch (getTextByIndex(elements, i)) {
                case "Тайник в зимний период НЕДОСТУПЕН":
                case "Традиционная часть тайника в зимний период недоступна":
                    //jsonObject.put("availability", getTextByIndex(elements, i));
                    i++;
            }
            jsonObject.put("created", getTextByIndex(elements, i++));
            jsonObject.put("updated", getTextByIndex(elements, i++));
            jsonObject.put("coordinates", getTextByIndex(elements, i++));
            jsonObject.put("country", getTextByIndex(elements, i++));
            jsonObject.put("region", getTextByIndex(elements, i++));
            jsonObject.put("city", getTextByIndex(elements, i++));
            jsonObject.put("difficulty", getTextByIndex(elements, i++));
            jsonObject.put("terrain", getTextByIndex(elements, i));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static JSONArray images(String html) {
        JSONArray jsonArray = new JSONArray();
        Document doc = Jsoup.parse(html);

        Elements linkElements = doc.select("a[href~=(?i)\\.(jpe?g)]");
        try {
            for (Element el : linkElements) {
                JSONObject jsonObject = new JSONObject();
                String href = el.attr("href").trim();
                if (href.contains("/caches/")) {
                    jsonObject.put("caches", href);
                    jsonObject.put("thumbnails", href.replace("/caches/", "/caches/thumbnails/"));
                } else if (href.contains("/areas/")) {
                    jsonObject.put("areas", href);
                    jsonObject.put("thumbnails", href.replace("/areas/", "/areas/thumbnails/"));
                }
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    private static String getTextByIndex(Elements els, int idx) {
        if (els.size() <= idx) return "";
        if (els.get(idx) != null) return els.get(idx).text();
        return "";
    }

    private static final String degrees = "(?:<sup>(?:&#9702;|0|o|O)</sup>|\\s+|&#176;|&deg;|&nbsp;|\\D{2,6}|градусов)";
    private static final String minutes = "(?:&rsquo;|'|&#39;|мин)";
    private static final String delimiter = "[,\\.]";
    private static final String coordinatePattern = "(\\d+)\\s*" + degrees + "\\s*(\\d+)\\s*" + delimiter + "\\s*(\\d+)";
    private static final Pattern geoPattern = Pattern.compile("[N|S]?\\s*" + coordinatePattern + "\\D+" + coordinatePattern + "\\s*" + minutes + "?");

    public static String insertCheckpointsLink(String text) {
        if (text == null) throw new IllegalArgumentException("text is null");

        text = text.replace("</strong><strong>", ""); // prepare string. actually this is server side responsibility

        Matcher pageMatcher = geoPattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (pageMatcher.find()) {
            int latitudeE6;
            int longitudeE6;
            try {
                int degrees = parseInt(pageMatcher.group(1));
                int minutes = parseInt(pageMatcher.group(2));
                double milliMinutes = parseDouble("." + pageMatcher.group(3));
                latitudeE6 = new Sexagesimal(degrees, (double) minutes + milliMinutes).toCoordinateE6();

                degrees = parseInt(pageMatcher.group(4));
                minutes = parseInt(pageMatcher.group(5));
                milliMinutes = Float.parseFloat("." + pageMatcher.group(6));
                longitudeE6 = new Sexagesimal(degrees, (double) minutes + milliMinutes).toCoordinateE6();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            pageMatcher.appendReplacement(sb, String.format("<a href=\"geo:%d,%d\" style=\"color: rgb(86,144,93)\"><b>%s</b></a>", latitudeE6, longitudeE6, pageMatcher.group()));
        }

        pageMatcher.appendTail(sb);
        return sb.toString();
    }

    public static JSONArray findCheckpoints(String text) {
        if (text == null) throw new IllegalArgumentException("text is null");

        text = text.replace("</strong><strong>", ""); // prepare string. actually this is server side responsibility

        Matcher pageMatcher = geoPattern.matcher(text);
        JSONArray result = new JSONArray();
        while (pageMatcher.find()) {
            int latitudeE6;
            int longitudeE6;
            try {
                int degrees = parseInt(pageMatcher.group(1));
                int minutes = parseInt(pageMatcher.group(2));
                double milliMinutes = parseDouble("." + pageMatcher.group(3));
                latitudeE6 = new Sexagesimal(degrees, (double) minutes + milliMinutes).toCoordinateE6();

                degrees = parseInt(pageMatcher.group(4));
                minutes = parseInt(pageMatcher.group(5));
                milliMinutes = Float.parseFloat("." + pageMatcher.group(6));
                longitudeE6 = new Sexagesimal(degrees, (double) minutes + milliMinutes).toCoordinateE6();
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            JSONObject checkpoint = new JSONObject();
            checkpoint.put("lat", latitudeE6);
            checkpoint.put("lon", longitudeE6);
            checkpoint.put("name", pageMatcher.group());
            result.put(checkpoint);
        }

        return result;
    }
}
