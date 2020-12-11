/**
 ------------------------------------------------------------------------------------------------
 * 1. Anam Arif Shaikh / Date: 5/3/2020
 ------------------------------------------------------------------------------------------------
 * 2. Java version: 1.8.0_231
 ------------------------------------------------------------------------------------------------
 * 3. Precise command-line compilation instructions:
 
      > javac MyWebServer.java
-------------------------------------------------------------------------------------------------
 * 4. Precise next command instruction:

	  > java MyWebServer
 ------------------------------------------------------------------------------------------------	  
 * 6. List of files needed for running the program.
 		
 	  > MyWebServer.java
 ------------------------------------------------------------------------------------------------	  
 * 7. Extra comments: 1. I tried to add file and directory gifs, hence gif folder in the home DIR.
 					  2. Also, it will create enum class file, after javac command. FYI  
 ------------------------------------------------------------------------------------------------
 
 **/

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyWebServer {

	public static void main(String arr[]) throws IOException {
		// Maximum incoming connection that can be made is 6, if more than 6 connection
		// arrives and the queue is full,
		// the connection will be declined
		int queueLen = 6; // port number of server where the server will listen and client will establish
							// a connection.

		int port = 2540;
		// Creates a socket for server that will bind the server with the port specified
		// along with the maximum number
		// of connection request it can accept in the queue

		ServerSocket serversocket = new ServerSocket(port, queueLen);

		System.out.println("Anam Shaikh Inet server 1.8 starting up, listening at port " + port + "\n");
		while (true) {
			// Server is ready and blocked to do any other operations until the client
			// accepts the connection,the client
			// will return a socket object containing address and port number and local port
			// number once it is connected.

			Socket sock = serversocket.accept();

			// Starts the worker thread and passes socket as a constructor to worker thread
			// and invokes the run method in worker class
			new Worker(sock).start();

		}
	}
}

class Worker extends Thread {
	Socket sock;

	// Constructor of worker which has socket as parameter
	Worker(Socket s) {
		sock = s;
	}

	@Override
	public void run() {
		PrintStream egressToClient;
		BufferedReader ingressFromClient = null;
		String requestType, fileName;

		try {
			// Reading input stream from Socket to get the HTTP request with headers
			ingressFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// Object to send output to client
			egressToClient = new PrintStream(sock.getOutputStream());
			try {
				// Reading the HTTP request from browser
				String requestFromUser = ingressFromClient.readLine();
				// Checking if request from user is null 
				if(requestFromUser==null) {
					return;
				}
				// Array of String to split HTTP request
				String[] tokens = requestFromUser.split(" ");
				// To identify if it is a GET or POST
				requestType = tokens[0];
				// Only processing GET request
				if (requestType.equalsIgnoreCase("GET") == true) {
					// getting the file name from the request
					fileName = tokens[1];
					// Making instance of File
					File f = new File(fileName);
					// Handling BAD REQUEST
					if (!(requestFromUser.endsWith("HTTP/1.0") || requestFromUser.endsWith("HTTP/1.1")))
						handlingError(egressToClient, sock, "400", "Bad Request",
								"Your browser sent a request that " + "server could not understand.");
					System.out.println("\nHTTP/1.0 400 Your browser sent a request that server could not understand");
					// Not allowing MR.X to access DIR other than File Server
					if (fileName.indexOf("..") != -1 || fileName.indexOf("/.ht") != -1 || fileName.endsWith("~"))

						handlingError(egressToClient, sock, "403", "Forbidden",
								"You don't have permission to access the requested URL.");
					System.out.println("\nHTTP/1.0 403 You don't have permission to access the requested URL");
					// Handling Favicon request
					if (fileName.equalsIgnoreCase("/favicon.ico"))
						return;
					// Handling CGI request
					if (fileName.contains("/addnums.fake-cgi")) {

//						System.out.println(fileName);
						// Cleaning filename to get the exact request
//						fileName = fileName.substring(4);
						// Getting addition of numbers by dynamically creating HTML
						getAddition(fileName, getContentType(fileName), egressToClient);
					}
					// If it is a valid filename the send HTTP response and display the file along
					// with getting the content type
					// Also adding !addnum to avoid requests getting here
					if (!(fileName.endsWith("/")) && !(fileName.contains("addnums.fake-cgi"))) {
						// Printing the HTTP request before Displaying the file on browser.
						egressToClient.print("\n HTTP/1.0 200 OK " + "Content-Type: " + getContentType(fileName)
								+ "\r\n" + "Date: " + new Date() + "\r\n" + "Server: FileServer 1.0\r\n\r\n");
						// Displaying the file on browser
						
						   String line ;
						   System.out.println(requestFromUser);
						      while ((line = ingressFromClient.readLine()) != null) {
						          if (line.length() == 0)
						            break;
						          
						          System.out.print(line + "\r\n\n");
						        

						        }
						displayToClient(getContentType(fileName), egressToClient, fileName, f);
					} else  {
						// If it is a directory then display the directory
						if(!(fileName.contains("addnums.fake-cgi")))
						displayDir(fileName, egressToClient, getContentType(fileName), requestFromUser);

					}
				}
				// Connection closed
				sock.close();
			} catch (IOException x) {
				System.out.println("Server error");
				x.printStackTrace();
			}
			// Connection closed
			sock.close();

		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	private void displayToClient(contentType contentType, PrintStream egressToClient, String fileName, File f)
			throws IOException, FileNotFoundException {
		try {
			// Substring to get the file as input
			InputStream ingressFromClient = new FileInputStream(fileName.substring(1, fileName.length()));
			// To hold contents of the file
			byte[] buffer = new byte[10000];
			// Read all the content of the file
			int bufferBytesRead = ingressFromClient.read(buffer);
			// Printing on Server
			System.out.println("\r\nAnam's server is sending response to request " + fileName );
			System.out.println("\nHTTP/1.0 200 OK " +"\nContent Type:" + contentType + "\nContent Length:" + f.length());
			egressToClient.print("\nHTTP/1.0 200 OK " + "Content-Type: " + contentType
					+ "\r\n" + "Date: " + new Date() + "\r\n" + "Server: Anam's Server 1.0\r\n\r\n");
			
			// Writing the buffer to the input browser
			egressToClient.write(buffer, 0, bufferBytesRead);
			// Clearing the buffer
			egressToClient.flush();
			// Closing input
			ingressFromClient.close();
			// Handling Exceptions
		} catch (FileNotFoundException e) {
			handlingError(egressToClient, sock, "404", "Not Found", "The requested URL was not found on this server.");
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// Handling IO Error
			System.out.println("Connection closed, restarting..."); 
		}
	}

	// Method to get the numbers from the Request and add them to send o/p
	public void getAddition(String fileName, contentType contentType, PrintStream egressToClient) { 
		// Parsing fileName to get to numbers
		String path = fileName.substring(1, fileName.length()); 
		// Storing each one in an array by splitting them
		String[] getNumbersAndNamefromPath = path.split("[=&]"); // split person, yourName, num1, input of num1, num2, input of num2 into
		// Getting Name
		String name=getNumbersAndNamefromPath[1];
		// Getting the first number to add	
		String numberOne = getNumbersAndNamefromPath[3];
		// Getting second number to add
		String numberTwo = getNumbersAndNamefromPath[5]; // local definition of num2, set num2 input to nameNum[5] index of type String
		// Adding them to get the sum
		int sum = Integer.parseInt(numberOne) + Integer.parseInt(numberTwo); 
		
		// Saving the response in a String
		String sendToClient = "Hi "+name+", the sum of " + numberOne + " and " + numberTwo + " is " + sum;  
		// Sending the response
		egressToClient.print(sendToClient+"\n"); 
		egressToClient.print("\n HTTP/1.0 200 OK " + "Content-Type: " + contentType
				+ "\r\n" + "Date: " + new Date() + "\r\n" + "Server: Anam's Server 1.0\r\n\r\n");
		// Printing on Server
		System.out.println("\r\nAnam's server is sending response to request " + fileName + "\nContent Type:"
				+ contentType + "\nContent Length:" + sendToClient.length());
		
		
	}
	// Displays error dynamically by passing a HTML in response
	public void handlingError(PrintStream egressToClient, Socket sock, String httpStatusCode, String title, String message) {
		egressToClient.print("\r\n HTTP/1.0 " + httpStatusCode + " " + title + "\r\n" + "\r\n"
				+ "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n" + "<TITLE>" + httpStatusCode + " " + title
				+ "</TITLE>\r\n" + "</HEAD><BODY>\r\n" + "<H1>" + title + "</H1>\r\n" + message + "<P>\r\n"
				+ "<HR><ADDRESS>FileServer 1.0 at " + sock.getLocalAddress().getHostName() + " Port "
				+ sock.getLocalPort() + "</ADDRESS>\r\n" + "</BODY></HTML>\r\n");

	}
	// This method displays directory 
	public void displayDir(String directory, PrintStream egressToClient, contentType contentType,
			String requestFromUser) throws IOException, FileNotFoundException {
		
		try {
			// Writing directory view in a file
			BufferedWriter display = new BufferedWriter(new FileWriter("Index.html")); //
			// Getting the first file
			File initialFile = new File("./" + directory + "/"); // get the first file.
			// Getting the list of files in a DIR and adding it to an array
			File[] directoryFiles = initialFile.listFiles(); // get the list of files present in the directory.
			// Creating a var for current DIR
			String currentDirectory = System.getProperty("user.dir");
			// Displaying current directory by writing it to the file
			display.write("\n <html><head><h1>Index of \"" + currentDirectory + "\"</h1></head>");
			// Tried to implement a table view, but too much formatting needed
//		display.write(
//				"<table> <tr> <th><pre><img src=\"comp.gif\" alt=\\\\\\\"Icon \\\\\\\"> </th> <th> <a href=\\\"?C=N;O=D\\\">Name</a>  <th><a href=\\\"?C=M;O=A\\\">Last modified</a> </th>  </th> <th> <a href=\\\"?C=S;O=A\\\">Size</a></th>  <th> <a href=\\\"?C=D;O=A\\\">Description</a><hr><img src=\\\"back.gif\\\" alt=\\\"[DIR]\\\"></th> </tr> ");
			// Displaying directory by writing it to a file
			display.write(
					"<pre><img src=\"comp.gif\" alt=\"Icon \">	<a href=\"?C=N;O=D\">Name</a>			<a href=\"?C=M;O=A\">Last modified</a>      <a href=\"?C=S;O=A\">Size</a>  <a href=\"?C=D;O=A\">Description</a><hr><img src=\"back.gif\" alt=\"[DIR]\"> <a href=\"/\">Parent Directory</a> ");
			display.write("<br><br>");
			display.write("<body>");
		
			display.write("<br><br>");
			// For populating field of Last Modified
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			for (File f : directoryFiles) {
				// Getting the name of the file
				String fileName = f.getName(); 
				if (fileName.startsWith(".") == true || fileName.startsWith("Index.html") == true) {
					continue;
				}
				// Checking if it is a DIR
				if (f.isDirectory() == true) { 
				// Tried to create a Table View 
//				display.write(" <tr> <th>  <img src=\"dir.gif\" alt=\"[DIR]\"> </th> <th> <a href=\"" + fileName + "/\">/" + fileName
//						+ " </a></th> <th><a>"+sdf.format(f.lastModified())+"</a></th> <th>Size </th> <th>Description<th> </tr> ");
					
				// Displaying the directory by writing it to a file
					display.write("<img src="+currentDirectory+"/gif/dir.gif\" alt=\"[DIR]\">	<a href=\"" + fileName + "/\">/" + fileName
							+ "</a>			<a>" + sdf.format(f.lastModified()) + "</a>			<a>" + f.length()
							+ " bytes</a>			<a>Description</a> </br> ");
				}
				
				// Checking if it is a file
				if (f.isFile() == true) {
				// Tried to create a table view
//						" <tr> <th> <img src=\"file.gif\" alt=\"[TXT]\">  </th> <th> <a href=\"" + fileName + "\" >" + fileName + "</a> </th> <th><a>\\"+sdf.format(f.lastModified())+"</a></th> <th>Size </th> <th>Description<th> </tr>");
					
					// Displaying file by writing it to the file
					display.write("<img src="+currentDirectory+"/gif/file.gif\" alt=\"[TXT]\">	<a href=\"" + fileName + "\" >" + fileName
							+ "</a>			<a>" + sdf.format(f.lastModified()) + "</a>			<a>" + f.length()
							+ " bytes</a>			<a>Description</a></br>");
				}
				display.flush();
			}
			// Finishing dynamic html by closing the tags
			display.write("</pre></body></html>");
			// Creating a temporary file
			File temporaryFile = new File("Index.html");
			InputStream file = new FileInputStream("Index.html");
			egressToClient.println("HTTP/1.1 200 OK" + "Content-Length: " + temporaryFile.length() + "Content-Type: "
					+ contentType + "\r\n\r\n");
			// Printing on Server side
			System.out.println("Anam's Server is printing the directory: " + directory); 
			
			byte[] displayFileBytes = new byte[10000]; 
			int numberOfBytes = file.read(displayFileBytes);
			// Sending the display to he server by writing it
			egressToClient.write(displayFileBytes, 0, numberOfBytes); 
			// Sending the display file to the Server
			System.out.write(displayFileBytes, 0, numberOfBytes); 
			// Closing display
			display.close();
			egressToClient.flush();
			file.close(); 
			// Deleting the temporary file
			temporaryFile.delete(); 
		} catch (FileNotFoundException e) {
			handlingError(egressToClient, sock, "404", "Not Found", "The requested URL was not found on this server.");
		} catch (IOException e) {
			
		}
	}

	private static contentType getContentType(String requestFromUser) {
		// Return content type if html
		if (requestFromUser.endsWith(".html") || requestFromUser.endsWith(".htm"))
			return contentType.valueOf("htmlFile");
		// Return content type if txt
		else if (requestFromUser.endsWith(".txt") || requestFromUser.endsWith(".java"))
			return contentType.valueOf("txtFile");
		// Return content type if file type is gifs
		else if (requestFromUser.endsWith(".gif"))
			return contentType.valueOf("gifFile");
		// Return content type if class file
		else if (requestFromUser.endsWith(".class"))
			return contentType.valueOf("classFile");
		// Return content type if jpeg or jpg
		else if (requestFromUser.endsWith(".jpg") || requestFromUser.endsWith(".jpeg"))
			return contentType.valueOf("jpgFile");

		else
		// Else return plain
			return contentType.valueOf("plain");
	}

}

enum contentType {
	// A better way to use enum over constants to provide type safety.
	htmlFile("text/html"), txtFile("text/plain"), gifFile("image/gif"), classFile("application/octet-stream"),
	jpgFile("image/jpeg"), plain("text/plain");

	private final String fileType;

	private contentType(String s) {
		fileType = s;
	}


}
