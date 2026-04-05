# 💬 Java TCP Chat & File Sync 

A robust, multi-threaded Client-Server chat application built entirely from scratch using **Java SE (Swing & TCP Sockets)**. It features real-time messaging, private mentions, seamless file transfers, and a live online user directory—all powered by a custom asynchronous binary/text protocol.

---

## ✨ Features

- **Real-Time Global Chat:** Instant message broadcasting to all connected users.
- **Private Messaging:** Built-in mention system. Just type `@username Hello!` to send a private message seamlessly.
- **File Transfer Protocol:** Send and receive binary files of any type. The system streams files over the active socket connection synchronously.
- **Live User Directory:** A side panel that instantly updates who goes online or offline.
- **Multi-threaded Architecture:** 100% non-blocking. The server spawns dedicated `ClientHandler` threads for every connection, ensuring zero wait times and UI lockups.
- **Zero Dependencies:** Pure Java (`java.net`, `java.io`, `javax.swing`). No external libraries required.

## 🛠️ Architecture & Tech Stack

- **Language:** Java SE 
- **GUI Framework:** Java Swing
- **Network Programming:** TCP Sockets (`java.net.Socket`, `ServerSocket`)
- **Concurrency:** Multi-threading (`Runnable`, `Thread`, `ConcurrentHashMap`)
- **I/O Management:** Data Streams (`DataInputStream`, `DataOutputStream`)

### 🔌 Custom Protocol
The app uses a lightweight, custom pipe-delimited (`|`) UTF-8 protocol:
- `CONNECT|username`
- `MESSAGE|username|content`
- `PRIVATE|username|content` 
- `FILE|sender|filename|bytesize` 
- `USERLIST|user1,user2...`

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 8 or higher.

### Running the Server
1. Navigate to the source folder.
2. Compile and run `ChatServer.java`.
3. The server will start listening silently on port `5000` by default.

### Running the Client(s)
1. Compile and run `ChatGUI.java` (or run it via your IDE).
2. Enter the Server IP (e.g., `127.0.0.1` for local testing).
3. Choose a unique username and hit **Connect**.
4. _Tip: You can launch multiple instances of the Client app simultaneously to test multi-user interactions!_

---

*Developed with ❤️ as a demonstration of Socket Programming and Thread Management fundamentals in Java.*
