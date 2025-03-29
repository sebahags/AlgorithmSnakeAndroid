package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.List;
import java.util.Random;

public class Eatable {
    public Point position;

    public void paint(Canvas canvas, int unitSize, int offsetX, int offsetY) {
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        int left = offsetX + position.x * unitSize;
        int top = offsetY + position.y * unitSize;
        canvas.drawRect(left, top, left + unitSize, top + unitSize, paint);
    }

    public void spawn(List<List<Point>> allSnakeBodies) {
        Random random = new Random();
        do {
            position = new Point(random.nextInt(98) + 1, random.nextInt(98) + 1);
        } while (allSnakeBodies.stream().anyMatch(body -> body.contains(position)));
    }
}
