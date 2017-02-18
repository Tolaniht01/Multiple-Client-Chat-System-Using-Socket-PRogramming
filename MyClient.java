import java.net.*;
import java.util.Scanner;
import java.io.*;

/**
 * Client class which connects to a server by creating a socket on the localhost
 * @author jatin
 *
 */
public class MyClient implements Runnable {
	
	private static Socket MyClient = null;
	private static BufferedReader ClientReader =null;
	private static PrintWriter ClientWriter =null;
	private static int closeChat = 0;
	
	public static void main(String[] args) throws IOException{


		
		try {
			MyClient = new Socket("localhost",2222);
		} catch (UnknownHostException e) {
			System.out.println("Could not find host");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
		}

		

		try {
			ClientReader = new BufferedReader(new InputStreamReader (MyClient.getInputStream()));
		} catch (IOException e) {

			System.out.println("Couldn't get input stream from client");
		}
		try {
			ClientWriter = new PrintWriter(MyClient.getOutputStream(),true);
		} catch (IOException e) {

			System.out.println("Couldn't get output stream from client");
		}

		Scanner input = new Scanner(new BufferedReader(new InputStreamReader(System.in)));

		//Starting new thread for communicating with server
		new Thread(new MyClient()).start();
		

		while(true){


			if(input.hasNextLine()){

				String s = input.nextLine();

				if(closeChat == 0){	
					try{	
						ClientWriter.println(s);
					//	System.out.println("Data sent to server");
					//	String line = ClientReader.readLine();
					//	System.out.println("Received by Client: "+line);
					}
					catch(Exception e){
						System.out.println("Could not send message");
					}
				}else{
					System.out.println("Chat is closed");
					break;
				}

			}

		}//end of while loop

		input.close();
		ClientWriter.close();
		ClientReader.close();
		MyClient.close();

	}

	//@Override
	public void run() {
		
		while(true){
			try {
				if(ClientReader.ready()){
					try {
						String line = ClientReader.readLine();
						System.out.println(line);
						if(line.equals("$closed")){
							System.out.println("Press any key");
							break;
						}
					}catch(Exception e){
						System.out.println("Error communicating with the server");
					}
				
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		closeChat = 1;
		
	}
}
