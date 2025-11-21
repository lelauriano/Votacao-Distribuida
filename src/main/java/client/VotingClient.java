// ...existing code...
package client;

import common.Election;
import common.Vote;
import network.NetworkPrimitive;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class VotingClient extends NetworkPrimitive {
    private final String serverHost = "localhost";
    private final int serverPort = 12345;

    // novo: sincronização para aguardar a Election recebida pelo listener
    private final CountDownLatch electionLatch = new CountDownLatch(1);
    private volatile Election receivedElection = null;

    public void connectToServer() {
        try (Socket s = new Socket(serverHost, serverPort)) {
            this.socket = s;
            initializeStreams();

            // inicia listener em thread separada
            startListenerThread();

            // aguarda até que a Election seja recebida pelo listener
            electionLatch.await();

            // agora faz a leitura do usuário e envia o voto
            Election e = receivedElection;
            if (e != null) {
                System.out.println("Type your option text exactly as shown (or option number): ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String choice = br.readLine().trim();

                try {
                    int idx = Integer.parseInt(choice);
                    if (idx >= 1 && idx <= e.getOptions().size()) {
                        choice = e.getOptions().get(idx - 1);
                    }
                } catch (NumberFormatException ignored) {}

                sendObject(new Vote(choice));

                // após enviar o voto, o listener ficará responsável por imprimir o resultado quando chegar
            } else {
                System.out.println("Não foi possível obter a eleição.");
            }
        } catch (Exception ex) {
            System.err.println("Client error: " + ex.getMessage());
        } finally {
            close();
        }
    }

    // novo: thread que lê objetos do servidor continuamente
    private void startListenerThread() {
        new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    Object obj = receiveObject();
                    if (obj == null) break;

                    if (obj instanceof Election) {
                        receivedElection = (Election) obj;
                        System.out.println("Received election:");
                        System.out.println(receivedElection);
                        electionLatch.countDown();
                    } else if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = (Map<String, Object>) obj;
                        System.out.println("Server: " + m.get("message"));
                        System.out.println("Results: " + m.get("results"));
                    } else {
                        System.out.println("Unexpected object from server: " + obj);
                    }
                }
            } catch (Exception e) {
                if (socket != null && !socket.isClosed()) {
                    System.err.println("Listener error: " + e.getMessage());
                }
            }
        }, "VotingClient-Listener").start();
    }

    public static void main(String[] args) {
        new VotingClient().connectToServer();
    }
}
 