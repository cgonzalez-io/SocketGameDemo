package Assign32starter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Stack;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
    private static final Logger logger = LoggerFactory.getLogger(SockServer.class);
    static Stack<String> imageSource = new Stack<String>();
    private static volatile boolean running = true;


    public static void stopServer() {
        running = false;
    }


    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(1000000); // 1000 seconds
            System.out.println("Server ready for connetion." + host + ":" + port);
            logger.debug("Server ready for connetion." + host + ":" + port);

            // placeholder for the person who wants to play a game
            String name = "";
            int points = 0;
            // read in one object, the message. we know a string was written only by knowing what the client sent.
            // must cast the object from Object to desired type to be useful
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket); // Offload to a method
                } catch (IOException e) {
                    logger.error("Error accepting client connection: {}", e.getMessage());
                }

            }

        } catch (IOException e) {
            logger.error("Server encountered an error: {}", e.getMessage());
        }
    }

    public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
        File file = new File(filename);

        if (!file.exists()) {
            logger.error("File not found: {}", filename);
            throw new FileNotFoundException("File not found: " + filename);
        }

        try (FileInputStream fis = new FileInputStream(file)) { // Use try-with-resources for FileInputStream
            byte[] imageBytes = fis.readAllBytes(); // Read file into bytes array
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes); // Convert to Base64
            obj.put("image", imageBase64);
            logger.info("Image successfully sent: {}", filename);
        } catch (IOException e) {
            logger.error("Error reading file: {}", filename, e);
            throw e;
        }
        return obj;
    }

    static JSONObject handleGame(JSONObject req, GameState state) throws Exception {
        JSONObject response = new JSONObject();
        response.put("type", "game");
        String command = req.getString("command");
        switch (command) {
            case "guess":
                // Retrieve the guess from the request and compare (case-insensitively) to state.currentAnswer.
                String guess = req.getString("guess").trim();
                boolean correct = guess.equalsIgnoreCase(state.currentAnswer);
                response.put("ok", true);
                response.put("result", correct);
                if (!correct) {
                    // Optionally, send the current pixelated image again (or a hint)
                    response.put("question", "Try again: " + " [current image]");
                    logger.info("Incorrect guess: " + guess);
                } else {
                    // Optionally, update the game score and then prepare for next image.
                    state.imageVersion = 1; // reset for next movie, or update with new values
                    // Choose a new movie and update state.currentAnswer accordingly.
                    response.put("message", "Correct! Here comes the next movie.");
                    // Also include the next image, e.g., call a method sendImg() with the next movie’s filename.
                    logger.info("Correct guess: " + guess);
                }
                break;
            case "next":
                // Increase the image version if possible.
                if (state.imageVersion < 4) { // assuming 4 is the maximum clarity
                    state.imageVersion++;
                    response.put("ok", true);
                    // Send the next image version:
                    String nextImageFile = "img/YourMovie" + state.imageVersion + ".png";
                    sendImg(nextImageFile, response); // use similar logic to your sendImg method
                    logger.info("Sent next image: " + nextImageFile);
                } else {
                    response.put("ok", false);
                    response.put("message", "No more 'next' available for this movie.");
                    logger.info("No more 'next' available for this movie.");
                }
                break;
            case "skip":
                if (state.skipsRemaining > 0) {
                    state.skipsRemaining--;
                    response.put("ok", true);
                    // Choose a new movie, update state.currentAnswer and reset imageVersion.
                    state.imageVersion = 1;
                    // For example, choose a new file:
                    String newImageFile = "img/AnotherMovie1.png";
                    state.currentAnswer = "AnotherMovieTitle"; // set the correct answer here
                    sendImg(newImageFile, response);
                    logger.info("Sent next image: " + newImageFile);
                } else {
                    response.put("ok", false);
                    response.put("message", "No skips remaining.");
                    logger.info("No skips remaining.");
                }
                break;
            case "remaining":
                response.put("ok", true);
                response.put("skipsRemaining", state.skipsRemaining);
                logger.info("Sent remaining skips: " + state.skipsRemaining);
                break;
            default:
                response.put("ok", false);
                response.put("message", "Unknown game command.");
                logger.info("Unknown game command: " + command);
        }
        logger.info("Sent response: " + response);
        return response;
    }

    private static JSONObject processRequest(JSONObject requestJson) {
        JSONObject response = new JSONObject();

        try {
            // Log the incoming request
            logger.info("Processing request: {}", requestJson.toString());

            // Validate the JSON request
            if (requestJson.isEmpty() || !requestJson.has("type")) {
                logger.error("Malformed JSON request: Missing 'type'. Request: {}", requestJson);
                throw new IllegalArgumentException("Malformed JSON request: Missing 'type'.");
            }

            // Handle requests based on the "type" field
            String requestType = requestJson.getString("type");
            logger.debug("Request type: {}", requestType);

            switch (requestType) {
                case "start":
                    logger.info("Handling 'start' request.");
                    response.put("type", "hello");
                    response.put("value", "Hello, please tell me your name.");
                    try {
                        sendImg("img/hi.png", response); // Example image
                        logger.debug("Welcome image sent successfully.");
                    } catch (Exception e) {
                        logger.error("Failed to send welcome image: {}", e.getMessage());
                        response.put("error", "Failed to send image: " + e.getMessage());
                    }
                    break;

                case "name":
                    String playerName = requestJson.getString("value");
                    logger.info("Handling 'name' request. Player name: {}", playerName);
                    response.put("type", "greeting");
                    response.put("value", "Welcome " + playerName
                            + "! Please type 'play' to start the game, or 'quit' to exit.");
                    logger.debug("Greeting message prepared for player: {}", playerName);
                    break;

                case "gameStart":
                    logger.info("Handling 'gameStart' request. Initializing game state.");
                    // Example handling of starting the game
                    GameState state = new GameState();
                    state.gameStage = States.IN_GAME_WITH_IMAGE;
                    state.skipsRemaining = GameType.SHORT.getValue();
                    state.currentAnswer = "The Dark Knight";
                    state.imageVersion = 1;
                    response = handleGame(requestJson, state);
                    logger.debug("Game start response prepared: {}", response);
                    break;

                default:
                    logger.warn("Unknown request type received: {}", requestType);
                    response.put("type", "error");
                    response.put("message", "Unknown request type: " + requestType);
                    break;
            }

        } catch (JSONException e) {
            logger.error("Invalid JSON request: {}. Error: {}", requestJson, e.getMessage());
            response.put("type", "error");
            response.put("message", "Invalid JSON request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Processing error for request: {}. Error: {}", requestJson.toString(), e.getMessage());
            response.put("type", "error");
            response.put("message", "Processing error: " + e.getMessage());
        }

        // Log the generated response
        logger.info("Generated response: {}", response);

        return response;
    }

    private static void handleClient(Socket clientSocket) {
        logger.debug("Accepted a connection from {}", clientSocket.getRemoteSocketAddress());

        // Use try-with-resources to ensure proper cleanup
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read and process the client's input
            String input = (String) in.readObject(); // Expecting a string; validate on the client side
            JSONObject requestJson = new JSONObject(input);
            logger.info("Received JSON: {}", requestJson);

            // Process the request and generate a response
            JSONObject response = processRequest(requestJson);

            // Send the response
            out.println(response);
            out.flush();
            logger.debug("Response sent to client: {}", response);

        } catch (Exception e) {
            logger.error("Error handling client connection: {}", e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.error("Error closing client socket: {}", e.getMessage());
            }
        }
    }

    private GameState initGameState(States gameStage, String currentAnswer, GameType gameType) {
        GameState state = new GameState();
        state.gameStage = gameStage; // set to the current game stage
        state.imageVersion = 1; // starting at 1 for the most pixelated version
        state.skipsRemaining = gameType.getValue(); // initialize based on game duration (short/medium/long)
        state.currentAnswer = currentAnswer; // set to the current movie's title
        state.gameStartTime = System.currentTimeMillis(); // set the start time
        return state;
    }

    enum States {
        NOT_STARTED,
        IN_GAME_NO_IMAGE,
        IN_GAME_WITH_IMAGE,
        GAME_OVER
    }

    enum GameType {
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

    enum ResponseType {
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


    class GameState {
        int imageVersion = 1;  // starting at 1 for the most pixelated version
        int skipsRemaining;    // initialize based on game duration (short/medium/long)
        String currentAnswer;// set to the current movie's title
        States gameStage;
        long gameStartTime;
        // additional fields as needed
    }


}
