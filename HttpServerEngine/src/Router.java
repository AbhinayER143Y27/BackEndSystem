public class Router {
    static class ServerStatus{
        String status = "Enigne Operationl";
        String version = "1.0";
        long uptimeMillis = System.currentTimeMillis();
    }

    private static String toJson(ServerStatus obj)
    {
        return "{\n" +
                " \"status\": \"" + obj.status + "\",\n" +
                " \"version\": \"" + obj.version + "\",\n" +
                " \"uptime\": " + obj.uptimeMillis + "\n" +
                "}";
    }

    public static String handleRoutes(String method, String path)
    {
        //These are the static routes.
        if(method.equals("GET") && path.equals("/"))
        {
            return "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nWelcome to the Root Engine.";
        }
        else if(method.equals("GET") && path.equals("/api/status"))
        {
            return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + toJson(new ServerStatus());
        }
        else if (method.equals("GET") && path.equals("/api/login")) {
            try {
                // Calling the JwtEngine from your HttpRaw class
                String rawToken = HttpRaw.JwtEngine.generateToken("abhinay_rana");
                return "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n" + rawToken;
            } catch (Exception e) {
                return "HTTP/1.1 500 Internal Server Error\r\n\r\nCrypto Engine Failed.";
            }
        }
        //This is the Dynamic Route Example(Extract teh data from the URL) for the user id - 123, 999.
        else if(method.equals("GET") && path.startsWith("/api/users/"))
        {
            String userId  = path.substring(11);
            return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"user_id\": \"" + userId + "\", \"action\": \"Fetched\"}";
        }
        //This is to catch all the routes(404)
        else {
            return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\n404 Error: The request path does not exist.";
        }
    }
}
