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
        try {
            for (int i = 0; i < textElements.size(); i++) {
                Node node = textElements.get(i).parent();
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
                            result += ((TextNode) node).text();
                        else
                            result += ((Element) node).text();
                    }
                }
                jsonObject.put(key, result);
            }

            Elements elements = doc.select("p>b");

            jsonObject.put("name", getTextByIndex(elements, 0));

            JSONObject authorObject = new JSONObject();
            authorObject.put("name", getTextByIndex(elements, 1));
            authorObject.put("id", doc.select("a[href~=profile.php]").get(0).attr("href").split("=")[1]);
            jsonObject.put("author", authorObject);

            jsonObject.put("created", getTextByIndex(elements, 2));
            jsonObject.put("updated", getTextByIndex(elements, 3));
            jsonObject.put("coordinates", getTextByIndex(elements, 4));
            jsonObject.put("country", getTextByIndex(elements, 5));
            jsonObject.put("region", getTextByIndex(elements, 6));
            jsonObject.put("city", getTextByIndex(elements, 7));
            jsonObject.put("difficulty", getTextByIndex(elements, 8));
            jsonObject.put("terrain", getTextByIndex(elements, 9));
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
        if (els.size() < idx) return "";
        if (els.get(idx) != null) return els.get(idx).text();
        return "";
    }
}
