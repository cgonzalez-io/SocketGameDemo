# Assignment 3 Requirements Checklist

This checklist outlines all the requirements for the assignment. As you complete each task, check off the corresponding box.

## Activity 2: The Movie Guessing Game (70 points)

- [ ] **Project Setup and Build**
    - [ ] Use one Gradle file to build the project.
    - [ ] Run the server with:  
      `gradle runServer -Pport=<port>`
    - [ ] Run the client with:  
      `gradle runClient -Pport=<port> -Phost=<hostIP>`

- [ ] **Client GUI and Interaction**
    - [ ] Use the provided GUI (ClientGui, PicturePanel, and OutputPanel) as a starting point.
    - [ ] Display an image grid that will show the movie images.
    - [ ] Append status messages and handle input in the output panel.

- [ ] **Game Initiation and Handshake**
    - [ ] **Initial Connection:**
        - [ ] Client opens a socket and sends a start message (e.g., `{ "type": "start" }`).
        - [ ] Server responds with a greeting requesting the player’s name and sends a welcome image.
    - [ ] **Player Name Exchange:**
        - [ ] Client submits the player’s name (e.g., `{ "type": "name", "value": "<player name>" }`).
        - [ ] Server acknowledges with a personalized welcome and displays a main menu.
    - [ ] **Main Menu:**
        - [ ] Present options: Play Game, View Leaderboard, or Quit.

- [ ] **Game Round Setup**
    - [ ] **Game Duration:**
        - [ ] Allow the player to choose the game length (short, medium, long) which sets the timer and skip limits.
    - [ ] **Initial Image:**
        - [ ] Server sends the first movie image (most pixelated; image 1) and associated instructions.
        - [ ] The image is sent as a Base64-encoded string and decoded by the client for display.

- [ ] **In-Game Commands and Functionality**
    - [ ] **Guess Command:**
        - [ ] Client sends a guess using JSON (e.g., `{ "type": "game", "command": "guess", "guess": "<movie title>" }`).
        - [ ] Server validates the answer and responds with the result.
    - [ ] **Next Command:**
        - [ ] Client requests a less pixelated version (`{ "type": "game", "command": "next" }`).
        - [ ] Server increases image clarity (up to image 4) and responds; returns error if already at clearest image.
    - [ ] **Skip Command:**
        - [ ] Client requests a new movie using skip (`{ "type": "game", "command": "skip" }`).
        - [ ] Enforce skip limits:
            - Short game: maximum 2 skips.
            - Medium game: maximum 4 skips.
            - Long game: maximum 6 skips.
        - [ ] Server returns an error if no skips remain.
    - [ ] **Remaining Command:**
        - [ ] Client queries how many skips remain (`{ "type": "game", "command": "remaining" }`).
        - [ ] Server responds with the count.

- [ ] **Game Timer and Scoring**
    - [ ] The game lasts for the duration specified (e.g., 30, 60, or 90 seconds).
    - [ ] Points are calculated based on:  
      `(number of correct guesses during the game / game duration in seconds) * 100`.
    - [ ] Display the player’s score at the end of the game (and optionally indicate if a new high score was achieved).

- [ ] **Graceful Termination**
    - [ ] When quitting the game, the server sends a quit image.
    - [ ] The client and server disconnect gracefully without crashes.

- [ ] **Robustness and Error Handling**
    - [ ] Ensure the server is robust and continues to operate even with invalid inputs.
    - [ ] Implement clear and informative error messages for the client.

- [ ] **Documentation and Demo**
    - [ ] Update the README.md with:
        - A detailed project description.
        - A checklist (this document).
        - A description of your JSON protocol (for both activities).
        - A link to a short screen capture (4–7 minutes) demonstrating the game.
        - An explanation of your design for robustness.
        - An explanation of what would need to change if using UDP.
    - [ ] Include a link to your GitHub repository and the project zip file with your submission.
