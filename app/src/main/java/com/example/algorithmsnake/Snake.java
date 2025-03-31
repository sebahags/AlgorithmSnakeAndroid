package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    public enum PathAlgorithm { ASTAR, BFS, DIJKSTRA }

    public List<Point> body;
    public Point direction;
    public int color;
    public PathAlgorithm algorithm;
    public boolean optimal;
    public int score = 0;

    public Snake(Point start, int color, PathAlgorithm algorithm, boolean optimal) {
        this.color = color;
        this.algorithm = algorithm;
        this.optimal = optimal;
        body = new ArrayList<>();
        direction = new Point(1, 0); // Initial direction: right

        // Initialize snake with 3 segments
        for (int i = 0; i < 3; i++) {
            body.add(new Point(start.x - i, start.y));
        }
    }

    public Point getHead() {
        return body.get(0);
    }

    public void setDirection(Point nextMove) {
        direction = new Point(nextMove.x - getHead().x, nextMove.y - getHead().y);
    }

    public void move() {
        Point newHead = new Point(getHead().x + direction.x, getHead().y + direction.y);
        body.add(0, newHead);
        body.remove(body.size() - 1);
    }

    public void eatEatable(){
        grow();
        score++;
    }

    public void grow() {
        // Duplicate the last segment to simulate growth.
        body.add(new Point(body.get(body.size() - 1)));
    }

    public void paint(Canvas canvas, int unitSize, int offsetX, int offsetY) {
        Paint paint = new Paint();
        paint.setColor(color);
        for (Point p : body) {
            // Multiply coordinates by unitSize and add the offsets
            int left = offsetX + p.x * unitSize;
            int top = offsetY + p.y * unitSize;
            canvas.drawRect(left, top, left + unitSize, top + unitSize, paint);
        }
    }
}
