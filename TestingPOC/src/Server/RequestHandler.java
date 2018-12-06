package Server;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class RequestHandler extends Thread{
//	static Set<String> topicList= TopicDAO.getTopicList();

    //testing
    static Set<String> topicList= new HashSet<>();
//	static {
//		topicList.add("test1");
//		topicList.add("test2");
//	}

    static boolean flag = false;
    static Logger logger = Logger.getLogger(RequestHandler.class);
    Socket socket;
    ArrayList<String> ipList = TopicDAO.getIpList();
    private static HashMap<String, HashSet<Socket>> topicswiseSubs = TopicDAO.getTopicswiseSubs();
    private static HashMap<String, List<String>> IP_TopicMap = TopicDAO.getIP_TopicMap();
    private static HashSet<String> topicForSubs = new HashSet<>();
    private static HashMap<String, ArrayList<String>> backlog = new HashMap<>();
    private static HashSet<String> allUpList = TopicDAO.getAllUpList();
    private static List<String> lostData = TopicDAO.getLostData();
    private static ArrayList<String> thisBacklog = TopicDAO.getThisBacklog();
//    private static HashMap<String, String> keyMap HashMap<> ();
    private static HashMap<String, String> keyMap = TopicDAO.getKeyMap();



//	static {
//		try {
//			backlog = TopicDAO.getBacklog(socket.getInetAddress().getHostAddress());
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

    public RequestHandler(Socket socket) {
        // TODO Auto-generated constructor stub
        this.socket= socket;
    }

    @Override
    public void run() {
//
//        try {
//            backlog = TopicDAO.getBacklog(socket.getInetAddress().getHostAddress());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        PropertyConfigurator.configure(RequestHandler.class.getResourceAsStream("log4j.info"));
        TopicDAO.setLogger(logger);

        logger.info("Req Handler Started");
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;

        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            while(true) {
                try {
                    logger.info("Inside the server");
                    String inputData = dataInputStream.readUTF();

//				logger.info("Server Received: " + inputData);
                    String[] dataArr = inputData.split("#");

                    if (dataArr[0].equalsIgnoreCase("00")) {

                        // inserting IPAddress and publicKey of new publisher into keyMap
//                        keyMap.put(socket.getInetAddress().getHostAddress(), dataArr[1]);
//                        System.out.println(keyMap);

                        keyMap = TopicDAO.getKeyMap();
                        keyMap.put(socket.getInetAddress().getHostAddress(), dataArr[1]);

                        TopicDAO.setKeyMap(keyMap);

                        logger.info(ipList);
                        System.out.println(IP_TopicMap);
                        System.out.println("Inside backlog fetcher before Map");

                        if (IP_TopicMap.containsKey(socket.getInetAddress().getHostAddress())) {

                            System.out.println("inside backlog fetcher in Server");
                            thisBacklog = TopicDAO.getBacklog(socket.getInetAddress().getHostAddress());
                            StringBuffer sb = new StringBuffer();

                            for (String s: thisBacklog){
                                sb.append(s);
                                sb.append("##");
                            }

                            //Sending Backlog
                            dataOutputStream.writeUTF(sb.toString());
                            dataOutputStream.flush();
                            Thread.sleep(500);
                            backlog.remove(socket.getInetAddress().getHostAddress());
                            TopicDAO.removeBacklog(socket.getInetAddress().getHostAddress());

                            //Setting up last snapshot

                            for (String s : IP_TopicMap.get(socket.getInetAddress().getHostAddress())) {
                                HashSet<Socket> sockList;
                                HashSet<Socket> temp;

                                if (topicswiseSubs.containsKey(s)) {

                                    sockList = new HashSet<>(topicswiseSubs.get(s));
                                    temp = new HashSet<>(sockList);
                                    //Removing the Old sockets those were crash closed for the same node IP address
                                    //Using one more HashSet temp to avoid COncurrent Modification exception while modifying elements
                                    for (Socket sock: sockList){
                                        if (sock.getInetAddress().getHostAddress().equals(socket.getInetAddress().getHostAddress())){
//                                            sockList.remove(sock);
                                            temp.remove(sock);
                                        }
                                    }
                                    temp.add(socket);
                                    //local Update
                                    topicswiseSubs.put(s, temp);
                                } else {
                                    sockList = new HashSet<>();
                                    sockList.add(socket);
                                    //Local update
                                    topicswiseSubs.put(s, sockList);
                                }
                                System.out.println("snapshot "+ topicswiseSubs);
                            }

                            logger.info("After sending backlog to the reborn client: "+ backlog);
                            System.out.println("After sending backlog to the reborn client: "+ backlog);
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
                        logger.info(socket.getLocalAddress());
                        //IP adress of the client
                        logger.info(socket.getInetAddress());

                        //Port of the client
                        logger.info(socket.getPort());
                        //Port of this server
                        logger.info(socket.getLocalPort());

                        dataOutputStream.writeUTF("20");
                        dataOutputStream.flush();

                    }

                    if (dataArr[0].equalsIgnoreCase("30") && topicswiseSubs.containsKey(dataArr[3])) {
//
//                        dataInputStream.close();
//                        dataOutputStream.flush();
//                        dataOutputStream.close();

                        topicswiseSubs =  TopicDAO.getTopicswiseSubs();
                        logger.info("Inside Publisher "+ dataArr[3] + "-" + dataArr[4]);
                        System.out.println("Inside Publisher "+ dataArr[3] + "-" + dataArr[4]);
                        lostData = TopicDAO.getLostData();
                        keyMap = TopicDAO.getKeyMap();

//                        System.out.println("KeyMap, :" + keyMap);
//                        System.out.println("DataArr[6]: " + dataArr[6]);
//                        System.out.println("IP: "+ socket.getInetAddress().getHostAddress());
//                        System.out.println("map lookup:"+keyMap.get(socket.getInetAddress().getHostAddress()));
                        // now we compare the publicKey stored in keyMap with the one appended to the msg
                        if (dataArr[6].equals(keyMap.get(socket.getInetAddress().getHostAddress())))
                        {
                            publishData(topicswiseSubs, dataArr[3], dataArr[3] + "-" + dataArr[4] + "~~~~" + dataArr[5] + "~~~~" + dataArr[6], (ArrayList<String>) lostData);
                            dataOutputStream.writeUTF("40");
                            dataOutputStream.flush();
                        }

                        else
                        {
                            System.out.println("Invalid Public Key. You have been rejected.");
                            dataOutputStream.writeUTF("43");
                            dataOutputStream.flush();
                        }

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
                        logger.info("Inside server Topic List");

                        StringBuffer topics = new StringBuffer();
                        for (String s : topicList) {
                            topics.append(s + ",");
                        }
                        logger.info("TopicsList as a String to be sent back: "+topics.toString());
                        if (topics.length()==0){
                            dataOutputStream.writeUTF("TL#");


                        }else
                            dataOutputStream.writeUTF("TL#" + topics.toString().substring(0, topics.length()-1));


                        dataOutputStream.flush();


                    }

                    if (dataArr[0].equalsIgnoreCase("AllUp")){
                        allUpList = TopicDAO.getAllUpList();
                        dataOutputStream.writeUTF(String.valueOf(allUpList.size()));
                        dataOutputStream.flush();
                    }

                    if (dataArr[0].equalsIgnoreCase("82")) {
                        String[] t = dataArr[3].split(",");

                        ipList.add(socket.getInetAddress().getHostAddress());
                        TopicDAO.setIpList(ipList);
                        logger.info(ipList);
                        logger.info(socket.getInetAddress().getHostAddress());
                        logger.info(socket.getInetAddress().getHostAddress());
                        allUpList.add(socket.getInetAddress().getHostAddress());


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
                            if (IP_TopicMap.containsKey(socket.getInetAddress().getHostAddress())){

                                temp = (ArrayList<String>) IP_TopicMap.get(socket.getInetAddress().getHostAddress());
                            }
                            temp.add(s);
                            IP_TopicMap.put(socket.getInetAddress().getHostAddress(), temp );

                        }


                        //global update
                        TopicDAO.setIP_TopicMap(IP_TopicMap);
                        TopicDAO.setTopicswiseSubs(topicswiseSubs);
                        logger.info(topicswiseSubs);
                        System.out.println(topicswiseSubs);
                        TopicDAO.setAllUpList(allUpList);

                        dataOutputStream.writeUTF("90");
                        dataOutputStream.flush();


                    }

                    if (dataArr[0].equalsIgnoreCase("83")) {
                        String[] t = dataArr[3].split(",");

//						ipList.add(socket.getInetAddress().getHostName());
//						TopicDAO.setIpList(ipList);
                        logger.info(ipList);
                        logger.info(socket.getInetAddress().getHostAddress());
                        logger.info(socket.getInetAddress().getHostAddress());

                        for (String s : t) {
                            HashSet<Socket> l = new HashSet<>(topicswiseSubs.get(s));
                            l.remove(socket);
                            topicswiseSubs.put(s, l);

                            ArrayList<String> temp = new ArrayList<>();
                            if (IP_TopicMap.containsKey(socket.getInetAddress().getHostAddress())){

                                temp = (ArrayList<String>) IP_TopicMap.get(socket.getInetAddress().getHostAddress());
                            }
                            temp.remove(s);
                            if (temp.size()==0){
                                IP_TopicMap.remove(socket.getInetAddress().getHostAddress());
                            }else {
                                IP_TopicMap.put(socket.getInetAddress().getHostAddress(), temp);
                            }

                        }

                        //global update
                        TopicDAO.setIP_TopicMap(IP_TopicMap);
                        TopicDAO.setTopicswiseSubs(topicswiseSubs);
                        logger.info(topicswiseSubs);
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

                        IP_TopicMap.remove(socket.getInetAddress().getHostAddress());

                        ipList.remove(socket.getInetAddress().getHostAddress());
                        TopicDAO.setIpList(ipList);


                        //global update
                        TopicDAO.setIP_TopicMap(IP_TopicMap);
                        TopicDAO.setTopicswiseSubs(topicswiseSubs);
                        logger.info(topicswiseSubs);
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
                } catch (SQLException e) {
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


    public static synchronized void publishData(HashMap<String, HashSet<Socket>> topicswiseSubs, String topic, String data, ArrayList<String> lostData) {
        String previous="";
        try {
            logger.info(topicswiseSubs);
            System.out.println("Trying to publish from loop "+ topic+"--");

            for (Socket socket : topicswiseSubs.get(topic)) {
                ArrayList<String> s = new ArrayList<>();
                previous = data;
                //If the node crashes, storing the missed data against it's IP
                if (socket.isClosed()){
                    System.out.println("Inside socket close backlog");
                    logger.info("Inside socket close backlog");
                    logger.info(backlog);
//                    if (backlog.containsKey(socket.getInetAddress().getHostAddress())){
//                         s = backlog.get(socket.getInetAddress().getHostAddress());
//                        s.add(data);
//
//                        backlog.put(socket.getInetAddress().getHostAddress(), s);
//
//                    }else {
                    s.add(data);
                    backlog.put(socket.getInetAddress().getHostAddress(), s);
//                    }
                    System.out.println("Backlog After update: "+backlog);
                    logger.info(backlog);

//						HashSet<Socket> socks = topicswiseSubs.get(topic);
//						socks.remove(socket);
//						topicswiseSubs.put(topic,socks );
//						TopicDAO.setTopicswiseSubs(topicswiseSubs);
                    continue;
                }
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());


                try {

                    dataOutputStream.writeUTF("99#"+String.valueOf(data));
                    dataOutputStream.flush();
                    Thread.sleep(1000);
                    System.out.println("Sent ");
//						String ack = dataInputStream.readUTF();
//                    System.out.println(ack);
//						if (ack==null) throw new Exception();

                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("Inside socket close Exception backlog" );

                    logger.info("Inside DataOutpout Failure ");
                    logger.info("Backlog: "+backlog);
//                    if (backlog.containsKey(socket.getInetAddress().getHostAddress())){
//                        s = backlog.get(socket.getInetAddress().getHostAddress());
//                        s.add(data);
//
//                        backlog.put(socket.getInetAddress().getHostAddress(), s);
//
//                    }else {
                    s.add(data);
                    backlog.put(socket.getInetAddress().getHostAddress(), s);
//                    }
                    logger.info(backlog);

                }finally {
                    dataOutputStream.flush();

                }
//					DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
//					if (!dataInputStream.readUTF().equalsIgnoreCase("puback")){
//						return "44";
//					}
            }

            if (!backlog.isEmpty()) {
                if (TopicDAO.setBacklog(backlog) == true) {
                    Thread.sleep(1000);
                    System.out.println("Returned True by DB insert");

                }
            }


        } catch(IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
