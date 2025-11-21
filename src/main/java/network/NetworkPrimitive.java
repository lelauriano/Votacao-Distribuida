package network;

import java.io.*;
import java.net.Socket;

/**
 * Small helper that manages object streams for socket communication.
 */
public abstract class NetworkPrimitive {
    protected Socket socket;
    protected ObjectOutputStream outputStream;
    protected ObjectInputStream inputStream;

    public void initializeStreams() throws IOException {
        // Note: When creating ObjectOutputStream, do it before the peer creates its ObjectInputStream
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void sendObject(Object obj) throws IOException {
        outputStream.writeObject(obj);
        outputStream.flush();
    }

    public Object receiveObject() throws IOException, ClassNotFoundException {
        return inputStream.readObject();
    }

    public void close() {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException ignored) {}
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException ignored) {}
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
