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
//						socket.setReuseAddress(true);
//						serverSocket.setReuseAddress(true);
						System.out.println("Accepted a conn with : "+ socket);
						socketList.add(socket);
						new RequestHandler(socket).start();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();
	}

	public static synchronized void publishData(Socket owner, ArrayList<String> topicList) {
		try {
			for (Socket socket : socketList) {
				if(owner != socket) {
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF(String.valueOf(topicList));
					dataOutputStream.flush();
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
