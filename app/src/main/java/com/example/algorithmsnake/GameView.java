package com.example.algorithmsnake;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point; // Make sure you're using android.graphics.Point
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameView extends View {
    // Game constants
    // Grid dimensions (number of units)
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 100;
    // Note: UNIT_SIZE is now calculated dynamically in onDraw based on view size
    // private static final int UNIT_SIZE = 5; // Removed fixed unit size

    private static final int GAME_SPEED = 25; // Default speed if not provided (milliseconds per update)
    private static final int MIN_POS = 1;     // Min valid grid coordinate (inclusive)
    private static final int MAX_POS = 98;    // Max valid grid coordinate (inclusive)

    // Game objects
    private List<Snake> snakes;
    private Eatable eatable;
    private boolean gameOver = false;
    private boolean playerMode = false;
    private int gameSpeedMillis; // Actual speed used for the game loop delay
    private Snake playerSnake;

    // A Handler for our game loop.
    private final Handler handler = new Handler();
    private final Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gameOver) {
                updateGame();
                invalidate();  // Triggers onDraw redraw
                // Continue the loop
                handler.postDelayed(this, gameSpeedMillis);
            } else {
                Log.d("GameLoop", "Game Over detected, stopping loop.");
                // Optionally ensure cleanup is called one last time if needed
                // cleanup(); // Usually handled by lifecycle methods
            }
        }
    };

    // Constructors
    public GameView(Context context, boolean isPlayerMode, int gameSpeed) {
        super(context);
        this.playerMode = isPlayerMode;
        // Ensure speed is within reasonable bounds (e.g., 15ms to 1000ms)
        this.gameSpeedMillis = Math.max(15, Math.min(gameSpeed, 1000));
        Log.d("GameViewInit", "Constructor: PlayerMode=" + isPlayerMode + ", Speed=" + this.gameSpeedMillis);
        initGame();
    }

    // Constructor used when inflating from XML (if applicable)
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Default values if inflated from XML without specific attributes
        this.playerMode = false; // Default to simulation
        this.gameSpeedMillis = 55; // Default speed
        Log.d("GameViewInit", "Constructor (AttributeSet): Using default PlayerMode=false, Speed=55");
        initGame();
    }

    // Basic constructor (less likely used directly with custom parameters)
    public GameView(Context context) {
        super(context);
        this.playerMode = false; // Default to simulation
        this.gameSpeedMillis = 55; // Default speed
        Log.d("GameViewInit", "Constructor (Context only): Using default PlayerMode=false, Speed=55");
        initGame();
    }

    // Initialize game state
    private void initGame() {
        Log.d("GameViewInit", "initGame() started.");
        gameOver = false; // Ensure game isn't over at start
        snakes = new ArrayList<>();

        // Create the player snake if needed
        // *** IMPORTANT: Ensure the Snake constructor sets a non-zero initial direction (e.g., new Point(1, 0)) ***
        if (playerMode) {
            // Initial position for player snake
            Point playerStartPos = new Point(GRID_WIDTH / 2, GRID_HEIGHT / 2);
            playerSnake = new Snake(playerStartPos, Color.MAGENTA, null, false, false); // Assuming 'null' algorithm means player-controlled
            snakes.add(playerSnake);
            Log.d("GameViewInit", "Player snake created at (" + playerStartPos.x + "," + playerStartPos.y + ")");
        }

        // Add AI-controlled snakes with different algorithms and starting positions
        // Ensure these start positions are valid (within MIN_POS/MAX_POS if applicable to start)
        snakes.add(new Snake(new Point(GRID_WIDTH / 2 - 10, GRID_HEIGHT / 2 + 5), Color.GREEN, Snake.PathAlgorithm.ASTAR, true, true));
        snakes.add(new Snake(new Point(GRID_WIDTH / 4, GRID_HEIGHT / 4), Color.RED, Snake.PathAlgorithm.BFS, true, true));
        snakes.add(new Snake(new Point(GRID_WIDTH * 3 / 4, GRID_HEIGHT * 3 / 4), Color.YELLOW, Snake.PathAlgorithm.DIJKSTRA, true, true));
        Log.d("GameViewInit", "AI snakes added. Total snakes: " + snakes.size());

        // Create and spawn the eatable in a valid location
        eatable = new Eatable();
        spawnEatableSafely(); // Use helper to ensure valid spawn

        // Start the game loop only after initialization is complete
        handler.removeCallbacks(gameRunnable); // Remove any existing callbacks first
        handler.postDelayed(gameRunnable, gameSpeedMillis);
        Log.d("GameViewInit", "Game loop scheduled to start in " + gameSpeedMillis + "ms.");

        // Enable focus for potential keyboard input (mainly for emulators)
        setFocusable(true);
        setFocusableInTouchMode(true);
        Log.d("GameViewInit", "initGame() finished.");
    }

    // Helper to spawn eatable, ensuring it's not on a snake
    private void spawnEatableSafely() {
        if (eatable == null) return;
        // Collect all points occupied by all snakes
        List<Point> allSnakePoints = snakes.stream()
                .flatMap(s -> s.body.stream())
                .collect(Collectors.toList());
        eatable.spawn(allSnakePoints, MIN_POS, MAX_POS); // Pass bounds to Eatable.spawn
        Log.d("GameViewLogic", "Eatable spawned at: (" + eatable.position.x + "," + eatable.position.y + ")");
    }


    // Update game logic for one tick
    private void updateGame() {
        if (gameOver || snakes == null || eatable == null) return; // Don't update if already over

        List<Snake> snakesToRemove = new ArrayList<>();
        // Create a copy of the list to iterate over, allowing safe removal from the original list
        List<Snake> currentSnakes = new ArrayList<>(snakes);
        List<List<Point>> allBodies = currentSnakes.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.body != null) // Ensure body is not null before mapping
                .map(s -> s.body)            // Get the List<Point> for each snake
                .collect(Collectors.toList());

        for (Snake snake : currentSnakes) {
            // Skip snakes already marked for removal in this tick
            if (snake == null || snakesToRemove.contains(snake)) continue;
            // --- Move Snake ---
            if (snake.isAi) {
                // ** REVERTED: Pass the list-of-lists 'allBodies' to moveAiSnake **
                moveAiSnake(snake, allBodies, snakesToRemove);
            } else if (playerMode && snake == playerSnake) {
                movePlayerSnake(snakesToRemove);
            }
            // --- Check for Eating ---
            if (!snakesToRemove.contains(snake) && eatable.position != null && snake.getHead().equals(eatable.position)) {
                snake.eatEatable();
                Log.d("GameViewLogic", "Snake " + snake.color + " ate the eatable. Score: " + snake.score);
                spawnEatableSafely(); // Respawn eatable safely
            }
        } // End loop through snakes

        // --- Remove dead snakes ---
        if (!snakesToRemove.isEmpty()) {
            Log.d("GameViewLogic", "Removing " + snakesToRemove.size() + " snakes.");
            snakes.removeAll(snakesToRemove);
        }

        // --- Check Game Over Conditions ---
        // Player mode: Player snake was removed
        if (playerMode && playerSnake != null && snakesToRemove.contains(playerSnake)) {
            Log.i("GameViewLogic", "Game Over: Player snake collided.");
            gameOver = true;
            playerSnake = null; // Clear reference
        }
        // Simulation mode: Only one or zero snakes left
        else if (!playerMode && snakes.size() <= 1) {
            Log.i("GameViewLogic", "Game Over: Simulation ended with " + snakes.size() + " snakes remaining.");
            gameOver = true;
        }

        // --- Stop Handler if Game Over ---
        if (gameOver) {
            stopGameLoop();
            invalidate(); // Ensure final state (with Game Over text) is drawn
        }
    }

    /**
     * Moves the player-controlled snake based on its current direction.
     * Marks the snake for removal if a collision occurs.
     * @param snakesToRemove List to add the player snake to if it collides.
     */
    private void movePlayerSnake(List<Snake> snakesToRemove) {
        if (playerSnake == null || gameOver) return;

        // Calculate the potential new head position
        Point newHead = new Point(playerSnake.getHead().x + playerSnake.direction.x,
                playerSnake.getHead().y + playerSnake.direction.y);

        // Check for collision at the new position
        if (willCollide(playerSnake, newHead)) {
            Log.d("PlayerMove", "Player collision detected at (" + newHead.x + "," + newHead.y + ")");
            snakesToRemove.add(playerSnake); // Mark for removal
        } else {
            // No collision, move the snake
            playerSnake.move();
            // Log.d("PlayerMove", "Player moved. New head: (" + playerSnake.getHead().x + "," + playerSnake.getHead().y + ")");
        }
    }

    /**
     * Moves an AI-controlled snake using pathfinding or fallback movement.
     * Marks the snake for removal if it collides or cannot move.
     * @param snake The AI snake to move.
     * @param allBodies A list containing the body points of all snakes currently in the game.
     * @param snakesToRemove List to add the AI snake to if it collides or gets stuck.
     */
    private void moveAiSnake(Snake snake, List<List<Point>> allBodies, List<Snake> snakesToRemove) {
        if (snake == null || !snake.isAi || gameOver) return;

        List<Point> path = null;
        boolean moved = false;

        // --- 1. Pathfinding ---
        try {
            if (snake.algorithm == Snake.PathAlgorithm.ASTAR) {
                path = Pathfinder.aStar(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
            } else if (snake.algorithm == Snake.PathAlgorithm.BFS) {
                path = Pathfinder.bfs(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
            } else if (snake.algorithm == Snake.PathAlgorithm.DIJKSTRA) {
                path = Pathfinder.dijkstra(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
            }
            // Log.d("AIMove", "Pathfinding for " + snake.color + ": " + (path != null && !path.isEmpty() ? path.size() + " steps" : "No path"));
        } catch (Exception e) {
            Log.e("AIMove", "Pathfinding error for snake " + snake.color + ": " + e.getMessage(), e);
            path = null; // Ensure path is null on error
        }


        // --- 2. Try moving along the calculated path ---
        if (path != null && !path.isEmpty()) {
            Point nextPosition = path.get(0); // Get the first step in the path
            // Ensure path step is not the current head and doesn't collide
            if (!nextPosition.equals(snake.getHead()) && !willCollide(snake, nextPosition)) {
                snake.setDirectionTowards(nextPosition); // Update direction based on path step
                snake.move();
                moved = true;
                // Log.d("AIMove", "AI Snake " + snake.color + " moved via path to (" + nextPosition.x + "," + nextPosition.y + ")");
            } else {
                Log.w("AIMove", "AI Snake " + snake.color + " path step invalid/collides: (" + nextPosition.x + "," + nextPosition.y + ")");
            }
        }

        // --- 3. If path failed or didn't exist, try moving in the current direction ---
        if (!moved) {
            // Ensure direction is not (0,0) before calculating next head
            if (snake.direction.x != 0 || snake.direction.y != 0) {
                Point currentDirHead = new Point(snake.getHead().x + snake.direction.x, snake.getHead().y + snake.direction.y);
                if (!willCollide(snake, currentDirHead)) {
                    // No need to set direction, just move
                    snake.move();
                    moved = true;
                    // Log.d("AIMove", "AI Snake " + snake.color + " moved via current direction.");
                }
            }
        }

        // --- 4. If still not moved, try perpendicular directions ---
        if (!moved) {
            List<Point> possibleDirs = getPerpendicularDirections(snake.direction);
            Collections.shuffle(possibleDirs); // Randomize turn choice to avoid deterministic loops

            for (Point dir : possibleDirs) {
                Point testHead = new Point(snake.getHead().x + dir.x, snake.getHead().y + dir.y);
                if (!willCollide(snake, testHead)) {
                    snake.direction = dir; // Set the new (perpendicular) direction
                    snake.move();
                    moved = true;
                    // Log.d("AIMove", "AI Snake " + snake.color + " moved via perpendicular direction ("+dir.x+","+dir.y+").");
                    break; // Move successfully
                }
            }
        }

        // --- 5. If absolutely no move was possible, remove the snake ---
        if (!moved) {
            snakesToRemove.add(snake);
            Log.w("AIMove", "AI Snake " + snake.color + " could not find any valid move and was removed.");
        }
    }


    /**
     * Checks if the proposed next position for a snake will result in a collision.
     * Collisions include going out of bounds or hitting any part of any snake's body,
     * except for the moving snake's own tail (allowing movement into the space just vacated).
     *
     * @param currentSnake The snake that is attempting to move.
     * @param nextPosition The potential next position of the snake's head.
     * @return true if a collision will occur, false otherwise.
     */
    private boolean willCollide(Snake currentSnake, Point nextPosition) {
        // --- Check Boundaries ---
        // Uses MIN_POS and MAX_POS which define the playable grid area
        if (nextPosition.x < MIN_POS || nextPosition.x > MAX_POS ||
                nextPosition.y < MIN_POS || nextPosition.y > MAX_POS) {
            // Log.v("CollisionCheck", "Boundary collision for (" + nextPosition.x + "," + nextPosition.y + ")");
            return true; // Collision with boundary
        }

        // --- Check Collision with All Snakes ---
        for (Snake snake : snakes) { // Iterate through all snakes currently on the board
            for (int i = 0; i < snake.body.size(); i++) {
                Point bodyPart = snake.body.get(i);

                if (bodyPart.equals(nextPosition)) { // Check if the body part occupies the target position
                    // --- Collision Exceptions ---

                    // 1. NEW: Ignore collision with the MOVING snake's OWN HEAD.
                    //    This prevents immediate game over if initial direction is (0,0)
                    //    or pathfinding starts with the current location. Assumes head is index 0.
                    if (snake == currentSnake && i == 0) {
                        continue; // It's the head of the snake trying to move, allow it (it will move FROM here)
                    }

                    // 2. Allow the snake to move into the position currently occupied by its OWN TAIL.
                    //    This is necessary because the tail will move out of that spot in the same game tick.
                    //    Assumes tail is the last element in the body list.
                    if (snake == currentSnake && i == snake.body.size() - 1) {
                        continue; // Allow moving into the space the tail is currently vacating
                    }

                    // If it's not one of the exceptions, it's a real collision.
                    // Log.v("CollisionCheck", "Collision detected at (" + nextPosition.x + "," + nextPosition.y + ") with snake " + snake.color + " body part " + i);
                    return true;
                }
            }
        }

        // No collisions detected
        return false;
    }

    /**
     * Gets a list of directions perpendicular to the given direction.
     * @param currentDirection The snake's current direction vector.
     * @return A list containing the two perpendicular direction vectors. Returns cardinal directions if input is (0,0).
     */
    private List<Point> getPerpendicularDirections(Point currentDirection) {
        List<Point> directions = new ArrayList<>();
        if (currentDirection == null) return directions; // Safety check

        if (currentDirection.x != 0) { // Moving horizontally (Left/Right)
            directions.add(new Point(0, 1));  // Down
            directions.add(new Point(0, -1)); // Up
        } else if (currentDirection.y != 0) { // Moving vertically (Up/Down)
            directions.add(new Point(1, 0));  // Right
            directions.add(new Point(-1, 0)); // Left
        } else {
            // Handle (0,0) case or if snake isn't moving - provide all directions as options
            directions.add(new Point(1, 0));
            directions.add(new Point(-1, 0));
            directions.add(new Point(0, 1));
            directions.add(new Point(0, -1));
        }
        return directions;
    }


    // --- Drawing Logic (onDraw) ---
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;

        try {
            int viewWidth = getWidth(); int viewHeight = getHeight();
            if (viewWidth <= 0 || viewHeight <= 0) return;

            int maxSquareSize = Math.min(viewWidth, viewHeight);
            // ** Use GRID_WIDTH for unit size calculation assuming square grid units based on width **
            int unitSize = Math.max(1, maxSquareSize / GRID_WIDTH);
            // Calculate game area size based on unitSize and grid dimensions
            int gameAreaWidth = unitSize * GRID_WIDTH;
            int gameAreaHeight = unitSize * GRID_HEIGHT; // Use GRID_HEIGHT here
            // Recalculate offsets based on potentially non-square game area
            int offsetX = (viewWidth - gameAreaWidth) / 2;
            int offsetY = (viewHeight - gameAreaHeight) / 2;

            // --- Drawing Starts ---

            // 1. Draw background for the whole view
            //canvas.drawColor(Color.DKGRAY); // Can use this or just black below

            // 2. Draw the game area background (Black)
            Paint gameAreaPaint = new Paint();
            gameAreaPaint.setColor(Color.BLACK);
            // Use calculated game area dimensions
            canvas.drawRect(offsetX, offsetY, offsetX + gameAreaWidth, offsetY + gameAreaHeight, gameAreaPaint);

            // 3. ** Draw Borders like the old version **
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStyle(Paint.Style.FILL); // Use FILL for solid borders

            // Ensure borders are drawn correctly using gameAreaWidth/Height and unitSize
            // Top border:
            canvas.drawRect(offsetX, offsetY, offsetX + gameAreaWidth, offsetY + unitSize, borderPaint);
            // Bottom border:
            canvas.drawRect(offsetX, offsetY + gameAreaHeight - unitSize, offsetX + gameAreaWidth, offsetY + gameAreaHeight, borderPaint);
            // Left border:
            canvas.drawRect(offsetX, offsetY, offsetX + unitSize, offsetY + gameAreaHeight, borderPaint);
            // Right border:
            canvas.drawRect(offsetX + gameAreaWidth - unitSize, offsetY, offsetX + gameAreaWidth, offsetY + gameAreaHeight, borderPaint);


            // 4. Draw Eatable
            if (eatable != null && eatable.position != null) {
                // Pass the calculated unitSize, offsetX, offsetY
                eatable.paint(canvas, unitSize, offsetX, offsetY);
            }

            // 5. Draw Snakes
            List<Snake> localSnakes = this.snakes; // Use local variable
            if (localSnakes != null) {
                // Iterate over a copy using the local variable
                for (Snake snake : new ArrayList<>(localSnakes)) {
                    if (snake != null) {
                        // Pass the calculated unitSize, offsetX, offsetY
                        snake.paint(canvas, unitSize, offsetX, offsetY);
                    }
                }
            }

            // 6. Draw Game Over Text (Keep improved version)
            if (gameOver) {
                Paint textPaint = new Paint();
                textPaint.setColor(Color.RED);
                // Make text size relative to unit size, ensure it's reasonable
                textPaint.setTextSize(Math.max(20f, unitSize * 4f)); // Adjust multiplier as needed
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setAntiAlias(true);

                String text = "Game Over!";
                // Center text within the game area
                float x = offsetX + gameAreaWidth / 2.0f;
                float y = offsetY + gameAreaHeight / 2.0f - (textPaint.descent() + textPaint.ascent()) / 2;

                // Optional: Background for text
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.argb(180, 0, 0, 0));
                float textWidth = textPaint.measureText(text);
                // Adjust background size based on text size
                float bgPadding = unitSize * 2f; // Padding around text
                canvas.drawRect(x - textWidth / 2 - bgPadding, y + textPaint.ascent() - bgPadding,
                        x + textWidth / 2 + bgPadding, y + textPaint.descent() + bgPadding, bgPaint);

                canvas.drawText(text, x, y, textPaint);
            }
        } catch (Exception e) {
            Log.e("onDraw", "Error during drawing: " + e.getMessage(), e);
        }
    }


    // --- Player Input Handling ---

    /**
     * Attempts to set the player snake's direction to the requested absolute direction.
     * Ignores the request if the game is over, not in player mode, snake is null,
     * or if the requested direction is the same as or opposite to the current direction.
     *
     * @param requestedDirection The absolute direction Point (e.g., new Point(0, -1) for UP).
     * Should be one of DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT.
     */
    public void setPlayerDirection(Point requestedDirection) {
        // Basic checks
        if (playerSnake == null || !playerMode || gameOver || requestedDirection == null) {
            return;
        }

        Point currentDir = playerSnake.direction;

        // Safety check if current direction is somehow null
        if (currentDir == null) {
            Log.w("SetPlayerDir", "Current direction is null, attempting to set directly.");
            playerSnake.direction = requestedDirection;
            return;
        }

        // 1. Check if requested direction is the same as current
        if (currentDir.equals(requestedDirection)) {
            Log.d("SetPlayerDir", "Ignoring direction change: Same direction requested.");
            return; // No change needed
        }

        // 2. Check if requested direction is the exact opposite
        if (requestedDirection.x == -currentDir.x && requestedDirection.y == -currentDir.y) {
            // Allow turning opposite only if snake length is 1 (edge case, unlikely)
            if (playerSnake.body != null && playerSnake.body.size() > 1) {
                Log.d("SetPlayerDir", "Ignoring direction change: Opposite direction requested.");
                return; // Prevent 180 degree turn
            }
        }

        // If checks pass, set the new direction
        playerSnake.direction = requestedDirection;
        Log.d("SetPlayerDir", "Set player direction to: (" + requestedDirection.x + "," + requestedDirection.y + ")");
    }


    // --- Game Lifecycle & Cleanup ---

    /**
     * Stops the game loop handler.
     */
    public void stopGameLoop() {
        if (handler != null) {
            handler.removeCallbacks(gameRunnable);
            Log.d("GameViewLifecycle", "Game loop callbacks removed.");
        }
    }

    /**
     * Performs cleanup: stops the game loop. Call this when the game is ending or the view is destroyed.
     */
    public void cleanup() {
        Log.d("GameViewLifecycle", "cleanup() called.");
        stopGameLoop();
        // Add any other resource cleanup needed here (e.g., release sound pools, bitmaps)
        snakes = null; // Help garbage collection
        eatable = null;
        playerSnake = null;
    }

    /**
     * Stops the game loop and marks the game as over visually.
     * Intended to be called explicitly when exiting the game screen via UI (like an exit button).
     */
    public void endGameAndCleanup() {
        Log.d("GameViewLifecycle", "endGameAndCleanup called.");
        if (!gameOver) {
            this.gameOver = true; // Mark game as over visually if not already
            Log.d("GameViewLifecycle", "Setting gameOver=true.");
            invalidate(); // Request redraw to show game over message immediately
        }
        cleanup(); // Perform actual cleanup (stop loop, release resources)
    }


    // Called when the view is detached from the window (e.g., screen navigation, activity destroyed).
    // This is a crucial place for cleanup.
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("GameViewLifecycle", "onDetachedFromWindow called. Performing cleanup.");
        cleanup(); // Ensure game loop stops and resources are released
    }

    // Optional: Handle visibility changes (e.g., app goes to background)
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            // Optional: Pause game loop when not visible? Depends on desired behavior.
            // stopGameLoop();
            Log.d("GameViewLifecycle", "Window became hidden/invisible.");
        } else if (visibility == View.VISIBLE) {
            // Optional: Resume game loop if it was paused?
            // if (!gameOver) { handler.postDelayed(gameRunnable, gameSpeedMillis); }
            Log.d("GameViewLifecycle", "Window became visible.");
        }
    }

    // --- Dependencies (Make sure these classes exist and are correct) ---
    // Needs:
    // - Snake class (with body List<Point>, Point direction, color, isAi, algorithm, eatEatable(), move(), getHead(), setDirectionTowards(Point))
    //   -> CRITICAL: Ensure Snake constructor sets an initial non-zero direction (e.g., new Point(1,0))
    // - Eatable class (with Point position, paint(), spawn(List<Point> occupied, int min, int max, int gridW, int gridH))
    // - Pathfinder class (with static aStar, bfs, dijkstra methods returning List<Point>)
    // - Snake.PathAlgorithm enum

}