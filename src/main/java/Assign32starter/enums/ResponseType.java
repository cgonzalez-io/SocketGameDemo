package Assign32starter.enums;

/**
 * Represents the type of response associated with actions or events in a system.
 * Each response type is mapped to a specific integer value for identification.
 * The types include:
 * - START: Indicates the beginning of an action or event.
 * - GAME: Represents an ongoing game-related action or response.
 * - ERROR: Denotes that an error has occurred.
 * - IMAGE: Refers to a response containing or related to an image.
 */
public enum ResponseType {
    START(1),
    GAME(2),
    ERROR(3),
    IMAGE(4);

    private final int value;

    ResponseType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
