package Assign32starter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * The Leaderboard class manages a leaderboard consisting of player names and their corresponding high scores.
 * It provides thread-safe operations for updating scores and retrieving a formatted leaderboard.
 */
public class Leaderboard {
    private static final String LEADERBOARD_FILE = "leaderboard.json";
    // Use a map to store leaderboard entries: key = playerName + ipAddress, value = score
    private static final Map<String, Double> scores = new HashMap<>();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Leaderboard.class);

    static {
        loadLeaderboard();
    }

    /**
     * Updates the score for a specific player in the leaderboard if the new score is higher than
     * the existing score or if the player does not already exist in the leaderboard.
     * If an update occurs, the leaderboard is saved persistently.
     *
     * @param playerKey the unique identifier for the player whose score is being updated.
     * @param newScore  the new score to be compared with the existing score for the player.
     */
    public static synchronized void updateScore(String playerKey, double newScore) {
        if (!scores.containsKey(playerKey) || newScore > scores.get(playerKey)) {
            scores.put(playerKey, newScore);
            saveLeaderboard();
        }
    }

    /**
     * Generates a formatted string representation of the leaderboard, showing player names
     * and their scores in descending order of scores.
     * The scores are displayed with two decimal places.
     *
     * @return a string representing the leaderboard with player names and scores
     */
    public static synchronized String getFormattedLeaderboard() {
        // Sort entries descending by score.
        StringBuilder sb = new StringBuilder("Leaderboard:\n");
        scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> sb.append(entry.getKey()).append(": ").append(String.format("%.2f", entry.getValue())).append("\n"));
        return sb.toString();
    }

    /**
     * Loads the leaderboard data from a file and populates the in-memory scores map.
     * This method reads the leaderboard file specified by the `LEADERBOARD_FILE` constant.
     * If the file does not exist, the method simply returns without performing any operation.
     * If the file exists, it parses the JSON content and extracts player keys and their corresponding scores,
     * adding them to the `scores` map.
     * The method is synchronized to ensure thread safety when accessing or modifying
     * shared resources such as the `scores` map.
     * Exception handling is provided to log any errors that might occur during file reading
     * or JSON parsing operations.
     */
    private static synchronized void loadLeaderboard() {
        File file = new File(LEADERBOARD_FILE);
        if (!file.exists()) {
            return; // No leaderboard file yet.
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonStr = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr.append(line);
            }
            JSONObject jsonObject = new JSONObject(jsonStr.toString());
            JSONArray leaderboardArray = jsonObject.getJSONArray("leaderboard");
            for (int i = 0; i < leaderboardArray.length(); i++) {
                JSONObject entry = leaderboardArray.getJSONObject(i);
                String key = entry.getString("playerKey");
                double score = entry.getDouble("score");
                scores.put(key, score);
            }
        } catch (Exception e) {
            logger.error("Error loading leaderboard: ", e);
        }
    }

    /**
     * Saves the current state of the leaderboard to a persistent storage file.
     * The leaderboard data is serialized into JSON format and written to the specified file.
     * Each player's score is represented as a JSON object containing their unique key and score value.
     * If an IOException occurs during writing, it is logged as an error.
     * A success message is logged after the operation completes.
     * This method is synchronized to ensure thread safety when accessing and writing the leaderboard data.
     */
    private static synchronized void saveLeaderboard() {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            JSONObject obj = new JSONObject();
            obj.put("playerKey", entry.getKey());
            obj.put("score", entry.getValue());
            array.put(obj);
        }
        JSONObject root = new JSONObject();
        root.put("leaderboard", array);
        try (PrintWriter writer = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            writer.write(root.toString(2));
        } catch (IOException e) {
            logger.error("Error saving leaderboard: ", e);
        } finally {
            logger.info("Leaderboard saved successfully.");
        }
    }
}
