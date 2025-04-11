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
    private final JLabel timerLabel = new JLabel("Time Remaining: --");
    private final JTabbedPane tabbedPane;
    private final JPanel gamePanel; // Panel that will contain your game UI (PicturePanel + OutputPanel)
    private final LeaderboardPanel leaderboardPanel;
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
    private String sessionID = null;
    private String gameLength = "short"; // default game length
    private Timer gameTimer;
    private int remainingSeconds; // duration determined from the game length.

    /**
     * Constructs a ClientGui object, initializes the GUI components, establishes
     * a server connection, and handles the initial setup operations.
     *
     * @param host The hostname or IP address of the server to connect to.
     * @param port The port number on the server to connect to.
     * @throws IOException                             If an I/O error occurs during the server connection setup.
     * @throws PicturePanel.InvalidCoordinateException If the PicturePanel is initialized
     *                                                 with invalid coordinates.
     */
    public ClientGui(String host, int port) throws IOException, PicturePanel.InvalidCoordinateException {
        this.host = host;
        this.port = port;

        frame = new JDialog();
        frame.setLayout(new BorderLayout());
        frame.setMinimumSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // Initialize the menu (separate method)
        initializeMenu();

        // Create a tabbed pane.
        tabbedPane = new JTabbedPane();

        // Build the game panel (using your existing components).
        gamePanel = new JPanel(new GridBagLayout());
        // Configure gamePanel with PicturePanel and OutputPanel as before:
        picPanel = new PicturePanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.25;
        gamePanel.add(picPanel, c);

        // setup the input, button, and output area
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 0.75;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        outputPanel = new OutputPanel();
        outputPanel.addEventHandlers(this);
        gamePanel.add(outputPanel, c);
        picPanel.newGame(1);

        // Add the game panel as the first tab.
        tabbedPane.addTab("Game", gamePanel);

        // Create and add the leaderboard panel as the second tab.
        leaderboardPanel = new LeaderboardPanel();
        tabbedPane.addTab("Leaderboard", leaderboardPanel);

        // Add a ChangeListener so that when the leaderboard tab is selected,
        // the client sends a request to update the leaderboard.
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == leaderboardPanel) {
                // When the leaderboard tab is active, request updated data from the server.
                submitCommand("leaderboard");
            }
        });

        // Attach tabbedPane to the frame.
        frame.add(tabbedPane, BorderLayout.CENTER);


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
        logger.info("Pretend I got an image: " + response.getString("image"));
        // Assuming the server sends sessionID along with the greeting.
        if (response.has("sessionID")) {
            sessionID = response.getString("sessionID");
            logger.debug("Session ID: " + sessionID);
        }
        // Decode and display the welcome image:
        if (response.has("image")) {
            String imgBase64 = response.getString("image");
            byte[] imageBytes = Base64.getDecoder().decode(imgBase64);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            picPanel.insertImage(bais, 0, 0);
        }

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

            String input = outputPanel.getInputText().trim();
            JSONObject request = new JSONObject();
            request.put("sessionID", sessionID);

            // Use the state flag to decide if it's registration or game command.
            if (!registered) {
                // First input is assumed to be the player's name.
                request.put("type", "name");
                request.put("value", input);
                // Send the registration request.
                os.writeObject(request.toString());
                os.flush();

                // Wait for the response.
                String responseStr = bufferedReader.readLine();
                JSONObject response = new JSONObject(responseStr);

                // Display the greeting.
                outputPanel.appendOutput(response.getString("value"));
                registered = true;
            } else if (input.equalsIgnoreCase("play")) {
                // Start or restart the game.
                request.put("type", "gameStart");
                request.put("gameLength", gameLength);  // New field added here
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
                } else if (input.equalsIgnoreCase("leaderboard")) {
                    request.put("command", "leaderboard");
                } else if (input.equalsIgnoreCase("quit")) {
                    request.put("command", "quit");
                } else if (input.equalsIgnoreCase("help")) {
                    outputPanel.appendOutput("Available commands: 'guess: [your answer]', 'next', 'skip', 'remaining', 'quit'.");
                    return;
                } else {
                    outputPanel.appendOutput("Unknown command. Try 'guess: [your answer]', 'next', 'skip', 'remaining', 'quit'.");
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

            // *** Final score & leaderboard handling ***
            if (response.has("finalScore")) {
                double finalScore = response.getDouble("finalScore");
                outputPanel.appendOutput("Final Score: " + String.format("%.2f", finalScore));
            }
            if (response.optString("type", "").equals("leaderboard")) {
                String lbText = response.getString("leaderboard");
                leaderboardPanel.updateLeaderboard(lbText);
                outputPanel.appendOutput("Leaderboard updated.");
            }


            // If quitting, close the connection.
            if (response.optString("command", "").equals("quit")) {
                outputPanel.appendOutput("You have quit the game.");
                close();
            }

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

    /**
     * Closes resources associated with the ClientGui, including the output stream,
     * buffered reader, and socket. If an error occurs during the closing of any resource,
     * it is logged without re-throwing the exception to ensure that the method completes
     * execution for all resources.
     */
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

    /**
     * Initializes the main menu of the application, including the session menu and game menu.
     * The session menu provides information about the current session, such as session ID,
     * player status, and connection status. The game menu allows the user to start a game
     * with a chosen length or quit the game.
     * The method creates a menu bar and sets it on the application's main frame. Each menu
     * item includes relevant action listeners to handle user interactions:
     * - The "Session Info" menu item displays details about the session in a dialog box,
     * including session ID, registration status, and connection status.
     * - The "Start" menu item prompts the user to select a game length (Short, Medium, Long)
     * and initiates a game accordingly.
     * - The "Quit" menu item triggers a quit command, likely signaling the end of the game.
     * The method connects these actions with the application's state and relevant components,
     * such as the output panel and socket connection. It ensures that the menu is functional
     * and integrated with the client's workflow.
     */
    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Session Menu
        JMenu sessionMenu = new JMenu("Session");
        JMenuItem sessionInfo = new JMenuItem("Session Info");
        sessionInfo.addActionListener(e -> {
            String info = "Session ID: " + (sessionID != null ? sessionID : "None") + "\n" +
                    "Player: " + (registered ? outputPanel.getInputText() : "Not registered") + "\n" +
                    "Connected: " + (sock != null && sock.isConnected());
            JOptionPane.showMessageDialog(frame, info, "Session Info", JOptionPane.INFORMATION_MESSAGE);
        });
        sessionMenu.add(sessionInfo);

        // Add the timer label as well.
        sessionMenu.addSeparator();
        sessionMenu.add(timerLabel);

        menuBar.add(sessionMenu);

        // Game Menu
        JMenu gameMenu = new JMenu("Game");

        // Start game option (with a prompt for game length).
        // Inside initializeMenu() in ClientGui:
        JMenuItem startGame = new JMenuItem("Start");
        startGame.addActionListener(e -> {
            // Prompt the user for game length (short, medium, long).
            String[] options = {"Short", "Medium", "Long"};
            String length = (String) JOptionPane.showInputDialog(frame, "Select game length:",
                    "Game Length", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (length != null) {
                // Set the game duration based on length.
                int duration;
                switch (length.toLowerCase()) {
                    case "medium":
                        duration = 60;
                        break;
                    case "long":
                        duration = 90;
                        break;
                    case "short":
                    default:
                        duration = 30;
                        break;
                }
                outputPanel.appendOutput("Starting " + length + " game...");
                // Set the game length field if needed.
                gameLength = length.toLowerCase();
                // Start the timer.
                startGameTimer(duration);
                // Send a play command.
                submitCommand("play");
            }
        });
        gameMenu.add(startGame);

        // Quit game option.
        JMenuItem quitGame = new JMenuItem("Quit");
        quitGame.addActionListener(e -> submitCommand("quit"));
        gameMenu.add(quitGame);

        menuBar.add(gameMenu);

        // Attach the menu bar to the frame's root pane.
        frame.getRootPane().setJMenuBar(menuBar);
    }

    /**
     * Helper method to simulate command submission from the menu.
     *
     * @param command The command to submit (e.g., "play", "quit").
     */
    private void submitCommand(String command) {
        outputPanel.setInputText(command);
        submitClicked(); // Use the existing submission logic.
    }

    private void startGameTimer(int durationSeconds) {
        remainingSeconds = durationSeconds;
        // Update the timerLabel immediately.
        timerLabel.setText("Time Remaining: " + remainingSeconds + " seconds");

        // Create a Swing Timer that fires every 1 second.
        gameTimer = new Timer(1000, e -> {
            remainingSeconds--;
            timerLabel.setText("Time Remaining: " + remainingSeconds + " seconds");
            if (remainingSeconds <= 0) {
                gameTimer.stop();
                // Time is up: Automatically send a "quit" command
                submitCommand("guess:"); //auto send empty guess to force check timer
                outputPanel.appendOutput("Time is up! Game over.");
            }
        });
        gameTimer.start();
    }

}
