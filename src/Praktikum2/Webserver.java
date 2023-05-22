package Praktikum2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


public class Webserver {
    /* TCP-Server, der Verbindungsanfragen entgegennimmt */

    /* Semaphore begrenzt die Anzahl parallel laufender Worker-Threads  */
    public Semaphore workerThreadsSem;

    /* Portnummer */
    public final int serverPort;

    /* Anzeige, ob der Server-Dienst weiterhin benoetigt wird */
    public boolean serviceRequested = true;

    /* Konstruktor mit Parametern: Server-Port, Maximale Anzahl paralleler Worker-Threads*/
    public Webserver(int serverPort, int maxThreads) {
        this.serverPort = serverPort;
        this.workerThreadsSem = new Semaphore(maxThreads);
    }

    public void startServer() {
        ServerSocket welcomeSocket; // TCP-Server-Socketklasse
        Socket connectionSocket; // TCP-Standard-Socketklasse

        int nextThreadNumber = 0;

        try {
            /* Server-Socket erzeugen */
            System.out.println("Creating new TCP Server Socket Port " + serverPort);
            welcomeSocket = new ServerSocket(serverPort);

            while (serviceRequested) {
                workerThreadsSem.acquire();  // Blockieren, wenn max. Anzahl Worker-Threads erreicht

                System.out.println("TCP Server is waiting for connection - listening TCP port " + serverPort);
                /*
                 * Blockiert auf Verbindungsanfrage warten --> nach Verbindungsaufbau
                 * Standard-Socket erzeugen und an connectionSocket zuweisen
                 */
                connectionSocket = welcomeSocket.accept();

                /* Neuen Arbeits-Thread erzeugen und die Nummer, den Socket sowie das Serverobjekt uebergeben */
                (new TCPWorkerThread(nextThreadNumber++, connectionSocket, this)).start();
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public static void main(String[] args) {
        /* Erzeuge Server und starte ihn */
        Webserver myServer = new Webserver(8080, 2);
        myServer.startServer();
    }
}

// ----------------------------------------------------------------------------
// User-Agent: Firefox/5.0 (<system-information>) <platform> (<platform-details>) <extensions>
class TCPWorkerThread extends Thread {
    /*
     * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
     * erhaelt
     */
    private int name;
    private Socket socket;
    private Webserver server;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String encodedCredentials;
    private String userCredentials;
    private String getPath = "";
    private String getRequest = "";
    private String protocol = "HTTP/1.0";
    private boolean browser = false;
    private boolean isDate = false;
    private boolean isTime = false;
    private DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    File file;

    Pattern getPattern = Pattern.compile("GET (/([^/,()!]*/)?[^/,()!]*?) (.+)");
    Pattern userCredentialsPattern = Pattern.compile("Authorization: Basic (.*)");
    Pattern userAgentPattern = Pattern.compile(".* Mozilla.* Firefox.*");
    Pattern datePattern = Pattern.compile("GET /date .*");
    Pattern timePattern = Pattern.compile("GET /time .*");

    Matcher getMatcher;
    Matcher userCredentialsMatcher;
    Matcher userAgentMatcher;
    Matcher dateMatcher;
    Matcher timeMatcher;


    public TCPWorkerThread(int num, Socket sock, Webserver server) {
        /* Konstruktor */
        this.name = num;
        this.socket = sock;
        this.server = server;
    }

    public void run() {
        System.out.println("TCP Worker Thread " + name + " is running!");

        try {
            /* Socket-Basisstreams durch spezielle Streams filtern */
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());

            System.out.println("Request: ");
            String stringHeader = " ";
            while (stringHeader.length() != 0) {
                stringHeader = readFromClient();

                dateMatcher = datePattern.matcher(stringHeader);
                if (dateMatcher.find())
                    isDate = true;

                timeMatcher = timePattern.matcher(stringHeader);
                if (timeMatcher.find())
                    isTime = true;

                getMatcher = getPattern.matcher(stringHeader);
                if (getMatcher.find()) {
                    getRequest = getMatcher.group(0);
                    getPath = getMatcher.group(1);
                }

                userCredentialsMatcher = userCredentialsPattern.matcher(stringHeader);
                if (userCredentialsMatcher.find()) {
                    encodedCredentials = userCredentialsMatcher.group(1);
                    userCredentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8.toString());

                }

                userAgentMatcher = userAgentPattern.matcher(stringHeader);
                if (userAgentMatcher.find())
                    browser = true;
            }

            file = new File("C:/Users/onur_/IdeaProjects/RechnernetzePraktikum/src/Praktikum2/Testweb/" + getPath);

            System.out.println("Response:");

            if (isDate) {
                writeToClient(protocol + " 200 OK");
                writeToClient("");
                writeToClient(date.format(now));
            } else if (isTime) {
                writeToClient(protocol + " 200 OK");
                writeToClient("");
                writeToClient(time.format(now));
            } else if (getRequest.length() == 0) {
                writeToClient(protocol + " 400 Bad Request");
                writeToClient("");
            } else if (!browser) {
                writeToClient(protocol + " 406 Not Acceptable");
                writeToClient("");
            } else if (!checkUserAccess()) {
                writeToClient(protocol + " 401 Unauthorized\r\nWWW-Authenticate: Basic");
                writeToClient("");
            } else if (!new File("C:/Users/onur_/IdeaProjects/RechnernetzePraktikum/src/Praktikum2/Testweb/"+ getPath).exists()) {
                writeToClient(protocol + " 404 Not Found");
                writeToClient("");
            } else {

                String fileExtension = file.getName().split("\\.")[1];
                writeToClient(protocol + " 200 OK");
                writeToClient("Content-Type: " + MimeTypes.getMimeType(fileExtension));
                writeToClient("Content-Length: " + file.length());
                writeToClient("");
                readFileData(file);
                outToClient.flush();
            }
            /* Socket-Streams schliessen --> Verbindungsabbau */
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection aborted by client!");
        } finally {
            System.out.println("TCP Worker Thread " + name + " stopped!");
            /* Platz fuer neuen Thread freigeben */
            server.workerThreadsSem.release();
        }
    }

    private boolean checkUserAccess() throws IOException {
        boolean access = false;
        if (new File("C:/Users/onur_/IdeaProjects/RechnernetzePraktikum/src/Praktikum2/Testweb/").exists()) {
            Scanner sc = new Scanner(new File("C:/Users/onur_/IdeaProjects/RechnernetzePraktikum/src/Praktikum2/Testweb/.htuser"));


            while (sc.hasNext()) {
                String line = sc.nextLine();
                if (line.equals(userCredentials)) {
                    access = true;
                }
            }
        }
        return access;
    }


    //Datei wird dem Client uebergeben
    private void readFileData(File file) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[1024];

        try {
            fileIn = new FileInputStream(file);
        int len;
            while ((len = fileIn.read(fileData)) > 0) {
                outToClient.write(fileData,0,len);
            }
        } finally {
            if (fileIn != null)
                fileIn.close();
        }
    }

    private String readFromClient() throws IOException {
        /* Lies die naechste Anfrage-Zeile (request) vom Client */
        String request = inFromClient.readLine();
        System.out.println(request);
        return request;
    }


    private void writeToClient(String line) throws IOException {
        /* Sende die Antwortzeile (mit CRLF) zum Client */
        outToClient.write((line + "\r\n").getBytes());
        System.out.println(line);
    }
}

