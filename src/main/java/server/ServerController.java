package server;

import common.Candidate;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerController {
    private List<Candidate> candidates;
    private ConcurrentHashMap<String, Boolean> votedCpfs; // Thread-safe para evitar voto duplo
    private ServerGUI gui;
    private boolean isRunning = true;
    private static final int PORT = 1234; // Mesma porta do NetProtocol

    public ServerController(ServerGUI gui) {
        this.gui = gui;
        this.votedCpfs = new ConcurrentHashMap<>();
        initializeElection();
    }

    private void initializeElection() {
        candidates = new ArrayList<>();
        candidates.add(new Candidate(99, "Branco / Nulo"));
        candidates.add(new Candidate(10, "Candidato Java"));
        candidates.add(new Candidate(20, "Candidato Python"));
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                gui.log("Server listening on port " + PORT);
                
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    // Multithreading: Cria uma nova thread para cada cliente 
                    new Thread(new ClientHandler(clientSocket, this)).start();
                }
            } catch (Exception e) {
                gui.log("Server Error: " + e.getMessage());
            }
        }).start();
    }

    // Métodos sincronizados para garantir integridade dos votos
    public synchronized boolean registerVote(String cpf, int candidateCode) {
        if (votedCpfs.containsKey(cpf)) return false;
        
        for (Candidate c : candidates) {
            if (c.getCode() == candidateCode) {
                c.addVote();
                votedCpfs.put(cpf, true);
                gui.updateChart(candidates); // Atualiza a GUI
                return true;
            }
        }
        return false;
    }

    public List<Candidate> getCandidates() { return candidates; }
}