package Server;

import org.apache.log4j.Logger;

import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class TopicDAO {

    static Set<String> topicList= new HashSet<>();
    private static Logger logger;
    Socket socket;
    private static HashMap<String, HashSet<Socket>> topicswiseSubs = new HashMap<String, HashSet<Socket>>();
    private static HashSet<String> topicForSubs = new HashSet<>();
    private static ArrayList<String> ipList = new ArrayList<>();
    private static HashMap<String, List<String>> IP_TopicMap = new HashMap<>();
    private static HashMap<String, ArrayList<String>> backlog = new HashMap<>();
    private static HashSet<String> allUpList = new HashSet<>();
    private static List<String> lostData = new ArrayList<>();
    private static ArrayList<String> thisBacklog = new ArrayList<>();
    private static HashMap<String, String> keyMap = new HashMap();
//    static {
//        DBConnectionFactory.setLogger(logger);
//    }

    public static Set<String> getTopicList() {
        return topicList;
    }

    public static void setTopicList(Set<String> topicList) {
        TopicDAO.topicList = topicList;
    }

    public static void removeBacklog(String hostName) {
        DBConnectionFactory.removeBacklog(hostName);

    }

    public static void setLogger(Logger logger) {
        TopicDAO.logger = logger;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static ArrayList<String> getThisBacklog() {
        return thisBacklog;
    }

    public static void setThisBacklog(ArrayList<String> thisBacklog) {
        TopicDAO.thisBacklog = thisBacklog;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public static HashMap<String, HashSet<Socket>> getTopicswiseSubs() {
        return topicswiseSubs;
    }

    public static void setTopicswiseSubs(HashMap<String, HashSet<Socket>> topicswiseSubs) {
        TopicDAO.topicswiseSubs = topicswiseSubs;
    }

    public static HashSet<String> getTopicForSubs() {
        return topicForSubs;
    }

    public static void setTopicForSubs(HashSet<String> topicForSubs) {
        TopicDAO.topicForSubs = topicForSubs;
    }


    public static ArrayList<String> getIpList() {
        return ipList;
    }

    public static void setIpList(ArrayList<String> ipList) {
        TopicDAO.ipList = ipList;
    }

    public static HashMap<String, List<String>> getIP_TopicMap() {
        return IP_TopicMap;
    }

    public static void setIP_TopicMap(HashMap<String, List<String>> IP_TopicMap) {
        TopicDAO.IP_TopicMap = IP_TopicMap;
    }

    public static ArrayList<String> getBacklog(String IP) throws SQLException {
        return DBConnectionFactory.getBacklog(IP);
    }

    public static synchronized boolean setBacklog(HashMap<String, ArrayList<String>> backlog) throws SQLException {

        TopicDAO.backlog = backlog;
        return DBConnectionFactory.setBacklog(backlog);
    }

    public static HashMap<String, String> getKeyMap() {
        return keyMap;
    }

    public static void setKeyMap(HashMap<String, String> keyMap) {
        keyMap = keyMap;
    }

    public static HashSet<String> getAllUpList() {
        return allUpList;
    }

    public static void setAllUpList(HashSet<String> allUpList) {
        TopicDAO.allUpList = allUpList;
    }


    public static List<String> getLostData() {
        return lostData;
    }

    public static void setLostData(List<String> lostData) {
        TopicDAO.lostData = lostData;
    }
}
