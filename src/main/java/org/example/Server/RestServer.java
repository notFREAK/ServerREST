package org.example.Server;

import static spark.Spark.*;
import com.google.gson.Gson;
import org.example.figure.Circle;
import org.example.figure.Line;
import org.example.figure.Rectangle;

import java.util.Collections;
import java.util.Map;

public class RestServer {
    private final ServerLogic logic;
    private final Gson gson = new Gson();
    private boolean running = false;

    static class GenericObject {
        String type;
        Map<String, Object> data;
    }


    public RestServer(ServerLogic logic) {
        this.logic = logic;
    }

    public void startServer(int port) {
        if (running) return;
        port(port);

        get("/objects", (req, res) -> {
            res.type("application/json");
            return logic.getAllObjectsAsMapsWithType();
        }, gson::toJson);

        post("/objects", (req, res) -> {
            res.type("application/json");
            String body = req.body();
            GenericObject go = gson.fromJson(body, GenericObject.class);

            if (go.type == null || go.data == null) {
                res.status(400);
                return gson.toJson(Collections.singletonMap("error", "Invalid JSON format"));
            }

            Object obj;
            switch (go.type) {
                case "Circle":
                    obj = gson.fromJson(gson.toJson(go.data), Circle.class);
                    break;
                case "Rectangle":
                    obj = gson.fromJson(gson.toJson(go.data), Rectangle.class);
                    break;
                case "Line":
                    obj = gson.fromJson(gson.toJson(go.data), Line.class);
                    break;
                default:
                    res.status(400);
                    return gson.toJson(Collections.singletonMap("error", "Unknown type"));
            }

            logic.addObject(obj);
            res.status(201);
            return gson.toJson(Collections.singletonMap("status", "OK"));
        });


        running = true;
    }

    public void stopServer(){
        if(!running)return;
        stop();
        running=false;
    }

    public boolean isRunning(){return running;}
}
