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
        logger.info("ClientHandler started for client: {}", clientSocket.getRemoteSocketAddress());
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Continue reading requests until the game is over or the connection is terminated.
            while (!gameState.getGameStage().equals(States.GAME_OVER)) {
                String input;
                try {
                    input = (String) in.readObject();
                    logger.info("Received from client {}: {}", clientSocket.getRemoteSocketAddress(), input);
                } catch (Exception e) {
                    logger.warn("Client {} disconnected or sent invalid data: {}", clientSocket.getRemoteSocketAddress(), e.getMessage());
                    break; // Exit if the client disconnects.
                }

                // Process the request and build a response.
                JSONObject requestJson = new JSONObject(input);
                logger.info("Received from client {}: {}", clientSocket.getRemoteSocketAddress(), requestJson);

                // Process the request using an instance method that uses gameState:
                JSONObject response = processRequest(requestJson);

                // Send the response back to the client:
                out.println(response);
                out.flush();
                logger.info("Response sent to client {}: {}", clientSocket.getRemoteSocketAddress(), response);

                // If the response type indicates the session is over (e.g., for a "quit" command), break.
                if (response.optString("type").equals("game") && response.optString("command").equals("quit")) {
                    break;
                }
            }
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

            // For most requests (other than registration), a sessionID is required.
            // Here we assume the "name" request is for registration and does not include a sessionID.
            if (!requestType.equals("start")) {
                if (!requestJson.has("sessionID")) {
                    response.put("type", "error");
                    response.put("ok", false);
                    response.put("message", "Missing sessionID. Please log in again.");
                    return response;
                }
                String sessionID = requestJson.getString("sessionID");
                // Retrieve the persistent GameState.
                GameState state = SessionManager.getSession(sessionID);
                if (state == null) {
                    response.put("type", "error");
                    response.put("ok", false);
                    response.put("message", "Invalid session. Please log in again.");
                    return response;
                }
                // Replace the local gameState with the persistent one.
                // For subsequent processing, use "state" to refer to the current game state.
                this.gameState.copyFrom(state);
            }

            // Process the request by type.
            switch (requestType) {
                case "start":
                    // Initial handshake: request the player's name.
                    response.put("type", "hello");
                    response.put("ok", true);
                    response.put("value", "Hello, please tell me your name.");
                    // Create a new persistent session.
                    String sessionId = SessionManager.createSession(this.gameState);
                    response.put("sessionID", sessionId);
                    SockServer.sendImg("img/hi.png", response); // Sends a welcome image.
                    break;

                case "name":
                    // The client has provided their name.
                    String playerName = requestJson.getString("value");
                    // Save the player's name in the GameState.
                    gameState.setPlayerName(playerName);
                    response.put("type", "greeting");
                    response.put("ok", true);
                    response.put("value", "Welcome " + playerName
                            + "! Please type 'play' to start the game, or 'quit' to exit.");
                    break;

                case "gameStart":
                    // Initialize game state for the session.
                    logger.info("Initializing game for client {}", clientSocket.getRemoteSocketAddress());

                    // Check if the client has specified a gameLength; default to "short" if not.
                    String gameLength = requestJson.optString("gameLength", "short").toLowerCase();
                    // Determine duration and skip count based on gameLength.
                    int duration;
                    GameType type;
                    switch (gameLength) {
                        case "medium":
                            duration = 60;
                            type = GameType.MEDIUM;
                            break;
                        case "long":
                            duration = 90;
                            type = GameType.LONG;
                            break;
                        case "short":
                        default:
                            duration = 30;
                            type = GameType.SHORT;
                            break;
                    }
                    // Set game duration and skip count.
                    gameState.setGameDuration(duration);
                    gameState.setSkipsRemaining(type.getValue());
                    // Set game start time.
                    gameState.setGameStartTime(System.currentTimeMillis());

                    // Choose a random movie
                    Movie selected = SockServer.chooseRandomMovie();
                    // Update the game state with the randomly selected movie.
                    gameState.setGameStage(States.IN_GAME_WITH_IMAGE);
                    gameState.setCurrentMovie(selected.getMovieName());
                    gameState.setCurrentAnswer(selected.getCorrectAnswer());
                    gameState.setImageVersion(1);  // Start with the first image
                    response.put("type", "game");
                    response.put("command", "start");
                    response.put("ok", true);
                    response.put("message", "Game started (" + gameLength + " mode). Here is your movie image. Enter your guess, or type 'next', 'skip', or 'remaining'.");
                    response.put("imageVersion", gameState.getImageVersion());
                    response.put("skipsRemaining", gameState.getSkipsRemaining());
                    response.put("gameDuration", duration);
                    SockServer.sendImg("img/" + gameState.getCurrentMovie() + "1.png", response);
                    break;

                case "game":
                    // Process in-game commands.
                    // The request should include a "command" field.
                    String command = requestJson.optString("command", "");
                    switch (command) {
                        case "guess":
                            // Check if the game is still within the allowed duration.
                            long elapsed = System.currentTimeMillis() - gameState.getGameStartTime();
                            if (elapsed > gameState.getGameDuration() * 1000L) {
                                response.put("ok", false);
                                response.put("message", "Time is up! Game over.");
                                // Optionally compute and send the score, update leaderboard, etc.
                                double score = gameState.computeScore();
                                response.put("finalScore", score);
                                // Leaderboard update logic (see below)
                                response.put("leaderboard", Leaderboard.getFormattedLeaderboard());
                                gameState.setGameStage(States.GAME_OVER);
                                break;
                            }
                            // Otherwise process the guess:
                            String clientGuess = requestJson.getString("guess").trim();
                            if (clientGuess.equalsIgnoreCase(gameState.getCurrentAnswer())) {
                                gameState.incrementCorrectGuesses();
                                response.put("ok", true);
                                response.put("result", true);
                                response.put("message", "Correct! Here comes your next movie.");
                                gameState.incrementCorrectGuesses();
                                // Update state with a new movie for demonstration.
                                selected = SockServer.chooseRandomMovie();
                                gameState.setImageVersion(1);
                                gameState.setCurrentMovie(selected.getMovieName());
                                gameState.setCurrentAnswer(selected.getCorrectAnswer());
                                SockServer.sendImg("img/" + gameState.getCurrentMovie() + "1.png", response);
                            } else {
                                // Incorrect guess.
                                response.put("ok", true);
                                response.put("result", false);
                                response.put("message", "Incorrect. Try again.");
                                // Optionally, repeat the current question.
                                response.put("question", "What is your guess for the current movie?");
                            }
                            break;

                        case "next":
                            if (gameState.getCurrentMovie() == null) {
                                response.put("ok", false);
                                response.put("message", "Game not started. Please type 'play' to start the game.");
                            } else if (gameState.getImageVersion() < 4) {
                                gameState.setImageVersion(gameState.getImageVersion() + 1);
                                response.put("ok", true);
                                response.put("message", "Providing a clearer image.");
                                response.put("imageVersion", gameState.getImageVersion());
                                String imgFile = "img/" + gameState.getCurrentMovie() + gameState.getImageVersion() + ".png";
                                SockServer.sendImg(imgFile, response);
                            } else {
                                response.put("ok", false);
                                response.put("message", "No more 'next' images available for this movie.");
                            }
                            break;

                        case "skip":
                            if (gameState.getSkipsRemaining() > 0) {
                                gameState.setSkipsRemaining(gameState.getSkipsRemaining() - 1);
                                // Choose a new movie and reset the image version.
                                selected = SockServer.chooseRandomMovie();
                                gameState.setImageVersion(1);
                                gameState.setCurrentMovie(selected.getMovieName());
                                gameState.setCurrentAnswer(selected.getCorrectAnswer());
                                response.put("ok", true);
                                response.put("message", "Movie skipped. Here is your new movie image.");
                                response.put("skipsRemaining", gameState.getSkipsRemaining());
                                SockServer.sendImg("img/" + gameState.getCurrentMovie() + "1.png", response);
                            } else {
                                response.put("ok", false);
                                response.put("message", "No skips remaining.");
                            }
                            break;

                        case "remaining":
                            // Return the number of skips remaining.
                            response.put("ok", true);
                            response.put("skipsRemaining", gameState.getSkipsRemaining());
                            break;

                        case "quit":
                            // End the game session.
                            response.put("ok", true);
                            response.put("command", "quit");
                            double score = gameState.computeScore();
                            response.put("finalScore", score);
                            // Create a unique key for the leaderboard using the player's name and IP address.
                            String playerKey = gameState.getPlayerName() + "@" +
                                    ((java.net.InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress().getHostAddress();
                            Leaderboard.updateScore(playerKey, score);
                            response.put("leaderboard", Leaderboard.getFormattedLeaderboard());
                            response.put("message", "Thank you for playing. Your score: " + String.format("%.2f", score));
                            gameState.setGameStage(States.GAME_OVER);
                            // Optionally, remove the session.
                            // SessionManager.removeSession(sessionID);
                            break;
                        case "leaderboard":
                            // Respond with persistent leaderboard information.
                            response.put("ok", true);
                            response.put("type", "leaderboard");
                            response.put("leaderboard", Leaderboard.getFormattedLeaderboard());
                            break;


                        default:
                            response.put("ok", false);
                            response.put("message", "Unknown game command: " + command);
                            break;
                    }
                    response.put("type", "game");
                    break;

                default:
                    response.put("type", "error");
                    response.put("ok", false);
                    response.put("message", "Unknown request type: " + requestType);
                    logger.warn("Unknown request type received: {}", requestType);
                    break;
            }
        } catch (Exception e) {
            logger.error("Processing error: {}", e.getMessage(), e);
            response.put("type", "error");
            response.put("ok", false);
            response.put("message", "Processing error: " + e.getMessage());
        }
        return response;
    }
}


