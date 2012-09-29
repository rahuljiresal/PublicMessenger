package org.messenger.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	static ArrayList<String> data;
	ArrayList<ServerConnection> clients;

	public Server() {

		data = new ArrayList<String>();
		clients = new ArrayList<ServerConnection>();

	}

	private void start() {

		while(true)
			acceptConnection();
	}



	private void acceptConnection() {

		ObjectInputStream in = null;
		ServerSocket socket = null;

		try{
			socket = new ServerSocket(2010, 10);
			System.out.println("accepting new connection...");
			Socket connection = socket.accept();
			System.out.println("connection received..");
			in = new ObjectInputStream(connection.getInputStream());

			String message = (String) in.readObject();
			Integer socketNumber = Integer.parseInt(message);

			ServerConnection conn = new ServerConnection(socketNumber);
			clients.add(conn);
			conn.start();

		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				in.close();
				socket.close();
			}
			catch(Exception ioException){
				ioException.printStackTrace();
			}
		}



	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {

		Server server = new Server();
		server.start();

	}
}
