package org.example.figure;

public class Circle {
    private int x;
    private int y;
    private int radius;

    public Circle(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "Circle(x=" + x + ", y=" + y + ", r=" + radius + ")";
    }
}
