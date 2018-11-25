import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class Client2 {

    public static void main(String...args){

        HashMap<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();

        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        map.put(1, list);
        list.add(4);
        map.put(2, list);

        Iterator itr = map.keySet().iterator();
        while (itr.hasNext()){
            Integer k = (Integer) itr.next();
            if (map.get(k).contains(2)){
                ArrayList<Integer> l = new ArrayList<>(map.get(k));
                l.remove(2);
                itr.remove();
                map.put(k, l);
            }
        }

        for (Map.Entry<Integer, List<Integer>> e: map.entrySet()){
            if (e.getValue().contains(2)){

                map.remove(e.getKey());
            }
        }

        System.out.println(map);

    }

}
