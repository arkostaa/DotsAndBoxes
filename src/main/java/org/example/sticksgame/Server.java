package org.example.sticksgame;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    private static final int PORT = 7777;

    public static void main(String[] args) {
        ServerSocket listener = null;      
        try {
            listener = new ServerSocket(PORT);
            System.out.println("Сервер запущен");
            while (true) {
                Game game = new Game();
                Game.Player player1 = game.new Player(listener.accept(), 'B');
                Game.Player player2 = game.new Player(listener.accept(), 'R');
                player1.setOpponent(player2);
                player2.setOpponent(player1);
                game.currentPlayer = player1;
                player1.start();
                player2.start();
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException e) {};
        } 
    }
}
