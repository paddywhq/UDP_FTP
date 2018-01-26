package com.github.herbix.udpdemo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {

	public static void main(String[] args) throws Throwable {
		DatagramSocket udpSocket = new DatagramSocket(8888);
		byte[] buffer = new byte[4096];
		
		DatagramPacket receivePacket = new DatagramPacket(buffer, 4096);
		int i = 0;
		
		while(true) {
			udpSocket.receive(receivePacket);
			i++;
			System.out.println(i + ": " + new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()));

			String s = i + " " + new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
			byte[] data = s.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length);
			sendPacket.setAddress(receivePacket.getAddress());
			sendPacket.setPort(receivePacket.getPort());
			udpSocket.send(sendPacket);
		}
	}

}
