package invaders.observer;

import invaders.rendering.Renderable;
import javafx.util.Duration;

import java.util.List;

public interface GameObserver {
    void updateTime(Duration time);
    void updateScore(int score);

    void onRenderablesRemoved(List<Renderable> removedRenderables);
}