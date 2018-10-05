package Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

	static ServerSocket serverSocket;
	static List<Socket> socketList;

	public static void main(String args[]) {

		socketList = new ArrayList<Socket>();	
		new Thread() {

			@Override
			public void run() {
				try {
					serverSocket = new  ServerSocket(6000);
					while(true) {
						Socket socket = serverSocket.accept();
						System.out.println("Accepted a conn");
						socketList.add(socket);
						new  RequestHandler(socket).start();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();
	}

	public static synchronized void publishData(Socket owner, String data) {
		try {
			for (Socket socket : socketList) {
				if(owner != socket) {
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF(data);
					dataOutputStream.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}


