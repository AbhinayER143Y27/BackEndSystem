public class Main {
    public static void main(String[] args) {
        // Create an instance of the HttpRaw class
        HttpRaw server = new HttpRaw();

        // Start the server on port 8080
        server.startServer(8080);
    }
}