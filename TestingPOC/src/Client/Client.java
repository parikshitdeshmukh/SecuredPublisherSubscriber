package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
	
	public static void main(String args[]) {
		
		try {
			Socket socket = new Socket("localhost",6000);
			Scanner scanner = new Scanner(System.in);
			new ClientListener(socket).start();
			DataOutputStream dataOutputStream = new  DataOutputStream(socket.getOutputStream());
			while(true) {
				System.out.println("Enter Data Now");
				String data = scanner.nextLine();
				dataOutputStream.writeUTF(data);
				dataOutputStream.flush();
				System.out.println("sent: " + data);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
