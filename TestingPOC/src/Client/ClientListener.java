package Client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientListener extends Thread{


	Socket socket;
	
	public ClientListener(Socket socket) {
		// TODO Auto-generated constructor stub
		this.socket = socket;
	}
	
	@Override 
	public void run() {
		try {
			DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
			while(true) {

				System.out.println("Received: " + dataInputStream.readUTF());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
