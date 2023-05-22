package Praktikum4;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.*;


public class DHCPv6Explorer {
    private static DatagramSocket clientSocket;

    public static void main(String[] args) throws IOException {
        clientSocket = new DatagramSocket();

        // Get the local DHCP server's IP address
        ServiceCode serviceCode1 = new ServiceCode();
        // serviceCode1.showNetwork();
        byte[] byteIPv6Address = serviceCode1.hexStringtoByteArray("ff020000000000000000000000010002");
        InetAddress dhcpServer = Inet6Address.getByAddress(null, byteIPv6Address, 10);

        writeToServer(serviceCode1, dhcpServer);
        readFromServer(serviceCode1, dhcpServer);

    }

    public static void writeToServer(ServiceCode serviceCode1, InetAddress dhcpServer) throws IOException {
        String solicitNum = "01";
        String transactionId = "123456";
        String optionClientID = "0001";
        String optionLength = "000A";
        String DUUIDType = "0003";
        String hardwareType = "0001";
        String linkLayerAddress = "00D861BD4670";

        byte[] sendData = serviceCode1
                .hexStringtoByteArray(String.format("%s%s%s%s%s%s%s", solicitNum, transactionId, optionClientID, optionLength, DUUIDType, hardwareType, linkLayerAddress)); //1 = solicit, 000003 = transactionID

        // Create a packet for sending data
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dhcpServer, 547);

        clientSocket.send(sendPacket);
    }

    public static void readFromServer(ServiceCode serviceCode, InetAddress dhcpServer) throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, 1024);

        clientSocket.receive(receivePacket);

        String receiveHexString = serviceCode.byteArraytoHexString(receiveData);

        String receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength(), "IBM-850");

        System.out.println(receiveString);
        System.out.println(receiveHexString);
    }

}