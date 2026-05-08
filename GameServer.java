import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    static List<ClientHandler> clients =
    new CopyOnWriteArrayList<>();

    public static void main(String[] args)
    throws Exception {

        int port =
        Integer.parseInt(
            System.getenv()
            .getOrDefault("PORT", "5050")
        );

        ServerSocket server =
        new ServerSocket(port);

        System.out.println(
            "Servidor TCP rodando na porta "
            + port
        );

        while (true) {

            Socket socket =
            server.accept();

            System.out.println(
                "Novo jogador conectado"
            );

            ClientHandler client =
            new ClientHandler(socket);

            clients.add(client);

            client.start();
        }
    }

    static class ClientHandler
    extends Thread {

        Socket socket;

        BufferedReader in;

        PrintWriter out;

        public ClientHandler(Socket socket)
        throws Exception {

            this.socket = socket;

            in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()
                )
            );

            out = new PrintWriter(
                socket.getOutputStream(),
                true
            );
        }

        public void run() {

            try {

                String msg;

                while (
                    (msg = in.readLine())
                    != null
                ) {

                    // retransmite
                    broadcast(msg, this);
                }

            } catch (Exception e) {

                System.out.println(
                    "Jogador desconectado"
                );

            } finally {

                clients.remove(this);

                try {
                    socket.close();
                } catch (Exception e) {}
            }
        }

        public void send(String msg) {

            out.println(msg);
        }
    }

    static void broadcast(
        String msg,
        ClientHandler sender
    ) {

        for (ClientHandler c : clients) {

            if (c != sender) {

                c.send(msg);
            }
        }
    }
}
