package client;
/*Cliente TCP*/
import common.Election;
import common.Vote;
import network.NetworkPrimitive;

import java.io.*;
import java.net.*;

public class VotingClient extends NetworkPrimitive {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    
    public void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            initializeStreams();
            
            // Receber dados da eleição do servidor
            Object received = receiveObject();
            if (received instanceof Election) {
                Election election = (Election) received;
                startVotingProcess(election);
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            close();
        }
    }
    
    private void startVotingProcess(Election election) {
        // Aqui você integraria com a interface gráfica
        System.out.println("Election: " + election.getQuestion());
        
        // Simulação do processo de votação
        String cpf = "12345678901"; // Isso viria da UI
        int optionIndex = 0; // Isso viria da UI
        
        Vote vote = new Vote(cpf, optionIndex);
        
        try {
            sendObject(vote);
            
            // Receber resposta do servidor
            Object response = receiveObject();
            if (response instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> voteResponse = (Map<String, Object>) response;
                System.out.println("Vote result: " + voteResponse.get("message"));
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during voting: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        new VotingClient().connectToServer();
    }
}