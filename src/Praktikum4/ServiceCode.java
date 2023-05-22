package Praktikum4;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class ServiceCode {

	public static void main(String[] args) {
		ServiceCode s = new ServiceCode();
		// msg type = 01
		// transactionid = 00001E (random Zahl)
		// client identfieier option = 0001000A (1 CLIENT_OPTIONID, A DUID-LENGTH)
		// DUID = 00030006 (DUID-Type 3, Hardware-Type 6)
		// Hardware-Type = 6
		System.out.println(Arrays.toString(s.hexStringtoByteArray("00030006")));
	}

	 static byte[] hexStringtoByteArray(String hex) {
		/* Konvertiere den String mit Hex-Ziffern in ein Byte-Array */
		byte[] val = new byte[hex.length() / 2];
		for (int i = 0; i < val.length; i++) {
			int index = i * 2;
			int num = Integer.parseInt(hex.substring(index, index + 2), 16);
			val[i] = (byte) num;
		}
		return val;
	}

	static String byteArraytoHexString(byte[] byteArray) {
		/* Konvertiere das Byte-Array in einen String mit Hex-Ziffern */
		String hex = "";
		if (byteArray != null) {
			for (int i = 0; i < byteArray.length; ++i) {
				hex = hex + String.format("%02X", byteArray[i]);
			}
		}
		return hex;
	}

	static void showNetwork() throws SocketException {
		/* Netzwerk-Infos fuer alle Interfaces ausgeben */
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while (en.hasMoreElements()) {
			NetworkInterface ni = en.nextElement();
			System.out.println("\nDisplay Name = " + ni.getDisplayName());
			System.out.println(" Name = " + ni.getName());
			System.out.println(" Scope ID (Interface ID) = " + ni.getIndex());
			System.out.println(" Hardware (LAN) Address = " + byteArraytoHexString(ni.getHardwareAddress()));

			List<InterfaceAddress> list = ni.getInterfaceAddresses();
			Iterator<InterfaceAddress> it = list.iterator();

			while (it.hasNext()) {
				InterfaceAddress ia = it.next();
				System.out
						.println(" Adress = " + ia.getAddress() + " with Prefix-Length " + ia.getNetworkPrefixLength());
			}
		}
	}
}
