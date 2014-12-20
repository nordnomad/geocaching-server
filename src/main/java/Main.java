import static spark.Spark.get;
import static utils.Loader.*;

public class Main {

    public static void main(String[] args) {
        get("/v1/info/:cacheId", (req, res) -> loadInfo(req.params(":cacheId")));
        get("/v1/comments/:cacheId", (req, res) -> loadComments(req.params(":cacheId")));
        get("/v1/images/:cacheId", (req, res) -> loadImages(req.params(":cacheId")));
    }

}
