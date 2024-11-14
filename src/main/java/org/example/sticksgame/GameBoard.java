package org.example.sticksgame;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameBoard extends JPanel {
    private static final int grid_size = 7;
    private final JLabel messageLabel;
    private final Square[] board;
    private final GameClient gameClient;

    public GameBoard(GameClient gameClient) {
        this.gameClient = gameClient;
        this.messageLabel = new JLabel("");
        this.board = new Square[grid_size * grid_size];

        setBackground(Color.ORANGE);
        setLayout(new GridLayout(grid_size, grid_size));

        for (int i = 0; i < board.length; i++) {
            board[i] = new Square(i);
            add(board[i]);
        }

        messageLabel.setOpaque(true);
        messageLabel.setBackground(Color.LIGHT_GRAY);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setFont(new Font("Roboto", Font.BOLD, 15));
    }

    public JLabel getMessageLabel() {
        return messageLabel;
    }

    public Square[] getBoard() {
        return board;
    }

    public class Square extends JPanel {
        @Serial
        private static final long serialVersionUID = 1L;
        private Color lineColor = Color.LIGHT_GRAY;
        private final int position;

        public Square(int pos) {
            this.position = pos;
            MouseAdapter mouseEvent = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    gameClient.sendMove(position);
                }
            };

            if (pos % 2 != 0) {
                this.addMouseListener(mouseEvent);
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int row = this.position / grid_size;
            if (this.position % 2 != 0) {
                g.setColor(lineColor);
                if (row % 2 == 0) {
                    g.fillRect(0, 17, 60, 16);
                } else {
                    g.fillRect(17, 0, 16, 60);
                }
            } else if (row % 2 == 0) {
                g.setColor(Color.GRAY);
                g.fillOval(10, 10, 30, 30);


            }
        }

        public void setLineColor(Color color) {
            this.lineColor = color;
            repaint();
        }
    }
}
