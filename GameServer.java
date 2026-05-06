import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    static final int PORT = 9999;
    static Map<String, PrintWriter> clientes = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Servidor rodando na porta " + PORT);
        ServerSocket server = new ServerSocket(PORT);

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> handleCliente(socket)).start();
        }
    }

    static void handleCliente(Socket socket) {
        String id = null;
        try {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Primeira mensagem = ID do jogador
            id = in.readLine();
            clientes.put(id, out);
            System.out.println("Jogador conectado: " + id);

            String linha;
            while ((linha = in.readLine()) != null) {
                // Formato recebido: "ID|x|y|z|rotX|rotY|anim"
                final String msg = id + "|" + linha;

                // Envia para todos os outros clientes
                for (Map.Entry<String, PrintWriter> entry : clientes.entrySet()) {
                    if (!entry.getKey().equals(id)) {
                        entry.getValue().println(msg);
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Jogador desconectado: " + id);
        } finally {
            if (id != null) clientes.remove(id);
        }
    }
}
