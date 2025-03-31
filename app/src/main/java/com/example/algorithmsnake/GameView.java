package com.example.algorithmsnake;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameView extends View {
    // Game constants
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    private static final int UNIT_SIZE = 5;
    private static final int GAME_SPEED = 25; // Milliseconds per update
    private static final int MIN_POS = 1;
    private static final int MAX_POS = 98;

    // Game objects
    private List<Snake> snakes;
    private Eatable eatable;
    private boolean gameOver = false;
    private boolean playerMode = false;
    private int gameSpeedMillis;// Change as needed
    private Snake playerSnake;

    // A Handler for our game loop.
    private Handler handler = new Handler();
    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gameOver) {
                updateGame();
                invalidate();  // Triggers onDraw
                handler.postDelayed(this, GAME_SPEED);
            }
        }
    };

    // Constructors
    public GameView(Context context, boolean isPlayerMode, int gameSpeed) {
        super(context);
        this.playerMode = isPlayerMode;
        this.gameSpeedMillis = gameSpeed;
        initGame();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.playerMode = false;
        this.gameSpeedMillis = 55;
        initGame();
    }

    public GameView(Context context) {
        super(context);
        this.playerMode = false;
        this.gameSpeedMillis = 55;
        initGame();
    }

    private void initGame() {
        // Initialize snake list and game objects
        snakes = new ArrayList<>();

        // Create the player snake if needed
        if (playerMode) {
            playerSnake = new Snake(new Point(50, 50), Color.MAGENTA, null, false);
            snakes.add(playerSnake);
        }

        // Add AI-controlled snakes
        snakes.add(new Snake(new Point(40, 55), Color.GREEN, Snake.PathAlgorithm.ASTAR, true));
        snakes.add(new Snake(new Point(20, 30), Color.RED, Snake.PathAlgorithm.BFS, true));
        snakes.add(new Snake(new Point(75, 75), Color.YELLOW, Snake.PathAlgorithm.DIJKSTRA, true));

        // Create and spawn the eatable
        eatable = new Eatable();
        // Use stream to collect snake bodies (each snake’s list of points)
        List<List<Point>> bodies = snakes.stream().map(s -> s.body).collect(Collectors.toList());
        eatable.spawn(bodies);

        // Start the game loop
        handler.postDelayed(gameRunnable, gameSpeedMillis);

        // Enable key events if you plan to test on an emulator with a keyboard
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    // Update game logic – similar to your desktop actionPerformed
    private void updateGame() {
        List<Snake> snakesToRemove = new ArrayList<>();

        for (Snake snake : new ArrayList<>(snakes)) {
            List<List<Point>> allBodies = snakes.stream().map(s -> s.body).collect(Collectors.toList());

            // For the player snake, you would later handle input via onKeyDown or touch events.
            if (playerMode && snake == playerSnake) {
                movePlayerSnake(snakesToRemove);
                continue;
            }

            // For AI snakes, use your pathfinding logic (using the Pathfinder methods)
            List<Point> path;
            if (snake.algorithm == Snake.PathAlgorithm.ASTAR) {
                path = Pathfinder.aStar(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
            } else if (snake.algorithm == Snake.PathAlgorithm.BFS) {
                path = Pathfinder.bfs(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
            } else if (snake.algorithm == Snake.PathAlgorithm.DIJKSTRA) {
                path = Pathfinder.dijkstra(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
            } else {
                path = new ArrayList<>();
            }

            boolean moved = false;
            if (!path.isEmpty()) {
                Point nextPosition = path.get(0);
                if (!willCollide(snake, nextPosition)) {
                    snake.setDirection(nextPosition);
                    snake.move();
                    moved = true;
                }
            }
            if (!moved) {
                Point newHead = new Point(snake.getHead().x + snake.direction.x, snake.getHead().y + snake.direction.y);
                if (!willCollide(snake, newHead)) {
                    snake.move();
                    moved = true;
                } else {
                    List<Point> possibleDirections = getPossibleDirections(snake.direction);
                    for (Point dir : possibleDirections) {
                        Point testHead = new Point(snake.getHead().x + dir.x, snake.getHead().y + dir.y);
                        if (!willCollide(snake, testHead)) {
                            snake.setDirection(testHead);
                            snake.move();
                            moved = true;
                            break;
                        }
                    }
                    if (!moved) {
                        snakesToRemove.add(snake);
                    }
                }
            }

            if (snake.getHead().equals(eatable.position)) {
                snake.eatEatable();
                List<List<Point>> bodies = snakes.stream().map(s -> s.body).collect(Collectors.toList());
                eatable.spawn(bodies);
            }
        }

        // Remove snakes marked for removal
        snakes.removeAll(snakesToRemove);

        if (snakes.size() <= 1) {
            gameOver = true;
            // Optionally stop the handler here if needed.
        }
    }

    private boolean willCollide(Snake currentSnake, Point nextPosition) {
        // Check boundaries: only allow positions from MIN_POS to MAX_POS inclusive
        if (nextPosition.x < MIN_POS || nextPosition.x > MAX_POS ||
                nextPosition.y < MIN_POS || nextPosition.y > MAX_POS) {
            return true;
        }
        // Check collision with all snakes
        for (Snake snake : snakes) {
            for (int i = 0; i < snake.body.size(); i++) {
                Point p = snake.body.get(i);
                if (p.equals(nextPosition)) {
                    // Allow the snake to move into its own tail
                    if (snake == currentSnake && i == snake.body.size() - 1) {
                        continue;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private List<Point> getPossibleDirections(Point currentDirection) {
        List<Point> directions = new ArrayList<>();
        // Add current direction
        directions.add(new Point(currentDirection.x, currentDirection.y));
        // Add perpendicular directions
        if (currentDirection.x != 0) { // Horizontal movement
            directions.add(new Point(0, 1));
            directions.add(new Point(0, -1));
        } else if (currentDirection.y != 0) { // Vertical movement
            directions.add(new Point(1, 0));
            directions.add(new Point(-1, 0));
        }
        return directions;
    }

    // A simple player move method – you could later adjust to handle touch or key events
    private void movePlayerSnake(List<Snake> snakesToRemove) {
        if (playerSnake == null) return;
        Point newHead = new Point(playerSnake.getHead().x + playerSnake.direction.x,
                playerSnake.getHead().y + playerSnake.direction.y);
        if (willCollide(playerSnake, newHead)) {
            snakesToRemove.add(playerSnake);
            playerSnake = null;
        } else {
            playerSnake.move();
            if (playerSnake.getHead().equals(eatable.position)) {
                playerSnake.eatEatable();
                List<List<Point>> bodies = snakes.stream().map(s -> s.body).collect(Collectors.toList());
                eatable.spawn(bodies);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Get the actual view size
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Determine the maximum square that fits in the view
        int squareSize = Math.min(viewWidth, viewHeight);
        // Compute the new unit size: the grid is 100 units wide.
        int newUnit = squareSize / WIDTH;  // WIDTH is 100
        // Compute offsets to center the grid
        int offsetX = (viewWidth - (newUnit * WIDTH)) / 2;
        int offsetY = (viewHeight - (newUnit * HEIGHT)) / 2;

        // Debugging Logs
        Log.d("GameViewDebug", "View Width: " + viewWidth);
        Log.d("GameViewDebug", "View Height: " + viewHeight);
        Log.d("GameViewDebug", "Calculated UNIT_SIZE (newUnit): " + newUnit);
        Log.d("GameViewDebug", "Game Area Width: " + (newUnit * WIDTH));
        Log.d("GameViewDebug", "Game Area Height: " + (newUnit * HEIGHT));
        Log.d("GameViewDebug", "OffsetX: " + offsetX);
        Log.d("GameViewDebug", "OffsetY: " + offsetY);

        // Draw background for the whole view (or just the game area if you prefer)
        canvas.drawColor(Color.BLACK);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);

        // Draw borders around the grid (top, bottom, left, right)
        // Top border:
        canvas.drawRect(offsetX, offsetY, offsetX + (WIDTH * newUnit), offsetY + newUnit, borderPaint);
        // Bottom border:
        canvas.drawRect(offsetX, offsetY + (HEIGHT * newUnit) - newUnit, offsetX + (WIDTH * newUnit), offsetY + (HEIGHT * newUnit), borderPaint);
        // Left border:
        canvas.drawRect(offsetX, offsetY, offsetX + newUnit, offsetY + (HEIGHT * newUnit), borderPaint);
        // Right border:
        canvas.drawRect(offsetX + (WIDTH * newUnit) - newUnit, offsetY, offsetX + (WIDTH * newUnit), offsetY + (HEIGHT * newUnit), borderPaint);

        // Draw the eatable
        eatable.paint(canvas, newUnit, offsetX, offsetY);

        // Draw all snakes
        for (Snake snake : snakes) {
            snake.paint(canvas, newUnit, offsetX, offsetY);
        }

        // Draw Game Over text if needed
        if (gameOver) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(newUnit * 2); // Adjust text size as needed
            String text = "Game Over!";
            float textWidth = textPaint.measureText(text);
            float x = offsetX + ((WIDTH * newUnit) - textWidth) / 2;
            float y = offsetY + (HEIGHT * newUnit) / 2;
            canvas.drawText(text, x, y, textPaint);
        }
    }

    // Optional: Override onKeyDown() or onTouchEvent() to handle user input.
    // For example, using onKeyDown for arrow key simulation (if using a hardware keyboard/emulator)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (playerSnake != null) {
            Point currentDir = playerSnake.direction;
            // For simplicity, use left/right to rotate the snake’s direction
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                playerSnake.direction = new Point(-currentDir.y, currentDir.x);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                playerSnake.direction = new Point(currentDir.y, -currentDir.x);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public void stopGameLoop(){
        handler.removeCallbacks(gameRunnable);
    }
    public void cleanup(){
        stopGameLoop();
    }
}