package com.github.herbix.udpdemo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient {

	public static void main(String[] args) throws Throwable {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String ip = in.readLine();
		
		byte[] buffer = new byte[4096];
		DatagramPacket receivePacket = new DatagramPacket(buffer, 4096);
		DatagramSocket udpSocket = new DatagramSocket(9999);
		
		for(int i=0; i<=50; i++)
		{
			String s = i + "";
			byte[] data = s.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(data, data.length);
			sendPacket.setAddress(InetAddress.getByName(ip));
			sendPacket.setPort(8888);
			udpSocket.send(sendPacket);
			
			udpSocket.receive(receivePacket);
			System.out.println(new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()));
		}
		
		udpSocket.close();
	}

}
