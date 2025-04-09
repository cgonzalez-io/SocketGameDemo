package Assign32starter.enums;

/**
 * Represents different types of games categorized by their duration.
 * Each game type is associated with a numeric value indicating the number of players
 * or the level of complexity/duration required.
 * The types include:
 * - SHORT: Represents a game that has 2 skips remaining, defined by a numeric value of 2.
 * - MEDIUM: Represents a game that has 4 skips remaining, defined by a numeric value of 4.
 * - LONG: Indicates a game that has 6 skips remaining, defined by a numeric value of 6.
 */
public enum GameType {
    SHORT(2),
    MEDIUM(4),
    LONG(6);

    private final int value;

    GameType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
