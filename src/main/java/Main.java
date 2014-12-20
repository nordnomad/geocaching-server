import static spark.Spark.get;
import static spark.SparkBase.port;
import static utils.Loader.*;

public class Main {

    public static void main(String[] args) {
        port(Integer.parseInt(System.getenv("PORT")));
        get("/v1/info/:cacheId", (req, res) -> loadInfo(req.params(":cacheId")));
        get("/v1/comments/:cacheId", (req, res) -> loadComments(req.params(":cacheId")));
        get("/v1/images/:cacheId", (req, res) -> loadImages(req.params(":cacheId")));
    }

}
