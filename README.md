# Dropbox API Connector in Java

A simple Java console application to authenticate with Dropbox using OAuth 2.0, obtain an access token, and fetch Dropbox team information via the Dropbox API.

---

## Features

- OAuth 2.0 Authorization Code flow with a local HTTP server to capture the authorization code.
- Exchanges authorization code for access token.
- Calls Dropbox `/2/team/get_info` API endpoint to retrieve team name and team ID.
- Uses Java 11+ HttpClient API for HTTP requests.

---

## Prerequisites

- Java 11 or higher installed
- A Dropbox API app with the following:
  - `CLIENT_ID` and `CLIENT_SECRET`
  - Redirect URI configured to `http://localhost:8080/callback`
  - Appropriate permissions (team info scopes)

---

## Setup

1. **Clone or download this repository**

2. **Configure your Dropbox App credentials:**

Open `DropboxAPIConnector.java` and replace the placeholders:

```java
private static final String CLIENT_ID = "vmmwztcw8sxjsrc";
private static final String CLIENT_SECRET = "a769l8zv38bjukf";
private static final String REDIRECT_URI = "http://localhost:8080/callback";

```
3. **Build and run the program**

```java
javac DropboxAPIConnector.java
java DropboxAPIConnector
```

## How it works

1. The program starts a local HTTP server on port 8080 to receive the OAuth redirect.

2. It prints the authorization URL to the console. Open it in your browser and authorize the app.

3. After authorization, Dropbox redirects to http://localhost:8080/callback?code=... where the code is captured.

4. The program exchanges the authorization code for an access token.

5. Finally, it calls Dropbox API to get team info and prints the team name and team ID.

## Notes

- The app uses a minimal regex to extract tokens from JSON responses. For production, consider using a JSON library (like Jackson or Gson).

- This example demonstrates the /2/team/get_info endpoint which requires a Dropbox Business team token.

- The local HTTP server runs only during the authorization step and stops automatically when done.

## Troubleshooting

- Make sure your Dropbox API app is configured correctly with the redirect URI.

- Verify that your app has the required permissions/scopes for team information.

- Ensure no other application is using port 8080 on your machine.

- If the access token retrieval fails, check your Client ID, Client Secret, and authorization code.

- If fetching team info returns errors, ensure the token has appropriate permissions.

## License

This project is provided as-is under the MIT License.

## Author

Hariharan Ganesan
ghariharan167@gmail.com

