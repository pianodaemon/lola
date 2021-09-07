package com.immortalcrab.cfdi.server;

import static spark.Spark.get;

public class Launcher {

    public static void main(String[] args) {
        get("/posts", (req, res) -> {
            return "Hello Sparkingly World!";
        });
    }
}
