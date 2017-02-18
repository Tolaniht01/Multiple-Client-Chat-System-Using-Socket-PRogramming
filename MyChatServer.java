
import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;

public class MyChatServer {

  static ServerSocket serverSocket = null;
  static Socket clientSocket = null;

  static int clientCount = 100;
  static clientThread[] threads = new clientThread[clientCount];

  public static void main(String args[]) 
  {

    
    
     try {
      serverSocket = new ServerSocket(2222);
      System.out.println("Server Established and Running");
    } catch (Exception e) {
	System.out.println("cant make server");      
	System.out.println(e);
    }

    
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < clientCount; i++) {
          if (threads[i] == null) {
             threads[i] = new clientThread(clientSocket, threads);
	     threads[i].start();
	     System.out.println("Socket Accepted for a new client");
            break;
          }
        }
        if (i == clientCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Too many clients!");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}




class clientThread extends Thread {

  private String clientName = null;
  private BufferedReader is = null;
  private PrintWriter os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int clientCount;
  int status =-1;

  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    clientCount = threads.length;
  }

  public void run() {
    int clientCount = this.clientCount;
    clientThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
      os = new PrintWriter(clientSocket.getOutputStream() , true);
      String name;


        os.println("Enter your name.");
        name = is.readLine().trim();

      /* Welcome the new the client. */
      os.println("Welcome " + name + ".\nTo close chat enter $close\n To see offline/online of users status enter $status\n To go offline enter $online\n To come back online enter $online\n");
      synchronized (this) {
        for (int i = 0; i < clientCount; i++) {
          if (threads[i] != null) {
            if(threads[i] == this){
            clientName = name;
	          status =1;
            break;
            }

          }
        }
        for (int i = 0; i < clientCount; i++) {
          if (threads[i] != null) {
          
            if(threads[i] != this){
            threads[i].os.println("New user " + name
                + " has entered into the chat");
            }


          }
        }
      }
      /* Start the conversation. */
      while (true) {

	os.println("Enter the name of the receiver or the command !");
        String line = is.readLine();
	if (line.equals("$close")) {
          break;
        }


	if(line.equals("$offline"))
	{
		status=0;
		os.println("You are Offline !!!");
		continue;

	}

	if(line.equals("$online"))
	{
		status=1;
		os.println("-------You are online !!!--------");
		continue;

	}

	if(line.equals("$status"))
	{
		os.println("--------List of Online Clients------");
		synchronized(this)
		{
			for(int i=0;i<clientCount;i++)
			{
				if(threads[i]!=null && threads[i].status==1 && threads[i].clientName!=null)
				{
					os.println(threads[i].clientName);
				}
			}
		}

		os.println("----- List of OFFline Clients ---------");
		synchronized(this)
		{
			for(int i=0;i<clientCount;i++)
			{
				if(threads[i]!=null && threads[i].status==0 && threads[i].clientName!=null)
				{
					os.println(threads[i].clientName);
				}
			}
		}

		continue;


	}	
	os.println("Enter the message !");

	
	String line2=is.readLine();



	int index=-1;
	synchronized (this) {
                for (int i = 0; i < clientCount; i++) {
 	//System.out.println("found1" + line );
	//if(threads[i] != null){	
	//System.out.println( threads[i].clientName );
 	//}
                 if (threads[i] != null && threads[i].clientName.equals(line)) 
		  {
		      //System.out.println("found" + line );
		     // os.println(threads[i].clientName);
                      index=i;
			break;
		   }
		 }
		}

		if(index==-1)
		{
			os.println("Receiver not present !!!!");
			continue;
		}



            if (!line2.isEmpty()) {
              synchronized (this) {
                for (int i = 0; i < clientCount; i++) {
                  if (threads[i] != null && threads[i] != this
                      && threads[i].clientName != null
                      && threads[i].clientName.equals(line) && i==index) {
                  	  if(threads[i].status==0)
                  	  {
                  	  	os.println("Client "+ line +" is currently Offline  mesaage cant be sent !!!!");
                  	  }
                  	  else
                  	  {
                      threads[i].os.println(this.clientName +" to "+ line +" --> "+line2);
                      os.println(this.clientName +" to "+ line +" --> "+line2);
                  	  }
                    /*
                     * Echo this message to let the client know the private
                     * message was sent.
                     */
                    //this.os.println(">" + name + "> " + words[1]);
                    break;
                  }
                }
              }
            }
          //}
        //}
	 else {
          /* The message is public, broadcast it to all other clients. */
          synchronized (this) {
            for (int i = 0; i < clientCount; i++) {
              if (threads[i] != null && threads[i].clientName != null) {
                threads[i].os.println("<" + name + "> " + line);
              }
            }
          }
        }
      }
      synchronized (this) {
        for (int i = 0; i < clientCount; i++) {
          if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
            threads[i].os.println("The user " + name
                + " has left the chat room");
          }
        }
      }
      os.println("$closed");
      

      //set the closed thread to null
      synchronized (this) {
        for (int i = 0; i < clientCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      //closing streams
      is.close();
      os.close();
      clientSocket.close();
    } catch (Exception e) {
      System.out.println("error");
    }
  }
}

