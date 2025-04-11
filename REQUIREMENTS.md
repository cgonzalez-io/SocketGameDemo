# Assignment 3 Requirements Checklist

This checklist outlines all the requirements for the assignment. As you complete each task, check off the corresponding
box.

## Activity 2: The Movie Guessing Game (70 points)

- [x] **Project Setup and Build**
    - [x] Use one Gradle file to build the project.
    - [x] Run the server with:  
      `gradle runServer -Pport=<port>`
    - [x] Run the client with:  
      `gradle runClient -Pport=<port> -Phost=<hostIP>`

- [x] **Client GUI and Interaction**
    - [x] Use the provided GUI (ClientGui, PicturePanel, and OutputPanel) as a starting point.
    - [x] Display an image grid that will show the movie images.
    - [x] Append status messages and handle input in the output panel.

- [x] **Game Initiation and Handshake**
    - [x] **Initial Connection:**
        - [x] Client opens a socket and sends a start message (e.g., `{ "type": "start" }`).
        - [x] Server responds with a greeting requesting the player’s name and sends a welcome image.
    - [x] **Player Name Exchange:**
        - [x] Client submits the player’s name (e.g., `{ "type": "name", "value": "<player name>" }`).
        - [x] Server acknowledges with a personalized welcome and displays a main menu.
    - [x] **Main Menu:**
        - [x] Present options: Play Game, View Leaderboard, or Quit.

- [x] **Game Round Setup**
    - [x] **Game Duration:**
        - [x] Allow the player to choose the game length (short, medium, long) which sets the timer and skip limits.
    - [x] **Initial Image:**
        - [x] Server sends the first movie image (most pixelated; image 1) and associated instructions.
        - [x] The image is sent as a Base64-encoded string and decoded by the client for display.

- [x] **In-Game Commands and Functionality**
    - [x] **Guess Command:**
        - [x] Client sends a guess using JSON (e.g.,
          `{ "type": "game", "command": "guess", "guess": "<movie title>" }`).
        - [x] Server validates the answer and responds with the result.
    - [x] **Next Command:**
        - [x] Client requests a less pixelated version (`{ "type": "game", "command": "next" }`).
        - [x] Server increases image clarity (up to image 4) and responds; returns error if already at clearest image.
    - [x] **Skip Command:**
        - [x] Client requests a new movie using skip (`{ "type": "game", "command": "skip" }`).
        - [x] Enforce skip limits:
            - Short game: maximum 2 skips.
            - Medium game: maximum 4 skips.
            - Long game: maximum 6 skips.
        - [x] Server returns an error if no skips remain.
    - [x] **Remaining Command:**
        - [x] Client queries how many skips remain (`{ "type": "game", "command": "remaining" }`).
        - [x] Server responds with the count.

- [x] **Game Timer and Scoring**
    - [x] The game lasts for the duration specified (e.g., 30, 60, or 90 seconds).
    - [x] Points are calculated based on:  
      `(number of correct guesses during the game / game duration in seconds) * 100`.
    - [x] Display the player’s score at the end of the game (and optionally indicate if a new high score was achieved).

- [x] **Graceful Termination**
    - [x] When quitting the game, the server sends a quit image.
    - [x] The client and server disconnect gracefully without crashes.

- [x] **Robustness and Error Handling**
    - [x] Ensure the server is robust and continues to operate even with invalid inputs.
    - [x] Implement clear and informative error messages for the client.

- [ ] **Documentation and Demo**
    - [x] Update the README.md with:
        - A detailed project description.
        - A checklist (this document).
        - A description of your JSON protocol (for both activities).
        - A link to a short screen capture (4–7 minutes) demonstrating the game.
        - An explanation of your design for robustness.
        - An explanation of what would need to change if using UDP.
    - [x] Include a link to your GitHub repository and the project zip file with your submission.
