# Movie Guessing Game Protocol for ClientHandler

##### Author: [Your Name]
##### Date: [Date]

## Overview
This document defines the ```json message formats used by clients and the server (via the ClientHandler) in the movie guessing game. All communication is carried out as ```json messages over a socket connection. The protocol covers the core interactions needed to initiate a game session and includes robust error handling to ensure that any improperly formatted or incomplete request results in a well-defined error response. All transactions are logged (with timestamps, client socket addresses, and detailed error messages) for monitoring and debugging purposes.

## Protocol Specification

### 1. Start Request

**Request:**
```json
{
  "type": "start"
}
```
Purpose:
Sent by the client immediately after connection establishment to signal the beginning of a session.

Success Response:

```json

{
  "type": "hello",
  "ok": true,
  "value": "Hello, please tell me your name.",
  "image": "<Base64-encoded welcome image>"
}
```
Details: The server responds with a greeting message and a welcome image (encoded in Base64).

Error Response:

```json

{
  "type": "error",
  "ok": false,
  "message": "<Error details>"
}
```
Possible Errors: Invalid json format or internal processing errors.

2. Name Request
Request:

```json

{
  "type": "name",
  "value": "<Player Name>"
}
```
Purpose:
Sent by the client after receiving the "hello" response. The player's chosen name is provided in the "value" field.

Success Response:

```json

{
  "type": "greeting",
  "ok": true,
  "value": "Welcome <Player Name>! Please type 'play' to start the game, or 'quit' to exit."
}
```
Details: The server acknowledges the player's name and instructs further action.

Error Response:

```json

{
  "type": "error",
  "ok": false,
  "message": "<Error details>"
}
```
Possible Errors: Missing or invalid "value" field.

3. Game Start Request
Request:

```json

{
  "type": "gameStart"
}
```
Purpose:
Sent by the client (e.g., after typing "play") to initiate the game session. The server then initializes the game state.

Success Response:

```json

{
  "type": "game",
  "command": "start",
  "ok": true,
  "message": "Here is your movie image. Enter your guess, or type 'next', 'skip', or 'remaining'.",
  "imageVersion": 1,
  "skipsRemaining": "<number>",
  "image": "<Base64-encoded movie image>"
}
```
Details: The server response includes:

message: Instructions to the client.

imageVersion: Indicates the current version of the movie image (e.g., most pixelated).

skipsRemaining: Number of skips allowed for the game session (set by the game type).

image: The first game image as a Base64-encoded string.

Error Response:

```json

{
  "type": "error",
  "ok": false,
  "message": "<Error details>"
}
```
Possible Errors: Errors during game state initialization.

4. Quiz Game Request (with Options)
Request:

```json

{
  "type": "quizgame",
  "question": "<Quiz Question>",
  "options": ["Paris", "London", "Berlin", "Madrid"],
  "answer": "<int>" 
}
```
Purpose:
To present a quiz question with multiple choice answers. The "options" field contains an array of possible answers, and the "answer" field holds the selected answer's index.

Success Response:

```json

{
  "type": "quizgame",
  "ok": true,
  "result": "<int>" 
}
```
Details: The response confirms that the provided answer is within the range of the options array.

Error Response:

```json

{
  "type": "quizgame",
  "ok": false,
  "message": "Answer is not in range of options"
}
```
Possible Errors: The provided answer index is out of range.

5. General Error Responses
For any invalid request (e.g., missing required fields, invalid data types, unsupported request types, or improperly formatted json), the server will return:

```json

{
  "type": "error",
  "ok": false,
  "message": "<Specific error description>"
}
```
Common Error Cases:

Missing Field: "Field <key> does not exist in request"

Invalid Field Type: "Field <key> needs to be of type: <expected type>"

Unsupported Request Type: "Type <type> is not supported."

Invalid json: "req not json"

Logging and Debugging
The server logs all events related to client communication. Each log entry includes:

Timestamp: When the event occurred.

Client Address: The remote socket address of the client.

Action: Details of the received request, processed response, or any errors encountered.

Error Details: For exceptions, both the error message and the stack trace (if applicable) are logged.

Logs are written both to the console and to a file (as configured via Logback) to facilitate monitoring and troubleshooting.