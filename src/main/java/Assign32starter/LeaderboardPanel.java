package Assign32starter;

import javax.swing.*;
import java.awt.*;

public class LeaderboardPanel extends JPanel {
    /**
     * The leaderboardArea is a read-only JTextArea component used for displaying
     * leaderboard information in the LeaderboardPanel. It is embedded within a
     * JScrollPane to allow scrolling when the content exceeds the visible area.
     */
    private final JTextArea leaderboardArea;

    /**
     * Constructs a new LeaderboardPanel object, which is a specialized JPanel
     * designed to display leaderboard information in a read-only format.
     * The panel uses a BorderLayout and contains a scrollable, non-editable text area.
     */
    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        leaderboardArea = new JTextArea();
        leaderboardArea.setEditable(false); // Read-only display
        JScrollPane scrollPane = new JScrollPane(leaderboardArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Updates the leaderboard display by setting the provided text
     * to the leaderboard area's text content.
     *
     * @param leaderboardText the new text to display in the leaderboard area
     */
    public void updateLeaderboard(String leaderboardText) {
        leaderboardArea.setText(leaderboardText);
    }
}
