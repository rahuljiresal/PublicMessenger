package org.messenger.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Client {

	Display display;
	Shell shell;
	Text blackboardText;
	Text sendMessageText;
	Button sendButton;
	Integer socketNum;

	Socket clientSocket = null;
	ObjectOutputStream out = null;
	ObjectInputStream in = null;
	private String hostPrefix;


	public Client() {
		display = new Display();
		shell = new Shell(display);
		shell.setText("Broadcaster");
		shell.setToolTipText("Broadcaster");
		shell.setLayout(new FillLayout());
		Composite mainComposite = new Composite(shell, SWT.NONE);

		GridLayout compositeGridLayout = new GridLayout();
		compositeGridLayout.numColumns = 5;
		mainComposite.setLayout(compositeGridLayout);

		blackboardText = new Text(mainComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		blackboardText.setEditable(false);
		GridData bbtextGridData = new GridData();
		bbtextGridData.horizontalSpan = 5;
		bbtextGridData.horizontalAlignment = GridData.FILL;
		bbtextGridData.verticalAlignment = GridData.FILL;
		bbtextGridData.verticalSpan = 20;
		bbtextGridData.grabExcessHorizontalSpace = true;
		bbtextGridData.grabExcessVerticalSpace = true;
		blackboardText.setLayoutData(bbtextGridData);


		sendMessageText = new Text(mainComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData sendMessageGridData = new GridData();
		sendMessageGridData.horizontalSpan = 2;
		sendMessageGridData.verticalSpan = 2;
		sendMessageGridData.horizontalAlignment = GridData.FILL;
		sendMessageGridData.verticalAlignment = GridData.FILL;
		sendMessageGridData.grabExcessHorizontalSpace = true;
		sendMessageText.setLayoutData(sendMessageGridData);

		sendButton = new Button(mainComposite, SWT.PUSH);
		sendButton.setText("Send");
		GridData sendButtonGridData = new GridData();
		sendButtonGridData.horizontalSpan = 1;
		sendButtonGridData.verticalSpan = 2;
		sendButtonGridData.horizontalAlignment = GridData.FILL;
		sendButtonGridData.verticalAlignment = GridData.FILL;
		sendButtonGridData.verticalIndent = GridData.FILL;
		sendButtonGridData.grabExcessVerticalSpace = true;

		calculateHostPrefixString();
		connectToServer();
		addListeners();

		//shell.pack();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}


	private void calculateHostPrefixString() {
		String username = System.getProperty("user.name");
		String hostname = null;
		InetAddress localMachine;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
			hostname = localMachine.getHostName();	
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hostPrefix = username /*+ "@" + hostname*/;
	}


	private void createRandomSocketNumber() {
		Random ran = new Random();
		socketNum = ran.nextInt(64000) + 1000;

	}


	private void connectToServer() {

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {

				int i = 0;
				boolean connected = false;
				while (!connected){

					createRandomSocketNumber();
					try{
						blackboardText.setText(blackboardText.getText() + "Connecting to server...\n");
						clientSocket = new Socket("localhost", 2010);

						out = new ObjectOutputStream(clientSocket.getOutputStream());
						out.flush();

						out.writeObject(socketNum.toString());
						out.flush();

						connected = true;

					}
					catch (Exception e){
						e.printStackTrace();
						blackboardText.setText(blackboardText.getText() + "Could not connect. Retrying...\n");
					}
					finally{
						try {
							clientSocket.close();
							out.close();
						}
						catch (Exception e){
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					i++;
				}
				
				try{
					clientSocket = new Socket("localhost", socketNum);
					out = new ObjectOutputStream(clientSocket.getOutputStream());
					//in = new ObjectInputStream(clientSocket.getInputStream());
					blackboardText.setText(blackboardText.getText() + "Connected");
					
				}
				catch (Exception e){
					e.printStackTrace();
				}
				blackboardText.setText("");


			}

		});
	}

	private void addListeners() {

		sendButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {

				try {
					String message = sendMessageText.getText();
					if (!(message.equalsIgnoreCase("<update>") ||
							message.equalsIgnoreCase("<bye>")))
						message = hostPrefix + ": " + message;
					
					out.writeObject(message);
					out.flush();
					
					if (message.equalsIgnoreCase("<update>")){
						try {
							if (in == null)
								in = new ObjectInputStream(clientSocket.getInputStream());
							String reply = (String)in.readObject();
							blackboardText.setText(reply);
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
						
					}
					
					else if (message.equalsIgnoreCase("<bye>")) {
						sendButton.setEnabled(false);
						sendMessageText.setEnabled(false);
						blackboardText.setEnabled(false);
					}
					
					sendMessageText.setText("");
				} catch (IOException e) {
					e.printStackTrace();
				}
				

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});

	}


	/**
	 * @param args

	 */


	public static void main(String[] args) {

		Client c = new Client();
	}

}
