import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer extends WebSocketServer {

    private static ConcurrentHashMap<WebSocket, String> players = new ConcurrentHashMap<>();

    public GameServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Novo jogador conectado");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        players.remove(conn);
        System.out.println("Jogador desconectado");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

        players.put(conn, message);

        // retransmite para TODOS menos quem enviou
        for (WebSocket client : players.keySet()) {
            if (client != conn && client.isOpen()) {
                client.send(message);
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Servidor iniciado");
    }

    public static void main(String[] args) {

        int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080")
        );

        GameServer server = new GameServer(port);

        server.start();

        System.out.println("Rodando porta " + port);
    }
}
