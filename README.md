# 🎬 Assignment 3 - Movie Guessing Game

Video Link: [YouTube](https://www.youtube.com/playlist?list=PLNJf3PhE4U6D9uje1Qz3t28TqqqIoDkZQ)

## 📦 Project Overview

This project implements a **movie guessing game** using a client-server model. The client displays pixelated images of
movie posters and allows users to guess the title, skip, or view a less pixelated version. A leaderboard is maintained
to track top scores.

---

## 🛠️ Running the System

### Terminal

#### Start the Server

```bash
gradle runServer -Pport=9000
```

#### Start the Client

```bash
gradle runClient -Phost=192.168.1.1 Pport=9000
```

---

## 🖼️ GUI Overview

The GUI has three main components:

- **Image Grid** (via `PicturePanel`)
- **Input and Output Panel** (via `OutputPanel`)
- **Tabbed Pane** (Game View and Leaderboard View)
- **Menu Bar** (Game controls, session info)

---

## ✅ Quick Start in Code

```java
ClientGui main = new ClientGui("localhost", 9000);
main.

newGame(2); // 2x2 grid
main.

insertImage("img/Pineapple.jpg",0,1); // Optional test
main.

show(true); // Show modal dialog
```

---

## 📋 ClientGui Features

### 🧩 Core Methods

- `show(boolean modal)` — Displays the window
- `newGame(int dimension)` — Initializes a new grid
- `insertImage(String filename, int row, int col)` — Adds image to grid
- `appendOutput(String message)` — Displays message in output panel
- `submitClicked()` — Handles guess/command logic

---

### 🧠 Menu Bar

- **Session Info** — View session ID and player name
- **Game Options:**
    - **Start** — Choose game length (Short/Medium/Long)
    - **Quit** — Ends game and connection

---

### 🗂️ Tabbed Pane

- **Game View** — Grid and input/output panel
- **Leaderboard View** — Scrollable leaderboard (persistent)

Leaderboard score formula:

```
score = (correct guesses / game duration in seconds) * 100
```

---

## 📷 PicturePanel Summary

```java
newGame(int dimension);

insertImage(String filename, int row, int col);

insertImage(ByteArrayInputStream imgBytes, int row, int col);
```

---

## ✍️ OutputPanel Summary

```java
getInputText();

setInputText(String text);

addEventHandlers(EventHandlers handlerObj);

appendOutput(String message);
```

---

## 🧾 Game Commands (JSON Format)

### 📨 Guess

```json
{
  "type": "game",
  "sessionID": "<session-id>",
  "command": "guess",
  "guess": "The Matrix"
}
```

### 📨 Next

```json
{
  "type": "game",
  "sessionID": "<session-id>",
  "command": "next"
}
```

### 📨 Skip

```json
{
  "type": "game",
  "sessionID": "<session-id>",
  "command": "skip"
}
```

### 📨 Remaining

```json
{
  "type": "game",
  "sessionID": "<session-id>",
  "command": "remaining"
}
```

### 📨 Quit

```json
{
  "type": "game",
  "sessionID": "<session-id>",
  "command": "quit"
}
```

### 📨 Leaderboard

```json
{
  "type": "game",
  "sessionID": "<session-id>",
  "command": "leaderboard"
}
```

---

## 🖼️ Image Conventions

- `1` = most pixelated
- `4` = clearest
- Server always starts with `1`

---
