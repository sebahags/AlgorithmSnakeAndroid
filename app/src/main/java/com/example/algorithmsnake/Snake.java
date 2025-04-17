package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

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
    private List<Point> currentPath;
    private Point currentTarget;

    public Snake(Point start, int color, PathAlgorithm algorithm, boolean optimal, boolean isAi) {
        this.color = color;
        this.algorithm = algorithm;
        this.optimal = optimal;
        this.isAi = isAi;
        this.body = new ArrayList<>();
        this.direction = new Point(1, 0);
        this.body.add(start);
        for (int i = 0; i < 3; i++) {
            body.add(new Point(start.x - i, start.y));
        }
        this.currentPath = null;
        this.currentTarget = null;
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
        score++;
        grow();
        if (this.isAi) {
            this.currentPath = null;
            this.currentTarget = null;
        }
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

        Paint headPaint = new Paint();
        headPaint.setColor(Color.argb(255,
                Math.min(255, Color.red(this.color) + 125),
                Math.min(255, Color.green(this.color) + 125),
                Math.min(255, Color.blue(this.color) + 125)));
        headPaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < body.size(); i++) {
            Point p = body.get(i);
            if (p == null) continue;

            float left = offsetX + p.x * unitSize;
            float top = offsetY + p.y * unitSize;
            float right = left + unitSize;
            float bottom = top + unitSize;

            if (i == 0) {
                canvas.drawRect(left, top, right, bottom, headPaint);
            } else {
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
    public List<Point> getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(List<Point> path, Point target) {
        this.currentPath = (path == null) ? null : new ArrayList<>(path);
        this.currentTarget = target;
    }

    public Point getCurrentTarget() {
        return currentTarget;
    }

}
