package Server;

import Parsers.MQTTParsers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class RequestHandler extends Thread{
	static Set<String> topicList= TopicDAO.getTopicList();
	Socket socket;
	private static HashMap<String, HashSet<Socket>> topicswiseSubs = TopicDAO.getTopicswiseSubs();
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
		MQTTParsers parser = new MQTTParsers();

		try {
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			
			while(true) {
				String inputData = dataInputStream.readUTF();
//				System.out.println("Server Received: " + inputData);
				String[] dataArr = inputData.split("#");

				if (dataArr[0].equalsIgnoreCase("10")){
					dataOutputStream.writeUTF("20");
					dataOutputStream.flush();

				}

				if (dataArr[0].equalsIgnoreCase("30") && topicList.contains(dataArr[3])){

					publishData((HashSet<Socket>) TopicDAO.getTopicswiseSubs().get(dataArr[3]), dataArr[3] + "-" +dataArr[4]);
					dataOutputStream.writeUTF("40");
					dataOutputStream.flush();


				}else if (dataArr[0].equalsIgnoreCase("30")){
					topicList.add(dataArr[3]);
					//Global Update
					TopicDAO.setTopicList(topicList);

					dataOutputStream.writeUTF("44");
					dataOutputStream.flush();
				}
				  if (dataArr[0].equalsIgnoreCase("33")){
					//local update
					topicList.add(dataArr[3]);
					//Global Update
					TopicDAO.setTopicList(topicList);
//					Server.publishData(socket, topicList);
					dataOutputStream.writeUTF("36");
					dataOutputStream.flush();


				}

//				else  if (dataArr[0].equalsIgnoreCase("30")){
//					//local update
//					topicList.add(dataArr[3]);
//					//Global Update
//					TopicDAO.setTopicList(topicList);
////					Server.publishData(socket, topicList);
//					dataOutputStream.writeUTF("40");
//					dataOutputStream.flush();
//
//
//				}
				if (dataArr[0].equalsIgnoreCase("TopicList")){
					System.out.println("Inside server Topic List");

					StringBuffer topics = new StringBuffer();
					for (String s: topicList){
						topics.append(s +",");
					}
					System.out.println(topics.toString());

					dataOutputStream.writeUTF("TL#"+topics.toString().substring(0,topics.length()));
					dataOutputStream.flush();


				}
				if (dataArr[0].equalsIgnoreCase("82")){
					String[] t = dataArr[3].split(",");

					for (String s:t){
						HashSet<Socket> sockList ;
						if (topicswiseSubs.containsKey(s)){
							sockList = new HashSet<>(topicswiseSubs.get(s));
							sockList.add(socket);
							//local Update
							topicswiseSubs.put(s,sockList );
						}else {
							sockList = new HashSet<>();
							sockList.add(socket);
							//Local update
							topicswiseSubs.put(s, sockList);

						}
					}

					//global update
					TopicDAO.setTopicswiseSubs(topicswiseSubs);
					System.out.println(topicswiseSubs);

					dataOutputStream.writeUTF("90");
					dataOutputStream.flush();


				}

				if (dataArr[0].equalsIgnoreCase("14")){

					Iterator itr = topicswiseSubs.keySet().iterator();
					while (itr.hasNext()){
						String k = (String) itr.next();
						if (topicswiseSubs.get(k).contains(socket)){
							HashSet<Socket> l = new HashSet<>(topicswiseSubs.get(k));
							l.remove(socket);
							itr.remove();
							topicswiseSubs.put(k, l);
						}
					}


					//global update
					TopicDAO.setTopicswiseSubs(topicswiseSubs);
					System.out.println(topicswiseSubs);
//
//					dataOutputStream.writeUTF("144");
//					dataOutputStream.flush();

				}


//				String[] inputArray = inputData.split("-");
//
//				if (inputArray[0].trim().compareTo("Pub") == 0 && inputArray[1].trim().equals("Topic") ){
//
//					topicList.add(inputArray[2]);
//					Server.publishData(socket, topicList);
//
//				}else if(inputArray[0].trim().compareTo("Pub")==0  && topicList.contains(inputArray[1])){
//
//					publishData((ArrayList<Socket>) topicswiseSubs.get(inputArray[1]), inputArray[1] + "-" +inputArray[2]);
//
//				}else if(inputArray[0].trim().compareTo("Sub")==0 && topicList.contains(inputArray[1])){
//
//					if(topicswiseSubs.get(inputArray[1])!=null) {
//						ArrayList<Socket> temp = new ArrayList<Socket>(topicswiseSubs.get(inputArray[1]));
//						temp.add(socket);
//						topicswiseSubs.put(inputArray[1], temp);
//					}else{
//						ArrayList<Socket> temp = new ArrayList<Socket>();
//						temp.add(socket);
//						topicswiseSubs.put(inputArray[1], temp);
//					}
//
//				}
				
//				if(inputData.compareTo("Topic") == 0) {
//					Server.publishData(socket, topicList);
//				}
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
		
		}
		
	}


	public static synchronized void publishData(HashSet<Socket> subs, String data) {
		try {
			for (Socket socket : subs) {

					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF("99#"+String.valueOf(data));
					dataOutputStream.flush();
//					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
//					if (!dataInputStream.readUTF().equalsIgnoreCase("puback")){
//						return "44";
//					}
			}

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
