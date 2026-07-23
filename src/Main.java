import java.io.*;
import java.net.*;

public class Main
{
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

                String Line;
                while((Line = reader.readLine()) != null && !Line.isEmpty())
                {
                    System.out.println(Line);
                }
                //Step B : Write the outgoing HTTP response
                OutputStream output = clientSocket.getOutputStream();
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "\r\n" +
                        "Hello Everyone this is Abhinay's Raw HTTP engine! The sockets works";

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