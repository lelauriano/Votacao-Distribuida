package server;

import common.VotePacket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ServerController controller;

    public ClientHandler(Socket socket, ServerController controller) {
        this.socket = socket;
        this.controller = controller;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            while (true) {
                VotePacket packet = (VotePacket) in.readObject();
                
                if (packet.getCommand() == VotePacket.Command.LOGIN) {
                    // Envia lista de candidatos ao conectar
                    VotePacket response = new VotePacket(VotePacket.Command.LIST_CANDIDATES);
                    response.setCandidates(controller.getCandidates());
                    out.writeObject(response);
                } 
                else if (packet.getCommand() == VotePacket.Command.VOTE) {
                    boolean success = controller.registerVote(packet.getPayloadData(), packet.getCandidateCode());
                    if (success) {
                        out.writeObject(new VotePacket(VotePacket.Command.CONFIRM, "Voto computado com sucesso!"));
                    } else {
                        out.writeObject(new VotePacket(VotePacket.Command.ERROR, "CPF já votou ou inválido!"));
                    }
                }
                else if (packet.getCommand() == VotePacket.Command.DISCONNECT) {
                    break;
                }
            }
        } catch (Exception e) {
            // Cliente desconectou abruptamente
        }
    }
}