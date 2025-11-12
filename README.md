# Voting System UI

This project implements a voting system user interface that allows users to participate in elections. The application is structured to facilitate easy interaction between clients and the server, ensuring a smooth voting experience.

## Project Structure

- **src/main/java/client/VotingClientUI.java**: Contains the `VotingClientUI` class responsible for the user interface of the voting client. It handles user interactions, displays election questions, and collects votes from users.

- **src/main/java/common/Election.java**: Contains the `Election` class, which represents the election data, including properties for the election question and options, along with getters for these properties.

- **src/main/java/network/NetworkPrimitive.java**: Contains the abstract class `NetworkPrimitive`, which provides methods for network communication, including initializing streams, sending and receiving objects, and closing connections.

- **src/main/java/server/VotingServer.java**: Contains the `VotingServer` class, which manages the server-side logic for the voting system. It handles incoming client connections, processes votes, and maintains the current election state.

- **src/main/resources/application.properties**: Contains configuration properties for the application, such as server settings and any other necessary configurations.

- **src/test/java/VotingSystemUITest.java**: Contains the test class `VotingSystemUITest`, which includes unit tests for the `VotingClientUI` class to ensure that the user interface behaves as expected.

## Setup Instructions

1. Clone the repository to your local machine.
2. Navigate to the project directory.
3. Build the project using Gradle.
4. Run the server and then the client to start voting.

## Usage

- Launch the server first to handle incoming connections.
- Start the client application to interact with the voting system.
- Follow the prompts to participate in the election.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.