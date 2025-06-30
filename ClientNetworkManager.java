import java.io.*;
import java.net.*;

public class ClientNetworkManager {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientNetworkManager(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void sendRequest(Object request) throws IOException {
        out.writeObject(request);
        out.flush();
    }

    public Object receiveResponse() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
