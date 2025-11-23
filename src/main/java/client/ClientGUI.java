package client;

import common.Candidate;
import common.VotePacket;
import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField cpfField;
    private JPanel optionsPanel;
    private ButtonGroup candidateGroup;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientGUI() {
        super("Urna Eletrônica");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Tela 1: Login
        JPanel loginPanel = new JPanel(new GridLayout(3, 1));
        cpfField = new JTextField();
        JButton btnConnect = new JButton("Entrar");
        loginPanel.add(new JLabel("Digite seu CPF:"));
        loginPanel.add(cpfField);
        loginPanel.add(btnConnect);
        
        // Tela 2: Votação
        JPanel votePanel = new JPanel(new BorderLayout());
        optionsPanel = new JPanel(new GridLayout(0, 1)); // Dinâmico
        JButton btnVote = new JButton("CONFIRMAR VOTO");
        votePanel.add(new JLabel("Escolha seu candidato:"), BorderLayout.NORTH);
        votePanel.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);
        votePanel.add(btnVote, BorderLayout.SOUTH);

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(votePanel, "VOTE");
        add(mainPanel);

        // Listeners
        btnConnect.addActionListener(e -> connectToServer());
        btnVote.addActionListener(e -> sendVote());
        
        setVisible(true);
    }

    private void connectToServer() {
        String cpf = cpfField.getText();
        if (cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "CPF inválido");
            return;
        }
        
        try {
            socket = new Socket("localhost", 1234);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Envia Handshake
            out.writeObject(new VotePacket(VotePacket.Command.LOGIN));
            
            // Recebe Lista de Candidatos
            VotePacket response = (VotePacket) in.readObject();
            if (response.getCommand() == VotePacket.Command.LIST_CANDIDATES) {
                populateCandidates(response.getCandidates());
                cardLayout.show(mainPanel, "VOTE");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar: " + ex.getMessage());
        }
    }

    private void populateCandidates(List<Candidate> candidates) {
        optionsPanel.removeAll();
        candidateGroup = new ButtonGroup();
        
        for (Candidate c : candidates) {
            JRadioButton rb = new JRadioButton(c.getName());
            rb.setActionCommand(String.valueOf(c.getCode())); // Guarda o ID no botão
            candidateGroup.add(rb);
            optionsPanel.add(rb);
        }
        optionsPanel.revalidate();
    }

    private void sendVote() {
        ButtonModel selection = candidateGroup.getSelection();
        if (selection == null) return;

        int candidateCode = Integer.parseInt(selection.getActionCommand());
        
        try {
            VotePacket vote = new VotePacket(VotePacket.Command.VOTE);
            vote.setPayloadData(cpfField.getText()); // Envia CPF
            vote.setCandidateCode(candidateCode);
            
            out.writeObject(vote);
            
            // Espera confirmação
            VotePacket confirm = (VotePacket) in.readObject();
            JOptionPane.showMessageDialog(this, confirm.getMessage());
            
            if (confirm.getCommand() == VotePacket.Command.CONFIRM) {
                System.exit(0); // Fecha após votar
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}