package org.parcial.arep;

import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.Arrays;

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
            path = path.replace("\"","");
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

    private static String invokeMethod(String query) throws Exception{
        String header = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: application/json\r\n"
                + "\r\n"; 
        String ans;
        System.out.println("Query: " + query);

        String command = query.split("=")[1];
        if(command.startsWith("Class")){
            String[] params = command.split("\\(")[1].split("\\)")[0].split(",");
            String className = params[0];
            Class c = Class.forName(className);
            ans = "{ \"" +
                    "Fields\" : \"" + Arrays.toString(c.getDeclaredFields())
                    + "\" , \"" +
                    "Methods\" : \"" + Arrays.toString(c.getDeclaredMethods())
                    + "\" }";
        }
        else if(command.startsWith("invoke")){
            String[] params = command.split("\\(")[1].split("\\)")[0].split(",");
            String className = params[0];
            String methodName = params[1];
            Class<?> c = Class.forName(className);
            Method m = c.getMethod(methodName);
            ans = "{ \"" +
                    "response\" : \"" + m.invoke(null).toString()
                    + "\" }";
        }
        else if(command.startsWith("unaryInvoke")){
            String[] params = command.split("\\(")[1].split("\\)")[0].split(",");
            String className = params[0];
            String methodName = params[1];
            String paramType = params[2];
            String paramValue = params[3];

            // Turning the strings into their proper classes
            Class<?> c = Class.forName(className);
            Class<?> paramTypeClass = obtainClassByType(paramType);
            Object param = castParam(paramType, paramValue);
            Method m = c.getMethod(methodName, paramTypeClass);
            ans = "{ \"" +
                    "response\" : \"" + m.invoke(null, param).toString()
                    + "\" }";
        }
        else if(command.startsWith("binaryInvoke")){
            String[] params = command.split("\\(")[1].split("\\)")[0].split(",");
            String className = params[0];
            String methodName = params[1];
            String paramType = params[2];
            String paramValue = params[3];
            String paramType2 = params[4];
            String paramValue2 = params[5];

            // Turning the strings into their proper classes
            Class<?> c = Class.forName(className);

            // Param Classes
            Class<?> paramTypeClass1 = obtainClassByType(paramType);
            Class<?> paramTypeClass2 = obtainClassByType(paramType2);

            // Casting params
            Object param1 = castParam(paramType, paramValue);
            Object param2 = castParam(paramType2, paramValue2);

            Method m = c.getMethod(methodName, paramTypeClass1, paramTypeClass2);
            ans = "{ \"" +
                    "response\" : \"" + m.invoke(null, param1, param2).toString()
                    + "\" }";
        }
        else{
            return NOT_FOUND; 
        }
        return header + ans;
    }

    public static Class<?> obtainClassByType(String paramType){
        if(paramType.equals("int")) return int.class;
        if(paramType.equals("double")) return double.class;
        return String.class;
    }

    public static Object castParam(String paramType, String paramValue){
        if(paramType.equals("int")) return Integer.valueOf(paramValue);
        if(paramType.equals("double")) return Double.valueOf(paramValue);
        return paramValue;
    }
}
