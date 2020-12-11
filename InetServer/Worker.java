import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Worker extends Thread {
	Socket sock;
	// Constructor of worker which has socket as parameter
	Worker(Socket s) {
		sock = s;
	} 

	@Override
	public void run() {
		PrintStream egressToClient;
		BufferedReader ingressFromClient = null;
		try {
			// Reading input stream from Socket for getting hostname from client
			ingressFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Getting hostname on server
			egressToClient = new PrintStream(sock.getOutputStream());
			try {
				// Reading input from Input Stream 
				String hostNameFromUserThruStream = ingressFromClient.readLine();
				// To identify request is coming from which Client based on IP address.
				InetAddress clientIP = sock.getInetAddress();
				// Printing on Server Side
				System.out.println("Client " + clientIP + " is Looking up for: " + hostNameFromUserThruStream);
				// Making a call to the below method and passing hostname acquired from the client and also writing to the output 
				printRemoteAddress(hostNameFromUserThruStream, egressToClient);
			} catch (IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			// closing the connection
			sock.close();
		
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	
	// This method takes input as hostname to search in inetAdress Class and writes to the output Stream of the server which is also i/p stream for client
	static void printRemoteAddress(String hostSearch, PrintStream egressToClient) throws IOException {
		try {
			// Beautifying code
			egressToClient.println("Looking up : " + hostSearch + "...");
			// Creating an object of InetAddress class by passing the hostname.
			InetAddress machine = InetAddress.getByName(hostSearch);
			//Writing to Client: I can just print the hostname provided by the Client, but still better to use the method
			egressToClient.println("Host name : " + machine.getHostName());
			//Writing to Client: getting the IP address of hostname requested by user, by using the InetAdress object by using getAddress method
			egressToClient.println("Host IP : " + toText(machine.getAddress()));

		} catch (UnknownHostException ex) {
			// if hostname doesn't exist in the global dns
			egressToClient.println("Hostname : "+hostSearch+"not found in global DNS ");
		}

	}

	// it is being used
	static String toText(byte ip[]) { /* Make portable for 128 bit format */
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < ip.length; ++i) {
			if (i > 0)
				result.append(".");
			result.append(0xff & ip[i]);
		}
		return result.toString();
	}
}