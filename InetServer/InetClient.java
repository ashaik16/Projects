import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class InetClient {

	public static void main(String args[]) throws Exception {
		String serverName = "";
		// checks if server address is not provided in args, we can take localhost by
		// default
		if (args.length == 0)
			serverName = "localhost";
		else
			// else we take the server name provided thru the args
			serverName = args[0];

		System.out.println("Anam Shaikh's Inet Client, 1.8.\n");
		System.out.println("Using server: " + serverName + ", Port: 1567");
		try {
			// Since I am using a Menu based approach, flag is used for quit condition to
			// get out of the loop or close the client.
			boolean flag = true;//
			// Loop to print menu
			while (flag == true) {
				// printing menu
				System.out.println("\n######################################");
				System.out.println("***************WELCOME****************");
				System.out.println("######################################");
				System.out.println("\nPress 1 : Search IP of the hostname");
				System.out.println("\nPress 2 : Quit");
				System.out.println("######################################\n");
				// input from the user
				BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
				// parsing BR to get Integer Value
				int condition = Integer.parseInt(inFromUser.readLine());
				// using switch case to serve the menu based approach
				switch (condition) {
				case 1:
					// requesting input **Note** Since Inet is used to get IP from hostname, I am
					// requesting just hostname instead of hostname along with IP
					System.out.print("\nPlease enter the hostname : ");
					// reading the input from the user
					var hostNameFromUser = inFromUser.readLine();
					
					// flushing Stream after new line
					System.out.flush();
					// calling the method

					// making a call to getRemoteAdress method and giving the inputs from the User
					getRemoteAddress(hostNameFromUser, serverName);
					break;

				case 2:
					// Beautifying exit
					System.out.println("########################################");
					System.out.println("\nThank You for visiting the application");
					System.out.println("########################################");
					// to get out of the while loop to quit the client application
					flag = false;
					break;

				default:
					// To notify client if any other input is received instead of case 1 or 2
					System.out.println("Please enter one of the above value");
					break;
				}
			}

		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	
	// This method takes input from user and reads/writes the I/O thru Socket connection using serverName and port
	static void getRemoteAddress(String hostNameFromUser, String serverName) {
		String textFromServer;
		try {
			// initializing socket with server details
			// value for port
			Socket sock = new Socket(serverName, 1567);
			// Reading o/p from the Server  connected thru Socket
			BufferedReader egressFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			// Giving input  to server thru Stream which is the hostNameFromUser
			PrintStream ingressToServer = new PrintStream(sock.getOutputStream());
			// Passing the input name (hostname) from the client to Server thru Stream
			ingressToServer.println(hostNameFromUser);
			ingressToServer.flush();

			// Used for synchronous blocking
			for (int i = 1; i <= 3; i++) {
				textFromServer = egressFromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}
			// CLosing the connection
			sock.close();
		} catch (IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
	
}
