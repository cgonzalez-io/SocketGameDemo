package Assign32starter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public class SockServer {
    private static final Logger logger = LoggerFactory.getLogger(SockServer.class);
    // Inside SockServer class:
    private static final List<Movie> movies = Arrays.asList(
            new Movie("TheDarkKnight", "The Dark Knight"),
            new Movie("TheLionKing", "The Lion King"),
            new Movie("JurassicPark", "Jurassic Park"),
            new Movie("BackToTheFuture", "Back to the Future"),
            new Movie("LordOfTheRings", "The Lord of the Rings")
    );
    private static volatile boolean running = true;

    public static void stopServer() {
        running = false;
    }

    /**
     * Entry point of the SockServer application. Initializes a server socket on the specified host and port.
     * Waits for incoming client connections and handles them using a separate thread for each client.
     * Logs server and connection-related events for debugging and monitoring purposes.
     *
     * @param args Command-line arguments to configure the server.
     *             args[0] specifies the server port (defaults to 9000 if not provided).
     *             args[1] specifies the timeout in seconds to stop the server (optional).
     */
    public static void main(String[] args) {
        int port = args.length > 1 ? Integer.parseInt(args[0]) : 9000;
        // Optionally, a third argument will be a timeout (in seconds) to stop the server.
        int stopAfterSeconds = args.length > 2 ? Integer.parseInt(args[1]) : 0;

        // Schedule a stop task if a timeout is provided.
        logger.info("Server starting on port {}. Stopping after {} seconds.", port, stopAfterSeconds);
        if (stopAfterSeconds > 0) {
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    logger.info("Server timeout reached. Stopping server.");
                    stopServer();
                }
            }, stopAfterSeconds * 1000L);  // Convert seconds to milliseconds
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Set a connection timeout if desired (e.g., 1000 seconds as before)
            serverSocket.setSoTimeout(1000000);
            logger.debug("Server ready for connection at " + serverSocket.getInetAddress() + ":" + port);
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from {}", clientSocket.getRemoteSocketAddress());
                    // Create a new client handler and start its thread:
                    Thread handlerThread = new Thread(new ClientHandler(clientSocket));
                    handlerThread.start();
                } catch (IOException e) {
                    logger.error("Error accepting client connection: {}", e.getMessage());
                    // Optionally, if the error is due to timeout and we're stopping, break out of loop
                    if (!running) break;
                }

            }
            logger.info("Server shutting down.");
        } catch (IOException e) {
            logger.error("Server encountered an error: {}", e.getMessage());
        }
    }

    /**
     * Encodes the contents of an image file into a Base64 string and embeds it
     * into the provided JSONObject under the key "image".
     *
     * @param filename the file path of the image to be read and encoded
     * @param obj      the JSONObject to which the Base64 encoded image string will be added
     * @return the modified JSONObject containing the Base64 encoded image under the key "image"
     * @throws FileNotFoundException if the specified image file does not exist
     * @throws IOException           if an error occurs while reading the file
     * @throws Exception             for any other general exception that may occur
     */
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

    /**
     * Selects and returns a random movie from the list of available movies.
     *
     * @return a randomly chosen Movie object from the collection of movies
     */
    public static Movie chooseRandomMovie() {
        Random rand = new Random();
        return movies.get(rand.nextInt(movies.size()));
    }


}
