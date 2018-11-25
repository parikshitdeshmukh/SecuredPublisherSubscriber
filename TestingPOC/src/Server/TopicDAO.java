package Server;

import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TopicDAO {

    static Set<String> topicList= new HashSet<>();
    Socket socket;
    private static HashMap<String, HashSet<Socket>> topicswiseSubs = new HashMap<String, HashSet<Socket>>();
    private static HashSet<String> topicForSubs = new HashSet<>();


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
}
