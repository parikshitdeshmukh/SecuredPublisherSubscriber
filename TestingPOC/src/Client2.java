import java.util.*;

public class Client2 {

    public static void main(String...args){



        String topics = "test1,test2,test3,";
        System.out.println(topics.substring(0, topics.length()-1));
        List<String> tl_List = new ArrayList<>(Arrays.asList(new String[]{"test3", "test2", "test1"}));

        StringBuffer topic = new StringBuffer("Test");
        topic.append(Integer.parseInt(String.valueOf(tl_List.get(0).charAt(4)))+Integer.valueOf(1));
        System.out.println(topic.toString());



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
