import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.util.Base64;

public class HttpRaw {

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

                    String method = requestParts[0];
                    String response = Router.handleRoutes(method, path);
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