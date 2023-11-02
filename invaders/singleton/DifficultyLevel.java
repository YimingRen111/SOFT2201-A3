package invaders.singleton;

import invaders.ConfigReader;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class DifficultyLevel {
    private static final String CONFIG_PATH_PREFIX = "src/main/resources/";
    private static DifficultyLevel instance;
    private JSONObject gameInfo;
    private JSONObject playerInfo;
    private JSONArray bunkersInfo;
    private JSONArray enemiesInfo;
    private String difficulty;

    // Private constructor to prevent instantiation
    private DifficultyLevel(String difficulty) {
        this.difficulty = difficulty; // Store the difficulty level
        parseConfig(CONFIG_PATH_PREFIX + "config_" + difficulty + ".json");
    }

    // Public method to get the instance of the class
    public static DifficultyLevel getInstance(String difficulty) {
        if (instance == null || !instance.difficulty.equals(difficulty)) {
            instance = new DifficultyLevel(difficulty);
        }
        return instance;
    }

    // Parse configuration file and store data
    private void parseConfig(String configPath) {
        JSONObject configData = ConfigReader.parse(configPath);
        if (configData != null) {
            this.gameInfo = (JSONObject) configData.get("Game");
            this.playerInfo = (JSONObject) configData.get("Player");
            this.bunkersInfo = (JSONArray) configData.get("Bunkers");
            this.enemiesInfo = (JSONArray) configData.get("Enemies");
        }
    }

    // Accessor methods for the configuration data
    public JSONObject getGameInfo() {
        return gameInfo;
    }

    public JSONObject getPlayerInfo() {
        return playerInfo;
    }

    public JSONArray getBunkersInfo() {
        return bunkersInfo;
    }

    public JSONArray getEnemiesInfo() {
        return enemiesInfo;
    }

    // Method to retrieve the current difficulty level from the instance
    public String getDifficulty() {
        if (this.gameInfo != null) {
            return (String) this.gameInfo.get("difficulty");
        }
        return null;
    }
}
