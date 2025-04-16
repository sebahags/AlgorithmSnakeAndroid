package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import java.util.List;
import java.util.Random;

// class for eatable object
public class Eatable {
    public Point position;
    private static final Random random = new Random();
    public void paint(Canvas canvas, int unitSize, int offsetX, int offsetY) {
        if (position == null || canvas == null) return;
        Paint paint = new Paint();
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.FILL);
        int left = offsetX + position.x * unitSize;
        int top = offsetY + position.y * unitSize;
        canvas.drawRect(left, top, left + unitSize, top + unitSize, paint);
    }

    public void spawn(List<Point> allOccupiedPoints, int minPos, int maxPos) {
        if (allOccupiedPoints == null) {
            Log.e("EatableSpawn", "Cannot spawn eatable, occupied points list is null.");
            position = new Point(-1, -1);
            return;
        }
        int range = maxPos - minPos + 1;
        if (range <= 0) {
            Log.e("EatableSpawn", "Invalid spawn range (minPos=" + minPos + ", maxPos=" + maxPos + ")");
            position = new Point(-1, -1);
            return;
        }
        int attempts = 0;
        int maxAttempts = (range * range) + 10;
        do {
            int x = random.nextInt(range) + minPos;
            int y = random.nextInt(range) + minPos;
            position = new Point(x, y);
            attempts++;
            if (attempts > maxAttempts) {
                Log.w("EatableSpawn", "Could not find a free spot for eatable after " + maxAttempts + " attempts. Grid might be full.");
                if (this.position == null) this.position = new Point(x,y);
                return;
            }
        } while (listContainsPoint(allOccupiedPoints, position));
        Log.d("EatableSpawn", "Eatable spawned at (" + position.x + "," + position.y + ") after " + attempts + " attempts.");
    }
    private boolean listContainsPoint(List<Point> list, Point point) {
        if (list == null || point == null) {
            return false;
        }
        for (Point p : list) {
            if (p != null && p.equals(point)) {
                return true;
            }
        }
        return false;
    }
}
