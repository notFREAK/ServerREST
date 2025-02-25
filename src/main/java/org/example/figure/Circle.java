package org.example.figure;

import java.awt.Graphics;

public class Circle implements Shape {
    private int x;
    private int y;
    private int radius;

    public Circle() {}

    public Circle(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }

    @Override
    public String getType() {
        return "Circle";
    }

    @Override
    public void draw(Graphics g) {
        g.drawOval(x, y, radius, radius);
    }

    @Override
    public String toString() {
        return "Circle(x=" + x + ", y=" + y + ", radius=" + radius + ")";
    }
}
