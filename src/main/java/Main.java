import java.util.Arrays;
import java.util.Date;

import static java.lang.String.format;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.SparkBase.port;
import static utils.Loader.*;

public class Main {

    public static void main(String[] args) {
        port(Integer.parseInt(System.getenv("PORT")));
        get("/v1/about", (req, res) -> new Date());
        get("/v1/info/:cacheId", (req, res) -> loadInfo(req.params(":cacheId")));
        get("/v0/info/:cacheId", (req, res) -> load(format(INFO_URL, req.params(":cacheId"))));
        get("/v1/comments/:cacheId", (req, res) -> loadComments(req.params(":cacheId")));
        get("/v1/images/:cacheId", (req, res) -> loadImages(req.params(":cacheId")));
        get("/v1/fullGeoCache/:cacheId", (req, res) -> loadFullGeoCache(req.params(":cacheId")));
        get("/v1/fullInfo/:rect/:exclude", (req, res) -> {
            String[] rect = req.params(":rect").split("_");
            String[] excludedCaches = req.params(":exclude").split("_");
            return loadFullData(rect, excludedCaches);
        });

        exception(Exception.class, (e, req, resp)-> resp.body(Arrays.toString(e.getStackTrace())));
    }

}
