package org.example.Server;

import com.google.gson.Gson;
import org.example.figure.Shape;
import org.example.figure.Circle;
import org.example.figure.Rectangle;
import org.example.figure.Line;
import static spark.Spark.*;

import org.example.common.ApiResponse;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MultiClientRestServer extends JFrame implements ServerLogic.ServerListener {
    private ServerLogic logic;
    private DefaultListModel<String> listModel;
    private JList<String> shapeList;
    private JButton startButton, stopButton;
    private boolean serverRunning = false;
    private Gson gson = new Gson();

    public MultiClientRestServer() {
        super("REST Server (Port 12345)");
        setSize(400,300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        logic = new ServerLogic();
        logic.addListener(this);

        listModel = new DefaultListModel<>();
        shapeList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(shapeList);

        startButton = new JButton("Запустить сервер");
        stopButton = new JButton("Остановить сервер");
        stopButton.setEnabled(false);

        JPanel topPanel = new JPanel();
        topPanel.add(startButton);
        topPanel.add(stopButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        for (Map<String, Object> map : logic.getAllShapes()) {
            int id = ((Number) map.get("id")).intValue();
            String type = (String) map.get("type");
            listModel.addElement("ID=" + id + ": " + type);
        }
    }

    private void startServer() {
        if (!serverRunning) {
            port(12345);

            post("/shapes", (req, res) -> {
                RestApi.AddShapeRequest request = gson.fromJson(req.body(), RestApi.AddShapeRequest.class);
                res.type("application/json");
                if (request == null || request.getType() == null || request.getData() == null) {
                    res.status(400);
                    return gson.toJson(new ApiResponse<Void>(false, "Invalid request"));
                }
                Map<String, Class<? extends Shape>> shapeMap = new HashMap<>();
                shapeMap.put("Circle", Circle.class);
                shapeMap.put("Rectangle", Rectangle.class);
                shapeMap.put("Line", Line.class);

                Class<? extends Shape> clazz = shapeMap.get(request.getType());
                if (clazz == null) {
                    res.status(400);
                    return gson.toJson(new ApiResponse<Void>(false, "Unknown shape type"));
                }
                Shape shape = gson.fromJson(gson.toJson(request.getData()), clazz);
                int id = logic.addShape(shape);
                res.status(201);
                return gson.toJson(new ApiResponse<Void>(true, "OK: Received " + request.getType() + " with id " + id));
            });


            get("/shapes", (req, res) -> {
                res.type("application/json");
                return gson.toJson(new ApiResponse<>(true, "OK", logic.getAllShapes()));
            });

            get("/shapes/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params("id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    res.type("application/json");
                    return gson.toJson(new ApiResponse<Void>(false, "Invalid id"));
                }
                Shape shape = logic.getShape(id);
                res.type("application/json");
                if (shape == null) {
                    res.status(404);
                    return gson.toJson(new ApiResponse<Void>(false, "Not found id " + id));
                }
                RestApi.ShapeResponse response = new RestApi.ShapeResponse(id, shape.getType(), shape);
                return gson.toJson(new ApiResponse<>(true, "OK", response));
            });

            delete("/shapes/:id", (req, res) -> {
                int id;
                try {
                    id = Integer.parseInt(req.params("id"));
                } catch (NumberFormatException e) {
                    res.status(400);
                    res.type("application/json");
                    return gson.toJson(new ApiResponse<Void>(false, "Invalid id"));
                }
                boolean result = logic.deleteShape(id);
                res.type("application/json");
                if (result) {
                    return gson.toJson(new ApiResponse<Void>(true, "OK: Deleted id " + id));
                } else {
                    res.status(404);
                    return gson.toJson(new ApiResponse<Void>(false, "Not found id " + id));
                }
            });

            serverRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void stopServer() {
        if (serverRunning) {
            stop();
            serverRunning = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    @Override
    public void onShapeAdded(int id, Shape shape) {
        SwingUtilities.invokeLater(() -> listModel.addElement("ID=" + id + ": " + shape.getType()));
    }

    @Override
    public void onShapeDeleted(int id) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < listModel.getSize(); i++) {
                String item = listModel.get(i);
                if (item.startsWith("ID=" + id + ":")) {
                    listModel.remove(i);
                    break;
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MultiClientRestServer().setVisible(true));
    }

    public static class RestApi {
        public static class AddShapeRequest {
            private String type;
            private Object data;
            public String getType() { return type; }
            public Object getData() { return data; }
        }
        public static class ShapeResponse {
            private int id;
            private String type;
            private Object data;
            public ShapeResponse(int id, String type, Object data) {
                this.id = id;
                this.type = type;
                this.data = data;
            }
            public int getId() { return id; }
            public String getType() { return type; }
            public Object getData() { return data; }
        }
    }
}
