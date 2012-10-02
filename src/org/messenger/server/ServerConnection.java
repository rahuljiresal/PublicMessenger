package org.messenger.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerConnection extends Thread {

	int socketNumber;
	ObjectInputStream in = null;
	ObjectOutputStream out = null;
	ServerSocket socket = null;


	public ServerConnection(Integer socketNumber) {
		this.socketNumber = socketNumber;
	}

	@Override
	public void run() {

		try{
			socket = new ServerSocket(socketNumber, 10);
			System.out.println("connecting client on " + socketNumber);
			Socket connection = socket.accept();
			System.out.println("client connected..");
			out = new ObjectOutputStream(connection.getOutputStream());
			in = new ObjectInputStream(connection.getInputStream());
			String message = null;
			do{
				try{
					message = (String)in.readObject();
					if (message.equalsIgnoreCase("<update>")){
						sendMessage(getServerDataAsString(Server.data));
					}

					else if (!message.equalsIgnoreCase("<bye>") && 
							!message.equalsIgnoreCase("<subscribe>") &&
							!message.equalsIgnoreCase("<unsubscribe>") &&
							message != null){
						System.out.println("received: " + message);
						Server.data.add(message);
					}
				}
				catch(ClassNotFoundException classNot){
					System.err.println("data received in unknown format");
				}
			}while(!message.equals("<bye>"));
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				in.close();
				out.close();
				socket.close();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private String getServerDataAsString(ArrayList<String> data) {

		String returnValue = new String();
		for (String s : data){
			returnValue += (s + "\n");
		}

		return returnValue;
	}

	private void sendMessage(String string) {

		try{
			out.writeObject(string);
			out.flush();
		}
		catch(Exception ioException){
			ioException.printStackTrace();
		}

	}

}
