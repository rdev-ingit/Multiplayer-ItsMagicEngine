import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {

    static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    static Map<String, String> estados = new ConcurrentHashMap<>();
    static Map<String, Long> ultimaVez = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", exchange -> {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            // CORS sempre primeiro
            Headers h = exchange.getResponseHeaders();
            h.add("Access-Control-Allow-Origin", "*");
            h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            h.add("Access-Control-Allow-Headers", "Content-Type, Accept");

            // Preflight
            if (method.equals("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (path.equals("/update") && method.equals("POST")) {
                String body = new String(exchange.getRequestBody().readAllBytes());
                String[] partes = body.split("\\|");
                if (partes.length >= 7) {
                    String id = partes[0].trim();
                    if (!id.isEmpty()) {
                        estados.put(id, body);
                        ultimaVez.put(id, System.currentTimeMillis());
                    }
                }
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();

            } else if (path.equals("/players") && method.equals("GET")) {
                String meuId = exchange.getRequestURI().getQuery();
                long agora = System.currentTimeMillis();

                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : new HashMap<>(estados).entrySet()) {
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

            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        });

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Servidor rodando na porta " + PORT);
    }
}
