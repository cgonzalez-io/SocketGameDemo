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
    }

    /**
     * Determines whether the current instance is equal to the specified object.
     * Two GameState objects are considered equal if their respective fields,
     * such as imageVersion, skipsRemaining, gameStartTime, currentAnswer,
     * gameStage, and currentMovie, are equal.
     * @param o the object to compare with the current GameState instance
     * @return {@code true} if the specified object is a GameState instance
     *         and all relevant fields are equal; otherwise, {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameState gameState)) return false;
        return getImageVersion() == gameState.getImageVersion() && getSkipsRemaining() == gameState.getSkipsRemaining() && getGameStartTime() == gameState.getGameStartTime() && Objects.equals(getCurrentAnswer(), gameState.getCurrentAnswer()) && getGameStage() == gameState.getGameStage() && Objects.equals(getCurrentMovie(), gameState.getCurrentMovie());
    }

    /**
     * Computes the hash code for the current instance of the GameState object.
     * The hash code is calculated based on the values of the following fields:
     * - imageVersion
     * - skipsRemaining
     * - currentAnswer
     * - gameStage
     * - gameStartTime
     * - currentMovie
     * @return the hash code value for this GameState instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(getImageVersion(), getSkipsRemaining(), getCurrentAnswer(), getGameStage(), getGameStartTime(), getCurrentMovie());
    }

    /**
     * Retrieves the title of the current movie in the game state.
     * @return the title of the current movie as a String
     */
    public String getCurrentMovie() {
        return currentMovie;
    }

    /**
     * Sets the current movie associated with the game state.
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
}
