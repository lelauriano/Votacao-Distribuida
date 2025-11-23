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
        super("Distributed Voting - Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);

        // Layout principal
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel loginPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        cpfField = new JTextField();
        JButton btnConnect = new JButton("Connect & Login");

        loginPanel.add(new JLabel("Enter your CPF (only digits):"));
        loginPanel.add(cpfField);
        loginPanel.add(btnConnect);

        JPanel votePanel = new JPanel(new BorderLayout());
        optionsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JButton btnVote = new JButton("CONFIRM VOTE");

        votePanel.add(new JLabel("Choose your candidate:"), BorderLayout.NORTH);
        votePanel.add(new JScrollPane(optionsPanel), BorderLayout.CENTER);
        votePanel.add(btnVote, BorderLayout.SOUTH);

        // Adiciona telas ao painel principal
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(votePanel, "VOTE");
        add(mainPanel);

        // Eventos dos botões
        btnConnect.addActionListener(e -> validateCPFAndConnect());
        btnVote.addActionListener(e -> sendVote());

        setVisible(true);
    }

    private void validateCPFAndConnect() {
        String cpf = cpfField.getText().trim();

        if (!isValidCPF(cpf)) {
            JOptionPane.showMessageDialog(this, "Invalid CPF.\nEnter 11 digits.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        connectToServer();
    }

    // Validação real de CPF
    private boolean isValidCPF(String cpf) {
        if (!cpf.matches("\\d{11}")) return false;

        // impede CPFs repetidos
        if (cpf.chars().distinct().count() == 1) return false;

        // cálculo dos dígitos verificadores
        try {
            int dig1 = calculateDigit(cpf.substring(0, 9), 10);
            int dig2 = calculateDigit(cpf.substring(0, 9) + dig1, 11);
            return cpf.equals(cpf.substring(0, 9) + dig1 + dig2);
        } catch (Exception e) {
            return false;
        }
    }

    private int calculateDigit(String base, int weight) {
        int sum = 0;
        for (char c : base.toCharArray()) {
            sum += (c - '0') * weight--;
        }
        int mod = (sum * 10) % 11;
        return (mod == 10) ? 0 : mod;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Envia comando LOGIN
            VotePacket login = new VotePacket(VotePacket.Command.LOGIN);
            out.writeObject(login);

            // Recebe lista de candidatos
            VotePacket response = (VotePacket) in.readObject();
            if (response.getCommand() == VotePacket.Command.LIST_CANDIDATES) {
                populateCandidates(response.getCandidates());
                cardLayout.show(mainPanel, "VOTE");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage());
        }
    }

    private void populateCandidates(List<Candidate> candidates) {
        optionsPanel.removeAll();
        candidateGroup = new ButtonGroup();

        for (Candidate c : candidates) {
            JRadioButton rb = new JRadioButton(c.getName());
            rb.setActionCommand(String.valueOf(c.getCode()));
            candidateGroup.add(rb);
            optionsPanel.add(rb);
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    private void sendVote() {
        ButtonModel selected = candidateGroup.getSelection();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a candidate.");
            return;
        }

        int candidateCode = Integer.parseInt(selected.getActionCommand());

        try {
            VotePacket vote = new VotePacket(VotePacket.Command.VOTE);
            vote.setPayloadData(cpfField.getText());
            vote.setCandidateCode(candidateCode);

            out.writeObject(vote);

            // Espera resposta
            VotePacket confirm = (VotePacket) in.readObject();

            JOptionPane.showMessageDialog(this, confirm.getMessage());

            if (confirm.getCommand() == VotePacket.Command.CONFIRM) {
                sendDisconnect();
                System.exit(0);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error sending vote: " + ex.getMessage());
        }
    }

    private void sendDisconnect() {
        try {
            out.writeObject(new VotePacket(VotePacket.Command.DISCONNECT));
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
