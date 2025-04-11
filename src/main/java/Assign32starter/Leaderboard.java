package Assign32starter;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The Leaderboard class manages a leaderboard consisting of player names and their corresponding high scores.
 * It provides thread-safe operations for updating scores and retrieving a formatted leaderboard.
 */
public class Leaderboard {
    /**
     * A thread-safe map that tracks the highest scores achieved by players.
     * The keys represent player names as strings, and the values represent their best scores as doubles.
     * This map ensures thread-safety using a {@link ConcurrentHashMap}, allowing concurrent access and updates to player scores.
     */
    // Map from player names to best score.
    private static final Map<String, Double> scores = new ConcurrentHashMap<>();

    /**
     * Updates the score for a specific player. If the player does not exist in the leaderboard,
     * the player is added with the given score. If the player's score already exists, the score
     * is updated only if the new score is higher than the existing score.
     *
     * @param playerName the name of the player whose score needs to be updated
     * @param newScore   the new score to be updated for the player
     */
    public static void updateScore(String playerName, double newScore) {
        scores.compute(playerName, (name, oldScore) -> {
            if (oldScore == null || newScore > oldScore) {
                return newScore;
            }
            return oldScore;
        });
    }

    /**
     * Retrieves the leaderboard as a formatted string, displaying player names and their scores
     * in descending order of scores.
     *
     * @return A string representation of the leaderboard where each line contains a player's name
     * followed by their score, formatted to two decimal places and sorted by score in descending
     * order.
     */
    public static String getLeaderboard() {
        // Return a formatted string listing the leaderboard, sorted by score descending.
        return scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(entry -> entry.getKey() + ": " + String.format("%.2f", entry.getValue()))
                .collect(Collectors.joining("\n"));
    }
}
