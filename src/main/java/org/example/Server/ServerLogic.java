package org.example.Server;

import com.google.gson.Gson;
import org.example.figure.Circle;
import org.example.figure.Line;
import org.example.figure.Rectangle;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class ServerLogic {
    private final List<Object> receivedObjects = Collections.synchronizedList(new ArrayList<>());
    private final Gson gson = new Gson();
    private final String JSON_FILE = "objects_rest.json";
    private final List<ServerListener> listeners = new ArrayList<>();
    private boolean loaded = false;

    public interface ServerListener {
        void onNewObjectReceived(Object obj);
    }

    public void addListener(ServerListener l) {
        listeners.add(l);
    }

    private void notifyNewObject(Object obj) {
        for (ServerListener l: listeners) {
            l.onNewObjectReceived(obj);
        }
    }

    public synchronized void addObject(Object o){
        receivedObjects.add(o);
        saveObjectsToFile();
        notifyNewObject(o);
    }

    public void loadObjectsFromFile(){
        if (loaded) return;
        loaded = true;
        File f = new File(JSON_FILE);
        if(!f.exists()) return;
        try (Reader r = new FileReader(f)) {
            List<Map<String,Object>> list = gson.fromJson(r, ArrayList.class);
            if (list == null) return;
            for (Map<String,Object> m : list) {
                Object obj = mapToObject(m);
                if (obj!=null) {
                    receivedObjects.add(obj);
                    notifyNewObject(obj);
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void saveObjectsToFile(){
        List<Map<String,Object>> list = new ArrayList<>();
        for (Object o: receivedObjects) {
            list.add(objectToMapWithType(o));
        }
        try(Writer w = new FileWriter(JSON_FILE)){
            gson.toJson(list,w);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllObjectsAsMapsWithType() {
        List<Map<String, Object>> list = new ArrayList<>();
        synchronized (receivedObjects) {
            for (Object o : receivedObjects) {
                list.add(objectToMapWithType(o));
            }
        }
        return list;
    }

    private Map<String, Object> objectToMapWithType(Object o) {
        Map<String, Object> map = new LinkedHashMap<>();
        String type;

        if (o instanceof Circle) {
            type = "Circle";
        } else if (o instanceof Rectangle) {
            type = "Rectangle";
        } else if (o instanceof Line) {
            type = "Line";
        } else {
            type = "Unknown";
        }

        map.put("type", type);

        Map<String, Object> dataMap = gson.fromJson(gson.toJson(o), Map.class);
        map.put("data", dataMap);

        return map;
    }

    private Object mapToObject(Map<String, Object> m) {
        String type = (String) m.get("type");
        if (type == null) return null;

        Object data = m.get("data");
        if (!(data instanceof Map)) return null;

        String jsonData = gson.toJson(data);

        switch (type) {
            case "Circle":
                return gson.fromJson(jsonData, Circle.class);
            case "Rectangle":
                return gson.fromJson(jsonData, Rectangle.class);
            case "Line":
                return gson.fromJson(jsonData, Line.class);
            default:
                return null;
        }
    }
}
