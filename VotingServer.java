package server;
/*Servidor TCP Multithread*/
import common.Election;
import common.Vote;
import network.NetworkPrimitive;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class VotingServer extends NetworkPrimitive {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    
    // Estruturas de dados para gerenciar a votação
    private Election currentElection;
    private Map<String, Boolean> votedCpfs = new ConcurrentHashMap<>();
    private Map<Integer, Integer> voteCount = new ConcurrentHashMap<>();
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            
            // Carregar dados da eleição (exemplo)
            loadSampleElection();
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
            
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    private void loadSampleElection() {
        List<String> options = Arrays.asList("Option A", "Option B", "Option C");
        currentElection = new Election("Which option do you prefer?", options);
        
        // Inicializar contagem de votos
        for (int i = 0; i < options.size(); i++) {
            voteCount.put(i, 0);
        }
    }
    
    private class ClientHandler extends NetworkPrimitive implements Runnable {
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                initializeStreams();
                
                // Enviar dados da eleição para o cliente
                sendObject(currentElection);
                
                // Receber voto do cliente
                Object received = receiveObject();
                if (received instanceof Vote) {
                    Vote vote = (Vote) received;
                    processVote(vote);
                }
                
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Client handler error: " + e.getMessage());
            } finally {
                close();
                clients.remove(this);
            }
        }
        
        private void processVote(Vote vote) {
            String cpf = vote.getCpf();
            int optionIndex = vote.getOptionIndex();
            
            // Validar CPF e prevenir duplicidade
            if (!isValidCPF(cpf)) {
                sendVoteResponse(false, "Invalid CPF");
                return;
            }
            
            if (votedCpfs.containsKey(cpf)) {
                sendVoteResponse(false, "CPF has already voted");
                return;
            }
            
            // Registrar voto
            votedCpfs.put(cpf, true);
            voteCount.put(optionIndex, voteCount.get(optionIndex) + 1);
            
            sendVoteResponse(true, "Vote registered successfully");
            updateAllClients();
        }
        
        private void sendVoteResponse(boolean success, String message) {
            try {
                Map<String, Object> response = new HashMap<>();
                response.put("success", success);
                response.put("message", message);
                sendObject(response);
            } catch (IOException e) {
                System.err.println("Error sending vote response: " + e.getMessage());
            }
        }
    }
    
    private boolean isValidCPF(String cpf) {
        // Implementar validação real de CPF aqui
        return cpf != null && cpf.matches("\\d{11}");
    }
    
    private void updateAllClients() {
        // Atualizar todos os clientes com resultados parciais
        Map<String, Object> update = new HashMap<>();
        update.put("type", "results_update");
        update.put("results", new HashMap<>(voteCount));
        
        for (ClientHandler client : clients) {
            try {
                client.sendObject(update);
            } catch (IOException e) {
                System.err.println("Error updating client: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        new VotingServer().startServer();
    }
}