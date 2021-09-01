package com.immortalcrab.cfdi.server;

import static spark.Spark.*;


public class Server 
{
    public static void main(String[] args) {

        get("/hello", (req, res) -> "Hello World (from Spark)");

        get("/hello-json", (req, res) -> {
            res.type("application/json");
            return "[{\"micampo\": 55}, {\"otro\": true}]";
        });

        post("/cfdi", (req, res) -> {
            String body = req.body();
            System.out.println(body);

            res.type("application/json");
            return "{\"msg\": \"" + body + "\"}";
        });
    }
}
