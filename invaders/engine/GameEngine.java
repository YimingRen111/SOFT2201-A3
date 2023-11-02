package invaders.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import invaders.builder.BunkerBuilder;
import invaders.builder.Director;
import invaders.builder.EnemyBuilder;
import invaders.factory.EnemyProjectile;
import invaders.factory.Projectile;
import invaders.gameobject.Bunker;
import invaders.gameobject.Enemy;
import invaders.gameobject.GameObject;
import invaders.entities.Player;
import invaders.memento.GameMemento;
import invaders.observer.GameEventPublisher;
import invaders.observer.GameObserver;
import invaders.observer.ScoreEvent;
import invaders.rendering.Renderable;
import invaders.singleton.DifficultyLevel;
import invaders.strategy.FastProjectileStrategy;
import invaders.strategy.SlowProjectileStrategy;
import javafx.util.Duration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class manages the main loop and logic of the game
 */
public class GameEngine {
	private List<GameObject> gameObjects = new ArrayList<>(); // A list of game objects that gets updated each frame
	private List<GameObject> pendingToAddGameObject = new ArrayList<>();
	private List<GameObject> pendingToRemoveGameObject = new ArrayList<>();

	private List<Renderable> pendingToAddRenderable = new ArrayList<>();
	private List<Renderable> pendingToRemoveRenderable = new ArrayList<>();

	private List<Renderable> renderables =  new ArrayList<>();

	private Player player;

	private boolean left;
	private boolean right;
	private int gameWidth;
	private int gameHeight;
	private int timer = 45;
	private float timeElapsed; // time after the game start
	private int score;
	private List<GameObserver> observers;
	private long lastUpdateTime;
	private GameMemento memento;

	public GameEngine(String difficulty){

		lastUpdateTime = System.nanoTime();
		observers = new ArrayList<>();

		DifficultyLevel difficultyLevel = DifficultyLevel.getInstance(difficulty);

		JSONObject gameInfo = difficultyLevel.getGameInfo();
		JSONObject playerInfo = difficultyLevel.getPlayerInfo();
		JSONArray bunkersInfo = difficultyLevel.getBunkersInfo();
		JSONArray enemiesInfo = difficultyLevel.getEnemiesInfo();
		GameEventPublisher.getInstance().subscribeScoreEvent(event -> updateScore(event.getScore()));

		// Get game width and height from the difficulty level instance
		gameWidth = ((Long)((JSONObject) gameInfo.get("size")).get("x")).intValue();
		gameHeight = ((Long)((JSONObject) gameInfo.get("size")).get("y")).intValue();

		//Get player info
		this.player = new Player(playerInfo);
		renderables.add(player);



		Director director = new Director();
		BunkerBuilder bunkerBuilder = new BunkerBuilder();
		//Get Bunkers info
		for(Object eachBunkerInfo : bunkersInfo){
			Bunker bunker = director.constructBunker(bunkerBuilder, (JSONObject) eachBunkerInfo);
			gameObjects.add(bunker);
			renderables.add(bunker);
		}


		EnemyBuilder enemyBuilder = new EnemyBuilder();
		//Get Enemy info
		for(Object eachEnemyInfo : enemiesInfo){
			Enemy enemy = director.constructEnemy(this, enemyBuilder, (JSONObject) eachEnemyInfo);
			gameObjects.add(enemy);
			renderables.add(enemy);
		}

	}

	/**
	 * Updates the game/simulation
	 */
	public void update(){
		timer+=1;

		long currentTime = System.nanoTime();
		float deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0f;
		lastUpdateTime = currentTime;
		updateTime(deltaTime);
		movePlayer();

		for(GameObject go: gameObjects){
			go.update(this);
		}

		for (int i = 0; i < renderables.size(); i++) {
			Renderable renderableA = renderables.get(i);
			for (int j = i + 1; j < renderables.size(); j++) {
				Renderable renderableB = renderables.get(j);

				// Check if either object is an enemy projectile and the other is an enemy to prevent self-collision
				if (!((renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("Enemy")) ||
						(renderableA.getRenderableObjectName().equals("Enemy") && renderableB.getRenderableObjectName().equals("EnemyProjectile")) ||
						(renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("EnemyProjectile")) ||
						(renderableA.getRenderableObjectName().equals("Enemy") && renderableB.getRenderableObjectName().equals("Enemy")))) {
					if (renderableA.isColliding(renderableB) && renderableA.getHealth() > 0 && renderableB.getHealth() > 0) {
						renderableA.takeDamage(1);
						renderableB.takeDamage(1);

						// if collide with player's projectile and enemy's projectile, gain points
						if ((renderableA.getRenderableObjectName().equals("PlayerProjectile") && renderableB.getRenderableObjectName().equals("EnemyProjectile")) ||
								(renderableA.getRenderableObjectName().equals("EnemyProjectile") && renderableB.getRenderableObjectName().equals("PlayerProjectile"))) {
							EnemyProjectile enemyProjectile = (EnemyProjectile)(renderableA.getRenderableObjectName().equals("EnemyProjectile") ? renderableA : renderableB);
							GameEventPublisher.getInstance().publishScoreEvent(new ScoreEvent(enemyProjectile.getScore()));
						}
					}
				}
			}
		}


		// ensure that renderable foreground objects don't go off-screen
		int offset = 1;
		for(Renderable ro: renderables){
			if(!ro.getLayer().equals(Renderable.Layer.FOREGROUND)){
				continue;
			}
			if(ro.getPosition().getX() + ro.getWidth() >= gameWidth) {
				ro.getPosition().setX((gameWidth - offset) -ro.getWidth());
			}

			if(ro.getPosition().getX() <= 0) {
				ro.getPosition().setX(offset);
			}

			if(ro.getPosition().getY() + ro.getHeight() >= gameHeight) {
				ro.getPosition().setY((gameHeight - offset) -ro.getHeight());
			}

			if(ro.getPosition().getY() <= 0) {
				ro.getPosition().setY(offset);
			}
		}

	}

	public List<Renderable> getRenderables(){
		return renderables;
	}

	public List<GameObject> getGameObjects() {
		return gameObjects;
	}
	public List<GameObject> getPendingToAddGameObject() {
		return pendingToAddGameObject;
	}

	public List<GameObject> getPendingToRemoveGameObject() {
		return pendingToRemoveGameObject;
	}

	public List<Renderable> getPendingToAddRenderable() {
		return pendingToAddRenderable;
	}

	public List<Renderable> getPendingToRemoveRenderable() {
		return pendingToRemoveRenderable;
	}


	public void leftReleased() {
		this.left = false;
	}

	public void rightReleased(){
		this.right = false;
	}

	public void leftPressed() {
		this.left = true;
	}
	public void rightPressed(){
		this.right = true;
	}

	public boolean shootPressed(){
		if(timer>45 && player.isAlive()){
			Projectile projectile = player.shoot();
			gameObjects.add(projectile);
			renderables.add(projectile);
			timer=0;
			return true;
		}
		return false;
	}

	private void movePlayer(){
		if(left){
			player.left();
		}

		if(right){
			player.right();
		}
	}

	public int getGameWidth() {
		return gameWidth;
	}

	public int getGameHeight() {
		return gameHeight;
	}

	public Player getPlayer() {
		return player;
	}

	public void updateScore(int value) {
		this.score += value;
		notifyScoreChanged();
	}

	public void setScore(int scoreValue){
		this.score = scoreValue;
	}

	public void updateTime(float deltaTime) {
		this.timeElapsed += deltaTime;
		notifyTimeChanged();
	}

	public void setTimeElapsed(float timeElapsedSnapshot){
		this.timeElapsed = timeElapsedSnapshot;
	}

	private void notifyScoreChanged() {
//		System.out.println("Score updated to: " + score); // Test
		for (GameObserver observer : observers) {
			observer.updateScore(score);
		}
	}

	private void notifyTimeChanged() {
//		System.out.println("Time updated to: " + timeElapsed); // Test
		for (GameObserver observer : observers) {
			observer.updateTime(Duration.millis(timeElapsed * 1000));
		}
	}

	public void addObserver(GameObserver observer) {
		observers.add(observer);
	}

	public void saveStateToMemento() {
		// Filter only enemies and their projectiles from the renderables before saving the state
		List<Renderable> enemiesAndProjectiles = renderables.stream()
				.filter(r -> r instanceof Enemy || r instanceof Projectile)
				.collect(Collectors.toList());

		// Create a deep copy of enemies and projectiles and save the current score and time
		memento = new GameMemento(new ArrayList<>(enemiesAndProjectiles), score, timeElapsed);
	}

	public void restoreStateFromMemento() {
		if (memento != null) {
			List<Renderable> toRemove = getEnemiesAndProjectiles();
			clearEnemiesAndProjectiles();
			memento.restoreState(this);
			for (GameObserver observer : observers) {
				observer.onRenderablesRemoved(toRemove);
			}

			notifyScoreChanged();
			notifyTimeChanged();
		}
	}



	public void addAllRenderablesAndGameObjects(List<Renderable> newRenderables) {
		renderables.addAll(newRenderables);

		for (Renderable r : newRenderables) {
			if (r instanceof GameObject) {
				gameObjects.add((GameObject) r);
			}
		}
	}

	public void clearEnemiesAndProjectiles() {
		// Remove only the enemies and enemy projectiles from gameObjects and renderables
		gameObjects.removeIf(obj -> obj instanceof Enemy || obj instanceof EnemyProjectile);
		renderables.removeIf(ren -> ren instanceof Enemy || ren instanceof EnemyProjectile);
	}

//	public void clearEnemiesAndProjectiles() {
//		gameObjects.stream()
//				.filter(obj -> obj instanceof Enemy || obj instanceof EnemyProjectile)
//				.forEach(obj -> ((Renderable)obj).takeDamage(((Renderable)obj).getHealth()));
//
//		renderables.stream()
//				.filter(ren -> ren instanceof Enemy || ren instanceof EnemyProjectile)
//				.forEach(ren -> ren.takeDamage(ren.getHealth()));
//	}


	private List<Renderable> getEnemiesAndProjectiles() {
		return renderables.stream()
				.filter(r -> r instanceof Enemy || r instanceof EnemyProjectile)
				.collect(Collectors.toList());
	}

	public void cheatRemoveFastProjectiles() {
		List<Renderable> fastProjectiles = renderables.stream()
				.filter(r -> r instanceof EnemyProjectile && ((EnemyProjectile) r).getStrategy() instanceof FastProjectileStrategy)
				.collect(Collectors.toList());
		fastProjectiles.forEach(projectile -> projectile.takeDamage(projectile.getHealth()));
		updateScore(fastProjectiles.size()*2);
	}

	public void cheatRemoveSlowProjectiles() {
		List<Renderable> slowProjectiles = renderables.stream()
				.filter(r -> r instanceof EnemyProjectile && ((EnemyProjectile) r).getStrategy() instanceof SlowProjectileStrategy)
				.collect(Collectors.toList());

		slowProjectiles.forEach(projectile -> projectile.takeDamage(projectile.getHealth()));
		updateScore(slowProjectiles.size());
	}



	public void cheatRemoveEnemiesWithFastProjectiles() {
		List<Renderable> enemiesWithFastProjectiles = renderables.stream()
				.filter(r -> r instanceof Enemy && ((Enemy) r).getProjectileStrategy() instanceof FastProjectileStrategy)
				.collect(Collectors.toList());

		enemiesWithFastProjectiles.forEach(enemy -> enemy.takeDamage(enemy.getHealth()));
		updateScore(enemiesWithFastProjectiles.size() * 4);
	}


	public void cheatRemoveEnemiesWithSlowProjectiles() {
		List<Renderable> enemiesWithSlowProjectiles = renderables.stream()
				.filter(r -> r instanceof Enemy && ((Enemy) r).getProjectileStrategy() instanceof SlowProjectileStrategy)
				.collect(Collectors.toList());

		enemiesWithSlowProjectiles.forEach(enemy -> enemy.takeDamage(enemy.getHealth()));
		updateScore(enemiesWithSlowProjectiles.size() * 3);
	}

}
