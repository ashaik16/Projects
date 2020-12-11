
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
* 6. Extra comments: The best caching for state of the client code I can think of. 
 
	   MAP				|					MAP 						|                 MAP
 <uuid --> counter>     |		<UUID --> LIST<PA,PB,PC,PD)> 			|			PA --> Proverb     

Step 1: Now using uuid and counter, I iterate the counter
Step 2: Then I get the value of that counter of that particular UUID from second map let say for uuid "asd-f-g-f-d-sdfg" I get List[counter], which can be PB
Step 3: Then I use that List[counter] which comes as PB, then I use the third map to get the respective value of that pointer from it.
---------------------------------------------------------------------------------

**/

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class JokeServer {

	
	//flag for while loop of socket to be running
	public static boolean flag=true;
	// set default mode Joke
	public static String Mode="Joke";
	//Tried to run logic of switching from primary server to secondary
	public static String secondaryServerPresent="false";
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		// Number of connection that can be made
		int q_len=6;
		//initializing port
		int port=0;
		
		Socket sock;
		// Logic for connecting to secondary server directly, this secondary value is obtained from arguments before running the JokServer.java
		if(args.length<1)
		{
			//Primary server port
			port = 4545;
		}
		else if(args.length>=1)
		{	
			//Secondary server port running logic of java JokeServer secondary
			port = 4546;
			// If "secondary" argument is passed in the arguments
			secondaryServerPresent="true";
		}
		
		// Starting the admin thread and waitng to connect
		ModeServerLooper AL= new ModeServerLooper(secondaryServerPresent);	
		Thread t=new Thread(AL);
		t.start();	// 
		
		
		//Intializing a socket to connect to client
		ServerSocket serversock =new ServerSocket(port, q_len);
		// making it pretty
		System.out.println("Anam's Joke Server running on "+port+"\n");
		while(flag){
			//Tried switching the logic from primary to secondary
			if(secondaryServerPresent.equalsIgnoreCase("true")) {
				port = 4546;
				sock=serversock.accept();
			}
			else
				//accepts a new conn from cleint
				sock=serversock.accept();			
			if(flag) new JokeworkerThread(sock).start();		
			try{
				Thread.sleep(1000);
				}
			catch(InterruptedException ex){}	
			}
		serversock.close();
	}
}





class JokeworkerThread extends Thread {
	Socket sock;
	/**
	 * @jokes - ArrayList initialization for jokes
	 * @proverb - ArrayList initialization for proverbs
	 * @jokesCopy and @ProverbsCopy.- ArrayList used to check for repetition of the
	 *            jokes and proverbs.
	 */
	// to save the uuid passed by the client
	private static String uuid;
// 	To create register of uuid and string of JA,JB,JC,JD
	private static Map<String, List<String>> jokeRegister = new HashMap<String, List<String>>();
// 	To create register of uuid and string of PA,PB,PC,PD
	private static Map<String, List<String>> proverbRegister = new HashMap<String, List<String>>();
// Joke map where JA, JB, JC, JD value has actual jokes	as values
	public static Map<String, String> jokemap = new HashMap<String, String>();
// Joke map where P, PB, PC, PD value has actual proverbs	as values
	public static Map<String, String> proverbmap = new HashMap<String, String>();
//	map to keep uuid and counter which is the no of times user talked to server requesting jokes
	private static Map<String, Integer> jokeCache = new HashMap<String, Integer>();
//	map to keep uuid and counter which is the no of times user talked to server requesting proverb
	private static Map<String, Integer> proverbCache = new HashMap<String, Integer>();
//  counter which is passed in jokeCache map for counting no of times user talked to server requesting jokes
	private static int jokeCounter;
//  counter which is passed in proverbCache map for counting no of times user talked to server requesting proverbs
	private static int proverbCounter;

	JokeworkerThread(Socket s) {
		sock = s;
	}
// Basic input output initialization
	static PrintStream egressToServer = null;
	BufferedReader ingressFromServer = null;
	static boolean flag = true;
	static String secondaryPresent="";

	public void run() {
		try {
			// Intializing Joke Map and proverMap by adding initial values to it.
			jokeMap();
			proverbMap();
			
			ingressFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			egressToServer = new PrintStream(sock.getOutputStream());

			try {
				//Reading username from client
				String username= ingressFromServer.readLine();
				//Reading uuid from client
				String id = ingressFromServer.readLine();
//				reading secondaryPresent value in case of  server started a secondary
				secondaryPresent=ingressFromServer.readLine();
				//Printing to test if uuid is unique for every client 
				// test was done with two simultaneous clients
//				System.out.println("User "+username+ " connected, whose uuid is "+id); 
				//Calling and passing getcontent method
				getContent(id,username,secondaryPresent);

			} catch (IOException x) {
				System.out.println("Error: Could not connect  to the server");
				x.printStackTrace();
			}
			sock.close();
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	// method whic checks which Mode is active i.e. Joke or Proverb
	public static void getContent(String id,String username,String secondaryPresent) {
		// if Mode is joke then check user in joke register
		// Was thinking of creating single register but that was a bit challenging using prefixes, would have tried if more time provided
		if (JokeServer.Mode.equalsIgnoreCase("Joke")) {
//			System.out.println("Logic for joke" + id);
			checkUserInJokeRegister(id,username,secondaryPresent);
			// else check check user in proverb register
		} else if (JokeServer.Mode.equalsIgnoreCase("Proverb")) {
//			System.out.println("Logic for proverb");
			checkUserInProverbRegister(id,username,secondaryPresent);
//				getProverb(id);
		}
	}


// IF MODE == Proverb : This method checks if UUID of user is present in the cache of Proverb.
	private static void checkUserInProverbRegister(String id,String username,String secondaryPresent) {
		// TODO Auto-generated method stub
		List<String> arrayUid = null;
	//	if uuid is not present in cache means client not visited the server (taken into consideration that server cannot survive a restart)
		if (!(proverbCache.containsKey(uuid))) {
			// hence we initialize the user register  
			List<String> arrayList = new ArrayList<String>();
			arrayList.add("PA");
			arrayList.add("PB");
			arrayList.add("PC");
			arrayList.add("PD");
//			adding user to the proverb register
			addUserToProverbRegister(uuid, arrayList,username,secondaryPresent);
			
		} else if (proverbCache.get(uuid) >= 3 ) {
			// This logic is to handle if the user has requested more than 3 proverbs, then after printing the last index of the list, its tme to randomize the List
			// we get the List of proverbs a particular user has viewed
			arrayUid = proverbRegister.get(uuid);
			
		//	System.out.println("Key is :" + uuid + " and value is : " + register.get(uuid));
			// In this method we get the List value the particular pointer
			String pointer= processUserProverbArray(arrayUid, proverbCounter);
			if(secondaryPresent.equals("true"))
			{
				egressToServer.println("<S2>"+pointer+" "+username+": "+proverbmap.get(pointer));
			}
			else
			{
//				We print the Pointer which can be PD username: THE PROVERB
				egressToServer.println(pointer+" "+username+": "+proverbmap.get(pointer));
			}
//			 since this the last step of randomization I add a seperator before it is going to shuffle 
			egressToServer.println(">>>>>>>>>>>>>>>>>>>>>>> RANDOMIZING PROVERB <<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			List<String> shuffling = proverbRegister.get(uuid);
//			register.get(uuid).
//			In below lines once the list is shuffled we set the list again
			List<String> shuffled = shuffleArray(shuffling);
			proverbRegister.get(uuid).set(0, shuffled.get(0));
			proverbRegister.get(uuid).set(1, shuffled.get(1));
			proverbRegister.get(uuid).set(2, shuffled.get(2));
			proverbRegister.get(uuid).set(3, shuffled.get(3));
		
			// System.out.println("Key for shuffled:" + uuid + " and value is : " + proverbRegister.get(uuid));
			// Then we update the uuid counter for proverb to 0 again to again start from 0th element of the list
			updateProverbCache(uuid, 0);
		}
		//Else if conter is less than 3 that means we dont have to worry about randomize step
		else if (proverbCache.get(uuid) < 3) {
			//	 we get the List of proverbs a particular user has viewed
			arrayUid = proverbRegister.get(uuid);
//		 	In this we get the counter of user, which denotes the no of time he requested Proverb
			proverbCounter = proverbCache.get(uuid);
//			System.out.println("Key is :" + uuid + " and value is : " + proverbCache.get(uuid));
//			In this method we get the pointer at which the current list is at
			String pointer= processUserProverbArray(arrayUid, proverbCounter);
			if(secondaryPresent=="true")
			{
				egressToServer.println("<S2"+pointer+" "+username+": "+proverbmap.get(pointer));	
			}
			else
			{
//			We print the Pointer which can be PA username: THE PROVERB
				egressToServer.println(pointer+" "+username+": "+proverbmap.get(pointer));
			}
			// and we update the cache
			updateProverbCache(uuid, ++proverbCounter);

		}
		
	}

//	 In this method first we take users who does are not present in cache, we add them in register and then update cache to 0 pointer instead of null
	private static void addUserToProverbRegister(String uuid2, List<String> arrayList,String username,String secondaryPresent) {
		proverbRegister.put(uuid, arrayList);
		proverbCounter= 0;
		String pointer = processUserProverbArray(arrayList, proverbCounter);
		if(secondaryPresent.equals("true"))
		{
			egressToServer.println("<S2>"+pointer+" "+username+": "+proverbmap.get(pointer));
		}
		else
		{
//			We print the Pointer which can be PD username: THE PROVERB
			egressToServer.println(pointer+" "+username+": "+proverbmap.get(pointer));	
		}
		//cache.put(uuid, counter++);
		updateProverbCache(uuid, ++proverbCounter);
		
	}
	
// This method updates counter in cache for that uuid
	private static void updateProverbCache(String uuid2, int counter2) {
		proverbCache.put(uuid2, counter2);

	}

// This method gets the List[counter] which is PA || PB|| PC || PD. Then we look that pointer in Map to get joke	
	public static String processUserProverbArray(List<String> arrayUid, int counter) {
		String pointer = arrayUid.get(counter);
		return pointer;

	}


	public static void checkUserInJokeRegister(String uuid,String username,String secondaryPresent) {
		List<String> arrayUid = null;

		if (!(jokeCache.containsKey(uuid))) {
			List<String> arrayList = new ArrayList<String>();
			arrayList.add("JA");
			arrayList.add("JB");
			arrayList.add("JC");
			arrayList.add("JD");
			addUserToJokeRegister(uuid, arrayList,username,secondaryPresent);
// This is the step where reshuffling is needed after after we send the value
		} else if (jokeCache.get(uuid) >= 3 ) {
			arrayUid = jokeRegister.get(uuid);

		//	System.out.println("Key is :" + uuid + " and value is : " + register.get(uuid));
			
			String pointer= processUserJokeArray(arrayUid, jokeCounter);
			// Logic for joke server started at secondary port
			if(secondaryPresent.equals("true"))
			{
				egressToServer.println("<S2>"+pointer+" "+username+": "+jokemap.get(pointer));
			}
			else
			{
				
//				We print the Pointer which can be JA username: THE JOKE
				egressToServer.println(pointer+" "+username+": "+jokemap.get(pointer));
			}
			// adding a seperator
			egressToServer.println(">>>>>>>>>>>>>>>>>>>>>>> RANDOMIZING JOKE <<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			List<String> shuffling = jokeRegister.get(uuid);
//			register.get(uuid).
//			In below lines once the list is shuffled we set the list again
			List<String> shuffled = shuffleArray(shuffling);
			jokeRegister.get(uuid).set(0, shuffled.get(0));
			jokeRegister.get(uuid).set(1, shuffled.get(1));
			jokeRegister.get(uuid).set(2, shuffled.get(2));
			jokeRegister.get(uuid).set(3, shuffled.get(3));
		

//			System.out.println("Key for shuffled:" + uuid + " and value is : " + jokeRegister.get(uuid));
		// This method updates cache
			updateJokeCache(uuid, 0);
		}

		else if (jokeCache.get(uuid) < 3) {
			arrayUid = jokeRegister.get(uuid);
			jokeCounter = jokeCache.get(uuid);
//			System.out.println("Key is :" + uuid + " and value is : " + jokeCache.get(uuid));
			String pointer= processUserJokeArray(arrayUid, jokeCounter);
			// Same logic for secondary server but with a tag <s2>
			if(secondaryPresent.equals("true"))
			{
				egressToServer.println("<S2>"+pointer+" "+username+": "+jokemap.get(pointer));
			}
			else
			{
//				We print the Pointer which can be JA username: THE JOKE
				egressToServer.println(pointer+" "+username+": "+jokemap.get(pointer));	
			}
			//updating cache
			updateJokeCache(uuid, ++jokeCounter);

		}

	}
// this method udates joke cache 
	private static void updateJokeCache(String uuid2, int counter2) {
		jokeCache.put(uuid2, counter2);

	}

	public static String processUserJokeArray(List<String> arrayUid, int counter) {
		String pointer = arrayUid.get(counter);
		return pointer;

	}
//	 In this method first we take users who does are not present in cache, we add them in joke register and then update cache to 0 pointer instead of null
	public static void addUserToJokeRegister(String uuid, List<String> array,String username,String secondaryPresent) {
		// Get the randomized array and then get the value and put it here
		jokeRegister.put(uuid, array);
		jokeCounter= 0;
		String pointer = processUserJokeArray(array, jokeCounter);
		if(secondaryPresent.equals("true"))
		{
			
			egressToServer.println("<S2>"+pointer+" "+username+": "+jokemap.get(pointer));
		}
		else
		{
			
			egressToServer.println(pointer+" "+username+": "+jokemap.get(pointer));
		}
//			was doing it wrong initially bu counter++
		//cache.put(uuid, counter++);
//		Then chnaged to ++counter and update the cache
		updateJokeCache(uuid, ++jokeCounter);
	}
// Logic to shuffle the list
	public static List<String> shuffleArray(List<String> shuffling) {
		// List<String> intList = Arrays.asList(shuffling);
		Collections.shuffle(shuffling);

		return shuffling;
	}

//	Joke map which has key value. This key is present in list of string as value in Joke register
	public static void jokeMap() {

		jokemap.put("JA", "If I get quarantined for two weeks with my wife and I die. I can assure you it was not the virus that killed me.");
		jokemap.put("JB", "Since everybody has now started washing their hands, the peanuts at the bar have lost their taste.");
		jokemap.put("JC", "I sneezed in the bank today, it was the most attention I have received from the staff in the last 10 years.");
		jokemap.put("JD", "Mexico is asking Trump to hurry up and build the wall NOW");
		
	}
//	Proverb map which has key value. This key is present in list of string as value in Proverb register
	public static void proverbMap() {

		proverbmap.put("PA", "Some people are worth melting for --Frozen");
		proverbmap.put("PB", "Hope is a good thing, maybe the best of things, and no good thing ever dies. ---Andy Dufresne, The Shawshank Redemption");
		proverbmap.put("PC", "All great things happen between method and madness --- Shakespheare, Hamlet");
		proverbmap.put("PD", "When you are backed against the wall, break the goddamn thing down -- Harvey Specter, Suits");
		
	}	

}