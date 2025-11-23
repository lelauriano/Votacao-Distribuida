package server;

import common.Candidate;
import javax.swing.JPanel;
import java.awt.*;
import java.util.List;

public class ChartPanel extends JPanel {
    private List<Candidate> data;

    public void updateData(List<Candidate> data) {
        this.data = data;
        this.repaint(); // Força o redesenho chamando paint()
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); // Limpa o fundo
        
        if (data == null || data.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / data.size();
        int maxVotes = 1;

        // Achar o maior valor para escala
        for (Candidate c : data) maxVotes = Math.max(maxVotes, c.getVotes());

        // Desenhar barras
        for (int i = 0; i < data.size(); i++) {
            Candidate c = data.get(i);
            int barHeight = (int) ((double) c.getVotes() / maxVotes * (height - 50));
            
            int x = i * barWidth + 10;
            int y = height - barHeight - 20;

            // Barra
            g2d.setColor(new Color(100, 149, 237)); // Cornflower Blue
            g2d.fillRect(x, y, barWidth - 20, barHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, barWidth - 20, barHeight);

            // Texto
            g2d.drawString(c.getName(), x, height - 5);
            g2d.drawString(String.valueOf(c.getVotes()), x + (barWidth/2) - 10, y - 5);
        }
    }
}