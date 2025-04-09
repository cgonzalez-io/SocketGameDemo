package Assign32starter;

import org.json.JSONObject;

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
    JDialog frame;
    PicturePanel picPanel;
    OutputPanel outputPanel;
    String currentMess;

    Socket sock;
    OutputStream out;
    ObjectOutputStream os;
    BufferedReader bufferedReader;

    // TODO: SHOULD NOT BE HARDCODED change to spec
    String host = "localhost";
    int port = 9000;

    /**
     * Construct dialog
     *
     * @throws IOException
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        String string = this.bufferedReader.readLine();
        System.out.println("Got a connection to server");
        JSONObject json = new JSONObject(string);
        outputPanel.appendOutput(json.getString("value")); // putting the message in the outputpanel

        // reading out the image (abstracted here as just a string)
        System.out.println("Pretend I got an image: " + json.getString("image"));
        /// would put image in picture panel
        String imgBase64 = json.getString("image");
        byte[] imageBytes = Base64.getDecoder().decode(imgBase64);
        ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
// Now, call the PicturePanel method to display this image.
        picPanel.insertImage(bais, 0, 0);

        close(); //closing the connection to server

        // Now Client interaction only happens when the submit button is used, see "submitClicked()" method
    }

    public static void main(String[] args) throws IOException {
        // create the frame


        try {
            String host = "localhost";
            int port = 8888;


            ClientGui main = new ClientGui(host, port);
            main.show(true);


        } catch (Exception e) {
            e.printStackTrace();
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
     * TODO: This is where your logic will go or where you will call appropriate methods you write.
     * Right now this method opens and closes the connection after every interaction, if you want to keep that or not is up to you.
     */
    @Override
    public void submitClicked() {
        try {
            open(); // open connection to server

            String input = outputPanel.getInputText().trim();
            JSONObject request = new JSONObject();
            request.put("type", "game");

            // Check if the input begins with "guess:" for a guess command
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
                // You can also design additional commands such as "start", "score", etc.
                outputPanel.appendOutput("Unknown command. Try 'guess: [your answer]', 'next', 'skip', or 'remaining'.");
                return;
            }

            // Send the request to the server:
            os.writeObject(request.toString());
            os.flush();

            // Wait for response from server:
            String responseStr = bufferedReader.readLine();
            JSONObject response = new JSONObject(responseStr);

            // Handle the response:
            if (response.has("image")) {
                // Decode and display the image:
                String imgBase64 = response.getString("image");
                byte[] imageBytes = Base64.getDecoder().decode(imgBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                // Insert the image in position (0,0) or as appropriate:
                picPanel.insertImage(bais, 0, 0);
            }

            if (response.has("message")) {
                outputPanel.appendOutput(response.getString("message"));
            }
            if (response.has("result")) {
                boolean result = response.getBoolean("result");
                outputPanel.appendOutput("Your answer is " + (result ? "correct" : "incorrect"));
            }
            if (response.has("skipsRemaining")) {
                outputPanel.appendOutput("Skips remaining: " + response.getInt("skipsRemaining"));
            }
            // Now, if the response indicates to "play", the user can then send a game start command.
            // For example, if the user types "play":
            if (input.equalsIgnoreCase("play")) {
                request = new JSONObject();
                request.put("type", "gameStart");
                os.writeObject(request.toString());
                os.flush();

                responseStr = bufferedReader.readLine();
                response = new JSONObject(responseStr);
                outputPanel.appendOutput(response.getString("message"));
                // Decode and display the game image:
                if (response.has("image")) {
                    String imgBase64 = response.getString("image");
                    byte[] imageBytes = Base64.getDecoder().decode(imgBase64);
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                    picPanel.insertImage(bais, 0, 0);
                }
            }

            close(); // close connection after handling
        } catch (Exception e) {
            e.printStackTrace();
            outputPanel.appendOutput("Error: " + e.getMessage());
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
            if (bufferedReader != null) bufferedReader.close();
            if (sock != null) sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
