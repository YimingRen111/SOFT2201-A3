package invaders.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GameEventPublisher {
    private static GameEventPublisher instance;
    private List<Consumer<ScoreEvent>> scoreListeners = new ArrayList<>();

    private GameEventPublisher() {}

    public static GameEventPublisher getInstance() {
        if (instance == null) {
            instance = new GameEventPublisher();
        }
        return instance;
    }

    public void subscribeScoreEvent(Consumer<ScoreEvent> listener) {
        scoreListeners.add(listener);
    }

    public void publishScoreEvent(ScoreEvent event) {
        for (Consumer<ScoreEvent> listener : scoreListeners) {
            listener.accept(event);
        }
    }
}


