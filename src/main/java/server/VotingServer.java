public class VotingServer {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // Handle client connection and voting logic here
        }
    }

    public static void main(String[] args) {
        new VotingServer().startServer();
    }
}