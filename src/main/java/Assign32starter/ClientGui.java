package Assign32starter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Base64;


/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status.
 * <p>
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with current state
 * -> modal means that it opens GUI and suspends background processes.
 * Processing still happens in the GUI. If it is desired to continue processing in the
 * background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * <p>
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 */
public class ClientGui implements Assign32starter.OutputPanel.EventHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ClientGui.class);

    JDialog frame;
    PicturePanel picPanel;
    OutputPanel outputPanel;
    String currentMess;

    Socket sock;
    OutputStream out;
    ObjectOutputStream os;
    BufferedReader bufferedReader;
    String host;
    int port;
    boolean registered = false; // Flag to check if the player is registered

    /**
     * Constructs a ClientGui object, initializes the GUI components, establishes
     * a server connection, and handles the initial setup operations.
     * @param host The hostname or IP address of the server to connect to.
     * @param port The port number on the server to connect to.
     * @throws IOException If an I/O error occurs during the server connection setup.
     * @throws PicturePanel.InvalidCoordinateException If the PicturePanel is initialized
     *                                                 with invalid coordinates.
     */
    public ClientGui(String host, int port) throws IOException, PicturePanel.InvalidCoordinateException {
        this.host = host;
        this.port = port;

        frame = new JDialog();
        frame.setLayout(new GridBagLayout());
        frame.setMinimumSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // setup the top picture frame
        picPanel = new PicturePanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.25;
        frame.add(picPanel, c);

        // setup the input, button, and output area
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 0.75;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        outputPanel = new OutputPanel();
        outputPanel.addEventHandlers(this);
        frame.add(outputPanel, c);

        picPanel.newGame(1);
        insertImage("img/TheDarkKnight1.png", 0, 0);

        open(); // opening server connection here
        currentMess = "{'type': 'start'}"; // very initial start message for the connection
        try {
            os.writeObject(currentMess);
            os.flush();
        } catch (IOException e) {
            logger.error("Error writing to output stream", e);
        }

        //Wait for the server to respond

        String string = this.bufferedReader.readLine();
        logger.info("Server sent: " + string);
        JSONObject response = new JSONObject(string);
        outputPanel.appendOutput(response.getString("value")); // putting the message in the outputpanel

        // reading out the image (abstracted here as just a string)
        System.out.println("Pretend I got an image: " + response.getString("image"));
        // Decode and display the welcome image:
        if (response.has("image")) {
            String imgBase64 = response.getString("image");
            byte[] imageBytes = Base64.getDecoder().decode(imgBase64);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            picPanel.insertImage(bais, 0, 0);
        }
        close();

        // Now Client interaction only happens when the submit button is used, see "submitClicked()" method
    }

    /**
     * The entry point for the application. Initializes and starts the ClientGui application.
     * Parses command-line arguments for host and port configuration.
     *
     * @param args The command-line arguments where:
     *             args[0] (optional) specifies the hostname (default is "localhost").
     *             args[1] (optional) specifies the port number (default is 9000).
     *             If the port number is invalid (e.g., out of range or not a number),
     *             a default port (8080) will be used.
     */
    public static void main(String[] args) {
        // Set default host and port
        String host = args.length > 0 ? args[0] : "localhost";
        int port = 9000; // Default port

        // Parse port number if provided in arguments
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                if (port < 0 || port > 65535) {
                    System.out.println("Invalid port range. Please use a port between 0 and 65535. Using default port 8080.");
                    port = 8080;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid port number provided. Using default port: 8080");
            }
        }

        // Initialize and start the ClientGui
        try {
            ClientGui main = new ClientGui(host, port);
            main.show(true);
        } catch (IOException e) {
            System.out.println("Failed to start the client. Please check your network connection or configuration and try again.");
            if (logger != null) {
                logger.error("I/O error occurred while starting the ClientGui with host {} and port {}", host, port, e);
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred. Please try again later.");
            if (logger != null) {
                logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Shows the current state in the GUI
     *
     * @param makeModal - true to make a modal window, false disables modal behavior
     */
    public void show(boolean makeModal) {
        frame.pack();
        frame.setModal(makeModal);
        frame.setVisible(true);
    }

    /**
     * Creates a new game and set the size of the grid
     *
     * @param dimension - the size of the grid will be dimension x dimension
     *                  No changes should be needed here
     */
    public void newGame(int dimension) {
        picPanel.newGame(1);
        outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
    }

    /**
     * Insert an image into the grid at position (col, row)
     *
     * @param filename - filename relative to the root directory
     * @param row      - the row to insert into
     * @param col      - the column to insert into
     * @return true if successful, false if an invalid coordinate was provided
     * @throws IOException An error occured with your image file
     */
    public boolean insertImage(String filename, int row, int col) throws IOException {
        System.out.println("Image insert");
        String error = "";
        try {
            // insert the image
            if (picPanel.insertImage(filename, row, col)) {
                // put status in output
                outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")"); // you can of course remove this
                return true;
            }
            error = "File(\"" + filename + "\") not found.";
        } catch (PicturePanel.InvalidCoordinateException e) {
            // put error in output
            error = e.toString();
        }
        outputPanel.appendOutput(error);
        return false;
    }

    /**
     * Submit button handling
     * <p>
     * Right now this method opens and closes the connection after every interaction, if you want to keep that or not is up to you.
     */
    @Override
    public void submitClicked() {
        try {
            open(); // open connection to server

            String input = outputPanel.getInputText().trim();
            JSONObject request = new JSONObject();

            // Use the state flag to decide if it's registration or game command.
            if (!registered) {
                // First input is assumed to be the player's name.
                request.put("type", "name");
                request.put("value", input);
                registered = true;
            } else if (input.equalsIgnoreCase("play")) {
                // Start or restart the game.
                request.put("type", "gameStart");
            } else {
                // Process in-game commands.
                request.put("type", "game");
                if (input.toLowerCase().startsWith("guess:")) {
                    request.put("command", "guess");
                    String answer = input.substring(6).trim();
                    request.put("guess", answer);
                } else if (input.equalsIgnoreCase("next")) {
                    request.put("command", "next");
                } else if (input.equalsIgnoreCase("skip")) {
                    request.put("command", "skip");
                } else if (input.equalsIgnoreCase("remaining")) {
                    request.put("command", "remaining");
                } else {
                    outputPanel.appendOutput("Unknown command. Try 'guess: [your answer]', 'next', 'skip', or 'remaining'.");
                    return;
                }
            }

            // Send the request to the server:
            os.writeObject(request.toString());
            os.flush();

            // Wait for response from server:
            String responseStr = bufferedReader.readLine();
            JSONObject response = new JSONObject(responseStr);

            // Display the main greeting or message from the server:
            if (response.has("value")) {
                outputPanel.appendOutput(response.getString("value"));
            } else if (response.has("message")) {
                outputPanel.appendOutput(response.getString("message"));
            }

            // If there's an image in the response, decode and display it:
            if (response.has("image")) {
                String imgBase64 = response.getString("image");
                byte[] imageBytes = Base64.getDecoder().decode(imgBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                this.newGame(1);
                picPanel.insertImage(bais, 0, 0);
            }

            // Optional: handle additional response content.
            if (response.has("result")) {
                boolean result = response.getBoolean("result");
                outputPanel.appendOutput("Your answer is " + (result ? "correct" : "incorrect"));
            }
            if (response.has("skipsRemaining")) {
                outputPanel.appendOutput("Skips remaining: " + response.getInt("skipsRemaining"));
            }

            close(); // close connection after handling
        } catch (Exception e) {
            outputPanel.appendOutput("Error: " + e.getMessage());
            logger.error("Error occurred during submit button handling", e);
        }
    }


    /**
     * Key listener for the input text box
     * <p>
     * Change the behavior to whatever you need
     */
    @Override
    public void inputUpdated(String input) {
        if (input.equals("surprise")) {
            outputPanel.appendOutput("You found me!");
        }
    }

    public void open() throws IOException {
        this.sock = new Socket(host, port); // connect to host and socket

        // get output channel
        this.out = sock.getOutputStream();
        // create an object output writer (Java only)
        this.os = new ObjectOutputStream(out);
        this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));

    }

    public void close() {
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            logger.error("Error closing output stream", e);
        }
        try {
            if (bufferedReader != null) bufferedReader.close();
        } catch (IOException e) {
            logger.error("Error closing buffered reader", e);
        }
        try {
            if (sock != null) sock.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }
    }
}
