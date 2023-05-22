//package Praktikum4;
//import java.io.IOException;
//import java.net.*;
//import java.util.Enumeration;
//import java.util.Iterator;
//import java.util.List;
//
//public class DHCPV2 {
//
//    private DatagramSocket clientSocket;
//    private Inet6Address inet6Address;
//
//
//    String multiCastAdress;
//    public void startJob(){
//
//    }
//
//    public void sendRequest() throws IOException {
//
//
//        /* String in Byte-Array umwandeln */
//        byte[] sendData = new byte[11];
//
//        /* Paket erzeugen mit Server-IP und Server-Zielport */
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
//                SERVER_IP_ADDRESS, SERVER_PORT);
//        /* Senden des Pakets */
//        clientSocket.send(sendPacket);
//
//        System.out.println("UDP Client has sent the message: " + sendString);
//    }
//
//    static byte[] hexStringtoByteArray(String hex) {
//        /* Konvertiere den String mit Hex-Ziffern in ein Byte-Array */
//        byte[] val = new byte[hex.length() / 2];
//        for (int i = 0; i < val.length; i++) {
//            int index = i * 2;
//            int num = Integer.parseInt(hex.substring(index, index + 2), 16);
//            val[i] = (byte) num;
//        }
//        return val;
//    }
//
//    static String byteArraytoHexString(byte[] byteArray) {
//        /* Konvertiere das Byte-Array in einen String mit Hex-Ziffern */
//        String hex = "";
//        if (byteArray != null) {
//            for (int i = 0; i < byteArray.length; ++i) {
//                hex = hex + String.format("%02X", byteArray[i]);
//            }
//        }
//        return hex;
//    }
//
//    static void showNetwork() throws SocketException {
//        /* Netzwerk-Infos fuer alle Interfaces ausgeben */
//        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
//        while (en.hasMoreElements()) {
//            NetworkInterface ni = en.nextElement();
//            System.out.println("\nDisplay Name = " + ni.getDisplayName());
//            System.out.println(" Name = " + ni.getName());
//            System.out.println(" Scope ID (Interface ID) = " + ni.getIndex());
//            System.out.println(" Hardware (LAN) Address = " + byteArraytoHexString(ni.getHardwareAddress()));
//
//            List<InterfaceAddress> list = ni.getInterfaceAddresses();
//            Iterator<InterfaceAddress> it = list.iterator();
//
//            while (it.hasNext()) {
//                InterfaceAddress ia = it.next();
//                System.out
//                        .println(" Adress = " + ia.getAddress() + " with Prefix-Length " + ia.getNetworkPrefixLength());
//            }
//        }
//    }
//
//}
