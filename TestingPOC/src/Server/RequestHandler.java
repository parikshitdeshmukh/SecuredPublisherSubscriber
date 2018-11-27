package Server;

import Parsers.MQTTParsers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class RequestHandler extends Thread{
	static Set<String> topicList= TopicDAO.getTopicList();
	Socket socket;
	ArrayList<String> ipList = TopicDAO.getIpList();
	private static HashMap<String, HashSet<Socket>> topicswiseSubs = TopicDAO.getTopicswiseSubs();
	private static HashMap<String, List<String>> IP_TopicMap = TopicDAO.getIP_TopicMap();
	private static HashSet<String> topicForSubs = new HashSet<>();
	private static HashMap<String, String> backlog = TopicDAO.getBacklog();
	
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
				try {
					String inputData = dataInputStream.readUTF();

//				System.out.println("Server Received: " + inputData);
					String[] dataArr = inputData.split("#");

					if (dataArr[0].equalsIgnoreCase("00")) {
						backlog = TopicDAO.getBacklog();

						System.out.println(ipList);

						if (IP_TopicMap.containsKey(socket.getInetAddress().getHostName())) {

							//Sending Backlog
							dataOutputStream.writeUTF(backlog.get(socket.getInetAddress().getHostName()));
							dataOutputStream.flush();
							Thread.sleep(500);
							backlog.remove(socket.getInetAddress().getHostName());

							//Setting up last snapshot

							for (String s : IP_TopicMap.get(socket.getInetAddress().getHostName())) {
								HashSet<Socket> sockList;
								if (topicswiseSubs.containsKey(s)) {

									sockList = new HashSet<>(topicswiseSubs.get(s));
									//Removing the Old sockets those were crash closed for the same node IP address
									for (Socket sock: sockList){
										if (sock.getInetAddress().getHostName().equals(socket.getInetAddress().getHostName())){
											sockList.remove(sock);
										}
									}
									sockList.add(socket);
									//local Update
									topicswiseSubs.put(s, sockList);
								} else {
									sockList = new HashSet<>();
									sockList.add(socket);
									//Local update
									topicswiseSubs.put(s, sockList);
								}
							}

							TopicDAO.setTopicswiseSubs(topicswiseSubs);
							TopicDAO.setBacklog(backlog);

						}else {
							dataOutputStream.writeUTF("null");
							dataOutputStream.flush();
						}


					}

					if (dataArr[0].equalsIgnoreCase("BackLog")) {

//						Iterator itr = topicswiseSubs.keySet().iterator();
//
//						while (itr.hasNext()) {
//							String k = (String) itr.next();
//							if (topicswiseSubs.get(k).contains(socket)) {
//								HashSet<Socket> l = new HashSet<>(topicswiseSubs.get(k));
//								l.remove(socket);
//								itr.remove();
//								topicswiseSubs.put(k, l);
//							}
//						}
						dataOutputStream.writeUTF("1234534567sdfghjdfghjkxcvbnertyuihvbnhuhbhn");
						dataOutputStream.flush();


					}




					if (dataArr[0].equalsIgnoreCase("10")) {

						//This server's IP address
						System.out.println(socket.getLocalAddress());
						//IP adress of the client
						System.out.println(socket.getInetAddress());

						//Port of the client
						System.out.println(socket.getPort());
						//Port of this server
						System.out.println(socket.getLocalPort());

						dataOutputStream.writeUTF("20");
						dataOutputStream.flush();

					}

					if (dataArr[0].equalsIgnoreCase("30") && topicList.contains(dataArr[3])) {

						topicswiseSubs =  TopicDAO.getTopicswiseSubs();
						publishData(topicswiseSubs, dataArr[3], dataArr[3] + "-" + dataArr[4]);
						dataOutputStream.writeUTF("40");
						dataOutputStream.flush();


					} else if (dataArr[0].equalsIgnoreCase("30")) {
						topicList.add(dataArr[3]);
						//Global Update
						TopicDAO.setTopicList(topicList);

						dataOutputStream.writeUTF("44");
						dataOutputStream.flush();
					}
					if (dataArr[0].equalsIgnoreCase("33")) {
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
					if (dataArr[0].equalsIgnoreCase("TopicList")) {
						System.out.println("Inside server Topic List");

						StringBuffer topics = new StringBuffer();
						for (String s : topicList) {
							topics.append(s + ",");
						}
						System.out.println(topics.toString());

						dataOutputStream.writeUTF("TL#" + topics.toString().substring(0, topics.length()));
						dataOutputStream.flush();


					}
					if (dataArr[0].equalsIgnoreCase("82")) {
						String[] t = dataArr[3].split(",");

						ipList.add(socket.getInetAddress().getHostName());
						TopicDAO.setIpList(ipList);
						System.out.println(ipList);
						System.out.println(socket.getInetAddress().getHostAddress());
						System.out.println(socket.getInetAddress().getHostName());

						for (String s : t) {
							HashSet<Socket> sockList;
							if (topicswiseSubs.containsKey(s)) {
								sockList = new HashSet<>(topicswiseSubs.get(s));
								sockList.add(socket);
								//local Update
								topicswiseSubs.put(s, sockList);
							} else {
								sockList = new HashSet<>();
								sockList.add(socket);
								//Local update
								topicswiseSubs.put(s, sockList);

							}

							ArrayList<String> temp = new ArrayList<>();
							if (IP_TopicMap.containsKey(socket.getInetAddress().getHostName())){

								temp = (ArrayList<String>) IP_TopicMap.get(socket.getInetAddress().getHostName());
							}
							temp.add(s);
							IP_TopicMap.put(socket.getInetAddress().getHostName(), temp );

						}


						//global update
						TopicDAO.setIP_TopicMap(IP_TopicMap);
						TopicDAO.setTopicswiseSubs(topicswiseSubs);
						System.out.println(topicswiseSubs);

						dataOutputStream.writeUTF("90");
						dataOutputStream.flush();


					}

					if (dataArr[0].equalsIgnoreCase("83")) {
						String[] t = dataArr[3].split(",");

//						ipList.add(socket.getInetAddress().getHostName());
//						TopicDAO.setIpList(ipList);
						System.out.println(ipList);
						System.out.println(socket.getInetAddress().getHostAddress());
						System.out.println(socket.getInetAddress().getHostName());

						for (String s : t) {
							HashSet<Socket> l = new HashSet<>(topicswiseSubs.get(s));
							l.remove(socket);
							topicswiseSubs.put(s, l);

							ArrayList<String> temp = new ArrayList<>();
							if (IP_TopicMap.containsKey(socket.getInetAddress().getHostName())){

								temp = (ArrayList<String>) IP_TopicMap.get(socket.getInetAddress().getHostName());
							}
							temp.remove(s);
							if (temp.size()==0){
								IP_TopicMap.remove(socket.getInetAddress().getHostName());
							}else {
								IP_TopicMap.put(socket.getInetAddress().getHostName(), temp);
							}

						}

						//global update
						TopicDAO.setIP_TopicMap(IP_TopicMap);
						TopicDAO.setTopicswiseSubs(topicswiseSubs);
						System.out.println(topicswiseSubs);

						dataOutputStream.writeUTF("93");
						dataOutputStream.flush();

					}

					if (dataArr[0].equalsIgnoreCase("14")) {

						Iterator itr = topicswiseSubs.keySet().iterator();
						while (itr.hasNext()) {
							String k = (String) itr.next();
							if (topicswiseSubs.get(k).contains(socket)) {
								HashSet<Socket> l = new HashSet<>(topicswiseSubs.get(k));
								l.remove(socket);
								itr.remove();
								topicswiseSubs.put(k, l);
							}
						}

						IP_TopicMap.remove(socket.getInetAddress().getHostName());

						ipList.remove(socket.getInetAddress().getHostName());
						TopicDAO.setIpList(ipList);


						//global update
						TopicDAO.setIP_TopicMap(IP_TopicMap);
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
				}catch (EOFException e){
//					socket.close();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				dataOutputStream.flush();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
	}


	public static synchronized void publishData(HashMap<String, HashSet<Socket>> topicswiseSubs, String topic, String data) {
		try {
			backlog = TopicDAO.getBacklog();

			for (Socket socket : topicswiseSubs.get(topic)) {
					//If the node crashes, storing the missed data against it's IP
					if (socket.isClosed()){
						System.out.println("Inside socket close backlog");
						System.out.println(backlog);
						if (backlog.containsKey(socket.getInetAddress().getHostName())){
							String s = backlog.get(socket.getInetAddress().getHostName()) + data + "#";

							backlog.put(socket.getInetAddress().getHostName(), s);

						}else {
							backlog.put(socket.getInetAddress().getHostName(), data+"#");
						}
						System.out.println(backlog);
//						HashSet<Socket> socks = topicswiseSubs.get(topic);
//						socks.remove(socket);
//						topicswiseSubs.put(topic,socks );
//						TopicDAO.setTopicswiseSubs(topicswiseSubs);
						continue;
					}
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF("99#"+String.valueOf(data));
					dataOutputStream.flush();
//					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
//					if (!dataInputStream.readUTF().equalsIgnoreCase("puback")){
//						return "44";
//					}
			}

			TopicDAO.setBacklog(backlog);

		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
