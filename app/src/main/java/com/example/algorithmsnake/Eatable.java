package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import java.util.List;
import java.util.Random;

public class Eatable {
    public Point position; // Position on the grid
    private static final Random random = new Random(); // Use a single Random instance

    /**
     * Draws the eatable item on the canvas.
     * @param canvas The Canvas to draw on.
     * @param unitSize The size (in pixels) of one grid unit.
     * @param offsetX The horizontal offset (in pixels) for centering the game area.
     * @param offsetY The vertical offset (in pixels) for centering the game area.
     */
    public void paint(Canvas canvas, int unitSize, int offsetX, int offsetY) {
        if (position == null || canvas == null) return; // Safety check

        Paint paint = new Paint();
        paint.setColor(Color.CYAN); // Eatable color
        paint.setStyle(Paint.Style.FILL);

        // Calculate screen coordinates
        int left = offsetX + position.x * unitSize;
        int top = offsetY + position.y * unitSize;

        // Draw the eatable as a rectangle (or circle, etc.)
        canvas.drawRect(left, top, left + unitSize, top + unitSize, paint);
    }

    /**
     * Spawns the eatable at a random position within the specified grid boundaries,
     * ensuring it does not overlap with any points occupied by the snakes.
     *
     * @param allOccupiedPoints A flat List<Point> containing all coordinates currently occupied by snake bodies.
     * @param minPos            The minimum valid x or y coordinate (inclusive, e.g., 1).
     * @param maxPos            The maximum valid x or y coordinate (inclusive, e.g., 98).
     */
    public void spawn(List<Point> allOccupiedPoints, int minPos, int maxPos) {
        if (allOccupiedPoints == null) {
            Log.e("EatableSpawn", "Cannot spawn eatable, occupied points list is null.");
            // Handle error, maybe set position to an invalid default?
            position = new Point(-1, -1);
            return;
        }

        int range = maxPos - minPos + 1; // Calculate the range of possible coordinates
        if (range <= 0) {
            Log.e("EatableSpawn", "Invalid spawn range (minPos=" + minPos + ", maxPos=" + maxPos + ")");
            position = new Point(-1, -1);
            return;
        }

        int attempts = 0;
        int maxAttempts = (range * range) + 10; // Set a limit to prevent infinite loops if grid is full

        do {
            // Generate random coordinates within the valid range [minPos, maxPos]
            int x = random.nextInt(range) + minPos;
            int y = random.nextInt(range) + minPos;
            position = new Point(x, y);
            attempts++;

            // Check if the grid might be full or if we're stuck
            if (attempts > maxAttempts) {
                Log.w("EatableSpawn", "Could not find a free spot for eatable after " + maxAttempts + " attempts. Grid might be full.");
                // Optional: Handle this case (e.g., stop spawning, trigger game end?)
                // For now, we might place it overlapping, or keep the old position if available.
                // If position was null before, we set it to something potentially overlapping.
                if (this.position == null) this.position = new Point(x,y); // Last attempt position
                return; // Exit the loop
            }

            // Loop condition: Continue generating new positions as long as the current 'position'
            // is contained within the list of 'allOccupiedPoints'.
        } while (listContainsPoint(allOccupiedPoints, position)); // Use helper for clarity

        Log.d("EatableSpawn", "Eatable spawned at (" + position.x + "," + position.y + ") after " + attempts + " attempts.");
    }

    /**
     * Helper method to check if a List<Point> contains a specific Point.
     * Avoids potential issues if Point.equals() is not overridden correctly,
     * although standard android.graphics.Point should work fine.
     *
     * @param list The list of points to check.
     * @param point The point to search for.
     * @return true if the list contains the point, false otherwise.
     */
    private boolean listContainsPoint(List<Point> list, Point point) {
        if (list == null || point == null) {
            return false;
        }
        for (Point p : list) {
            if (p != null && p.equals(point)) { // Check for null points in list too
                return true;
            }
        }
        return false;
    }
}
