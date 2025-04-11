# Movie Guessing Game Protocol

**Author:** [Christian J. Gonzalez]

## Overview

This protocol defines the JSON message formats exchanged between the client and server in the movie guessing game. The
game uses session management, dynamic game length selection, and several in-game commands to manage gameplay.
Additionally, the protocol supports a multiple-choice quiz and persistent leaderboard updates. All messages are
exchanged over a socket connection, and each interaction is logged with timestamps and client details for effective
debugging and monitoring.

All communication must follow the format described below, and error handling is standardized so that any missing fields,
invalid types, or unsupported commands return a clear error response.

---

## Protocol Specification

### 1. Start Request

**Purpose:**  
Signals the beginning of a new session immediately after connection establishment.

**Request:**

```json
{
  "type": "start"
}
```

**Success Response:**

```json
{
  "type": "hello",
  "ok": true,
  "value": "Hello, please tell me your name.",
  "image": "<Base64-encoded welcome image>",
  "sessionID": "<generated-session-ID>"
}
```

**Error Response:**

```json
{
  "type": "error",
  "ok": false,
  "message": "<Error details>"
}
```

---

### 2. Name Request

**Purpose:**  
Sent by the client after the greeting, to register the player’s name.

**Request:**

```json
{
  "type": "name",
  "sessionID": "<session-ID>",
  "value": "<Player Name>"
}
```

**Success Response:**

```json
{
  "type": "greeting",
  "ok": true,
  "value": "Welcome <Player Name>! Please type 'play' to start the game, or 'quit' to exit."
}
```

**Error Response:**

```json
{
  "type": "error",
  "ok": false,
  "message": "Field value does not exist in request"
}
```

---

### 3. Game Start Request

**Purpose:**  
Initiates the game session when the player types “play.” The client may include the desired game length; if omitted, the
server defaults to "short."

**Request:**

```json
{
  "type": "gameStart",
  "sessionID": "<session-ID>",
  "gameLength": "<short|medium|long>"
}
```

**Success Response:**

```json
{
  "type": "game",
  "command": "start",
  "ok": true,
  "message": "Game started (short mode). Here is your movie image. Enter your guess, or type 'next', 'skip', or 'remaining'.",
  "imageVersion": 1,
  "skipsRemaining": 2,
  "gameDuration": 30,
  "image": "<Base64-encoded movie image>"
}
```

**Error Response:**

```json
{
  "type": "error",
  "ok": false,
  "message": "<Error details>"
}
```

---

### 4. In-Game Commands

After the game starts, the client sends commands using `"type": "game"` along with a `"command"` field. All such
commands must include the `sessionID`.

#### a) Guess Command

**Purpose:**  
Submits the player’s guess for the current movie.

**Request:**

```json
{
  "type": "game",
  "sessionID": "<session-ID>",
  "command": "guess",
  "guess": "<Player Guess>"
}
```

**Success Response (Correct Guess):**

```json
{
  "type": "game",
  "command": "guess",
  "ok": true,
  "result": true,
  "message": "Correct! Here comes your next movie.",
  "image": "<Base64-encoded new movie image>",
  "imageVersion": 1,
  "skipsRemaining": 2
}
```

**Success Response (Incorrect Guess):**

```json
{
  "type": "game",
  "command": "guess",
  "ok": true,
  "result": false,
  "message": "Incorrect. Try again.",
  "question": "What is your guess for the current movie?"
}
```

> The server first checks whether the answer is submitted within the allowed game duration. If the time is up, the guess
> is rejected.

#### b) Next Command

**Purpose:**  
Requests a less pixelated version of the current movie image.

**Request:**

```json
{
  "type": "game",
  "sessionID": "<session-ID>",
  "command": "next"
}
```

**Success Response:**

```json
{
  "type": "game",
  "command": "next",
  "ok": true,
  "message": "Providing a clearer image.",
  "imageVersion": 2,
  "image": "<Base64-encoded movie image>"
}
```

**Error Response:**

```json
{
  "type": "game",
  "command": "next",
  "ok": false,
  "message": "No more 'next' images available for this movie."
}
```

#### c) Skip Command

**Purpose:**  
Skips the current movie. The number of allowed skips depends on the game length (e.g., 2 for short, 4 for medium, 6 for
long).

**Request:**

```json
{
  "type": "game",
  "sessionID": "<session-ID>",
  "command": "skip"
}
```

**Success Response:**

```json
{
  "type": "game",
  "command": "skip",
  "ok": true,
  "message": "Movie skipped. Here is your new movie image.",
  "skipsRemaining": 1,
  "image": "<Base64-encoded new movie image>"
}
```

**Error Response:**

```json
{
  "type": "game",
  "command": "skip",
  "ok": false,
  "message": "No skips remaining."
}
```

#### d) Remaining Command

**Purpose:**  
Queries how many skips remain for the session.

**Request:**

```json
{
  "type": "game",
  "sessionID": "<session-ID>",
  "command": "remaining"
}
```

**Success Response:**

```json
{
  "type": "game",
  "command": "remaining",
  "ok": true,
  "skipsRemaining": 1
}
```

#### e) Quit Command

**Purpose:**  
Ends the game session. The final score is computed based on correct guesses and game duration, and leaderboard is
updated persistently.

**Request:**

```json
{
  "type": "game",
  "sessionID": "<session-ID>",
  "command": "quit"
}
```

**Success Response:**

```json
{
  "type": "game",
  "command": "quit",
  "ok": true,
  "finalScore": 95.0,
  "leaderboard": "<Formatted leaderboard>",
  "message": "Thank you for playing. Your score: 95.0"
}
```

#### f) Leaderboard Command

**Purpose:**  
Retrieves the persistent leaderboard.

**Request:**

```json
{
  "type": "game",
  "sessionID": "<session-ID>",
  "command": "leaderboard"
}
```

**Success Response:**

```json
{
  "type": "leaderboard",
  "ok": true,
  "leaderboard": "<Formatted leaderboard text>"
}
```

---

### 5. Quiz Game Request (Multiple-Choice)

**Purpose:**  
Used when sending or answering a quiz question.

**Request:**

```json
{
  "type": "quizgame",
  "sessionID": "<session-ID>",
  "question": "<Quiz Question>",
  "options": [
    "Option 1",
    "Option 2",
    "Option 3",
    "Option 4"
  ],
  "answer": 2
}
```

**Success Response:**

```json
{
  "type": "quizgame",
  "ok": true,
  "result": 2
}
```

**Error Response:**

```json
{
  "type": "quizgame",
  "ok": false,
  "message": "Answer is not in range of options"
}
```

---

### 6. General Error Responses

**Format:**

```json
{
  "type": "error",
  "ok": false,
  "message": "<Specific error description>"
}
```

**Examples:**

- `"Field <key> does not exist in request"`
- `"Field <key> needs to be of type: <expected type>"`
- `"Type <type> is not supported."`
- `"req not JSON"`

---

### 7. Timing and Score Tracking

- **Timing:** The game runs for a fixed duration (30/60/90 seconds). The server ignores inputs after time expires.
- **Scoring Formula:**  
  `score = (number of correct guesses / game duration in seconds) * 100`

---

### 8. Leaderboard Persistence

- **Key Format:**  
  `<Player Name>@<Client IP>`
- **Update Rule:** Only update if the new score is higher than the previous one.
- **Request:** Use the `"leaderboard"` command.

---

### 9. Logging

- Logs contain:
    - Timestamp
    - Client IP
    - Request/Response content
    - Errors (stack traces, exceptions)