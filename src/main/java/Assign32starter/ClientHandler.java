package Assign32starter;

import Assign32starter.entity.GameState;
import Assign32starter.enums.GameType;
import Assign32starter.enums.States;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class is responsible for handling client connections in a server application.
 * Each instance of ClientHandler manages communication with a single client, processes
 * incoming requests, and generates appropriate responses. It is designed to be run
 * as a separate thread to handle multiple clients concurrently.
 * Responsibilities:
 * - Manage the lifecycle of a client session, including initialization, request
 * processing, and cleanup.
 * - Read and parse JSON-formatted requests from the client.
 * - Use an instance of GameState to manage game-related state for the client session.
 * - Process client requests and send formatted JSON responses.
 * - Log communication and errors for debugging and monitoring.
 * Key Features:
 * - Ensures clean resource management by closing the client socket and streams
 * after communication is complete.
 * - Handles various client commands, including initializing a game session, greeting
 * the user, and managing game-specific actions.
 * - Supports JSON as the primary format for both requests and responses.
 * Thread-Safety:
 * - Each ClientHandler instance handles a single client and uses a dedicated
 * GameState object, ensuring thread isolation for client-specific data.
 * Usage:
 * - Initialize the ClientHandler with a client socket when a new client connects.
 * - Execute the `run` method (usually by submitting the instance to a thread executor).
 */
public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    // Each handler gets its own GameState object.
    private final GameState gameState;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        // Initialize a new GameState for this client session.
        this.gameState = new GameState();
    }

    /**
     * Handles communication with the client connected via the provided socket.
     * Implements the `run` method of the `Runnable` interface to process incoming
     * client requests, generate corresponding responses, and ensure proper cleanup
     * of resources.
     * The method executes the following steps:
     * 1. Reads a JSON-formatted request from the client's input stream.
     * 2. Parses the request and logs the received content.
     * 3. Processes the request using the `processRequest` method, which relies on the
     * game state to determine the appropriate response.
     * 4. Sends the generated response back to the client via the output stream.
     * 5. Handles any exceptions during the process, logging errors as necessary.
     * 6. Ensures the client socket is closed in the `finally` block to free resources.
     * The communication relies on JSON for request and response formatting.
     */
    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read the request from the client:
            String input = (String) in.readObject();
            JSONObject requestJson = new JSONObject(input);
            logger.info("Received from client {}: {}", clientSocket.getRemoteSocketAddress(), requestJson);

            // Process the request using an instance method that uses gameState:
            JSONObject response = processRequest(requestJson);

            // Send the response back to the client:
            out.println(response);
            out.flush();
            logger.info("Response sent to client {}: {}", clientSocket.getRemoteSocketAddress(), response);
        } catch (Exception e) {
            logger.error("Error processing client {}: {}", clientSocket.getRemoteSocketAddress(), e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket {}: {}", clientSocket.getRemoteSocketAddress(), e.getMessage());
            }
        }
    }

    /**
     * Processes the JSON request and uses the handler's gameState instance.
     */
    private JSONObject processRequest(JSONObject requestJson) {
        JSONObject response = new JSONObject();
        try {
            // Ensure the request has a "type"
            if (!requestJson.has("type")) {
                throw new IllegalArgumentException("Missing 'type' in request.");
            }
            String requestType = requestJson.getString("type");
            logger.debug("Processing request type: {}", requestType);

            switch (requestType) {
                case "start":
                    // Initial handshake: request the player's name.
                    response.put("type", "hello");
                    response.put("value", "Hello, please tell me your name.");
                    SockServer.sendImg("img/hi.png", response); // Reuse the static sendImg method.
                    break;

                case "name":
                    // The client has provided their name.
                    String playerName = requestJson.getString("value");
                    response.put("type", "greeting");
                    response.put("value", "Welcome " + playerName
                            + "! Please type 'play' to start the game, or 'quit' to exit.");
                    break;

                case "gameStart":
                    // Initialize game state for the session.
                    logger.info("Initializing game for client {}", clientSocket.getRemoteSocketAddress());
                    gameState.setGameStage(States.IN_GAME_WITH_IMAGE);
                    gameState.setSkipsRemaining(GameType.SHORT.getValue());
                    gameState.setCurrentAnswer("The Dark Knight"); // Example answer
                    gameState.setImageVersion(1); // Start with the first image version.
                    response.put("type", "game");
                    response.put("command", "start");
                    response.put("message", "Here is your movie image. Enter your guess, or type 'next', 'skip', or 'remaining'.");
                    response.put("imageVersion", gameState.getImageVersion());
                    response.put("skipsRemaining", gameState.getSkipsRemaining());
                    // Send the first image for the game.
                    SockServer.sendImg("img/TheDarkKnight1.png", response);
                    break;

                // You can add additional game-related command handling (guess, next, etc.)
                default:
                    response.put("type", "error");
                    response.put("message", "Unknown request type: " + requestType);
                    logger.warn("Unknown request type received: {}", requestType);
                    break;
            }
        } catch (Exception e) {
            logger.error("Processing error: {}", e.getMessage(), e);
            response.put("type", "error");
            response.put("message", "Processing error: " + e.getMessage());
        }
        return response;
    }
}


