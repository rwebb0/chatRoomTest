# Local Chat Application
This is a simple, locally-hosted chat application built with Java. The application allows users to create or join chat rooms, communicate securely using encryption, and view previous messages. The application is designed to run on a local network.

## Features
- Create or Join Rooms: Users can create a chat room or join an existing one using a room code.
- Secure Communication: Messages are encrypted using AES-GCM with a unique key derived from the room code.
- Message History: The chat history is stored locally in an SQLite database and is accessible whenever the room is joined.
- User Notifications: The application notifies when users join or leave the chat.
- Leave Chat: Users can leave the chat room, which will close the chat window and return them to the start screen.


## Requirements
- Java 8 or higher
- SQLite (bundled with the application)


## Setup Instructions
1. Clone the Repository:
```bash
git clone https://github.com/yourusername/local-chat-application.git
cd local-chat-application
```

2. Compile and Run the Application:
   - If using an IDE like IntelliJ IDEA:
   - Open the project in your IDE.
   - Ensure that the Java SDK is configured (Java 8 or higher).
   - Run the StartScreen class. 


3. If using the command line:
```
javac -d bin -sourcepath src src/org/example/StartScreen.java
java -cp bin org.example.StartScreen
```


4. Using the Application:

- Creating a Room:
  - Enter your username.
  - Click "Create a Room."
  - The application will generate a unique room code that you can share with others on the same network.


- Joining a Room:
  - Enter your username.
  - Click "Join a Room."
  - Enter the room code provided by the room creator.


- Encryption:
  - Messages are encrypted using AES-GCM with a 256-bit key.
  - Each room has a unique encryption key derived from the room code, ensuring that messages are secure and private.


- SQLite Database:
  - The chat history is stored in a local SQLite database named chat.db.
  - This database is automatically created in the project directory.

## Limitations
- Local Network Only: This application is designed for use on a local network. Users outside the local network will not be able to join the chat rooms unless additional networking configurations (such as port forwarding) are made.
- No Internet Access: The application does not support remote connections over the internet without additional configuration.


## Future Enhancements
- Remote Connectivity: Implementing features to allow remote users to join the chat through the internet.
- Improved UI/UX: Enhancing the user interface for a more modern chat experience.


## Troubleshooting
- Cannot Connect to a Room:
    - Ensure that the devices are on the same local network.
    - Verify that the correct room code is being used. 


- Application Fails to Start:
  - Ensure Java is correctly installed and set up on your system.
  - Check that all dependencies are correctly compiled and included.
  

## Contributing
Contributions are welcome! Please download and edit this program however you may want. If there are any ideas you want to run or suggest, please let me know.