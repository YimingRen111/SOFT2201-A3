package invaders.memento;

import invaders.engine.GameEngine;
import invaders.factory.EnemyProjectile;
import invaders.gameobject.Enemy;
import invaders.physics.Vector2D;
import invaders.rendering.Renderable;
import invaders.strategy.FastProjectileStrategy;
import invaders.strategy.ProjectileStrategy;
import invaders.strategy.SlowProjectileStrategy;
import java.util.ArrayList;
import java.util.List;

public class GameMemento {
    private List<Renderable> renderablesSnapshot;
    private int scoreSnapshot;
    private float timeElapsedSnapshot;

    public GameMemento(List<Renderable> renderables, int score, float timeElapsed) {
        this.renderablesSnapshot = deepCopyRenderables(renderables);
        this.scoreSnapshot = score;
        this.timeElapsedSnapshot = timeElapsed;
    }

    public void restoreState(GameEngine gameEngine) {
        // Add back the saved enemies and projectiles
        gameEngine.addAllRenderablesAndGameObjects(renderablesSnapshot);

        // Restore the score and time elapsed
        gameEngine.setScore(scoreSnapshot);
        gameEngine.setTimeElapsed(timeElapsedSnapshot);
    }


    // Implement deep copy logic
    private List<Renderable> deepCopyRenderables(List<Renderable> originalRenderables) {
        List<Renderable> copies = new ArrayList<>();
        for (Renderable original : originalRenderables) {
            Renderable copy = createDeepCopyRenderable(original);
            copies.add(copy);
        }
        return copies;
    }

    private Renderable createDeepCopyRenderable(Renderable original) {
        if (original instanceof Enemy) {
            Enemy enemyOriginal = (Enemy) original;
            Enemy enemyCopy = new Enemy(new Vector2D(enemyOriginal.getPosition().getX(), enemyOriginal.getPosition().getY()));
            enemyCopy.setLives((int) enemyOriginal.getHealth());
            enemyCopy.setImage(enemyOriginal.getImage());
            enemyCopy.setProjectileImage(enemyOriginal.getProjectileImage());
            enemyCopy.setxVel(enemyOriginal.getxVel());
            // Copy the projectile strategy
            if (enemyOriginal.getProjectileStrategy() instanceof FastProjectileStrategy) {
                enemyCopy.setProjectileStrategy(new FastProjectileStrategy());
            } else if (enemyOriginal.getProjectileStrategy() instanceof SlowProjectileStrategy) {
                enemyCopy.setProjectileStrategy(new SlowProjectileStrategy());
            }
            return enemyCopy;
        }else if (original instanceof EnemyProjectile) {
            EnemyProjectile enemyProjectile = (EnemyProjectile) original;
            Vector2D positionCopy = new Vector2D(enemyProjectile.getPosition().getX(), enemyProjectile.getPosition().getY());
            ProjectileStrategy strategyCopy = null;
            if (enemyProjectile.getStrategy() instanceof FastProjectileStrategy) {
                strategyCopy = new FastProjectileStrategy();
            } else if (enemyProjectile.getStrategy() instanceof SlowProjectileStrategy) {
                strategyCopy = new SlowProjectileStrategy();
            }
            EnemyProjectile projectileCopy = new EnemyProjectile(positionCopy, strategyCopy, enemyProjectile.getImage());
            return projectileCopy;
        }
        return original;
    }
}
