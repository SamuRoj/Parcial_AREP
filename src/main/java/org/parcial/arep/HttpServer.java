package org.parcial.arep;

import java.net.*;
import java.io.*;

public class HttpServer {

    private static boolean isRunning = true;

    private static final String NOT_FOUND = "HTTP/1.1 404 NOT FOUND\r\n"
            + "Content-Type: text/html\r\n"
            + "\r\n"
            + "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "<meta charset=\"UTF-8\">\n"
            + "<title>Error Page</title>\n"
            + "</head>\n";

    public static void main(String[] args) throws Exception {
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
            String firstLine = " ";
            boolean isFirst = true;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("HttpServer line: " + inputLine);
                if(isFirst){
                    firstLine = inputLine;
                    isFirst = false;
                }
                if (!in.ready()) {
                    break;
                }
            }

            String path = firstLine.split(" ")[1];
            URI uri = new URI(path);
            String uriPath = uri.getPath();
            String query = uri.getQuery();

            if(uriPath.startsWith("/compreflex") && query.startsWith("comando")){
                outputLine = invokeMethod(query);
            }
            else{
                outputLine = NOT_FOUND;
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String invokeMethod(String query) {

        return "";
    }
}
