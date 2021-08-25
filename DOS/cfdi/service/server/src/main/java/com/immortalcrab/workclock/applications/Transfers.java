package com.immortalcrab.workclock.applications;

import io.vertx.ext.web.Route;

public class Transfers {

    public static void hello(Route router) {

        router.handler(req -> {
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello road warriors from vert.x!!!");
        });
    }
}
