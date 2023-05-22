package Praktikum1;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.*;
import java.net.Socket;

    public class Jcurl {
        private static String protocol;
        private static String host;
        private static String pfad = "";
        private static Socket clientSocket;
        private static DataOutputStream outToServer;


        public Jcurl(String[] args){
            String[] protocolAndURL = args[0].split("://");
            protocol = protocolAndURL[0];
            String[] hostAndPath = protocolAndURL[1].split("/", 2);
            host = hostAndPath[0];
            if (hostAndPath.length == 2){
                pfad = hostAndPath[1];
            }
        }
        public static void main(String[] args) throws IOException {
            Jcurl client = new Jcurl(args);
            client.startClient();
        }

        /**
         * GET-REQUEST starten
         */
        public void startClient() throws IOException {
            int port;
            if (protocol.equals("https")) {
                try {
                    port = 443;
                    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    clientSocket = factory.createSocket(host, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (protocol.equals("http")){
                port = 80;
                clientSocket = new Socket(host, port);
            }
             outToServer = new DataOutputStream(clientSocket.getOutputStream());
            writeToServer();
            readFromServer();
            clientSocket.close();
        }



        public static void writeToServer() throws IOException {
            String requestString;

            requestString = "GET /" +pfad + " HTTP/1.0";
            String header = "Host: " + host;
           writeLineToServer(requestString);
           writeLineToServer(header);
           writeLineToServer("");

        }

        private static void writeLineToServer(String line) throws IOException {
            /* Sende eine Zeile (mit CRLF) zum Server */
            outToServer.write((line + "\r\n").getBytes());
            System.err.println(line);
        }


        public static void readFromServer() throws IOException {
            String inputLine;
            DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

            System.err.println("------------Response-------------");
            while (!(inputLine = inFromServer.readLine()).equals("")) {
                System.err.println(inputLine);
            }
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inFromServer.read(buffer)) > 0) {
                System.out.write(buffer, 0, len);
            }
        }
    }

