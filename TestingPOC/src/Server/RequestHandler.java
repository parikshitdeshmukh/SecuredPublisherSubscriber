package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RequestHandler extends Thread{

	Socket socket;
	
	public RequestHandler(Socket socket) {
		// TODO Auto-generated constructor stub
		this.socket= socket;
	}
	
	@Override
	public void run() {
		
		System.out.println("Req Handler Started");
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
		try {
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			
			while(true) {
				String inputData = dataInputStream.readUTF();
				System.out.println("Server Received: " + inputData);
				
				if(inputData.compareTo("Topic") == 0) {
					Server.publishData(socket, inputData);
				}
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
		
		}
		
	}
}
