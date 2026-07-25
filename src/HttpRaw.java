import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.util.Base64;

public class HttpRaw {

    // Internal data structure for server status
    static class ServerStatus {
        String status = "Engine Operational";
        String version = "1.0";
        long uptimeMillis = System.currentTimeMillis();
    }

    // Converts Java Object to JSON format
    private static String toJson(ServerStatus obj) {
        return "{\n" +
                " \"status\": \"" + obj.status + "\",\n" +
                " \"version\": \"" + obj.version + "\",\n" +
                " \"uptime\": " + obj.uptimeMillis + "\n" +
                "}";
    }

    // JWT Engine for authentication
    static class JwtEngine {
        private static final String SECRET_KEY = "AbhinaysSuperKeyForRawEngine";

        public static String generateToken(String username) throws Exception {
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payload = "{\"sub\":\"" + username + "\",\"role\":\"admin\"}";

            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

            String unsignedToken = encodedHeader + "." + encodedPayload;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] signatureBytes = mac.doFinal(unsignedToken.getBytes());
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);

            return unsignedToken + "." + encodedSignature;
        }
    }

    // The main server logic
    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Engine Started. Listening to port " + port);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Connection Established with the client...");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String requestLine = reader.readLine();

                    if (requestLine == null || requestLine.isEmpty()) {
                        continue;
                    }

                    System.out.println("Request Line: " + requestLine);
                    String[] requestParts = requestLine.split(" ");
                    String path = requestParts.length > 1 ? requestParts[1] : "/";

                    String authorizationHeader = null;
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        if (line.startsWith("Authorization: Bearer ")) {
                            authorizationHeader = line.substring(22);
                        }
                    }

                    if (authorizationHeader != null) {
                        System.out.println(">>> TOKEN INTERCEPTED: " + authorizationHeader);
                    }

                    OutputStream output = clientSocket.getOutputStream();
                    String response;

                    if (path.equals("/")) {
                        response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "\r\n" +
                                "Welcome to the Root Engine.";
                    } else if (path.equals("/api/status")) {
                        ServerStatus currentStatus = new ServerStatus();
                        String jsonBody = toJson(currentStatus);
                        response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "\r\n" +
                                jsonBody;
                    } else if (path.equals("/api/login")) {
                        try {
                            String rawToken = JwtEngine.generateToken("abhinay_rana");
                            response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/plain\r\n" +
                                    "\r\n" +
                                    rawToken;
                        } catch (Exception e) {
                            response = "HTTP/1.1 500 Internal Server Error\r\n\r\nCrypto Engine Failed.";
                        }
                    } else {
                        response = "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "\r\n" +
                                "404 Error: The requested path does not exist.";
                    }

                    output.write(response.getBytes());
                    output.flush();
                } catch (IOException e) {
                    System.out.println("Connection Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server Crashed: " + e.getMessage());
        }
    }
}