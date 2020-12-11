import java.io.IOException;//Throws a checked exception related to failure of input and output operations.
import java.net.ServerSocket;
import java.net.Socket;

public class InetServer {

	public static void main(String arr[]) throws IOException {
		// Maximum incoming connection that can be made is 6, if more than 6 connection arrives and the queue is full,
		// the connection will be declined
		int queueLen = 0; // port number of server where the server will listen and client will establish  a connection.

		int port = 1567;
		// Creates a socket for server that will bind the server with the port specified along with the maximum number
		// of connection request it can accept in the queue

		ServerSocket serversocket = new ServerSocket(port, queueLen);
																		
		System.out.println("Anam Shaikh Inet server 1.8 starting up, listening at port 1567.\n");
		while (true) {
			//Server is ready and blocked to do any other operations until the client  accepts the connection,the client 
			//will return a socket object  containing address and port number and local port number once it is connected.

			Socket sock = serversocket.accept(); 
			
			// Starts the worker thread and passes socket as a constructor to worker thread
			// and invokes the run method in worker class
			new Worker(sock).start();

		}
	}
}