import java.io.*;
import java.net.*;

public class ListenerThread extends Thread {
    private Socket socket;
    private ObjectInputStream in;

    public ListenerThread(Socket socket) {
        this.socket = socket;
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object response = in.readObject();
                // Handle the response (e.g., update UI, notify user)
                System.out.println("Received: " + response);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
