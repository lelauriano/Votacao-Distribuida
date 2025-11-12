public abstract class NetworkPrimitive {
    protected Socket socket;
    protected ObjectOutputStream outputStream;
    protected ObjectInputStream inputStream;
    
    public void initializeStreams() throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
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
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }
}