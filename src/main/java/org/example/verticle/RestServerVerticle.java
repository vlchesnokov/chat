package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router httpRouter = Router.router(vertx);
        httpRouter.route().handler(BodyHandler.create());
        httpRouter.post("/sendMessage")
                .handler(request -> {
                    vertx.eventBus().send("router", request.getBodyAsString());
                    request.response().end("ok");
                });
        httpRouter.get("/getHistory")
                .handler(request ->
                        vertx.eventBus().send("getHistory", request.getBodyAsString(), result ->
                                request.response().end(result.result().body().toString())
                        )
                );
        httpRouter.get("/picture/:id")
                .handler(request ->
                        vertx.eventBus().send("getPicture", request.pathParam("id"), result ->
                                request.response().end((Buffer) result.result().body())
                        )
                );
        httpRouter.post("/picture")
                .handler(request ->
                        vertx.fileSystem().readFile(request.fileUploads().stream().findFirst().get().uploadedFileName(), (f) -> {
                            if (f.succeeded()) {
                                vertx.eventBus().send("savePicture", f.result(), result ->
                                        request.response().end(result.result().body().toString())
                                );
                            }
                        })
                );
        httpServer.requestHandler(httpRouter::accept);
        httpServer.listen(8081);
    }
}
