package Assign32starter.entity;

import Assign32starter.enums.States;

import java.util.Objects;

/**
 * Represents the state of a game at a specific point in time.
 * This class encapsulates the various attributes that define the game's current status,
 * including the image version, skips remaining, current answer, game stage, and start time.
 */
public class GameState {
    int imageVersion = 1;  // starting at 1 for the most pixelated version
    int skipsRemaining;    // initialize based on game duration (short/medium/long)
    String currentAnswer;// set to the current movie's title
    States gameStage;
    long gameStartTime;
    String currentMovie; // set to the current movie's title
    int correctGuesses = 0; // number of correct guesses

    /**
     * Default constructor for the GameState class.
     * Initializes a new instance of the GameState with default values:
     * - skipsRemaining is set to 0.
     * - currentAnswer is set to an empty string.
     * - gameStage is set to States.NOT_STARTED.
     * - gameStartTime is set to 0.
     */
    public GameState() {
        this.skipsRemaining = 0;
        this.currentAnswer = "";
        this.gameStage = States.NOT_STARTED;
        this.gameStartTime = 0;
        this.currentMovie = "";
        this.gameStartTime = System.currentTimeMillis();
    }

    /**
     * Checks whether this GameState instance is equal to the specified object.
     * Two GameState objects are considered equal if all their corresponding fields,
     * including imageVersion, skipsRemaining, currentAnswer, gameStage, gameStartTime,
     * and currentMovie, are equal.
     *
     * @param o the object to be compared for equality with this GameState instance
     * @return true if the specified object is equal to this GameState instance; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameState gameState)) return false;
        return getImageVersion() == gameState.getImageVersion() && getSkipsRemaining() == gameState.getSkipsRemaining() && getGameStartTime() == gameState.getGameStartTime() && getCorrectGuesses() == gameState.getCorrectGuesses() && Objects.equals(getCurrentAnswer(), gameState.getCurrentAnswer()) && getGameStage() == gameState.getGameStage() && Objects.equals(getCurrentMovie(), gameState.getCurrentMovie());
    }

    /**
     * Computes the hash code for the GameState object. The hash code is calculated
     * based on the values of the following fields: imageVersion, skipsRemaining,
     * currentAnswer, gameStage, gameStartTime, currentMovie, and correctGuesses.
     *
     * @return an integer representing the hash code of the current GameState object
     */
    @Override
    public int hashCode() {
        return Objects.hash(getImageVersion(), getSkipsRemaining(), getCurrentAnswer(), getGameStage(), getGameStartTime(), getCurrentMovie(), getCorrectGuesses());
    }

    /**
     * Returns a string representation of the GameState object. The representation includes
     * the values of the fields: imageVersion, skipsRemaining, currentAnswer, gameStage,
     * gameStartTime, currentMovie, and correctGuesses.
     *
     * @return a string describing the current state of the GameState object
     */
    @Override
    public String toString() {
        return "GameState{" +
                "imageVersion=" + imageVersion +
                ", skipsRemaining=" + skipsRemaining +
                ", currentAnswer='" + currentAnswer + '\'' +
                ", gameStage=" + gameStage +
                ", gameStartTime=" + gameStartTime +
                ", currentMovie='" + currentMovie + '\'' +
                ", correctGuesses=" + correctGuesses +
                '}';
    }

    /**
     * Retrieves the title of the current movie in the game state.
     *
     * @return the title of the current movie as a String
     */
    public String getCurrentMovie() {
        return currentMovie;
    }

    /**
     * Sets the current movie associated with the game state.
     *
     * @param currentMovie the title of the current movie to set
     */
    public void setCurrentMovie(String currentMovie) {
        this.currentMovie = currentMovie;
    }

    /**
     * Retrieves the current version of the image associated with the game state.
     *
     * @return the current image version as an integer
     */
    public int getImageVersion() {
        return imageVersion;
    }

    /**
     * Updates the version of the image to a specific value.
     *
     * @param imageVersion the new version of the image to set, typically used to
     *                     adjust the clarity or pixelation level of an image in the game.
     */
    public void setImageVersion(int imageVersion) {
        this.imageVersion = imageVersion;
    }

    /**
     * Retrieves the remaining number of skips available in the game state.
     *
     * @return the number of skips remaining
     */
    public int getSkipsRemaining() {
        return skipsRemaining;
    }

    /**
     * Sets the remaining skips for the current game session.
     *
     * @param skipsRemaining the number of skips remaining for the player
     */
    public void setSkipsRemaining(int skipsRemaining) {
        this.skipsRemaining = skipsRemaining;
    }

    /**
     * Retrieves the current answer associated with the game state.
     *
     * @return the current answer as a String
     */
    public String getCurrentAnswer() {
        return currentAnswer;
    }

    /**
     * Sets the current answer for the game state.
     *
     * @param currentAnswer the correct answer or title of the current movie in the game
     */
    public void setCurrentAnswer(String currentAnswer) {
        this.currentAnswer = currentAnswer;
    }

    /**
     * Retrieves the current stage of the game.
     * The game stage represents the current state or phase of gameplay,
     * defined by the {@code States} enumeration.
     *
     * @return the current game stage, which can be one of the following values:
     * {@code NOT_STARTED}, {@code IN_GAME_NO_IMAGE}, {@code IN_GAME_WITH_IMAGE},
     * or {@code GAME_OVER}.
     */
    public States getGameStage() {
        return gameStage;
    }

    /**
     * Sets the current game stage to the specified state.
     *
     * @param gameStage the new game stage to set; must be a valid {@link States} enum value
     */
    public void setGameStage(States gameStage) {
        this.gameStage = gameStage;
    }

    /**
     * Retrieves the start time of the game.
     *
     * @return the start time of the game as a long value.
     */
    public long getGameStartTime() {
        return gameStartTime;
    }

    /**
     * Sets the game's start time.
     *
     * @param gameStartTime the start time of the game, represented as a long value (e.g., Unix timestamp in milliseconds).
     */
    public void setGameStartTime(long gameStartTime) {
        this.gameStartTime = gameStartTime;
    }

    /**
     * Retrieves the number of correct guesses made in the current game state.
     *
     * @return the number of correct guesses as an integer
     */
    // Getter and setter for correct guesses.
    public int getCorrectGuesses() {
        return correctGuesses;
    }

    /**
     * Increments the count of correct guesses in the game state.
     * This method increases the value of the `correctGuesses` field by one,
     * typically to track the player's correct answers during gameplay.
     */
    public void incrementCorrectGuesses() {
        this.correctGuesses++;
    }

    /**
     * Resets the count of correct guesses in the game state.
     * This method sets the {@code correctGuesses} field to 0,
     * effectively clearing the record of correct answers provided during the game.
     */
    public void resetCorrectGuesses() {
        this.correctGuesses = 0;
    }

    /**
     * Computes the score of the game based on the number of correct guesses and
     * the duration of the game in seconds. The score is calculated as the number
     * of correct guesses per second multiplied by 100. If the duration of the game
     * is zero, the method returns a score of zero to avoid division by zero.
     *
     * @return the computed score as a double value, or 0 if the duration is zero
     */
    // Method to compute the score.
    public double computeScore() {
        long durationMillis = System.currentTimeMillis() - gameStartTime;
        double durationSeconds = durationMillis / 1000.0;
        if (durationSeconds == 0) {
            return 0;
        }
        return (correctGuesses / durationSeconds) * 100;
    }

    /**
     * Copies the state of the specified {@code GameState} object into the current instance.
     * If the provided {@code GameState} object is {@code null}, the method does nothing.
     *
     * @param other the {@code GameState} object from which to copy the state.
     *              This includes fields such as {@code imageVersion}, {@code skipsRemaining},
     *              {@code currentAnswer}, {@code gameStage}, {@code gameStartTime}, and {@code currentMovie}.
     */
    // copyFrom method as before:
    public void copyFrom(GameState other) {
        if (other == null) return;
        this.imageVersion = other.imageVersion;
        this.skipsRemaining = other.skipsRemaining;
        this.currentAnswer = other.currentAnswer;
        this.gameStage = other.gameStage;
        this.gameStartTime = other.gameStartTime;
        this.currentMovie = other.currentMovie;
        this.correctGuesses = other.correctGuesses;
    }

}
