# Activity 2 Completion Plan: Movie Guessing Game

This document outlines the steps required to complete Activity 2. The plan covers several areas: dynamic game flow, UI
improvements, leaderboard integration, and overall polishing of the application.

---

## 1. Dynamic Game Flow

### Objectives

- Ensure that the server tracks game state properly on a per-client basis (using ClientHandler and GameState).
- Implement and test all game commands:
    - **Game Initialization:** "gameStart" should properly set all game state fields (current movie, current answer,
      image version, and skips remaining).
    - **In-Game Commands:**
        - **Guess:** Compare the client's guess (via "guess" command) with the correct answer.
        - **Next:** Increment the image version (up to a maximum, e.g., 4) and send back the clearer image.
        - **Skip:** Allow the client to skip to another movie if the allowed skip count permits.
        - **Remaining:** Return the current number of remaining skips.
        - **Quit:** End the game session and send a farewell image/message.
- Ensure that commands sent before the game is fully initiated (e.g., "next" or "guess") return a clean error message
  instructing the user to type "play" first.
- Remove hardcoded game logic from tests so that the game state is dynamic (for example, using a random or round-robin
  selection of movies).

### Steps

1. **Game Initialization:**
    - In the `"gameStart"` branch, update the GameState object to set:
        - `currentMovie` (e.g., `"TheDarkKnight"`),
        - `currentAnswer` (e.g., `"The Dark Knight"`),
        - `imageVersion` to 1,
        - `skipsRemaining` based on the chosen game type.
    - Send a response that includes these values and the first movie image (using a valid image filename built from
      `currentMovie` + imageVersion).

2. **In-Game Command Handling:**
    - For command `"guess"`: Compare the client's guess (from field `"guess"`) with `currentAnswer`.
        - If correct, update the game state with a new movie (and reset imageVersion, assign new `currentAnswer`).
        - If incorrect, return an error message and optionally repeat the current question.
    - For command `"next"`: Increase `imageVersion` by 1 (if less than maximum allowed); use `currentMovie` and new
      version to build the image filename.
    - For command `"skip"`: Decrement `skipsRemaining`; if available, select a new movie and update the game state (
      reset imageVersion, update `currentMovie` and `currentAnswer`).
    - For command `"remaining"`: Return the current value of `skipsRemaining`.
    - For command `"quit"`: Mark the game as over and send a farewell message/image.

3. **Error-Handling:**
    - For any command received when the game is not started, return:
      `"Game not started. Please type 'play' to start the game."`
    - Log detailed errors if any unexpected value or a missing state is encountered.

---

## 2. UI Improvements

### Objectives

- Enhance the ClientGui to control game flow more clearly through UI elements rather than relying solely on text
  commands.
- Include dedicated buttons (or menu items) for:
    - **Starting the Game:** “Play” button that sends a `"gameStart"` request.
    - **Guessing the Answer:** Either a dedicated input area or a button that submits the guess.
    - **Requesting a Clearer Image:** “Next” button.
    - **Skipping the Movie:** “Skip” button (only enabled if skips remain).
    - **Viewing Remaining Skips:** “Remaining” button or status label.
    - **Quitting the Game:** “Quit” button to gracefully end the session.
- Improve status feedback, such as displaying the current score, timer, or game state.

### Steps

1. **Add Buttons/Controls:**
    - Modify the ClientGui layout (and/or the OutputPanel) to include buttons for “Play”, “Next”, “Skip”, “Remaining”,
      and “Quit”.
    - Connect these buttons to the existing event handler methods (or create new ones) so that they send the
      corresponding JSON requests.

2. **Enhance Output Display:**
    - Use labels or status panels to show current game stats (e.g., number of skips left, current score, time
      remaining).
    - Ensure that the main image display (in PicturePanel) updates smoothly when new images are received.

3. **User Flow:**
    - Disable in-game command buttons until after registration and game initialization.
    - Enable “Play” to start a game; once in-game, enable other commands as appropriate.
    - Provide visual cues (e.g., pop-up messages) for errors or key events.

---

## 3. Leaderboard Integration

### Objectives

- Implement a persistent leaderboard that tracks the highest scores or best times.
- Update the leaderboard at the end of each game session.
- Allow users to view the leaderboard from the UI.

### Steps

1. **Design Data Structure:**
    - Create a Leaderboard class or file storage mechanism (e.g., using a text file or simple database) to record player
      names and scores.
    - Update the leaderboard if the current player improves their score.

2. **Integrate with Game Flow:**
    - At game end (e.g., after a quit command or timer expiry), calculate the score based on number of correct guesses
      and game duration.
    - Save/update the leaderboard.
    - Provide an option in the UI to view the leaderboard.

3. **UI Feedback:**
    - Display leaderboard information in a separate panel or dialog window.
    - Consider sorting by highest score or fastest game completion.

---

## 4. Polishing and Testing

### Objectives

- Clean up hardcoded test statements; ensure the game logic is dynamic.
- Perform thorough testing across various game scenarios.
- Polish UI and overall user experience.

### Steps

1. **Dynamic Testing:**
    - Update JUnit tests to account for dynamic game state (e.g., not relying on fixed movie names but simulating
      multiple rounds).
    - Simulate various scenarios (correct guess, wrong guess, no skips left, etc.) and verify responses.

2. **Code Clean-Up:**
    - Refactor repeated code into helper methods where possible.
    - Ensure that all exceptions are handled gracefully and logged.

3. **User Experience:**
    - Verify that the game flow is intuitive (registration → game start → in-game commands → game end).
    - Refine UI layouts, font sizes, colors, or transitions based on usability feedback.
    - Consider adding a help menu or instructions that briefly describe the available commands.

---

## Conclusion

This plan aims to complete Activity 2 by finalizing the dynamic game flow, adding UI improvements to control the game
more naturally, integrating a leaderboard for persistent scoring, and polishing overall functionality. Working
step-by-step through this plan will help ensure that the server reliably processes commands, the game state is
maintained per session, and the user interface provides a clear, engaging experience.

*End of Plan*
