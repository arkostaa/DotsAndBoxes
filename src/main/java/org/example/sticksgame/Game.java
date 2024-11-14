package org.example.sticksgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class Game {
    private static final Logger logger = Logger.getLogger(Game.class.getName());
    private static final int grid_size = 7;
    private final char[] board = new char[grid_size * grid_size];
    private int Points_Player1 = 0;
    private int Points_Player2 = 0;
    Player currentPlayer;

    public String getFinalScores() {
        return "Синий: " + Points_Player1 + " очков\nКрасный: " + Points_Player2 + " очков";
    }

    public char gameWinner() {
        logger.info("Игра окончена. Синий: " + Points_Player1 + " очков, Красный: " + Points_Player2 + " очков");
        if (Points_Player1 == Points_Player2) {
            return 'T';
        } else {
            return (Points_Player1 > Points_Player2) ? 'B' : 'R';
        }
    }

    public int DoneSquares(int pos, Player player) {
        int row = pos / grid_size;
        int doneSquaresCount = 0;

        if (row % 2 == 0) {
            if (row != 0) {
                if (board[pos - 2 * grid_size] != 0
                        && board[pos - grid_size + 1] != 0
                        && board[pos - grid_size - 1] != 0) {
                    ++doneSquaresCount;
                    player.squareCompleted(pos - grid_size);
                }
            }
            if (row != (grid_size - 1)) {
                if (board[pos + 2 * grid_size] != 0
                        && board[pos + grid_size + 1] != 0
                        && board[pos + grid_size - 1] != 0) {
                    ++doneSquaresCount;
                    player.squareCompleted(pos + grid_size);
                }
            }
        } else {
            int col = pos % grid_size;

            if (col != 0) {
                if (board[pos - 2] != 0
                        && board[pos - grid_size - 1] != 0
                        && board[pos + grid_size - 1] != 0) {
                    ++doneSquaresCount;
                    player.squareCompleted(pos - 1);
                }
            }
            if (col != (grid_size - 1)) {
                if (board[pos + 2] != 0
                        && board[pos + grid_size + 1] != 0
                        && board[pos - grid_size + 1] != 0) {
                    ++doneSquaresCount;
                    player.squareCompleted(pos + 1);
                }
            }
        }

        return doneSquaresCount;
    }

    public boolean boardFilledUp() {
        int totalPossibleSquares = (grid_size / 2) * (grid_size / 2);
        return (totalPossibleSquares == (Points_Player1 + Points_Player2));
    }

    public synchronized boolean legalMove(int position, Player player) {
        if ((player == currentPlayer) && (board[position] == 0)) {
            board[position] = currentPlayer.playerColor;

            logger.info("Игрок " + player.playerColor + " сделал ход в позиции " + position);
            int numSquares = DoneSquares(position, player);
            if (numSquares > 0) {
                currentPlayer.thisPlayerMoved(position, true);
                if (player.playerColor == 'B') {
                    Points_Player1 += numSquares;
                } else {
                    Points_Player2 += numSquares;
                }
                logger.info("Игрок " + player.playerColor + " заработал " + numSquares + " очков.");
            } else {
                currentPlayer.thisPlayerMoved(position, false);
                currentPlayer = currentPlayer.opponent;
            }

            return true;
        }
        return false;
    }

    class Player extends Thread {
        private BufferedReader input;
        private PrintWriter output;
        private final Socket socket;
        private Player opponent;
        private final char playerColor;

        public Player(Socket socket, char pColor) {
            this.socket = socket;
            this.playerColor = pColor;
            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("W " + playerColor);
                output.println("I Ожидание подключения противника");
                logger.info("Игрок " + playerColor + " подключился.");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Не удалось подключиться к игроку", e);
            }
        }

        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        public void thisPlayerMoved(int position, boolean goAgain) {
            String again = goAgain ? "Y" : "N";
            output.println("V " + again + " " + position);
            opponent.output.println("O " + again + " " + position);
        }

        public void squareCompleted(int position) {
            String message = "S " + playerColor + " " + position;
            output.println(message);
            opponent.output.println(message);
        }

        @Override
        public void run() {
            String command;
            try {
                output.println("I Оба игрока подключились");
                if (playerColor == 'B') {
                    output.println("I Ваш ход");
                }

                while (true) {
                    command = input.readLine();
                    command = command.isEmpty() ? " " : command;
                    if (command.charAt(0) == 'M') {
                        int position = Integer.parseInt(command.substring(2));
                        if (legalMove(position, this)) {
                            if (boardFilledUp()) {
                                char winner = gameWinner();
                                output.println("E " + winner);
                                opponent.output.println("E " + winner);
                            }
                        } else {
                            output.println("I Invalid move");
                        }
                    } else if (command.charAt(0) == 'Q') {
                        if (command.charAt(2) == playerColor) {
                            logger.info("Игрок " + playerColor + " вышел из игры.");
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Игрок отключился", e);
            } finally {
                try {
                    socket.close();
                    logger.info("Соединение с игроком " + playerColor + " закрыто.");
                } catch (IOException ignored) {}
            }
        }
    }
}
