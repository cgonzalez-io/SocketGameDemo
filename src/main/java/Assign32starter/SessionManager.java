package Assign32starter;

import Assign32starter.entity.GameState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SessionManager class provides functionality for managing game sessions.
 * It maintains a thread-safe mapping of session identifiers to their respective game states.
 * The class includes methods for creating, retrieving, and removing game sessions,
 * ensuring thread safety and uniqueness for session identifiers.
 */
public class SessionManager {
    /**
     * A thread-safe map that stores active game sessions.
     * Each session is identified by a unique session ID (String)
     * and is associated with a specific game state ({@link GameState}).
     * This map is used to manage and track multiple game sessions,
     * allowing operations like creating, retrieving, and removing sessions.
     */
    private static final Map<String, GameState> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new session for the given game state and returns a unique session identifier.
     * The session is stored in a shared session store, allowing it to be retrieved or managed later.
     *
     * @param state the current game state to associate with the session
     * @return a unique identifier for the newly created session
     */
    public static String createSession(GameState state) {
        String sessionId = generateSessionId(); // Implement a method to generate a unique token.
        sessions.put(sessionId, state);
        return sessionId;
    }

    /**
     * Retrieves the game state associated with the given session ID.
     *
     * @param sessionId the unique identifier for the desired game session
     * @return the {@code GameState} associated with the given session ID,
     * or {@code null} if no session exists for the provided ID
     */
    public static GameState getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Removes the session associated with the given session ID from the session manager.
     *
     * @param sessionId the unique identifier of the session to be removed
     */
    public static void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Generates a unique identifier to be used as a session ID.
     * This method utilizes a UUID to ensure the uniqueness of the generated string.
     *
     * @return a unique session ID in the form of a string
     */
    private static String generateSessionId() {
        // e.g., use UUID.randomUUID().toString()
        return java.util.UUID.randomUUID().toString();
    }
}

