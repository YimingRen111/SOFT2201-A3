package invaders.engine;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.*;

class KeyboardInputHandler {
    private final GameEngine model;
    private boolean left = false;
    private boolean right = false;
    private Set<KeyCode> pressedKeys = new HashSet<>();

    private Map<String, MediaPlayer> sounds = new HashMap<>();

    KeyboardInputHandler(GameEngine model) {
        this.model = model;

        // TODO (longGoneUser): Is there a better place for this code?
        URL mediaUrl = getClass().getResource("/shoot.wav");
        String jumpURL = mediaUrl.toExternalForm();

        Media sound = new Media(jumpURL);
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        sounds.put("shoot", mediaPlayer);
    }

    void handlePressed(KeyEvent keyEvent) {
        if (pressedKeys.contains(keyEvent.getCode())) {
            return;
        }
        pressedKeys.add(keyEvent.getCode());

        if (keyEvent.getCode().equals(KeyCode.SPACE)) {
            if (model.shootPressed()) {
                MediaPlayer shoot = sounds.get("shoot");
                shoot.stop();
                shoot.play();
            }
        }

        if (keyEvent.getCode().equals(KeyCode.LEFT)) {
            left = true;
        }
        if (keyEvent.getCode().equals(KeyCode.RIGHT)) {
            right = true;
        }
        if (keyEvent.getCode().equals((KeyCode.S))){
            model.saveStateToMemento();
        }
        if (keyEvent.getCode().equals((KeyCode.L))){
            model.restoreStateFromMemento();
        }
        if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.Q) {
            model.cheatRemoveFastProjectiles();
        }
        if (keyEvent.getCode() == KeyCode.W) {
            model.cheatRemoveSlowProjectiles();
        }
        if (keyEvent.getCode() == KeyCode.E) {
            model.cheatRemoveEnemiesWithFastProjectiles();
        }
        if (keyEvent.getCode() == KeyCode.R) {
            model.cheatRemoveEnemiesWithSlowProjectiles();
        }

        if (left) {
            model.leftPressed();
        }

        if(right){
            model.rightPressed();
        }
    }

    void handleReleased(KeyEvent keyEvent) {
        pressedKeys.remove(keyEvent.getCode());

        if (keyEvent.getCode().equals(KeyCode.LEFT)) {
            left = false;
            model.leftReleased();
        }
        if (keyEvent.getCode().equals(KeyCode.RIGHT)) {
            model.rightReleased();
            right = false;
        }
    }
}
