package com.example.algorithmsnake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Snake {
    // Enum to define the pathfinding algorithm used by AI snakes
    public enum PathAlgorithm { ASTAR, BFS, DIJKSTRA }

    // --- Snake Attributes ---
    public List<Point> body;        // List of points representing the snake's segments
    public Point direction;         // Current direction of movement (e.g., (1,0) for right)
    public int color;               // Color used to draw the snake
    public PathAlgorithm algorithm; // Pathfinding algorithm (null for player)
    public boolean optimal;         // Pathfinding setting (e.g., prefer optimal vs. suboptimal path)
    public int score = 0;           // Snake's score (e.g., number of eatables eaten)
    public boolean isAi;            // ** NEW: Flag to indicate if this snake is AI-controlled **

    /**
     * Constructor for creating a Snake instance.
     *
     * @param start     The starting position of the snake's head.
     * @param color     The color of the snake.
     * @param algorithm The pathfinding algorithm to use (null if player-controlled).
     * @param optimal   Pathfinding setting (relevant for AI).
     * @param isAi      True if the snake is AI-controlled, false if player-controlled.
     */
    public Snake(Point start, int color, PathAlgorithm algorithm, boolean optimal, boolean isAi) { // Added isAi parameter
        this.color = color;
        this.algorithm = algorithm;
        this.optimal = optimal;
        this.isAi = isAi; // ** Set the isAi flag **
        this.body = new ArrayList<>();
        this.direction = new Point(1, 0); // Initial direction: right (can be adjusted)

        // Initialize snake with 3 segments, starting from the head position
        // Head is at index 0
        for (int i = 0; i < 3; i++) {
            // Add segments extending to the left of the start point initially
            body.add(new Point(start.x - i, start.y));
        }
    }

    /**
     * Gets the current position of the snake's head.
     * @return The Point representing the head's coordinates.
     */
    public Point getHead() {
        if (body.isEmpty()) {
            // Handle potential error case, though body should not be empty after construction
            return new Point(-1, -1); // Or throw an exception
        }
        return body.get(0); // Head is the first element in the list
    }

    /**
     * Sets the snake's direction based on the next target point (used by AI pathfinding).
     * Calculates the direction vector needed to move from the current head to the next point.
     * @param nextMove The target Point the snake should move towards in the next step.
     */
    public void setDirectionTowards(Point nextMove) {
        if (nextMove == null) return;
        Point head = getHead();
        // Calculate direction vector: (targetX - currentX, targetY - currentY)
        // Ensure it's a unit vector (or handle non-unit vectors if necessary)
        int dx = Integer.compare(nextMove.x, head.x); // Gives -1, 0, or 1
        int dy = Integer.compare(nextMove.y, head.y); // Gives -1, 0, or 1
        this.direction = new Point(dx, dy);
    }

    /**
     * Moves the snake one step in its current direction.
     * Adds a new head segment and removes the tail segment.
     */
    public void move() {
        Point head = getHead();
        // Calculate the new head position based on the current direction
        Point newHead = new Point(head.x + direction.x, head.y + direction.y);
        // Add the new head to the beginning of the body list
        body.add(0, newHead);
        // Remove the last segment (tail) to simulate movement
        if (!body.isEmpty()) { // Safety check
            body.remove(body.size() - 1);
        }
    }

    /**
     * Called when the snake eats an eatable. Increases score and grows the snake.
     */
    public void eatEatable() {
        grow();
        score++;
    }

    /**
     * Grows the snake by adding a segment at the tail's position.
     * The new segment will effectively stay in place while the rest of the snake moves.
     */
    public void grow() {
        if (body.isEmpty()) return; // Cannot grow if body doesn't exist
        // Add a new point that duplicates the current tail's position
        Point currentTail = body.get(body.size() - 1);
        body.add(new Point(currentTail.x, currentTail.y)); // Add duplicate point at the end
    }

    /**
     * Draws the snake on the canvas.
     * @param canvas The Canvas to draw on.
     * @param unitSize The size (in pixels) of one grid unit.
     * @param offsetX The horizontal offset (in pixels) for centering the game area.
     * @param offsetY The vertical offset (in pixels) for centering the game area.
     */
    public void paint(Canvas canvas, int unitSize, int offsetX, int offsetY) {
        if (canvas == null || body == null || body.isEmpty()) return;

        Paint paint = new Paint();
        paint.setColor(this.color);
        paint.setStyle(Paint.Style.FILL); // Ensure segments are filled

        // Draw each segment of the snake's body
        for (Point p : body) {
            // Calculate the screen coordinates for the top-left corner of the segment's rectangle
            int left = offsetX + p.x * unitSize;
            int top = offsetY + p.y * unitSize;
            // Draw the rectangle representing the segment
            canvas.drawRect(left, top, left + unitSize, top + unitSize, paint);
        }

        // Optional: Draw the head differently (e.g., different color, add eyes)
        // Paint headPaint = new Paint();
        // headPaint.setColor(Color.WHITE); // Example: White head
        // Point head = getHead();
        // int headLeft = offsetX + head.x * unitSize;
        // int headTop = offsetY + head.y * unitSize;
        // canvas.drawRect(headLeft, headTop, headLeft + unitSize, headTop + unitSize, headPaint);
    }
}
