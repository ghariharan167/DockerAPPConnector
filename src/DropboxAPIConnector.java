import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class DropboxAPIConnector {

    private static final String CLIENT_ID = "vmmwztcw8sxjsrc";  // Replace with your Client ID
    private static final String CLIENT_SECRET = "a769l8zv38bjukf";  // Replace with your Client Secret
    private static final String REDIRECT_URI = "http://localhost:8080/callback";  // Your Redirect URI
    private static String authorizationCode = null;
    private static final String authUrl =  "https://www.dropbox.com/oauth2/authorize?client_id=" + CLIENT_ID +
            "&response_type=code&redirect_uri=" + REDIRECT_URI;
    private static final String tokenUrl = "https://api.dropboxapi.com/oauth2/token";
    private static final String apiUrl = "https://api.dropboxapi.com/2/team/get_info";
    private static final String ENCODING_TYPE  = "UTF-8";
    private static HttpServer server;

    public static void main(String[] args) throws Exception {
        // Step 1: Get the authorization code
        String authCode = getAuthorizationCode();

        // Step 2: Exchange the authorization code for an access token
        String accessToken = getAccessToken(authCode);

        if (accessToken != null) {
            // Step 3: Use the access token to fetch the team info
            fetchTeamInfo(accessToken);
        } else {
            System.out.println("Failed to get access token.");
        }
    }

    // Step 1: Get the authorization code from the user
    private static String getAuthorizationCode() throws IOException {

        // Start a local HTTP server to capture the authorization code
        startLocalServer();

        // Direct the user to authorize the app
        System.out.println("Go to the following URL to authorize the app:");
        System.out.println(authUrl);
        System.out.println("After authorizing, you'll be redirected. Press Enter once you're done.");
        new Scanner(System.in).nextLine();

        return authorizationCode;
    }

    // Start a simple HTTP server to capture the authorization code from the redirect
    private static void startLocalServer() throws IOException {
        System.out.println("Starting local server on port 8080...");
        System.out.println("Server available at: " + server);
        if (server != null && server.getAddress() != null) {
            server.stop(0);
        }

        server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/callback", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("code=")) {
                    // Extract the authorization code
                    authorizationCode = query.split("code=")[1];
                    System.out.println("Authorization code: " + authorizationCode);
                    String response = "Authorization code received. You can close this window.";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });
        server.start();
    }

    // Step 2: Exchange the authorization code for an access token
    private static String getAccessToken(String authCode) throws IOException, InterruptedException {
        System.out.println("Getting access token...");

        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("code=" + URLEncoder.encode(authCode, ENCODING_TYPE) +
                        "&grant_type=authorization_code" +
                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, ENCODING_TYPE)))
                .build();


        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            String accessToken = extractKey(responseBody, "access_token");
            System.out.println("Access Token: " + accessToken);
            return accessToken;
        } else {
            System.out.println("Failed to get access token. HTTP Code: " + response.statusCode());
            System.out.println("Error Body: " + response.body());
            return null;
        }
    }

    private static String extractKey(String jsonResponse, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            System.out.println("Key not found.");
            return null;
        }
    }



    // Step 3: Fetch team name and team ID using the access token
    private static void fetchTeamInfo(String accessToken) throws IOException, InterruptedException {

        // Prepare the request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + accessToken)
                .POST(BodyPublishers.noBody())
                .build();

        // Send the request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();
            System.out.println("Get Team Info Response Body: " + responseBody);
            String teamName = extractKey(responseBody, "name");
            String teamId = extractKey(responseBody, "team_id");

            System.out.println("Team Name: " + teamName);
            System.out.println("Team ID: " + teamId);
        } else {
            System.out.println("Error response from Dropbox API: " + response.body());
            System.out.println("Failed to fetch team info. HTTP Code: " + response.statusCode());
        }
    }
}
