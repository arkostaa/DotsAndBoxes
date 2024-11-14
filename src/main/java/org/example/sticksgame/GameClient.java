package org.example.sticksgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.logging.Logger;
import java.util.logging.Level;

public class GameClient {
    private static final Logger logger = Logger.getLogger(GameClient.class.getName());
    private static final int GRID_SIZE = 7;
    private static final int PORT = 7777;
    private static final String PLAY_AGAIN_PROMPT = "Сыграть ещё?";
    private final JFrame frame = new JFrame("Палочки");
    private final GameBoard gameBoard;
    private char playerColor;
    private BufferedReader inputBuffer;
    private PrintWriter outputBuffer;
    private Socket sock;

    public GameClient(String serverAddress) {
        logger.info("Initializing GameClient...");
        gameBoard = new GameBoard(this);
        setupFrame();
        connectToServer(serverAddress);
    }

    private void setupFrame() {
        logger.info("Setting up game frame...");
        frame.getContentPane().add(gameBoard, "Center");
        frame.getContentPane().add(gameBoard.getMessageLabel(), "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GRID_SIZE * 50 + 15, GRID_SIZE * 50 + 60);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private void connectToServer(String serverAddress) {
        try {
            logger.info("Connecting to server at " + serverAddress + ":" + PORT);
            sock = new Socket(serverAddress, PORT);
            inputBuffer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            outputBuffer = new PrintWriter(sock.getOutputStream(), true);
            logger.info("Connected to server successfully.");
        } catch (IOException ex) {
            // Убираем ex.printStackTrace() и полагаемся на логгер для вывода информации об ошибке
            logger.log(Level.SEVERE, "Failed to connect to server", ex);
        }
    }


    public void startGame() {
        logger.info("Starting game loop...");
        while (true) {
            play();
            if (!replay()) {
                endGame();
                break;
            }
        }
    }

    public void sendMove(int position) {
        logger.info("Sending move: " + position);
        outputBuffer.println("M " + position);
    }

    private void endGame() {
        logger.info("Ending game...");
        outputBuffer.println("Q " + playerColor);
        try {
            sock.close();
            logger.info("Socket closed.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing socket", e);
            throw new RuntimeException(e);
        }
        frame.dispose();
    }

    private boolean replay() {
        int response = JOptionPane.showConfirmDialog(frame, PLAY_AGAIN_PROMPT, gameBoard.getMessageLabel().getText(),
                JOptionPane.YES_NO_OPTION);
        frame.dispose();
        logger.info("Replay response: " + (response == JOptionPane.YES_OPTION ? "Да" : "Нет"));
        return (response == JOptionPane.YES_OPTION);
    }

    private void handleMessages() throws IOException {
        boolean endGame = false;
        String response;
        int position;
        char messageType;

        while (!endGame) {
            response = inputBuffer.readLine();
            logger.info("Received message: " + response);
            messageType = response.isEmpty() ? ' ' : response.charAt(0);
            switch (messageType) {
                case 'S':
                    position = Integer.parseInt(response.substring(4));
                    gameBoard.getBoard()[position].setBackground(
                            (response.charAt(2) == 'B') ? Color.BLUE : Color.RED);
                    break;

                case 'V':
                    if (response.charAt(2) == 'Y') {
                        gameBoard.getMessageLabel().setText("Ходите еще раз");
                    } else {
                        gameBoard.getMessageLabel().setText("Ход противника, подождите");
                    }
                    position = Integer.parseInt(response.substring(4));
                    gameBoard.getBoard()[position].setLineColor(
                            (playerColor == 'B') ? Color.BLUE : Color.RED);
                    break;

                case 'O':
                    if (response.charAt(2) == 'Y') {
                        gameBoard.getMessageLabel().setText("Повторный ход противника");
                    } else {
                        gameBoard.getMessageLabel().setText("Ваш ход");
                    }
                    position = Integer.parseInt(response.substring(4));
                    gameBoard.getBoard()[position].setLineColor(
                            (playerColor == 'B') ? Color.RED : Color.BLUE);
                    break;

                case 'E':
                    if (response.charAt(2) == playerColor) {
                        gameBoard.getMessageLabel().setText("Вы выиграли!");
                    } else if (response.charAt(2) == 'T') {
                        gameBoard.getMessageLabel().setText("Ничья!");
                    } else {
                        gameBoard.getMessageLabel().setText("Вы проиграли!");
                    }
                    endGame = true;
                    break;

                case 'I':
                    gameBoard.getMessageLabel().setText(response.substring(2));
                    break;

                default:
                    logger.warning("Unknown message type: " + messageType);
                    break;
            }
        }
    }

    private void play() {
        String serverMessage;

        try {
            while (true) {
                serverMessage = inputBuffer.readLine() + " ";
                logger.info("Waiting for server message...");
                if (serverMessage.charAt(0) == 'W') {
                    playerColor = serverMessage.charAt(2);
                    String colorLabel = (playerColor == 'B') ? "Синий" : "Красный";
                    frame.setTitle("Палочки " + colorLabel + " Игрок");
                    logger.info("Player color set: " + colorLabel);
                    break;
                }
            }

            handleMessages();
        } catch (Exception error) {
            logger.log(Level.SEVERE, "Error during gameplay", error);
            try {
                sock.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing socket after exception", e);
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        logger.info("GameClient started.");
        String serverAddress = (args.length == 0) ? "localhost" : args[0];
        GameClient player = new GameClient(serverAddress);
        player.startGame();
    }
}
