package client;

/*Cliente TCP*/
import common.Election;
import common.Vote;                  // [Corrigido] Importação adicionada
import network.NetworkPrimitive;     // [Corrigido] Importação adicionada

import java.io.*;
import java.net.*;
import java.util.Map;

public class VotingClient extends NetworkPrimitive {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    // [Corrigido] Removido 'throws CloneNotSupportedException' pois não usaremos clone()
    public void connectToServer() {
        try {
            // [Corrigido] Atribuindo ao atributo 'socket' da classe pai, não a uma variável local
            this.socket = new Socket(SERVER_HOST, SERVER_PORT);
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
            close(); // [Corrigido] Era clone(), alterado para close() para fechar conexão
        }
    }

    // Métodos auxiliares que pareciam estar faltando ou incompletos no seu snippet original
    // Mantive a estrutura lógica baseada no que você enviou anteriormente

    private void startVotingProcess(Election election) {
        // Aqui você integraria com a interface gráfica
        System.out.println("Election: " + election.getQuestion());

        // Simulação do processo de votação (Isso viria da UI na prática)
        String cpf = "12345678901";
        int optionIndex = 0;

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