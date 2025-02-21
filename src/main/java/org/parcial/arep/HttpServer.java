package org.parcial.arep;

import java.net.*;
import java.io.*;

public class HttpServer {

    private static boolean isRunning = true;


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(45000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 45000.");
            System.exit(1);
        }

        while (isRunning) {
            Socket clientSocket = null;
            try {
                System.out.println("HttpServer listo para recibir...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            // Reading the request received
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("HttpServer line: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            // Output of the request
            outputLine = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "<meta charset=\"UTF-8\">" +
                            "<title>Title of the document</title>\n" +
                            "</head>" +
                            "<body>" +
                            "<h1>Mi propio mensaje</h1>" +
                            "</body>" +
                            "</html>";
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
}
