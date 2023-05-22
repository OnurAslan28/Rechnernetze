
/* RFTClientRcvThread.java
 Version 1.0
 Praktikum Rechnernetze HAW Hamburg
 Autor: M. Huebner
 */

import java.io.IOException;
import java.net.*;

public class RFTClientRcvThread extends Thread {
    /* Receive UDP packets and handle the ACKs */
    private RFTClient myRFTC;
    private long sendbase;

    public RFTClientRcvThread(RFTClient rftClient) {
        myRFTC = rftClient;
        sendbase = 0;
    }

    public void run() {
        RFTpacket ack;
        int dupCounter = 0; // count duplicate ACKs for fast retransmit
        DatagramPacket receivePacket;
        byte[] receiveData;

        receiveData = new byte[myRFTC.UDP_PACKET_SIZE];
        receivePacket = new DatagramPacket(receiveData, myRFTC.UDP_PACKET_SIZE);

        myRFTC.testOut("RcvThread: Waiting for ACKs!");

        while (!isInterrupted()) {
            try {
                // Set SoTimeout due to possible complete loss of Acks
                myRFTC.clientSocket.setSoTimeout(myRFTC.CONNECTION_TIMEOUT);
                // wait for ack
                myRFTC.clientSocket.receive(receivePacket);
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("-------------------------->>>>> Connection Timeout! Server down?");
                return;
            } catch (IOException e) {
                return;
            }

            //damit kein leeres Packet am ende noch gesendet wird.
            if(receivePacket.getLength() < 8){
                continue;
            }

            // Analyse received ACK
            ack = new RFTpacket(receivePacket.getData(), receivePacket.getLength());
            myRFTC.testOut("RcvThread: ACK received for seq num: " + ack.getSeqNum() + " -- sendbase: " + sendbase);

            //long tempTimestamp = myRFTC.sendBuf.getSendbasePacket().getTimestamp();

            if (ack.getSeqNum() > sendbase) {
                /* -------- Evaluate ACK ----------- */

                if (myRFTC.sendBuf.getSendbasePacket().getTimestamp() != -1 && ack.getSeqNum() == sendbase + myRFTC.sendBuf.getSendbasePacket().getLen()) {
                    long currentRtt = System.nanoTime() - myRFTC.sendBuf.getSendbasePacket().getTimestamp();
                    myRFTC.computeTimeoutInterval(currentRtt);
                }

                sendbase = ack.getSeqNum();

                myRFTC.sendBuf.remove(sendbase);
                dupCounter = 0;

                if (myRFTC.sendBuf.getSendbasePacket() != null) {
                    myRFTC.rft_timer.startTimer(myRFTC.timeoutInterval, true);

                } else {
                    myRFTC.rft_timer.cancelTimer();
                }

            } else {
                /* ------- Fast Retransmit ? ----- */
                if (myRFTC.fastRetransmitMode) {
                    if (sendbase == ack.getSeqNum()) {
                        dupCounter++;
                    }
                    if (dupCounter == 3) {
                        myRFTC.timeoutTask();
                        dupCounter = 0;
                    }

                }
            }
        }
    }
}
