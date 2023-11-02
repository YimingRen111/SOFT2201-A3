package invaders.engine;

import java.util.List;
import java.util.ArrayList;

import invaders.entities.EntityViewImpl;
import invaders.entities.SpaceBackground;
import invaders.observer.GameObserver;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import invaders.entities.EntityView;
import invaders.rendering.Renderable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;


public class GameWindow implements GameObserver {
	private Scene scene;
    private Pane pane;
    private GameEngine model;
    private List<EntityView> entityViews =  new ArrayList<EntityView>();
    private Renderable background;
    private Stage primaryStage;
    private Label timeLabel;
    private Label scoreLabel;
    private int score;
    private Duration time;
    private HBox statusDisplay;


    private double xViewportOffset = 0.0;
    private double yViewportOffset = 0.0;
    // private static final double VIEWPORT_MARGIN = 280.0;
    private final int initialWidth;
    private final int initialHeight;
    private Timeline timeline;


    public GameWindow(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.initialWidth = 800; // Set the initial width
        this.initialHeight = 600; // Set the initial height
        this.score = 0;
        this.time = Duration.ZERO;
        this.timeLabel = new Label("Time: 0:00");
        this.scoreLabel = new Label("Score: 0");
        this.statusDisplay = new HBox(10, timeLabel, scoreLabel);
        // Initialize the difficulty menu
        createDifficultyMenu();
    }

    private void createDifficultyMenu() {
        VBox menu = new VBox(10);
        menu.setPrefWidth(initialWidth);
        menu.setPrefHeight(initialHeight);
        menu.setStyle("-fx-alignment: center;");

        Button easyButton = new Button("Easy");
        Button normalButton = new Button("Normal");
        Button hardButton = new Button("Hard");

        easyButton.setOnAction(event -> onDifficultySelected("easy"));
        normalButton.setOnAction(event -> onDifficultySelected("medium"));
        hardButton.setOnAction(event -> onDifficultySelected("hard"));

        menu.getChildren().addAll(easyButton, normalButton, hardButton);

        Scene menuScene = new Scene(menu, initialWidth, initialHeight);
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void onDifficultySelected(String difficulty) {
        // set game engine based on the difficulty.
        this.model = new GameEngine(difficulty);
        this.model.addObserver(this);
        this.pane = new Pane();
        this.pane.getChildren().add(statusDisplay);
        this.background = new SpaceBackground(model, pane);

        this.scene = new Scene(pane, model.getGameWidth(), model.getGameHeight());

        // add keyboardHandler
        KeyboardInputHandler keyboardInputHandler = new KeyboardInputHandler(model);
        scene.setOnKeyPressed(keyboardInputHandler::handlePressed);
        scene.setOnKeyReleased(keyboardInputHandler::handleReleased);

        primaryStage.setScene(scene);
        primaryStage.show();

        run();
    }

    public void run() {
        timeline = new Timeline(new KeyFrame(Duration.millis(17), event -> {
            // Update and draw game
            model.update();
            draw();
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void draw(){
        model.update();

        List<Renderable> renderables = model.getRenderables();
        for (Renderable entity : renderables) {
            boolean notFound = true;
            for (EntityView view : entityViews) {
                if (view.matchesEntity(entity)) {
                    notFound = false;
                    view.update(xViewportOffset, yViewportOffset);
                    break;
                }
            }
            if (notFound) {
                EntityView entityView = new EntityViewImpl(entity);
                entityViews.add(entityView);
                pane.getChildren().add(entityView.getNode());
            }
        }

        for (Renderable entity : renderables){
            if (!entity.isAlive()){
                for (EntityView entityView : entityViews){
                    if (entityView.matchesEntity(entity)){
                        entityView.markForDelete();
                    }
                }
            }
        }

        for (EntityView entityView : entityViews) {
            if (entityView.isMarkedForDelete()) {
                pane.getChildren().remove(entityView.getNode());
            }
        }


        model.getGameObjects().removeAll(model.getPendingToRemoveGameObject());
        model.getGameObjects().addAll(model.getPendingToAddGameObject());
        model.getRenderables().removeAll(model.getPendingToRemoveRenderable());
        model.getRenderables().addAll(model.getPendingToAddRenderable());

        model.getPendingToAddGameObject().clear();
        model.getPendingToRemoveGameObject().clear();
        model.getPendingToAddRenderable().clear();
        model.getPendingToRemoveRenderable().clear();

        entityViews.removeIf(EntityView::isMarkedForDelete);

    }

	public Scene getScene() {
        return scene;
    }

    public void updateTime(Duration time) {
        this.time = time;
        long seconds = (long) time.toSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format("Time: %d:%02d", absSeconds / 60, absSeconds % 60);
        timeLabel.setText(positive);

//        System.out.println(timeLabel.getText()); //Test
    }

    @Override
    public void updateScore(int score) {
        this.score = score;
        System.out.println("GameWindow score update: " + score);
        scoreLabel.setText("Score: " + this.score);
    }

    @Override
    public void onRenderablesRemoved(List<Renderable> removedRenderables) {
        // Test
//        System.out.println("onRenderablesRemoved called. Renderables to remove: " + removedRenderables.size());
//        System.out.println("Current entityViews: " + entityViews);
//        System.out.println("Removed renderables: " + removedRenderables);

        pane.getChildren().removeIf(node -> {
            for (EntityView view : entityViews) {
                if (removedRenderables.contains(view.getRenderable()) && node == view.getNode()) {
                    return !(view.getRenderable() instanceof SpaceBackground);
                }
            }
            return false;
        });

        entityViews.removeIf(view -> removedRenderables.contains(view.getRenderable()) && !(view.getRenderable() instanceof SpaceBackground));
    }

}
