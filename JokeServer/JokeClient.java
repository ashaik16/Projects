

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;


/**
 -------------------------------------------------------------------------------------------
 * 1. Anam Arif Shaikh / Date: 4/19/2020
 ------------------------------------------------------------------------------------------
 * 2. Java version: 1.8.0_231
 --------------------------------------------------------------------------------------------
 * 3. Precise command-line compilation examples / instructions:
 
      > javac JokeClient.java
 --------------------------------------------------------------------------------------------
 * 4. Precise examples / instructions to run this program:     
		
	  > javac JokeClient
-------------------------------------------------------------------------------------------
 * 5. In separate shell windows:

	> java JokeServer
	> java JokeClient
	> java JokeClientAdmin
 -------------------------------------------------------------------------------------------
 * 6. All acceptable commands are displayed on the various consoles:
 
 	  > java JokeClient localhost
 	  > java JokeClient 127.0.0.1
 	  > java JokeClient 192.168.3.1 (Your ip)
 ---------------------------------------------------------------------------------------------	  
 * 5. List of files needed for running the program. (All files can run independent fo each other but they will wait for(except checklist) one of them, like JokeServer waiting for Joke Cient
 		
 	  > JokeClient.java
 	  > JokeServer.java
 	  > JokeClientAdmin.java
 	  > checklist.html
 -----------------------------------------------------------------------------------------------	  
 * 6. Extra comments: I tried implementing switching from primary server to secondary server using JokeAdminClient, but got into whole lot of other issues. 
 ---------------------------------------------------------------------------------
 
 **/
@SuppressWarnings("unused")



public class JokeClient {

	// code moved to server during code revision

//private static ArrayList<String> jokes; 
//private static ArrayList<String> jokesCopy;

	/**
	 * @param args
	 */
	public static void main(String args[]) throws IOException {
		// TODO Auto-generated method stu
//		 Initializing server name
		String serverName;
		String secondaryPresent="false";
		int port = 0;
		// Thought of using the IP address as the unique value to be passed
		// But it would clearly fail while testing locally with multiple clients
		//Using uuid with every new client helps identifying different threads on the same local
		// InetAddress machine=InetAddress.getLocalHost();
		// Logic to handle secondary arguments and server name or zero arguments
		if (args.length < 1)
		{
			// specifying server and port if only primary server
			serverName = "localhost";
			port=4545;
		}
		else if(args.length >1)
		{
			//Specifying secondary server details if started Joke server is started as secondary 
		    serverName = args[1];
		    secondaryPresent="true";
		    port=4546;
		}
		    else
		    {
		    	serverName = args[0]; 	
		    }
		// Making it pretty
		System.out.println("Anam's  Joke client \n");
		// Printing which server and what port is being used
		System.out.println("Using Server " + serverName + ", port "+port);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));;
		// creating radom 32 bit UUID
		UUID uuid = UUID.randomUUID();
		//converting it to string to be passed
		String uuid1 = uuid.toString();
		// checking UUID
		// System.out.println(uuid1);

		try {
			// for switchcase
			String condition;
			// Welcoming client
			System.out.println("\n###############################");
			System.out.println("########### Welcome ###########");
			System.out.println("###############################\n");
			System.out.println("Please enter your username");
			//Reading name of the user
			String username= in.readLine();
			System.out.flush();
			//Greeting user as asked
			System.out.println("Hello "+username+" ");
			do {
				// Creating menu option for user
				System.out.println("\n###############################     MENU     ################################");
				System.out.println("Press Enter to proceed get a Joke or Proverb");
				System.out.println("Press q : Quit");
				System.out.println("################################################################################");
				// To read inut from user
				BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
				// get the input from user which is condition
				condition = in.readLine();
				switch (condition) {
				case "":
					// if user hits Enter then parse username, uuid generated and server details
					getRemoteAddress(username, serverName, uuid,secondaryPresent);
					break;
					// else quit 
				case "q":
				case "Q":
					// Beautifying exit
					System.out.println("########################################");
					System.out.println("Thank You for visiting the application");
					System.out.println("########################################");
					// to get out of the while loop to quit the client application
					break;

				default:
					// To notify client if any other input is received instead of case 1 or 2
					System.out.println("Please enter one of the above value");
					break;
				}	
			} while (!(condition.equalsIgnoreCase("q")));
			
		} catch (IOException x) {
			x.printStackTrace();
		}
		
		
		
		
	}

	/**
	 * @sock- variable is for establishing a new socket connection.
	 * @toServer- variable is to send the user input domain to server.
	 * @fromServer- variable is to get back the output from the server as IP of the
	 *              domain.
	 **/

	static void getRemoteAddress(String username, String serverName, UUID uuid,String secondaryPresent) {
		Socket sock;
		BufferedReader egressFromServer;
		PrintStream ingressToServer;
		String printStreamFromServer;
		int port;

		// Socket connection for prmary or secondary
		try {
			if(secondaryPresent=="true")
			{
				// if secondary server is started then secondaryPresent switch is true
				port = 4546;
			}
			else
			{
				port = 4545;
			}
			// passing the values to connect to Server
			sock = new Socket(serverName, port);
			
			egressFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			ingressToServer = new PrintStream(sock.getOutputStream());
			
			// Sending username to server
			ingressToServer.println(username);

			ingressToServer.flush();
			// sending uuid to server
			ingressToServer.println(uuid);
			// sending secndaryPresent value to server
			ingressToServer.println(secondaryPresent);

			ingressToServer.flush();

			// Getting joke or proverb from the server
			printStreamFromServer = egressFromServer.readLine();
			// Displaying joke or proverb from the server
			System.out.println(printStreamFromServer);

			sock.close();
		} catch (IOException x) {
			// If problems connecting server
			System.out.println("Error: Cannot connect to server through Socket");
//			x.printStackTrace();
		}
	}
	
}