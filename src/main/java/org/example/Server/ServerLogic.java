package org.example.Server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.factory.ShapeFactory;
import org.example.figure.Shape;
import org.example.figure.Circle;
import org.example.figure.Rectangle;
import org.example.figure.Line;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ServerLogic {
    private final Map<Integer, Shape> storedShapes = new LinkedHashMap<>();
    private int currentId = 0;
    private final Gson gson = new Gson();
    private final String JSON_FILE = "shapes.json";
    private final List<ServerListener> listeners = new ArrayList<>();

    public interface ServerListener {
        void onShapeAdded(int id, Shape shape);
        void onShapeDeleted(int id);
    }

    public void addListener(ServerListener listener) {
        listeners.add(listener);
    }

    private void notifyShapeAdded(int id, Shape shape) {
        for (ServerListener l : listeners) {
            l.onShapeAdded(id, shape);
        }
    }

    private void notifyShapeDeleted(int id) {
        for (ServerListener l : listeners) {
            l.onShapeDeleted(id);
        }
    }

    public synchronized int addShape(Shape shape) {
        int id = currentId++;
        storedShapes.put(id, shape);
        saveShapesToFile();
        notifyShapeAdded(id, shape);
        return id;
    }

    public synchronized boolean deleteShape(int id) {
        if (storedShapes.containsKey(id)) {
            storedShapes.remove(id);
            saveShapesToFile();
            notifyShapeDeleted(id);
            return true;
        }
        return false;
    }

    public synchronized Shape getShape(int id) {
        return storedShapes.get(id);
    }

    public synchronized List<Map<String, Object>> getAllShapes() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, Shape> entry : storedShapes.entrySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", entry.getKey());
            map.put("type", entry.getValue().getType());
            map.put("data", entry.getValue());
            list.add(map);
        }
        return list;
    }

    private synchronized void saveShapesToFile() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, Shape> entry : storedShapes.entrySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", entry.getKey());
            map.put("type", entry.getValue().getType());
            map.put("data", entry.getValue());
            list.add(map);
        }
        try (Writer writer = new FileWriter(JSON_FILE)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void loadShapesFromFile() {
        File file = new File(JSON_FILE);
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
            List<Map<String, Object>> list = gson.fromJson(reader, listType);
            if (list != null) {
                for (Map<String, Object> map : list) {
                    int id = ((Number) map.get("id")).intValue();
                    String type = (String) map.get("type");
                    Object dataObj = map.get("data");
                    String jsonData = gson.toJson(dataObj);
                    Class<? extends Shape> clazz = ShapeFactory.getShapeClass(type);
                    if (clazz != null) {
                        Shape shape = gson.fromJson(jsonData, clazz);
                        storedShapes.put(id, shape);
                        if (id >= currentId) {
                            currentId = id + 1;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerLogic() {
        loadShapesFromFile();
    }
}
