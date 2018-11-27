package Server;

import java.net.Socket;
import java.util.*;

public class TopicDAO {

    static Set<String> topicList= new HashSet<>();
    Socket socket;
    private static HashMap<String, HashSet<Socket>> topicswiseSubs = new HashMap<String, HashSet<Socket>>();
    private static HashSet<String> topicForSubs = new HashSet<>();
    private static ArrayList<String> ipList = new ArrayList<>();
    private static HashMap<String, List<String>> IP_TopicMap = new HashMap<>();
    private static HashMap<String, String> backlog = new HashMap<>();


    public static Set<String> getTopicList() {
        return topicList;
    }

    public static void setTopicList(Set<String> topicList) {
        TopicDAO.topicList = topicList;
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

    public static HashMap<String, String> getBacklog() {
        return backlog;
    }

    public static void setBacklog(HashMap<String, String> backlog) {
        TopicDAO.backlog = backlog;
    }
}
