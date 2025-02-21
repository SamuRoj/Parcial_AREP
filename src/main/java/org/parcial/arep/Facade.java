package org.parcial.arep;

import java.net.*;
import java.io.*;

public class Facade {

    private static boolean isRunning = true;
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "http://localhost:45000";

    private static final String NOT_FOUND = "HTTP/1.1 404 NOT FOUND\r\n"
            + "Content-Type: text/html\r\n"
            + "\r\n"
            + "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "<meta charset=\"UTF-8\">\n"
            + "<title>Error Page</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "<h1>El recurso solicitado no fue encontrado</h1>\n"
            + "</body>\n"
            + "</html>\n";


    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        while (isRunning) {

            // Starting up the facade server
            Socket clientSocket = null;
            try {
                System.out.println("Facade listo para recibir...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            // Reading the request received
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            String firstLine = "";
            boolean isFirst = true;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Facade line: " + inputLine);
                if(isFirst){
                    firstLine = inputLine;
                    isFirst = false;
                }
                if (!in.ready()) {
                    break;
                }
            }

            // Answering the request
            String method = firstLine.split(" ")[0];
            String path = firstLine.split(" ")[1];
            URI uri = new URI(path);

            if(path.startsWith("/cliente")){
                outputLine = readIndex();
            }
            else if(path.startsWith("/consulta")){
                System.out.println("In consulta");
                outputLine = httpServerRequest(method, uri.getQuery());
            }
            else{
                outputLine = NOT_FOUND;
            }

            // Sending the answer of the request
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String readIndex() throws IOException {
        String header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n";
        BufferedReader in = new BufferedReader(new FileReader("src/main/resources/index.html"));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine).append("\n");
        }
        in.close();
        return header + response;
    }

    public static String httpServerRequest(String method, String query) throws IOException {
        // Connection to HttpServer class
        URL obj = new URL(GET_URL + "/compreflex" + "?" + query);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("User-Agent", USER_AGENT);

        //The following invocation perform the connection implicitly before getting the code
        int responseCode = con.getResponseCode();
        System.out.println(method + " Response Code :: " + responseCode);

        String header;
        String ans;

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            header = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n";
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append("\n");
            }
            ans = response.toString();
            System.out.println(ans);
            in.close();
        } else {
            System.out.println(method + " request not worked");
            return NOT_FOUND;
        }
        System.out.println(method + " DONE");
        return header + ans;
    }
}

