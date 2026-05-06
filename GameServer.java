import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

    // Armazena o último estado de cada jogador
    static Map<String, String> estados = new ConcurrentHashMap<>();
    static Map<String, Long> ultimaVez = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Jogador envia sua posição
        server.createContext("/update", exchange -> {
            if (!exchange.getRequestMethod().equals("POST")) { exchange.sendResponseHeaders(405, -1); return; }

            String body = new String(exchange.getRequestBody().readAllBytes());
            // Formato: "id|x|y|z|rotX|rotY|anim"
            String[] partes = body.split("\\|");
            if (partes.length >= 7) {
                String id = partes[0];
                estados.put(id, body);
                ultimaVez.put(id, System.currentTimeMillis());
            }

            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        });

        // Jogador busca estados dos outros
        server.createContext("/players", exchange -> {
            if (!exchange.getRequestMethod().equals("GET")) { exchange.sendResponseHeaders(405, -1); return; }

            String meuId = exchange.getRequestURI().getQuery(); // ?meuId
            long agora = System.currentTimeMillis();

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : estados.entrySet()) {
                // Remove jogadores inativos há mais de 5 segundos
                if (agora - ultimaVez.getOrDefault(entry.getKey(), 0L) > 5000) {
                    estados.remove(entry.getKey());
                    continue;
                }
                if (!entry.getKey().equals(meuId)) {
                    sb.append(entry.getValue()).append("\n");
                }
            }

            byte[] resp = sb.toString().getBytes();
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        });

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Servidor rodando na porta " + PORT);
    }
    }
