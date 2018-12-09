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

    //testing
    static Set<String> topicList= new HashSet<>();


    static boolean flag = false;
    static Logger logger = Logger.getLogger(RequestHandler.class);
    Socket socket;

    //TopicDAO is a helper class which is used for storing the common data that would be used by different thread across teh application
    ArrayList<String> ipList = TopicDAO.getIpList();
    private static HashMap<String, HashSet<Socket>> topicswiseSubs = TopicDAO.getTopicswiseSubs();
    private static HashMap<String, List<String>> IP_TopicMap = TopicDAO.getIP_TopicMap();
    private static HashMap<String, ArrayList<String>> backlog = new HashMap<>();
    private static HashSet<String> allUpList = TopicDAO.getAllUpList();
    private static HashMap<String, String> IPWiseLostData = TopicDAO.getIPWiseLostData();
    private static ArrayList<String> thisBacklog = TopicDAO.getThisBacklog();
    private static HashMap<String, String> keyMap = TopicDAO.getKeyMap();


    //initializing connection with database to reduce the delay while fetching backlog data for first time
    static {
        try {
            boolean isConnected = TopicDAO.connectToDB();
            if (isConnected){
                System.out.println("Connected to DB Successfully");
                logger.info("Connected to DB Successfully");

            }else {
                System.out.println("Didn't connect to DB");
                logger.info("Didn't connect to DB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestHandler(Socket socket) {
        // TODO Auto-generated constructor stub
        this.socket= socket;
    }

    @Override
    public void run() {

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
                    String inputData = dataInputStream.readUTF();
                    logger.info("Inside the server");

                    String[] dataArr = inputData.split("#");

                    //Fetching backlog data
                    if (dataArr[0].equalsIgnoreCase("00")) {

                        keyMap = TopicDAO.getKeyMap();
                        keyMap.put(socket.getInetAddress().getHostAddress(), dataArr[1]);

                        TopicDAO.setKeyMap(keyMap);

                        System.out.println("Inside backlog fetcher before Map");
                        logger.info("Inside backlog fetcher before Map");

                        if (IP_TopicMap.containsKey(socket.getInetAddress().getHostAddress())) {

                            System.out.println("Inside backlog fetcher in Server");
                            logger.info("Inside backlog fetcher in Server");
                            thisBacklog = TopicDAO.getBacklog(socket.getInetAddress().getHostAddress());
                            StringBuffer sb = new StringBuffer();

                            for (String s: thisBacklog){
                                sb.append(s);
                                sb.append("##");
                            }

                            //Sending Backlog
                            dataOutputStream.writeUTF(sb.toString());
                            dataOutputStream.flush();

                            //removing the backlog from database
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
                                logger.info("Snapshot: "+ topicswiseSubs);
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

                    if (dataArr[0].equalsIgnoreCase("10")) {

                        dataOutputStream.writeUTF("20");
                        dataOutputStream.flush();

                    }

                    //Publishing data
                    if (dataArr[0].equalsIgnoreCase("30") && topicswiseSubs.containsKey(dataArr[3])) {

                        topicswiseSubs =  TopicDAO.getTopicswiseSubs();
                        logger.info("Inside Publisher "+ dataArr[3] + "-" + dataArr[4]);
                        System.out.println("Inside Publisher "+ dataArr[3] + "-" + dataArr[4]);
                        IPWiseLostData = TopicDAO.getIPWiseLostData();
                        keyMap = TopicDAO.getKeyMap();
                        if (dataArr[6].equals(keyMap.get(socket.getInetAddress().getHostAddress()))) {
                            publishData(topicswiseSubs, dataArr[3], dataArr[3] + "-" + dataArr[4] + "~~~~" + dataArr[5] + "~~~~" + dataArr[6], IPWiseLostData);

                            dataOutputStream.writeUTF("40");
                            dataOutputStream.flush();
                        }else {
                            System.out.println("Invalid Public Key. You have been rejected.");
                            logger.error("Invalid Public Key. You have been rejected.");
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

                    //Adding topic to the topic list
                    if (dataArr[0].equalsIgnoreCase("33")) {
                        //local update
                        topicList.add(dataArr[3]);
                        //Global Update
                        TopicDAO.setTopicList(topicList);
                        dataOutputStream.writeUTF("36");
                        dataOutputStream.flush();


                    }

                    //Sending list of available topics to subscriber
                    if (dataArr[0].equalsIgnoreCase("TopicList")) {

                        StringBuffer topics = new StringBuffer();
                        for (String s : topicList) {
                            topics.append(s + ",");
                        }
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

                    //Subscribe to topic
                    if (dataArr[0].equalsIgnoreCase("82")) {
                        String[] t = dataArr[3].split(",");

                        ipList.add(socket.getInetAddress().getHostAddress());
                        TopicDAO.setIpList(ipList);
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
                        logger.info("Topic wise subs: "+topicswiseSubs);
                        System.out.println("Topic wise Subs: "+topicswiseSubs);
                        TopicDAO.setAllUpList(allUpList);

                        dataOutputStream.writeUTF("90");
                        dataOutputStream.flush();


                    }

                    //Un subscribe
                    if (dataArr[0].equalsIgnoreCase("83")) {
                        String[] t = dataArr[3].split(",");
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
                        logger.info("After Unsubscribe -- Topic wise Subs: "+topicswiseSubs);
                        System.out.println("After Unsubscribe -- Topic wise Subs: "+topicswiseSubs);

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
                        logger.info("Topic wise subs afterwards: "+ topicswiseSubs);

                    }

                }catch (EOFException e){
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


    //Publisher to sockets
    public static synchronized void publishData(HashMap<String, HashSet<Socket>> topicswiseSubs, String topic, String data, HashMap<String, String> IPWiseLostData) {
        try {
            logger.info(topicswiseSubs);
            System.out.println("Trying to publish from PublishData loop "+ topic+"--");
            boolean jN=true;

            for (Socket socket : topicswiseSubs.get(topic)) {
                ArrayList<String> s = new ArrayList<>();

                //If the node crashes, storing the missed data against it's IP
                if (socket.isClosed()){
                    System.out.println("Inside socket close backlog");
                    logger.info("Inside socket close backlog");
                    s.add(data);
                    backlog.put(socket.getInetAddress().getHostAddress(), s);
                    continue;
                }

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                try {

                    dataOutputStream.writeUTF("99#" + String.valueOf(data));
                    dataOutputStream.flush();
                    System.out.println("Sent !");
                    logger.info("SENT !!");
                    IPWiseLostData.put(socket.getInetAddress().getHostAddress(), data);
                    TopicDAO.setIPWiseLostData(IPWiseLostData);

                }
                //If node crashes
                catch (Exception e){
                    e.printStackTrace();
                    System.out.println("Inside socket close Exception backlog" );
                    logger.error("Inside socket close Exception backlog ");
                    if (IPWiseLostData.get(socket.getInetAddress().getHostAddress()).length()!=0) {
                        s.add(IPWiseLostData.get(socket.getInetAddress().getHostAddress()));
                        s.add(data);
                    }
                    else s.add(data);
                    IPWiseLostData.put(socket.getInetAddress().getHostAddress(), "");
                    TopicDAO.setIPWiseLostData(IPWiseLostData);
                    backlog.put(socket.getInetAddress().getHostAddress(), s);

                }finally {
                    dataOutputStream.flush();

                }

                System.out.println("Lost Data: IP:"+ socket.getInetAddress().getHostAddress() + "Data: "+IPWiseLostData.get(socket.getInetAddress().getHostAddress()).toString().split("~~~~"));
                logger.info("Lost Data: IP:"+ socket.getInetAddress().getHostAddress() + "Data: "+IPWiseLostData.get(socket.getInetAddress().getHostAddress()).toString().split("~~~~"));

            }

            if (!backlog.isEmpty()) {
                if (TopicDAO.setBacklog(backlog) == true) {
                    System.out.println("Returned True by DB insert");

                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}