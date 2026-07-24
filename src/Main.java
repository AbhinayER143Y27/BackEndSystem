import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.util.Base64;

public class Main
{
    //Now the serialization will beggin in here
    static class ServerStatus
    {
        String status = "Engine Operational";
        String version = "1.0";
        long uptimeMillis = System.currentTimeMillis();
    }
    // This is what the serialization looks like from the perspective of the Jackson or the Gson libraries(Converts the Java Object(POJO) into Json format)
    static String toJson(ServerStatus obj)
    {
        return"{\n"+
                " \"status\": \"" + obj.status + "\",\n"+
                " \"version\": \"" + obj.version + "\",\n"+
                " \"uptime\": "+ obj.uptimeMillis + "\n"+
                "}";
    }
    // This is the JWT engine so that the token authentication could take place and a random person on the search bar cannot access the admin panel just by entering '/'.
    static class JwtEngine
    {
        // This is not a production so we are using a hardcoded secret Key for the physics.
        private static final String SECRET_KEY = "AbhinaysSuperKeyForRawEngine";

        public static String generateToken(String username) throws Exception{
            //step A - The header algo
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";// hs256 is the algo being used
            //step B - Payload which contains teh users data
            String payload = "{\"sub\":\"" + username + "\",\"role\":\"admin\"}"; // this is the actual users data
            //Step c - Base64URl encode both strings, this is not a encoding procedure just a network transport safety anyone can decode this.
            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

            String unsignedToken = encodedHeader + "." + encodedPayload;
            Mac mac = Mac.getInstance("HmacSHA256"); // This is the hashing engine
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(),"HmacSHA256");// glued string and secret key to this and an irreversible signature will be given to us.
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(unsignedToken.getBytes());
            String encodedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
            return unsignedToken + "." + encodedSignature;
        }
    }
    public static void main(String[] args)
    {
        try(ServerSocket serverSocket = new ServerSocket(8080))
        {
            System.out.println("Engine Started. Listening to port 8080");
            while(true)
            {
                Socket clientSocket = serverSocket.accept();//Freeze teh execution until the browser connects
                System.out.println("Connection Established with the client...");

                //step A: To read the incoming HTTP Request so that one can know what the browser is sending to the terminal
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                //This is for the Dynamic Dispatcher way
                String requestLine = reader.readLine();         //So what is being checked is the methods whether they exists or not if yes then
                if(requestLine == null || requestLine.isEmpty()) // split the string and store or else continue with the work.
                {
                    clientSocket.close();
                    continue;
                }
                System.out.println("Request Line:........... " + requestLine);
                String[] requestParts = requestLine.split(" ");
                String method = requestParts[0];
                String path = requestParts[1];

                String authorizationHeader = null;
                String line;
                while((line = reader.readLine()) != null && !line.isEmpty())
                {
                    System.out.println(line);
                    if(line.startsWith("Authorization: Bearer "))
                    {
                        authorizationHeader = line.substring(22);
                    }
                }
                if(authorizationHeader != null)
                {
                    System.out.println(">>> TOKEN INTERCEPTED: " + authorizationHeader);
                }
                //Step B : Write the outgoing HTTP response
                OutputStream output = clientSocket.getOutputStream();
                String response = "";
                if(path.equals("/")){
                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n"+
                            "\r\n"+
                            "Welcome to the Root Engine.";
                }
//                else if(path.equals("/api/status"))
//                {
//                    response = "HTTP/1.1 200 OK\r\n" +
//                            "Content-Type: application/json \r\n"+
//                            "\r\n" +
//                            "{\"staus\": \"Engine Operational\", \"verison\": \"1.0\"}";
//                }      This else if is a static response where as the next one which is supposed to be in this place is a dynamic one.

                else if(path.equals("/api/status"))
                {
                    ServerStatus currentStatus = new ServerStatus();
                    // we will now serialize this into a JSON string dynamically
                    String jsonBody = toJson(currentStatus);

                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "\r\n" +
                            jsonBody;
                }
                else if(path.equals("/api/login"))  //This is the keymaker.
                {
                    try{
                        String rawToken = JwtEngine.generateToken("abhinay_rana");
                        response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "\r\n" +
                                rawToken;
                    }
                    catch(Exception e)
                    {
                        response = "HTTP/1.1 500 Internal Server Error\r\n\r\nCrypto Engine Failed.";
                    }
                }
                else {
                    response = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/plain\r\n"+
                            "\r\n" +
                            "404 Error: The requested path does not exist.";
                }

                output.write(response.getBytes());
                output.flush();
                clientSocket.close();
            }
        }
        catch (IOException e)
        {
            System.out.println("Server Crashed: " + e.getMessage());
        }
    }
}