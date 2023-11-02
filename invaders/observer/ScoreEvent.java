package invaders.observer;

public class ScoreEvent {
    private int score;

    public ScoreEvent(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
