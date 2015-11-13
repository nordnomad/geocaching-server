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
                            result += (node).outerHtml();
                        else
                            result += (node).outerHtml();
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
}
