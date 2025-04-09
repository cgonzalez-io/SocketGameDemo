package Assign32starter;

import org.json.JSONObject;

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
    static Stack<String> imageSource = new Stack<String>();

    public static void main(String[] args) {
        Socket sock;
        try {

            //opening the socket here, just hard coded since this is just a bas example
            ServerSocket serv = new ServerSocket(8888); // TODO, should not be hardcoded
            System.out.println("Server ready for connetion");

            // placeholder for the person who wants to play a game
            String name = "";
            int points = 0;

            // read in one object, the message. we know a string was written only by knowing what the client sent.
            // must cast the object from Object to desired type to be useful
            while (true) {
                sock = serv.accept(); // waiting for connection
                ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                OutputStream out = sock.getOutputStream();

                String s = (String) in.readObject();
                JSONObject json = new JSONObject(s); // the received request
                JSONObject response = new JSONObject();

                // Step 1: Start handshake and ask for player's name.
                if (json.getString("type").equals("start")) {
                    System.out.println("Received start request");
                    response.put("type", "hello");
                    response.put("value", "Hello, please tell me your name.");
                    // Send a welcome image (converted to Base64)
                    sendImg("img/hi.png", response);
                }
                // Step 2: Receive player's name and send back the main menu
                else if (json.getString("type").equals("name")) {
                    String playerName = json.getString("value");
                    System.out.println("Player name received: " + playerName);
                    response.put("type", "greeting");
                    response.put("value", "Welcome " + playerName
                            + "! Please type 'play' to start the game, or 'quit' to exit.");
                }
                // Step 3: Initiate the game when the player chooses to play
                else if (json.getString("type").equals("gameStart")) {
                    System.out.println("Game initiation requested by client.");
                    // Initialize game state (you could also create a GameState object)
                    // For example, for a short game:
                    int initialSkips = 2;  // short game: 2 skips allowed
                    String correctAnswer = "The Dark Knight"; // example answer
                    // Optionally save these in a per-connection GameState object

                    // For this example, send back the first (most pixelated) image.
                    response.put("type", "game");
                    response.put("command", "start");
                    response.put("message", "Here is your movie image. Enter your guess, or type 'next', 'skip', or 'remaining'.");
                    // Reset image version to 1 (most pixelated)
                    response.put("imageVersion", 1);
                    // Include game state data if desired
                    response.put("skipsRemaining", initialSkips);
                    // Send the first image (e.g., TheDarkKnight1.png)
                    sendImg("img/TheDarkKnight1.png", response);
                }
                // Additional command handling (e.g., guesses) go here...
                else {
                    response.put("type", "error");
                    response.put("message", "Unknown request type.");
                }

                // Write out the response using a PrintWriter (or ObjectOutputStream)
                PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
                outWrite.println(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject sendImg(String filename, JSONObject obj) throws Exception {
        File file = new File(filename);
        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            byte[] imageBytes = fis.readAllBytes(); // requires Java 9+, or use alternative for older Java versions
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            obj.put("image", imageBase64);
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
                } else {
                    // Optionally, update the game score and then prepare for next image.
                    state.imageVersion = 1; // reset for next movie, or update with new values
                    // Choose a new movie and update state.currentAnswer accordingly.
                    response.put("message", "Correct! Here comes the next movie.");
                    // Also include the next image, e.g., call a method sendImg() with the next movie’s filename.
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
                } else {
                    response.put("ok", false);
                    response.put("message", "No more 'next' available for this movie.");
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
                } else {
                    response.put("ok", false);
                    response.put("message", "No skips remaining.");
                }
                break;
            case "remaining":
                response.put("ok", true);
                response.put("skipsRemaining", state.skipsRemaining);
                break;
            default:
                response.put("ok", false);
                response.put("message", "Unknown game command.");
        }
        return response;
    }

    class GameState {
        int imageVersion = 1;  // starting at 1 for the most pixelated version
        int skipsRemaining;    // initialize based on game duration (short/medium/long)
        String currentAnswer;  // set to the current movie's title
        long gameStartTime;
        // additional fields as needed
    }


}
