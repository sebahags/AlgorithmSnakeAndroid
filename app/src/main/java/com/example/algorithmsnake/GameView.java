package com.example.algorithmsnake;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// class for gamelogic and rendering the game
public class GameView extends View {
    private static final int GRID_WIDTH = 75;
    private static final int GRID_HEIGHT = 75;
    private static final int MIN_POS = 1;
    private static final int MAX_POS = 73;
    private List<Snake> snakes;
    private Eatable eatable;
    private boolean gameOver = false;
    private boolean playerMode = false;
    private int gameSpeedMillis;
    private Snake playerSnake;
    private final Handler handler = new Handler(); // gameloop handler
    private final Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!gameOver) {
                updateGame();
                invalidate();
                handler.postDelayed(this, gameSpeedMillis);
            } else {
                Log.d("GameLoop", "Game Over detected, stopping loop.");
            }
        }
    };

    // constructors
    public GameView(Context context, boolean isPlayerMode, int gameSpeed) {
        super(context);
        this.playerMode = isPlayerMode;
        this.gameSpeedMillis = Math.max(15, Math.min(gameSpeed, 100)); // check in case for gamespeed
        Log.d("GameViewInit", "Constructor: PlayerMode=" + isPlayerMode + ", Speed=" + this.gameSpeedMillis);
        initGame();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.playerMode = false; // Default to simulation
        this.gameSpeedMillis = 55; // Default speed
        Log.d("GameViewInit", "Constructor (AttributeSet): Using default PlayerMode=false, Speed=55");
        initGame();
    }

    public GameView(Context context) {
        super(context);
        this.playerMode = false;
        this.gameSpeedMillis = 55;
        Log.d("GameViewInit", "Constructor (Context only): Using default PlayerMode=false, Speed=55");
        initGame();
    }

    private void initGame() {
        Log.d("GameViewInit", "initGame() started.");
        gameOver = false;
        snakes = new ArrayList<>();
        setKeepScreenOn(true);
        if (playerMode) {
            Point playerStartPos = new Point(GRID_WIDTH / 2, GRID_HEIGHT / 2);
            playerSnake = new Snake(playerStartPos, Color.MAGENTA, null, false, false);
            snakes.add(playerSnake);
            Log.d("GameViewInit", "Player snake created at (" + playerStartPos.x + "," + playerStartPos.y + ")");
        }

        // npc snakes
        snakes.add(new Snake(new Point(GRID_WIDTH / 2 - 10, GRID_HEIGHT / 2 + 5), Color.GREEN, Snake.PathAlgorithm.ASTAR, true, true));
        snakes.add(new Snake(new Point(GRID_WIDTH / 4, GRID_HEIGHT / 4), Color.RED, Snake.PathAlgorithm.BFS, true, true));
        snakes.add(new Snake(new Point(GRID_WIDTH * 3 / 4, GRID_HEIGHT * 3 / 4), Color.YELLOW, Snake.PathAlgorithm.DIJKSTRA, true, true));
        Log.d("GameViewInit", "AI snakes added. Total snakes: " + snakes.size());

        // eatable spawning
        eatable = new Eatable();
        spawnEatableSafely();

        // start gameloop
        handler.removeCallbacks(gameRunnable);
        handler.postDelayed(gameRunnable, gameSpeedMillis);
        Log.d("GameViewInit", "Game loop scheduled to start in " + gameSpeedMillis + "ms.");
        setFocusable(true);
        setFocusableInTouchMode(true);
        Log.d("GameViewInit", "initGame() finished.");
    }

    private void spawnEatableSafely() {
        if (eatable == null) return;
        List<Point> allSnakePoints = snakes.stream()
                .flatMap(s -> s.body.stream())
                .collect(Collectors.toList());
        eatable.spawn(allSnakePoints, MIN_POS, MAX_POS); // Pass bounds to Eatable.spawn
        Log.d("GameViewLogic", "Eatable spawned at: (" + eatable.position.x + "," + eatable.position.y + ")");
    }


    // game update logic
    private void updateGame() {
        if (gameOver || snakes == null || eatable == null) return;
        List<Snake> snakesToRemove = new ArrayList<>();
        List<Snake> currentSnakes = new ArrayList<>(snakes);
        List<List<Point>> allBodies = currentSnakes.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.body != null)
                .map(s -> s.body)
                .collect(Collectors.toList());

        for (Snake snake : currentSnakes) {
            if (snake == null || snakesToRemove.contains(snake)) continue;
            // snake movement
            if (snake.isAi) {
                moveAiSnake(snake, allBodies, snakesToRemove);
            } else if (playerMode && snake == playerSnake) {
                movePlayerSnake(snakesToRemove);
            }
            // eating of eatable
            if (!snakesToRemove.contains(snake) && eatable.position != null && snake.getHead().equals(eatable.position)) {
                snake.eatEatable();
                Log.d("GameViewLogic", "Snake " + snake.color + " ate the eatable. Score: " + snake.score);
                spawnEatableSafely();
            }
        }

        // remove colliding snakes
        if (!snakesToRemove.isEmpty()) {
            Log.d("GameViewLogic", "Removing " + snakesToRemove.size() + " snakes.");
            snakes.removeAll(snakesToRemove);
        }

        // check if game over
        if (playerMode && playerSnake != null && snakesToRemove.contains(playerSnake)) {
            Log.i("GameViewLogic", "Game Over: Player snake collided.");
            gameOver = true;
            playerSnake = null;
        }
        else if (!playerMode && snakes.size() <= 1) {
            Log.i("GameViewLogic", "Game Over: Simulation ended with " + snakes.size() + " snakes remaining.");
            gameOver = true;
        }

        if (gameOver) {
            stopGameLoop();
            invalidate();
        }
    }

    private void movePlayerSnake(List<Snake> snakesToRemove) {
        if (playerSnake == null || gameOver) return;

        // next pos for the head
        Point newHead = new Point(playerSnake.getHead().x + playerSnake.direction.x,
                playerSnake.getHead().y + playerSnake.direction.y);

        // collision check
        if (willCollide(playerSnake, newHead)) {
            Log.d("PlayerMove", "Player collision detected at (" + newHead.x + "," + newHead.y + ")");
            snakesToRemove.add(playerSnake);
        } else {
            playerSnake.move();
        }
    }

    // npc snake movmeent
    private void moveAiSnake(Snake snake, List<List<Point>> allBodies, List<Snake> snakesToRemove) {
        if (snake == null || !snake.isAi || gameOver || eatable == null || eatable.position == null) return;
        List<Point> path = null;
        boolean moved = false;
        Point nextStep = null;
        List<Point> currentPath = snake.getCurrentPath();
        Point currentTarget = snake.getCurrentTarget();

        if (currentTarget == null || !currentTarget.equals(eatable.position)) {
            Log.d("AIMove", "Snake " + snake.color + ": Target changed or null. Clearing path.");
            currentPath = null;
            snake.setCurrentPath(null, null);
        }

        if (currentPath != null && !currentPath.isEmpty()) {
            Point potentialNextStep = currentPath.get(0); // next step in the path
            if (willCollide(snake, potentialNextStep)) {
                Log.w("AIMove", "Snake " + snake.color + ": Next step on stored path (" + potentialNextStep.x + "," + potentialNextStep.y + ") is blocked. Clearing path.");
                snake.setCurrentPath(null, null);
                currentPath = null;
            } else {
                nextStep = potentialNextStep;
                Log.d("AIMove", "Snake " + snake.color + ": Following stored path. Next step: (" + nextStep.x + "," + nextStep.y + ")");
            }
        }

        if (nextStep == null) {
            Log.d("AIMove", "Snake " + snake.color + ": No valid stored path. Calculating new path to ("+ eatable.position.x + "," + eatable.position.y + ")");
            List<Point> newPath = null;
            try {
                if (snake.algorithm == Snake.PathAlgorithm.ASTAR) {
                    newPath = Pathfinder.aStar(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
                } else if (snake.algorithm == Snake.PathAlgorithm.BFS) {
                    newPath = Pathfinder.bfs(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
                } else if (snake.algorithm == Snake.PathAlgorithm.DIJKSTRA) {
                    newPath = Pathfinder.dijkstra(snake, eatable, allBodies, snake.optimal, MIN_POS, MAX_POS);
                }
            } catch (Exception e) {
                Log.e("AIMove", "Pathfinding error for snake " + snake.color + ": " + e.getMessage(), e);
                newPath = null;
            }

            if (newPath != null && !newPath.isEmpty()) {
                Log.d("AIMove", "Snake " + snake.color + ": New path calculated with " + newPath.size() + " steps.");
                snake.setCurrentPath(newPath, eatable.position);
                currentPath = snake.getCurrentPath();
                Point firstStepOfNewPath = currentPath.get(0);

                if (willCollide(snake, firstStepOfNewPath)) {
                    Log.w("AIMove", "Snake " + snake.color + ": Immediately calculated path step (" + firstStepOfNewPath.x + "," + firstStepOfNewPath.y + ") is blocked. Clearing path again.");
                    snake.setCurrentPath(null, null); // Clear path
                    currentPath = null;
                } else {
                    nextStep = firstStepOfNewPath;
                }
            } else {
                Log.w("AIMove", "Snake " + snake.color + ": Pathfinding failed or returned empty path.");
                snake.setCurrentPath(null, null);
                currentPath = null;
            }
        }

        if (nextStep != null) {
            if (!willCollide(snake, nextStep)) {
                snake.setDirectionTowards(nextStep);
                snake.move();
                moved = true;

                List<Point> pathInSnake = snake.getCurrentPath();
                if (pathInSnake != null && !pathInSnake.isEmpty()) {
                    if (pathInSnake.get(0).equals(nextStep)) {
                        pathInSnake.remove(0);
                    } else {
                        Log.e("AIMove", "Snake " + snake.color + ": Path inconsistency detected before removing step. Clearing path.");
                        snake.setCurrentPath(null, null);
                    }
                }
            } else {
                Log.w("AIMove", "Snake " + snake.color + ": Final collision check failed for step (" + nextStep.x + "," + nextStep.y + "). Clearing path.");
                snake.setCurrentPath(null, null);
            }
        }

        if (!moved) {
            Log.d("AIMove", "Snake " + snake.color + ": Entering fallback movement logic.");
            if (snake.direction != null && (snake.direction.x != 0 || snake.direction.y != 0)) {
                Point currentDirHead = new Point(snake.getHead().x + snake.direction.x, snake.getHead().y + snake.direction.y);
                if (!willCollide(snake, currentDirHead)) {
                    Log.d("AIMove", "Snake " + snake.color + ": Fallback - Moving in current direction.");
                    snake.move();
                    moved = true;
                }
            }

            if (!moved) {
                Log.d("AIMove", "Snake " + snake.color + ": Fallback - Trying perpendicular directions.");
                List<Point> possibleDirs = getPerpendicularDirections(snake.direction);
                Collections.shuffle(possibleDirs);
                for (Point dir : possibleDirs) {
                    Point testHead = new Point(snake.getHead().x + dir.x, snake.getHead().y + dir.y);
                    if (!willCollide(snake, testHead)) {
                        Log.d("AIMove", "Snake " + snake.color + ": Fallback - Moving perpendicularly.");
                        snake.direction = dir;
                        snake.move();
                        moved = true;
                        break;
                    }
                }
            }
        }
        if (!moved) {
            snakesToRemove.add(snake);
            Log.w("AIMove", "AI Snake " + snake.color + " could not find any valid move (including fallbacks) and was removed.");
        }
    }

    // collision check
    private boolean willCollide(Snake currentSnake, Point nextPosition) {
        // game area boundaries
        if (nextPosition.x < MIN_POS || nextPosition.x > MAX_POS ||
                nextPosition.y < MIN_POS || nextPosition.y > MAX_POS) {
            return true;
        }

        // other snakes
        for (Snake snake : snakes) {
            for (int i = 0; i < snake.body.size(); i++) {
                Point bodyPart = snake.body.get(i);

                if (bodyPart.equals(nextPosition)) {
                    if (snake == currentSnake && i == 0) {
                        continue; // the head will move
                    }

                    // allow to "collide" with tail since it will move
                    if (snake == currentSnake && i == snake.body.size() - 1) {
                        continue;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private List<Point> getPerpendicularDirections(Point currentDirection) {
        List<Point> directions = new ArrayList<>();
        if (currentDirection == null) return directions;
        if (currentDirection.x != 0) {
            directions.add(new Point(0, 1));
            directions.add(new Point(0, -1));
        } else if (currentDirection.y != 0) {
            directions.add(new Point(1, 0));
            directions.add(new Point(-1, 0));
        } else {
            directions.add(new Point(1, 0));
            directions.add(new Point(-1, 0));
            directions.add(new Point(0, 1));
            directions.add(new Point(0, -1));
        }
        return directions;
    }


    // rendering the game
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) return;
        try {
            int viewWidth = getWidth(); int viewHeight = getHeight();
            if (viewWidth <= 0 || viewHeight <= 0) return;
            int maxSquareSize = Math.min(viewWidth, viewHeight);
            int unitSize = Math.max(1, maxSquareSize / GRID_WIDTH);
            int gameAreaWidth = unitSize * GRID_WIDTH;
            int gameAreaHeight = unitSize * GRID_HEIGHT;
            int offsetX = (viewWidth - gameAreaWidth) / 2;
            int offsetY = (viewHeight - gameAreaHeight) / 2;

            // game area background
            Paint gameAreaPaint = new Paint();
            gameAreaPaint.setColor(Color.BLACK);
            canvas.drawRect(offsetX, offsetY, offsetX + gameAreaWidth, offsetY + gameAreaHeight, gameAreaPaint);

            // game area boarders
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(offsetX, offsetY, offsetX + gameAreaWidth, offsetY + unitSize, borderPaint);
            canvas.drawRect(offsetX, offsetY + gameAreaHeight - unitSize, offsetX + gameAreaWidth, offsetY + gameAreaHeight, borderPaint);
            canvas.drawRect(offsetX, offsetY, offsetX + unitSize, offsetY + gameAreaHeight, borderPaint);
            canvas.drawRect(offsetX + gameAreaWidth - unitSize, offsetY, offsetX + gameAreaWidth, offsetY + gameAreaHeight, borderPaint);

            // eatable
            if (eatable != null && eatable.position != null) {
                eatable.paint(canvas, unitSize, offsetX, offsetY);
            }

            // snakes
            List<Snake> localSnakes = this.snakes;
            if (localSnakes != null) {
                for (Snake snake : new ArrayList<>(localSnakes)) {
                    if (snake != null) {
                        snake.paint(canvas, unitSize, offsetX, offsetY);
                    }
                }
            }

            // game over text
            if (gameOver) {
                Paint textPaint = new Paint();
                textPaint.setColor(Color.RED);
                textPaint.setTextSize(Math.max(20f, unitSize * 4f)); // Adjust multiplier as needed
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setAntiAlias(true);
                String text = "Game Over!";
                float x = offsetX + gameAreaWidth / 2.0f;
                float y = offsetY + gameAreaHeight / 2.0f - (textPaint.descent() + textPaint.ascent()) / 2;
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.argb(180, 0, 0, 0));
                float textWidth = textPaint.measureText(text);
                float bgPadding = unitSize * 2f;
                canvas.drawRect(x - textWidth / 2 - bgPadding, y + textPaint.ascent() - bgPadding,
                        x + textWidth / 2 + bgPadding, y + textPaint.descent() + bgPadding, bgPaint);
                canvas.drawText(text, x, y, textPaint);
            }
        } catch (Exception e) {
            Log.e("onDraw", "Error during drawing: " + e.getMessage(), e);
        }
    }

    public void setPlayerDirection(Point requestedDirection) {
        if (playerSnake == null || !playerMode || gameOver || requestedDirection == null) {
            return;
        }

        Point currentDir = playerSnake.direction;

        if (currentDir == null) {
            Log.w("SetPlayerDir", "Current direction is null, attempting to set directly.");
            playerSnake.direction = requestedDirection;
            return;
        }

        // check for current dir
        if (currentDir.equals(requestedDirection)) {
            Log.d("SetPlayerDir", "Ignoring direction change: Same direction requested.");
            return;
        }

        // opposite direction not allowed
        if (requestedDirection.x == -currentDir.x && requestedDirection.y == -currentDir.y) {
            if (playerSnake.body != null && playerSnake.body.size() > 1) {
                Log.d("SetPlayerDir", "Ignoring direction change: Opposite direction requested.");
                return;
            }
        }

        playerSnake.direction = requestedDirection;
        Log.d("SetPlayerDir", "Set player direction to: (" + requestedDirection.x + "," + requestedDirection.y + ")");
    }

    public void stopGameLoop() {
        if (handler != null) {
            handler.removeCallbacks(gameRunnable);
            Log.d("GameViewLifecycle", "Game loop callbacks removed.");
        }
    }

    public void cleanup() {
        Log.d("GameViewLifecycle", "cleanup() called.");
        stopGameLoop();
        snakes = null;
        eatable = null;
        playerSnake = null;
        setKeepScreenOn(false);
    }

    public void endGameAndCleanup() {
        Log.d("GameViewLifecycle", "endGameAndCleanup called.");
        if (!gameOver) {
            this.gameOver = true;
            Log.d("GameViewLifecycle", "Setting gameOver=true.");
            invalidate();
        }
        cleanup();
        setKeepScreenOn(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("GameViewLifecycle", "onDetachedFromWindow called. Performing cleanup.");
        cleanup();
        setKeepScreenOn(false);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            Log.d("GameViewLifecycle", "Window became hidden/invisible.");
        } else if (visibility == View.VISIBLE) {
            Log.d("GameViewLifecycle", "Window became visible.");
        }
    }
}