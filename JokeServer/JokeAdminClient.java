
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

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
* 6. Extra comments: I tried confirming two times before shutting down the server from client Admin, but failed. I should have been a simple logic of reading but I think, it was messing but with the other input stream
---------------------------------------------------------------------------------

**/
public class JokeAdminClient {

	public static void main(String[] args) throws IOException {
		// @sName- ServerName of the computer.assigned as localhost
		
//		 Initializing server name
		String serverName;
		String secondaryPresent="false";
		
		if (args.length < 1)
			serverName = "localhost";
		// Logic to handle secondary arguments and server name or zero arguments
		else if(args.length >1)
		{
		    serverName = args[1];
		    secondaryPresent="true";
		}
		    else
		    	serverName = args[0]; 	
		// // Making it pretty
		System.out.println("\n###############################");
		System.out.println("######### Welcome Admin #########");
		System.out.println("###############################\n");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			boolean flag = false;
			String Mode = "Joke"; // to store the mode input from admin
			// for switchcase
			int condition = 0;
			do {
				// Welcoming client
				System.out.println("\n###############################  ADMIN MENU    ################################");
				System.out.println("Press 1: Joke Mode");
				System.out.println("Press 2: Proverb Mode");
				System.out.println("Press 3: Shutdown Mode");
				System.out.println("Press 4: To Change Server To Secodary");
				System.out.println("Press 5: Quit");
				System.out.println("################################################################################");
				System.out.flush();
				// input from the user
				BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
				// parsing BR to get Integer Value
				condition = Integer.parseInt(inFromUser.readLine());
				switch (condition) {
			
				case 1:
					// Incase admin wants to switch to joke mode which is also default
					Mode = "Joke";
					break;
				case 2:
					// Incase admin wants to switch to proverb mode
					Mode = "Proverb";
					break;
				case 3:
					System.out.println("Do you really want to shutdown the server? ");
					
//					tried to implement confirmation berfore shutting down server
//					System.out.println("--> Hit 1 for Yes");
//					System.out.println("--> Hit 0 for No");
//					BufferedReader inforcheck = new BufferedReader(new InputStreamReader(System.in));
//					int yesOrNo = Integer.parseInt(inforcheck.readLine());
//					if (yesOrNo == 1) {
						Mode = "Shutdown";
//						System.out.println("###################################################");
//						System.out.println("############ Server shutdown confirmed ############");
//						System.out.println("###################################################");
//						flag = true;
//						
//					} else
//						{
//						System.out.println("Server shutdown request rolling back");
					break;
				case 4:
//					TODO: Secondary server implementation
					// If admin client wants to switch to secondary server
					Mode = "secondaryServer";
					secondaryPresent="true";
					break;
				case 5:
					// quit
					System.out.println("########################################");
					System.out.println("############ Job well done  ############");
					System.out.println("########################################");
					break;

				default:
					// To notify client if any other input is received instead of case 1 or 2
					System.out.println("Please enter one of the above value");
					System.out.println("Incorrect value using default setting Mode as Joke");
					Mode = "Joke";
					break;
				}
				// calling select mode to pass mode and name of the server
				selectMode(Mode, serverName);
			}while (condition != 5);
//			} while (condition != 4 && flag!=false);
			
		} catch (IOException x) {
			x.printStackTrace();
		}
	}

	/**
	 * @sMode-servermode this is to establish connection between jokeserver and
	 *                   adminclient
	 */
	public static void selectMode(String Mode, String serverName) {

		BufferedReader egressFromServer;
		PrintStream ingressToServer;
		String textFromServer;
		int port;
		try {
			// secondary server logic 
			if(Mode.equalsIgnoreCase("secondaryServer"))
			{
				port=5051;
			}
			else
			{
				port=5050;
			}
			Socket sock = new Socket(serverName, port);

			egressFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			ingressToServer = new PrintStream(sock.getOutputStream());
			// injecting server mode to server
			ingressToServer.println(Mode);

			ingressToServer.flush();

//			System.out.println(ingressToServer);
			// reading the o/p from server
			textFromServer = egressFromServer.readLine();
			// logic of checking the logic fro null before printing it
			if (textFromServer != null)

				System.out.println(textFromServer);

			sock.close();

		} catch (IOException x) {

			System.out.println("Error: Cannot connect to server through Socket");
//			System.out.println("Please check server address or port");

			x.printStackTrace();

		}
	}
}








class ModeServerLooper implements Runnable {
	public static boolean adminFlag = true;
	int port;

	public ModeServerLooper(String secondaryServerPresent) {
		if(secondaryServerPresent.equalsIgnoreCase("true"))
		{
//			secondary port if joke server is running on secondary port
			port=5051;
		}
		else
		{
//			primary server port
			port=5050;	
		}
	}

	public void run() {
		System.out.println("Running Admin Server Mode");
		//	no of connection
		int q_len = 6;
		int port = 5050;
		Socket sock;
		try {
			ServerSocket servsock = new ServerSocket(port, q_len);
			while (adminFlag) {
//				// waiting for adminClinet connection
				sock = servsock.accept();
				new ModeWorker(sock).start();
			}
			servsock.close();
		} catch (IOException io) {
			System.out.println(io);
		}
	}
}




class ModeWorker extends Thread{
	Socket sock;
	//private Object sd;
	ModeWorker(Socket s) {sock =s;}
	
	public void run(){
		PrintStream egressToServer =null;
		BufferedReader ingressFromServer =null;

		try{
			ingressFromServer =new BufferedReader(new InputStreamReader(sock.getInputStream()));
			egressToServer=new PrintStream(sock.getOutputStream());
//			Was thinking of some logic to switch from primary to secondary
			if(JokeServer.flag != true ) {
				System.out.println("Listener is shutting down server");
				egressToServer.println("Server shutdown");
			}
			else try {
				// initalizing mode
				String Mode;
//				getting mode from the server
				Mode=ingressFromServer.readLine();
				// checking in the mode provided by the Admin client and taking appropriate steps of notifying admin client and server
//			Was thinking of handling the secondary server switch from ClientAdmim side 
				if (Mode.equalsIgnoreCase("secondaryServer")) 
				{ 
					System.out.println("Secondary Server Started");
					egressToServer.println("Secondary server is started");
					egressToServer.println("Mode=secondaryServer");
					JokeServer.Mode="secondaryServer";
					JokeServer.secondaryServerPresent="true";
					
				
				}
				// changing mode to Joke  as per the mode received
				if(Mode.equalsIgnoreCase("Joke")) {
					JokeServer.Mode="joke";
					System.out.println("Admin changed mode to Joke");
					egressToServer.println("Completed: Request of mode change to Joke");
					egressToServer.println("Mode = Joke");						
				}
				// chnaging mode to Proverb  as per the mode received
				if(Mode.equalsIgnoreCase("Proverb")) {
					JokeServer.Mode="proverb";
					System.out.println("Admin changed mode to Proverb");
					egressToServer.println("Completed: Request of mode change to Proverb");
					egressToServer.println("Mode=Proverb");						
				}
				// Client sends mode as shutdown, then shutdown notified
				if (Mode.equalsIgnoreCase("Shutdown")) { 
					JokeServer.flag = false;
					System.out.println("Shutdown request from Admin");
					egressToServer.println("Server is shutting down");
					System.exit(0);
				}
			}catch(IOException x){
				System.out.println("Error: Cannot connect to server through Socket");
				x.printStackTrace();
			}
			sock.close();
		}catch (IOException ioe){System.out.println(ioe);}
	}
	
	
	public static void printJoke() {
		
		
	}
	
	
}