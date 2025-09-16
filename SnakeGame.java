import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends Application {
    // Game board size
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int TILE_SIZE = 20;
    private static final int ROWS = HEIGHT / TILE_SIZE;
    private static final int COLS = WIDTH / TILE_SIZE;

    // Directions
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction direction = Direction.RIGHT;
    private boolean moved = false; // Prevent reversing direction in one frame
    private boolean running = false;

    // Snake body as a list of points (x, y)
    private LinkedList<int[]> snake = new LinkedList<>();
    private int[] food = new int[2];
    private int score = 0;
    private Random random = new Random();

    // Game speed (higher is slower)
    private static final long MOVE_INTERVAL = 120_000_000; // nanoseconds
    private long lastMove = 0;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        // Initialize game
        resetGame();

        // Handle key presses for direction
        scene.setOnKeyPressed(e -> {
            if (!moved) return;
            KeyCode code = e.getCode();
            switch (code) {
                case UP:
                    if (direction != Direction.DOWN) direction = Direction.UP;
                    break;
                case DOWN:
                    if (direction != Direction.UP) direction = Direction.DOWN;
                    break;
                case LEFT:
                    if (direction != Direction.RIGHT) direction = Direction.LEFT;
                    break;
                case RIGHT:
                    if (direction != Direction.LEFT) direction = Direction.RIGHT;
                    break;
                default:
                    break;
            }
            moved = false;
        });

        // Main game loop using AnimationTimer
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running) {
                    drawGameOver(gc);
                    return;
                }
                if (now - lastMove > MOVE_INTERVAL) {
                    update();
                    draw(gc);
                    lastMove = now;
                    moved = true;
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Snake Game (JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();
        canvas.requestFocus();
    }

    // Reset the game to initial state
    private void resetGame() {
        snake.clear();
        // Start with 3 segments in the center
        int startX = COLS / 2;
        int startY = ROWS / 2;
        for (int i = 0; i < 3; i++) {
            snake.add(new int[] { startX - i, startY });
        }
        direction = Direction.RIGHT;
        score = 0;
        running = true;
        spawnFood();
    }

    // Update snake position and check collisions
    private void update() {
        // Get current head position
        int[] head = snake.getFirst();
        int newX = head[0];
        int newY = head[1];
        switch (direction) {
            case UP: newY--; break;
            case DOWN: newY++; break;
            case LEFT: newX--; break;
            case RIGHT: newX++; break;
        }
        // Check wall collision
        if (newX < 0 || newY < 0 || newX >= COLS || newY >= ROWS) {
            running = false;
            return;
        }
        // Check self collision
        for (int i = 0; i < snake.size(); i++) {
            int[] part = snake.get(i);
            if (part[0] == newX && part[1] == newY) {
                running = false;
                return;
            }
        }
        // Move snake
        snake.addFirst(new int[] { newX, newY });
        // Check food collision
        if (newX == food[0] && newY == food[1]) {
            score++;
            spawnFood();
        } else {
            snake.removeLast(); // Remove tail if not eating
        }
    }

    // Draw the game board, snake, food, and score
    private void draw(GraphicsContext gc) {
        // Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        // Draw grid
        gc.setStroke(Color.DARKGRAY);
        for (int x = 0; x <= WIDTH; x += TILE_SIZE) {
            gc.strokeLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y <= HEIGHT; y += TILE_SIZE) {
            gc.strokeLine(0, y, WIDTH, y);
        }
        // Draw food
        gc.setFill(Color.RED);
        gc.fillRect(food[0] * TILE_SIZE, food[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        // Draw snake
        gc.setFill(Color.LIMEGREEN);
        for (int i = 0; i < snake.size(); i++) {
            int[] part = snake.get(i);
            gc.fillRect(part[0] * TILE_SIZE, part[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
        // Draw score
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(24));
        gc.fillText("Score: " + score, 10, 28);
    }

    // Draw game over message
    private void drawGameOver(GraphicsContext gc) {
        draw(gc);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(48));
        gc.fillText("Game Over", WIDTH / 2.0 - 120, HEIGHT / 2.0);
        gc.setFont(Font.font(24));
        gc.fillText("Press any key to restart", WIDTH / 2.0 - 130, HEIGHT / 2.0 + 40);
    }

    // Spawn food at a random empty location
    private void spawnFood() {
        while (true) {
            int x = random.nextInt(COLS);
            int y = random.nextInt(ROWS);
            boolean onSnake = false;
            for (int[] part : snake) {
                if (part[0] == x && part[1] == y) {
                    onSnake = true;
                    break;
                }
            }
            if (!onSnake) {
                food[0] = x;
                food[1] = y;
                break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
