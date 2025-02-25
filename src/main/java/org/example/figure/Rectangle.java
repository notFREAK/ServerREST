package org.example.figure;

import java.awt.Graphics;

public class Rectangle implements Shape {
    private int x;
    private int y;
    private int width;
    private int height;

    public Rectangle() {}

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public String getType() {
        return "Rectangle";
    }

    @Override
    public void draw(Graphics g) {
        g.drawRect(x, y, width, height);
    }

    @Override
    public String toString() {
        return "Rectangle(x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ")";
    }
}
