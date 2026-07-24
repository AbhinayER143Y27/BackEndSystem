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
    public static void main(String[] args)
    {
        try(ServerSocket serverSocket = new ServerSocket(8080))
        {
            System.out.println("Engine Started. Listening to port 8080");
            while(true)
            {
                Socket clientSocket = serverSocket.accept();//Freeze teh execution until the browser connects
                System.out.println("Connection Established with the client...");

                //step A: To read the incomming HTTP Request so that one can know what the browser is sending to the terminal
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

                String Line;
                while((Line = reader.readLine()) != null && !Line.isEmpty())
                {
                    System.out.println(Line);
                }
                //Step B : Write the outgoing HTTP response
                OutputStream output = clientSocket.getOutputStream();
                String response;
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