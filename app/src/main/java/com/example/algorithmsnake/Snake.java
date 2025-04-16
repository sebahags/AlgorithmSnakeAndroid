package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;

//class for snake object
public class Snake {
    public enum PathAlgorithm { ASTAR, BFS, DIJKSTRA }
    public List<Point> body;
    public Point direction;
    public int color;
    public PathAlgorithm algorithm;
    public boolean optimal;
    public int score = 0;
    public boolean isAi;

    public Snake(Point start, int color, PathAlgorithm algorithm, boolean optimal, boolean isAi) {
        this.color = color;
        this.algorithm = algorithm;
        this.optimal = optimal;
        this.isAi = isAi;
        this.body = new ArrayList<>();
        this.direction = new Point(1, 0);

        for (int i = 0; i < 3; i++) {
            body.add(new Point(start.x - i, start.y));
        }
    }

    public Point getHead() {
        if (body.isEmpty()) {
            return new Point(-1, -1);
        }
        return body.get(0);
    }

    public void setDirectionTowards(Point nextMove) {
        if (nextMove == null) return;
        Point head = getHead();
        int dx = Integer.compare(nextMove.x, head.x);
        int dy = Integer.compare(nextMove.y, head.y);
        this.direction = new Point(dx, dy);
    }

    public void move() {
        Point head = getHead();
        Point newHead = new Point(head.x + direction.x, head.y + direction.y);
        body.add(0, newHead);
        if (!body.isEmpty()) {
            body.remove(body.size() - 1);
        }
    }

    public void eatEatable() {
        grow();
        score++;
    }

    public void grow() {
        if (body.isEmpty()) return;
        Point currentTail = body.get(body.size() - 1);
        body.add(new Point(currentTail.x, currentTail.y));
    }

    public void paint(Canvas canvas, int unitSize, int offsetX, int offsetY) {
        if (canvas == null || body == null || body.isEmpty()) return;
        Paint paint = new Paint();
        paint.setColor(this.color);
        paint.setStyle(Paint.Style.FILL);

        for (Point p : body) {
            int left = offsetX + p.x * unitSize;
            int top = offsetY + p.y * unitSize;
            canvas.drawRect(left, top, left + unitSize, top + unitSize, paint);
        }
    }
}
