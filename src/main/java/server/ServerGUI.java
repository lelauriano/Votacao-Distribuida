package server;

import common.Candidate;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private ChartPanel chartPanel;

    public ServerGUI() {
        super("Server - Votação Distribuída");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Painel de Gráfico (Customizado)
        chartPanel = new ChartPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Resultados em Tempo Real"));
        add(chartPanel, BorderLayout.CENTER);

        // Painel de Log
        logArea = new JTextArea(5, 20);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);

        setVisible(true);
    }

    public void log(String msg) {
        SwingUtilities.invokeLater(() -> logArea.append(msg + "\n"));
    }

    public void updateChart(List<Candidate> candidates) {
        SwingUtilities.invokeLater(() -> chartPanel.updateData(candidates));
    }

    public static void main(String[] args) {
        ServerGUI gui = new ServerGUI();
        new ServerController(gui).startServer();
    }
}