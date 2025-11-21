package server;

import common.Election;
import common.Vote;
import network.NetworkPrimitive;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.Scanner;

public class VotingServer {
    private final int port;
    private final Election election;
    private final ExecutorService pool;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private volatile boolean accepting = true;
    private ServerSocket serverSocket;

    public VotingServer(int port, Election election, int poolSize) {
        this.port = port;
        this.election = election;
        this.pool = Executors.newFixedThreadPool(poolSize);
    }
    
    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        
        // MODIFICAÇÃO 1: Log da Thread Principal (Accept Thread)
        System.out.println("[" + Thread.currentThread().getName() + "] VotingServer listening on port " + port);
        System.out.println(election);

        try {
            while (accepting) {
                Socket s = serverSocket.accept();
                if (!accepting) { // in case we closed while blocking on accept
                    s.close();
                    break;
                }
                ClientHandler handler = new ClientHandler(s);
                clients.add(handler);
                pool.submit(handler);
                
                // Log de nova conexão
                System.out.println("[" + Thread.currentThread().getName() + "] Client connected: " + s.getRemoteSocketAddress());
            }
        } catch (IOException e) {
            if (accepting) {
                System.err.println("Server accept error: " + e.getMessage());
            }
            // else we're shutting down
        } finally {
            shutdown();
        }
    }
    
    public void broadcast(Map<String, Object> payload) {
        for (ClientHandler ch : clients) {
            try {
                ch.sendObject(payload);
            } catch (IOException e) {
                System.err.println("Failed to broadcast to client: " + e.getMessage());
                // Handler will be removed on cleanup
            }
        }
    }

    public void removeClient(ClientHandler ch) {
        clients.remove(ch);
    }
    
    public void shutdown() {
        accepting = false;
        System.out.println("Shutting down server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException ignored) {}
        // close all client handlers
        for (ClientHandler ch : clients) ch.safeClose();
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) pool.shutdownNow();
        } catch (InterruptedException ignored) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Server stopped.");
    }
    
    public void startController(long timeoutSeconds) {
        if (timeoutSeconds <= 0) return; // no automatic closing
        Thread controller = new Thread(() -> {
            try {
                // MODIFICAÇÃO 2: Log da Thread Controladora
                System.out.println("[" + Thread.currentThread().getName() + "] Controller started: election will close in " + timeoutSeconds + " seconds.");
                TimeUnit.SECONDS.sleep(timeoutSeconds);
                // Close the election
                election.close();
                System.out.println("Controller: election closed automatically.");
                Map<String, Object> payload = new HashMap<>();
                payload.put("message", "ELECTION_CLOSED");
                payload.put("results", election.getResultsSnapshot());
                // Broadcast final results
                broadcast(payload);
            } catch (InterruptedException e) {
                System.out.println("Controller interrupted.");
                Thread.currentThread().interrupt();
            }
        }, "Election-Controller"); // Nome da Thread Controladora
        controller.setDaemon(true);
        controller.start();
    }
    
    private class ClientHandler extends NetworkPrimitive implements Runnable {
        private final Socket sock;
        private volatile boolean alive = true;

        public ClientHandler(Socket sock) {
            this.sock = sock;
            this.socket = sock; // set parent field so NetworkPrimitive methods work
        }

        @Override
        public void run() {
            try {
                // Important: initialize streams (NetworkPrimitive writes ObjectOutputStream first)
                initializeStreams();

                // Immediately send the election object (clients expect this)
                sendObject(election);

                // Read objects until the client disconnects or election closes
                while (alive && sock != null && !sock.isClosed()) {
                    Object obj;
                    try {
                        obj = receiveObject();
                    } catch (IOException | ClassNotFoundException e) {
                        // likely client disconnect or stream closed
                        break;
                    }
                    if (obj == null) break;

                    if (obj instanceof Vote) {
                        Vote vote = (Vote) obj;
                        String rawOption = vote.getOption();
                        // Resolve option text (accept "1".."n" or exact text, case-insensitive)
                        String chosen = resolveOption(rawOption);
                        Map<String, Object> resp = new HashMap<>();

                        if (!election.isOpen()) {
                            // Election closed - inform client
                            resp.put("message", "ELECTION_CLOSED");
                            resp.put("results", election.getResultsSnapshot());
                            sendObject(resp);
                            // Also ensure we broadcast closure to all (in case controller didn't)
                            Map<String, Object> bc = new HashMap<>();
                            bc.put("message", "ELECTION_CLOSED");
                            bc.put("results", election.getResultsSnapshot());
                            broadcast(bc);
                            break;
                        }

                        if (chosen == null) {
                            resp.put("message", "INVALID_OPTION");
                            resp.put("results", election.getResultsSnapshot());
                            sendObject(resp);
                        } else {
                            boolean ok = election.vote(chosen); // synchronized inside Election
                            if (ok) {
                                resp.put("message", "OK");
                            } else {
                                resp.put("message", "REJECTED"); // could be due to election closed
                            }
                            resp.put("results", election.getResultsSnapshot());

                            // send a direct response to the client who voted
                            sendObject(resp);

                            // broadcast updated results to all connected clients
                            Map<String, Object> bc = new HashMap<>();
                            bc.put("message", "UPDATE");
                            bc.put("results", election.getResultsSnapshot());
                            broadcast(bc);

                            // MODIFICAÇÃO 3: Log da Thread do Pool (Processamento de Voto)
                            System.out.println("[" + Thread.currentThread().getName() + "] Vote processed from " + sock.getRemoteSocketAddress() + " -> " + chosen);
                        }
                    } else {
                        // Unknown object type: reply with info but keep connection
                        Map<String, Object> resp = new HashMap<>();
                        resp.put("message", "UNRECOGNIZED_OBJECT");
                        resp.put("type", obj.getClass().getName());
                        sendObject(resp);
                    }
                }

            } catch (IOException e) {
                System.err.println("ClientHandler IO error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        /**
         * Helper: accept either 1-based index string or option text (case-insensitive).
         */
        private String resolveOption(String raw) {
            if (raw == null) return null;
            String t = raw.trim();
            // try number
            try {
                int idx = Integer.parseInt(t);
                if (idx >= 1 && idx <= election.getOptions().size()) {
                    return election.getOptions().get(idx - 1);
                }
            } catch (NumberFormatException ignored) { }
            // try exact match case-insensitive
            for (String o : election.getOptions()) {
                if (o.equalsIgnoreCase(t)) return o;
            }
            return null;
        }

        /**
         * Safe send that delegates to NetworkPrimitive.
         */
        @Override
        public void sendObject(Object obj) throws IOException {
            super.sendObject(obj);
        }
        
        private void cleanup() {
            alive = false;
            removeClient(this);
            safeClose();
            System.out.println("Client disconnected: " + sock.getRemoteSocketAddress());
        }
        
        public void safeClose() {
            try { close(); } catch (Exception ignored) {}
        }
    }
    
    public static void main(String[] args) throws Exception {
        int port = 12345;
        int poolSize = 16; // change as needed
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        if (args.length >= 2) poolSize = Integer.parseInt(args[1]);

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Enter election question:");
            String question = sc.nextLine().trim();

            System.out.println("Enter options (comma-separated):");
            String opts = sc.nextLine().trim();

            List<String> options = new ArrayList<>();
            for (String o : opts.split(",")) {
                if (!o.trim().isEmpty()) options.add(o.trim());
            }

            if (options.size() < 2) {
                System.err.println("Need at least two options. Exiting.");
                return;
            }

            Election election = new Election(question, options);
            VotingServer server = new VotingServer(port, election, poolSize);

            // Ask for controller timeout
            System.out.println("Enter automatic close timeout in seconds (or blank to skip):");
            String timeoutLine = sc.nextLine().trim();
            long timeoutSeconds = 0;
            if (!timeoutLine.isEmpty()) {
                try { timeoutSeconds = Long.parseLong(timeoutLine); } catch (NumberFormatException ignored) {}
            }

            // Start automatic controller (if timeoutSeconds > 0)
            server.startController(timeoutSeconds);

            // Start the server (blocking in a separate thread so we can accept "exit" command)
            Thread serverThread = new Thread(() -> {
                try {
                    server.startServer();
                } catch (IOException e) {
                    System.err.println("Server failed: " + e.getMessage());
                }
            }, "VotingServer-AcceptThread"); // Nome da Thread Aceitadora
            serverThread.start();

            // Allow manual shutdown by typing "exit"
            System.out.println("Type 'exit' and press Enter to stop the server gracefully.");
            while (sc.hasNextLine()) {
                String cmd = sc.nextLine().trim();
                if ("exit".equalsIgnoreCase(cmd)) {
                    System.out.println("Manual shutdown requested.");
                    server.shutdown();
                    break;
                }
            }

            // Wait for server thread to finish before exiting main
            serverThread.join();
        }
    }
}
