package org.example.client;

import org.example.client.ShapeService.AddShapeRequest;
import org.example.client.ShapeService.ShapeResponse;
import org.example.client.ShapeService;
import org.example.common.ApiResponse;
import org.example.factory.ShapeFactory;
import org.example.figure.Shape;
import org.example.figure.Circle;
import org.example.figure.Rectangle;
import org.example.figure.Line;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Response;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ClientGUI extends JFrame {
    private ShapeService service;
    private Retrofit retrofit;

    private JComboBox<String> objectTypeBox;
    private JPanel paramsPanel;
    private CardLayout cardLayout;

    // Поля для ввода параметров фигуры
    private JPanel circlePanel;
    private JTextField circleXField, circleYField, circleRadiusField;

    private JPanel rectanglePanel;
    private JTextField rectXField, rectYField, rectWidthField, rectHeightField;

    private JPanel linePanel;
    private JTextField lineX1Field, lineY1Field, lineX2Field, lineY2Field;

    private JButton addButton, getObjectsButton, deleteButton, getShapeByIdButton;
    private JTextArea responseArea;
    private DrawingPanel drawingPanel;
    private JTextField shapeIdField;

    private Shape currentShape;

    public ClientGUI() {
        super("REST Client for Shapes");
        setSize(800,500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:12345/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ShapeService.class);

        drawingPanel = new DrawingPanel();
        drawingPanel.setPreferredSize(new Dimension(400,400));
        add(drawingPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        objectTypeBox = new JComboBox<>(new String[]{"Circle", "Rectangle", "Line"});
        topPanel.add(new JLabel("Тип фигуры:"));
        topPanel.add(objectTypeBox);
        rightPanel.add(topPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        paramsPanel = new JPanel(cardLayout);

        circlePanel = new JPanel(new GridLayout(3,2,5,5));
        circleXField = new JTextField("100");
        circleYField = new JTextField("200");
        circleRadiusField = new JTextField("50");
        circlePanel.add(new JLabel("X:")); circlePanel.add(circleXField);
        circlePanel.add(new JLabel("Y:")); circlePanel.add(circleYField);
        circlePanel.add(new JLabel("Радиус:")); circlePanel.add(circleRadiusField);

        rectanglePanel = new JPanel(new GridLayout(4,2,5,5));
        rectXField = new JTextField("10");
        rectYField = new JTextField("10");
        rectWidthField = new JTextField("100");
        rectHeightField = new JTextField("50");
        rectanglePanel.add(new JLabel("X:")); rectanglePanel.add(rectXField);
        rectanglePanel.add(new JLabel("Y:")); rectanglePanel.add(rectYField);
        rectanglePanel.add(new JLabel("Ширина:")); rectanglePanel.add(rectWidthField);
        rectanglePanel.add(new JLabel("Высота:")); rectanglePanel.add(rectHeightField);

        linePanel = new JPanel(new GridLayout(4,2,5,5));
        lineX1Field = new JTextField("0");
        lineY1Field = new JTextField("0");
        lineX2Field = new JTextField("200");
        lineY2Field = new JTextField("200");
        linePanel.add(new JLabel("X1:")); linePanel.add(lineX1Field);
        linePanel.add(new JLabel("Y1:")); linePanel.add(lineY1Field);
        linePanel.add(new JLabel("X2:")); linePanel.add(lineX2Field);
        linePanel.add(new JLabel("Y2:")); linePanel.add(lineY2Field);

        paramsPanel.add(circlePanel, "Circle");
        paramsPanel.add(rectanglePanel, "Rectangle");
        paramsPanel.add(linePanel, "Line");

        JPanel centerParamsPanel = new JPanel();
        centerParamsPanel.add(paramsPanel);
        rightPanel.add(centerParamsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        addButton = new JButton("Добавить фигуру");
        responseArea = new JTextArea(5,20);
        responseArea.setEditable(false);
        bottomPanel.add(new JScrollPane(responseArea), BorderLayout.NORTH);
        bottomPanel.add(addButton, BorderLayout.CENTER);

        JPanel commandPanel = new JPanel();
        shapeIdField = new JTextField(5);
        deleteButton = new JButton("Удалить по id");
        getShapeByIdButton = new JButton("Получить по id");
        getObjectsButton = new JButton("Получить все фигуры");
        commandPanel.add(new JLabel("ID:"));
        commandPanel.add(shapeIdField);
        commandPanel.add(deleteButton);
        commandPanel.add(getShapeByIdButton);
        commandPanel.add(getObjectsButton);
        bottomPanel.add(commandPanel, BorderLayout.SOUTH);

        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        objectTypeBox.addActionListener(e -> {
            String type = (String) objectTypeBox.getSelectedItem();
            cardLayout.show(paramsPanel, type);
            updateCurrentShape();
            drawingPanel.repaint();
        });

        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateAndRepaint(); }
            public void removeUpdate(DocumentEvent e) { updateAndRepaint(); }
            public void changedUpdate(DocumentEvent e) { updateAndRepaint(); }
            private void updateAndRepaint() {
                updateCurrentShape();
                drawingPanel.repaint();
            }
        };
        circleXField.getDocument().addDocumentListener(docListener);
        circleYField.getDocument().addDocumentListener(docListener);
        circleRadiusField.getDocument().addDocumentListener(docListener);
        rectXField.getDocument().addDocumentListener(docListener);
        rectYField.getDocument().addDocumentListener(docListener);
        rectWidthField.getDocument().addDocumentListener(docListener);
        rectHeightField.getDocument().addDocumentListener(docListener);
        lineX1Field.getDocument().addDocumentListener(docListener);
        lineY1Field.getDocument().addDocumentListener(docListener);
        lineX2Field.getDocument().addDocumentListener(docListener);
        lineY2Field.getDocument().addDocumentListener(docListener);

        addButton.addActionListener(e -> addShapeToServer());
        getObjectsButton.addActionListener(e -> getAllShapesFromServer());
        deleteButton.addActionListener(e -> deleteShapeFromServer());
        getShapeByIdButton.addActionListener(e -> getShapeByIdFromServer());
    }

    private void addShapeToServer() {
        String type = (String) objectTypeBox.getSelectedItem();
        Map<String, Integer> params = gatherParams(type);
        try {
            Shape shape = ShapeFactory.createShape(type, params);
            currentShape = shape;
            drawingPanel.repaint();
            new Thread(() -> {
                try {
                    AddShapeRequest req = new AddShapeRequest(type, params);
                    Call<ApiResponse<Void>> call = service.addShape(req);
                    Response<ApiResponse<Void>> response = call.execute();
                    ApiResponse<Void> respObj = response.body();
                    SwingUtilities.invokeLater(() -> {
                        if (respObj != null) {
                            responseArea.append("Server: " + respObj.getMessage() + "\n");
                        } else {
                            responseArea.append("Server: null response\n");
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> responseArea.append("Error adding shape\n"));
                }
            }).start();
        } catch (IllegalArgumentException ex) {
            responseArea.append("Ошибка: " + ex.getMessage() + "\n");
        }
    }

    private void getAllShapesFromServer() {
        new Thread(() -> {
            try {
                Call<ApiResponse<List<Map<String, Object>>>> call = service.getAllShapes();
                Response<ApiResponse<List<Map<String, Object>>>> response = call.execute();
                ApiResponse<List<Map<String, Object>>> apiResp = response.body();
                SwingUtilities.invokeLater(() -> {
                    if (apiResp != null && apiResp.isSuccess() && apiResp.getData() != null) {
                        StringBuilder sb = new StringBuilder("Все фигуры:\n");
                        for (Map<String, Object> map : apiResp.getData()) {
                            sb.append("ID=").append(map.get("id")).append(": ").append(map.get("type")).append("\n");
                        }
                        JOptionPane.showMessageDialog(ClientGUI.this, sb.toString(), "Список фигур",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(ClientGUI.this, "Нет фигур", "Список фигур",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> responseArea.append("Error getting all shapes\n"));
            }
        }).start();
    }

    private void deleteShapeFromServer() {
        int id = parseIntOrZero(shapeIdField.getText());
        new Thread(() -> {
            try {
                Call<ApiResponse<Void>> call = service.deleteShape(id);
                Response<ApiResponse<Void>> response = call.execute();
                ApiResponse<Void> respObj = response.body();
                SwingUtilities.invokeLater(() -> {
                    if (respObj != null) {
                        responseArea.append("Server: " + respObj.getMessage() + "\n");
                    } else {
                        responseArea.append("Server: null response\n");
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> responseArea.append("Error deleting shape\n"));
            }
        }).start();
    }

    private void getShapeByIdFromServer() {
        int id = parseIntOrZero(shapeIdField.getText());
        new Thread(() -> {
            try {
                Call<ApiResponse<ShapeResponse>> call = service.getShape(id);
                Response<ApiResponse<ShapeResponse>> response = call.execute();
                ApiResponse<ShapeResponse> apiResp = response.body();
                SwingUtilities.invokeLater(() -> {
                    if (apiResp != null && apiResp.isSuccess() && apiResp.getData() != null) {
                        responseArea.append("Server: " + apiResp.getData() + "\n");
                    } else {
                        responseArea.append("Server: " + (apiResp != null ? apiResp.getMessage() : "null response") + "\n");
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> responseArea.append("Error getting shape by id\n"));
            }
        }).start();
    }

    private Map<String, Integer> gatherParams(String type) {
        Map<String, Integer> params = new HashMap<>();
        switch (type) {
            case "Circle":
                params.put("x", parseIntOrZero(circleXField.getText()));
                params.put("y", parseIntOrZero(circleYField.getText()));
                params.put("radius", parseIntOrZero(circleRadiusField.getText()));
                break;
            case "Rectangle":
                params.put("x", parseIntOrZero(rectXField.getText()));
                params.put("y", parseIntOrZero(rectYField.getText()));
                params.put("width", parseIntOrZero(rectWidthField.getText()));
                params.put("height", parseIntOrZero(rectHeightField.getText()));
                break;
            case "Line":
                params.put("x1", parseIntOrZero(lineX1Field.getText()));
                params.put("y1", parseIntOrZero(lineY1Field.getText()));
                params.put("x2", parseIntOrZero(lineX2Field.getText()));
                params.put("y2", parseIntOrZero(lineY2Field.getText()));
                break;
        }
        return params;
    }

    private int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateCurrentShape() {
        String type = (String) objectTypeBox.getSelectedItem();
        Map<String, Integer> params = gatherParams(type);
        try {
            currentShape = ShapeFactory.createShape(type, params);
        } catch (IllegalArgumentException e) {
            currentShape = null;
        }
    }

    class DrawingPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int leftMargin = 10, topMargin = 10;
            int drawWidth = getWidth() - 2 * leftMargin;
            int drawHeight = getHeight() - 2 * topMargin;
            g2.translate(leftMargin, topMargin);
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, drawWidth, drawHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(0, 0, drawWidth, drawHeight);

            if (currentShape != null) {
                g2.setColor(Color.RED);
                currentShape.draw(g2);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}
