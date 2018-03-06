/*
 * Name: Tushar Garud
 * UTA Id: 1001420891
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat room server.
 * It has a main frame window with a text field for entering
 * messages and a textarea to see the messages from others.
 */
public class HTTPMultithreadedSocketClient {
	
	private PrintWriter out;
	private JFrame frame = new JFrame("Client");
	private JTextField dataField = new JTextField(40);
	private JTextArea messageArea = new JTextArea(8, 60);
	private JScrollPane scrollPane = new JScrollPane(messageArea);
	private Socket socket;
	private BufferedReader in;
	private int maxMsgReceived;

	//A separate thread to poll the server continuously by sending GET requests
	private class ServerSocketReader extends Thread 
	{                
		public void run() 
		{
			try 
			{
				//Create a GET request packet
				HTTPPacket requestPacket = new HTTPPacket();
				requestPacket.setRequest_type("GET");
				requestPacket.setHttp_version("HTTP/1.1");
				requestPacket.setHost("localhost");
				requestPacket.setUser_agent("HTTPTool/1.0");
				requestPacket.setAccept_type("text/plain");
				requestPacket.setAccept_language("en-us");
				
				while(true)
				{
					//Wait for 500 milliseconds before sending next GET request
					Thread.sleep(500);
					
					//Update date and messages count
					requestPacket.setResource("http://localhost/messages/" + String.valueOf(maxMsgReceived));
					requestPacket.setDate_time(new Date());										
					
					//Send the GET request to server
					out.println(requestPacket.toString()); 
					out.flush();	        			        		

					//Check for input
					if(in.ready()) {						
						String line, request="";
						
						//Read response from server
						while ((line = in.readLine()) != null) {
							if(line.equals(""))
								break;
							request += line + "\n";
						}
						
						//Read response data
						if(in.ready())
							request += in.readLine();
						
						//Create a packet object from the string received
						HTTPPacket packet = HTTPPacket.decode(request);
						
						//Check contents inside the the packet
						String input = packet.getData();						
						if (input == null)
							break;
						
						//If new message count received, update message count
						if(input.startsWith("msgcount:")) {
							maxMsgReceived = Integer.parseInt(input.split(":")[1]);
						} else {
							//If it contains a message, display the message
							messageArea.append(input + "\n");
							//Scroll down the window
							scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
						}
					}
				}
			}
			catch(Exception ex) 
			{
			}
		}
	}

	/**
	 * Constructs the client by laying out the GUI and registering a
	 * listener with the textfield so that pressing Enter in the
	 * listener sends a POST request to the server.
	 */
	public HTTPMultithreadedSocketClient() {

		//Initialize message count to zero
		maxMsgReceived=0;

		// Layout GUI
		messageArea.setEditable(false);
		frame.getContentPane().add(dataField, "North");
		frame.getContentPane().add(scrollPane, "Center");        

		// Add Listeners
		dataField.addActionListener(new ActionListener() {
			
			/**
			 * Responds to pressing the enter key in the textfield
			 * by sending the contents of the text field to the server
			 * in a POST request.
			 */
			public void actionPerformed(ActionEvent e) {
				
				//Create a HTTP POST request packet
				HTTPPacket packet = new HTTPPacket();
				packet.setRequest_type("POST");
				packet.setResource("/");
				packet.setHttp_version("HTTP/1.1");
				packet.setHost("localhost");
				packet.setUser_agent("HTTPTool/1.0");
				packet.setContent_type("text/plain");
				packet.setContent_length(dataField.getText().length());
				packet.setDate_time(new Date());
				packet.setData(dataField.getText());

				//Send the request to the server
				out.println(packet.toString());
				out.flush();

				//If user entered 'Exit', then leave the chat room
				if(dataField.getText().equalsIgnoreCase("exit")) {
					try {
						frame.dispose();
						in.close();
						out.close();
					} catch(IOException ex) {}
				}
				else {
					//Reset the textbox to blank
					dataField.setText("");
				}            	
			}
		});
	}

	/**
	 * Implements the connection logic by setting 
	 * the server's IP address, connecting and setting up streams
	 */
	public void connectToServer() throws IOException {

		// Get the user name from a dialog box.
		String userName = "";
		while(userName.equals("")) {
			userName = JOptionPane.showInputDialog(
					frame,
					"Enter user name:",
					"Welcome to the Capitalization Program",
					JOptionPane.QUESTION_MESSAGE);

			if(!Pattern.compile("[a-zA-Z]+").matcher(userName).matches()) {
				userName="";
				JOptionPane.showMessageDialog(null, "Please enter a valid name containing only letters.");
			}
		}    	

		//Set frame title
		frame.setTitle(userName);

		//Set the server IP address
		String serverAddress = "192.168.0.9";

		// Get the server address from a dialog box.		
		/*       String serverAddress = JOptionPane.showInputDialog(
            frame,
            "Enter IP Address of the Server:",
            "Welcome to the Capitalization Program",
            JOptionPane.QUESTION_MESSAGE);		*/

		// Make connection and initialize streams
		socket = new Socket(serverAddress, 9898);
		in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		out.println(userName);

		//Start the reader thread to read messages from server
		new ServerSocketReader().start();
	}

	/**
	 * Create a client process, display it and connect to the server
	 */
	public static void main(String[] args) throws Exception {
		HTTPMultithreadedSocketClient client = new HTTPMultithreadedSocketClient();
		client.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		client.frame.pack();
		client.frame.setVisible(true);
		client.connectToServer();
	}
}