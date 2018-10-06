package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class RequestHandler extends Thread{
	static ArrayList<String> topicList= new ArrayList<>();
	Socket socket;
	private static HashMap<String, ArrayList<Socket>> topicswiseSubs = new HashMap<>();
	private static HashSet<String> topicForSubs = new HashSet<>();
	
	public RequestHandler(Socket socket) {
		// TODO Auto-generated constructor stub
		this.socket= socket;
	}
	
	@Override
	public void run() {
		
//		System.out.println("Req Handler Started");
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;
		try {
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			
			while(true) {
				String inputData = dataInputStream.readUTF();
//				System.out.println("Server Received: " + inputData);

				String[] inputArray = inputData.split("-");

				if (inputArray[0].trim().compareTo("Pub") == 0 && inputArray[1].trim().equals("Topic") ){

					topicList.add(inputArray[2]);
					Server.publishData(socket, topicList);

				}else if(inputArray[0].trim().compareTo("Pub")==0  && topicList.contains(inputArray[1])){

					publishData((ArrayList<Socket>) topicswiseSubs.get(inputArray[1]), inputArray[1] + "-" +inputArray[2]);

				}else if(inputArray[0].trim().compareTo("Sub")==0 && topicList.contains(inputArray[1])){

					if(topicswiseSubs.get(inputArray[1])!=null) {
						ArrayList<Socket> temp = new ArrayList<Socket>(topicswiseSubs.get(inputArray[1]));
						temp.add(socket);
						topicswiseSubs.put(inputArray[1], temp);
					}else{
						ArrayList<Socket> temp = new ArrayList<Socket>();
						temp.add(socket);
						topicswiseSubs.put(inputArray[1], temp);
					}

				}
				
//				if(inputData.compareTo("Topic") == 0) {
//					Server.publishData(socket, topicList);
//				}
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
		
		}
		
	}


	public static synchronized void publishData(ArrayList<Socket> subs, String data) {
		try {
			for (Socket socket : subs) {

					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF(String.valueOf(data));
					dataOutputStream.flush();

			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
