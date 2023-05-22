package Praktikum1;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.*;
import java.net.Socket;



public class Jcurl2 {
    private static String protocol;
    private static String host;
    private static String pfad = "";
    private static Socket clientSocket;



    public Jcurl2(String[] args){
        // URL Syntax: http(s)://Hostname/Pfadname
        // Protokoll und Rest der URL trennen
        String[] protocolAndURL = args[0].split("://");
        protocol = protocolAndURL[0];
        // Hostname und Pfad trennen (falls vorhanden)
        String[] hostAndPath = protocolAndURL[1].split("/", 2);
        host = hostAndPath[0];
        // Es gibt einen Pfad
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
    public void start() throws IOException {
        int port;
        if (protocol.equals("https")) {
            try {
                port = 443;
                // Verbindungsaufbau
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                clientSocket = factory.createSocket(host, port);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (protocol.equals("http")){
            port = 80;
            // Verbindungsaufbau
            clientSocket = new Socket(host, port);
        }
        // Anfrage an Server schicken
        writeToServer();
        // Header und Response-Datenstrom ausgeben
        readFromServer();
        // Verbindungsabbau
        clientSocket.close();
    }



    // GET-Request mit den Informationen an Server schicken
    public static void writeToServer() throws IOException {
        PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
        String header = "Host: " + host + "\r\n";
        System.err.println("------------Request-------------");
        System.err.println(header);
        outToServer.println("GET /" + pfad + " HTTP/1.1\r\n"
                + header);


    }



    // Datenstrom aus Response lesen und ausgeben
    public static void readFromServer() throws IOException {
        String inputLine;
        // Header des Response ausgeben
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
        System.err.println("------------Response-------------");
        while (!(inputLine = inFromServer.readLine()).equals("")) {
            System.err.println(inputLine);
        }
        // Ausgabe des Response-Datenstroms
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inFromServer.read(buffer)) > 0) {
            System.out.write(buffer, 0, len);
        }
    }
}
