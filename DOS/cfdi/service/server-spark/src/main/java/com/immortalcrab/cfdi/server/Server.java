package com.immortalcrab.cfdi.server;

import static spark.Spark.*;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.immortalcrab.as400.parser.PairExtractor;
import com.immortalcrab.as400.request.FacturaRequest;
import org.javatuples.Pair;


public class Server 
{
    public static void main(String[] args) {

        get("/hello", (req, res) -> "Hello World (from Spark)");

        get("/hello-json", (req, res) -> {
            res.type("application/json");
            return "[{\"micampo\": 55}, {\"otro\": true}]";
        });

        post("/cfdi", (req, res) -> {
            byte[] body = req.bodyAsBytes();

            List<Pair<String, String>> l = null;
            try {
                var bais = new ByteArrayInputStream(body);
                var isr = new InputStreamReader(bais);
                l = PairExtractor.go4it(isr);
                System.out.println(l);

            } catch (Exception e) {
                System.out.println(e);
            }

            Map<String, Object> ds = null;
            try {
                FacturaRequest fact = FacturaRequest.render(l);
                ds = fact.getDs();
                System.out.println(ds);
            } catch (Exception e) {
                System.out.println(e);
            }

            res.type("application/json");
            return "{\"msg\": \"OK!!\"}";
        });
    }
}
