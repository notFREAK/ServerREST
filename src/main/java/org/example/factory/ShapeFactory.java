package org.example.factory;

import org.example.figure.Circle;
import org.example.figure.Line;
import org.example.figure.Rectangle;
import org.example.figure.Shape;

import java.util.HashMap;
import java.util.Map;

public class ShapeFactory {
    private static final Map<String, Class<? extends Shape>> shapeClasses = new HashMap<>();

    static {
        shapeClasses.put("Circle", Circle.class);
        shapeClasses.put("Rectangle", Rectangle.class);
        shapeClasses.put("Line", Line.class);
    }

    public static Shape createShape(String type, Map<String, Integer> params) {
        switch (type) {
            case "Circle":
                return new Circle(params.get("x"), params.get("y"), params.get("radius"));
            case "Rectangle":
                return new Rectangle(params.get("x"), params.get("y"), params.get("width"), params.get("height"));
            case "Line":
                return new Line(params.get("x1"), params.get("y1"), params.get("x2"), params.get("y2"));
            default:
                throw new IllegalArgumentException("Неизвестный тип фигуры: " + type);
        }
    }

    public static Class<? extends Shape> getShapeClass(String type) {
        return shapeClasses.get(type);
    }
}