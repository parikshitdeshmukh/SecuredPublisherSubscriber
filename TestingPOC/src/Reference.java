import java.util.Set;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
public class Reference  {

        Set<String> nodesList = new TreeSet<String>();
        Map<String, String[]> nodesHashes= new HashMap<String, String[]>();
        RingArrayList<String> nodesArrList;
        //    ArrayList<String> sibligs;
        String portStr="";
        String insertNode="";
        boolean flag=false;


        static final String REMOTE_PORT0 = "11108";
        static final String REMOTE_PORT1 = "11112";
        static final String REMOTE_PORT2 = "11116";
        static final String REMOTE_PORT3 = "11120";
        static final String REMOTE_PORT4 = "11124";
        static final int SERVER_PORT = 10000;

        public void createNodesList() throws NoSuchAlgorithmException {
            nodesList.add(genHash("5554"));
            nodesHashes.put(genHash("5554"),new String[]{"5554","11108"});

            nodesList.add(genHash("5556"));
            nodesHashes.put(genHash("5556"), new String[]{"5556", "11112"});

            nodesList.add(genHash("5558"));
            nodesHashes.put(genHash("5558"), new String[]{"5558", "11116"});

            nodesList.add(genHash("5560"));
            nodesHashes.put(genHash("5560"), new String[]{"5560", "11120"});

            nodesList.add(genHash("5562"));
            nodesHashes.put(genHash("5562"),new String[]{"5562", "11124"});

            nodesArrList= new RingArrayList<String>(nodesList);

            Log.d("Create", "ArrayList : " + nodesArrList.toString());

        }



        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            // TODO Auto-generated method stub

            if (selection.equals("all")){
                Log.d("MyDelete"," GOnna Delete for wiping");


                File[] filesList = getContext().getFilesDir().listFiles();

                if(filesList != null){
                    for (File file : filesList) {

                        Log.d("MyDelete", "Deleting key : "+ file.getName());
                        getContext().deleteFile(file.getName());



                    }
                }
                return 1;

            }else {
                getContext().deleteFile(selection);
                ArrayList<String> sibligsDel = new ArrayList<String>();


                for (int k = 0; k < nodesArrList.size(); k++) {

                    try {
                        if (k == 0) {

                            if (genHash(selection).compareTo(nodesArrList.get(k)) < 0 || genHash(selection).compareTo(nodesArrList.get(nodesArrList.size() - 1)) > 0) {

                                Log.d("Delete - wala@", "Inside special case for key : " + genHash(selection + " Actual port :" + selection));
                                //                                insertNode = nodesArrList.get(0);
                                //hashed values of 5554/6..
                                sibligsDel.add(nodesArrList.get(0));
                                sibligsDel.add(nodesArrList.get(k + 1));
                                sibligsDel.add(nodesArrList.get(k + 2));

                            }

                        } else {
                            if (genHash(selection).compareTo(nodesArrList.get(k)) < 0 && genHash(selection).compareTo(nodesArrList.get(k - 1)) > 0) {

                                //                                insertNode = nodesArrList.get(i);
                                Log.d("Delete - wala@", "Inside Normal case for key : " + genHash(selection + " Actual port :" + selection));

                                sibligsDel.add(nodesArrList.get(k));
                                sibligsDel.add(nodesArrList.get(k + 1));
                                sibligsDel.add(nodesArrList.get(k + 2));
                            }
                        }

                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                }

                Log.d("Delete", "This key belongs to :" + sibligsDel.toString());
                try {
                    Log.d("Delete", "The actual port of mine :" + portStr + " hash is: " + genHash(portStr));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                List<String> replicaList = new ArrayList<String>(sibligsDel);
                try {
                    replicaList.remove(genHash(portStr));
                    Log.d("Delete", "My port hash :" + genHash(portStr));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                BlockingQueue<String> blockingQDel;

                blockingQDel = new ArrayBlockingQueue<String>(1);
                Log.d("Delete", "Replica List of 2 nodes : " + replicaList.toString());

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "deleteReplica", selection, portStr, replicaList.get(0), replicaList.get(1), blockingQDel);

                try {

                    String t = blockingQDel.take();
                    Log.d("Delete", "Queues data for queryReplica: " + t);
                    if (t == "deleted")
                        return 0;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return 0;
            }
        }

        @Override
        public String getType(Uri uri) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            // TODO Auto-generated method stub

            Log.d("Insert", "Inside Insert");
            String key = (String)values.get("key");
            String value = (String) values.get("value");
            String keyhash = "";
            FileOutputStream outputStream;
            ArrayList<String> sibligs= new ArrayList<String>();

            try {
                keyhash = genHash(key);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            Log.d("Insert", "My key hash : "+ keyhash);


            for(int i=0; i<nodesArrList.size(); i++){

                if(i==0) {
                    if (keyhash.compareTo(nodesArrList.get(i)) < 0 || keyhash.compareTo(nodesArrList.get(nodesArrList.size() - 1)) > 0) {

                        Log.d("Insert", "Inside special case");
                        insertNode = nodesArrList.get(0);

                        //hashed values of 5554/6..
                        sibligs.add(insertNode);
                        sibligs.add(nodesArrList.get(i+1));
                        sibligs.add(nodesArrList.get(i+2));

                    }
                }else {
                    if (keyhash.compareTo(nodesArrList.get(i))<0 && keyhash.compareTo(nodesArrList.get(i-1))>0){

                        insertNode = nodesArrList.get(i);
                        sibligs.add(insertNode);
                        sibligs.add(nodesArrList.get(i+1));
                        sibligs.add(nodesArrList.get(i+2));
                    }
                }
            }

            Log.d("Insert","Sibligs : "+ sibligs.toString());
            Log.d("Insert","Key : "+ key);
            Log.d("Insert","value : "+ value);
            Log.d("Insert", "My port : "+portStr);

            if (portStr.equals(nodesHashes.get(sibligs.get(0))[0]) || portStr.equals(nodesHashes.get(sibligs.get(1))[0]) || portStr.equals(nodesHashes.get(sibligs.get(2))[0]) ){
                FileOutputStream outputStreamIn;
                try {

                    outputStreamIn = getContext().openFileOutput(key, Context.MODE_PRIVATE);
                    Date date = new Date();
                    long timeMilli = date.getTime();
                    String val= value+"#"+String.valueOf(timeMilli)+"#"+insertNode;
                    outputStreamIn.write(val.getBytes());
                    outputStreamIn.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> replicaList= new ArrayList<String>(sibligs);
                try {
                    replicaList.remove(genHash(portStr));
                    Log.d("Insert", "Hash of my port : "+ genHash(portStr));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                Log.d("Insert", "Replica List of 2 nodes : " + replicaList.toString());

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "replica", key, value, insertNode, replicaList.get(0), replicaList.get(1));

            }else {
                Log.d("Insert", " GOing to insert in all three : "+ sibligs.toString());
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "insertAll", key, value, sibligs.get(0),sibligs.get(1), sibligs.get(2));
            }
//
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


            return null;
        }

        @Override
        public boolean onCreate() {
            // TODO Auto-generated method stub

            try {
                createNodesList();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            //String telLineNo = tel.getLine1Number();
            portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

            try{

                ServerSocket serverSocket =  new ServerSocket(SERVER_PORT);
                new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket); //Gets it's own thread which can run indefinitely

            }catch (IOException e){
                Log.e("Socket Issue", "Not able to create Server Socket");

            }

            BlockingQueue<String> blockingQAt;
            File[] filesList = getContext().getFilesDir().listFiles();
            StringBuilder bigSB = new StringBuilder();

//        if(filesList != null){
//            for (File file : filesList) {
//                try {
//                    String line;
//                    FileInputStream in = getContext().openFileInput(file.getName());
//                    InputStreamReader in1 = new InputStreamReader(in);
//                    BufferedReader br = new BufferedReader(in1);
//                    StringBuilder sb = new StringBuilder();
//
//                    while ((line = br.readLine()) != null) {
//                        sb.append(line);
//                    }
//
//                    Log.d("@-Query", file.getName()+" "+ sb.toString());
//                    bigSB.append(file.getName()+"|"+sb.toString()+"--");
//
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

            List<String> replicaList = new ArrayList<String>(nodesArrList);
            try {
                replicaList.remove(genHash(portStr));
                Log.d("ONCreate-Query", "Replica list : "+ replicaList);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            blockingQAt = new ArrayBlockingQueue<String>(1);

            new queryOnCreate().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, replicaList, blockingQAt);

            try {

                String temp = blockingQAt.take();
                Log.d("ONCreate-Query", "ONCreate Queues data for queryReplica: " +temp);

                String bigSBString = "";

//            if (temp.equals("none") )
//                bigSBString = bigSB.substring(0, bigSB.length() - 2);
//            else {
//                bigSB.append(temp);
//                bigSBString = bigSB.toString();
//            }

                if (temp.equals("none")){
                    Log.d("ONCreate-Query", "I am first AVD :"+ portStr);

                }
                if (!temp.equals("none")) {

                    Log.d("ONCreate-Query", "Got data from others and adding in my avd");

                    bigSB.append(temp);
                    bigSBString = bigSB.toString();

                    Log.d("ONCreate-Query", "ONCreate Final BIG sb data: " + bigSB);

//                Map<String, String> keyTSMap = new HashMap<String, String>();
                    Map<String, List<String>> keyTSMap = new HashMap<String, List<String>>();
                    String[] s = bigSBString.split("--");

                    for (int i = 0; i < s.length; i++) {
                        Log.d("@-Query", "Inside for loop => s: " + s[i]);
                        String[] s1 = s[i].split("\\|");
                        String[] t = s1[1].split("#");
                        ArrayList<String> sibligsQ = new ArrayList<String>();

                        for (int k = 0; k < nodesArrList.size(); k++) {

                            if (k == 0) {
                                if (genHash(s1[0]).compareTo(nodesArrList.get(k)) < 0 || genHash(s1[0]).compareTo(nodesArrList.get(nodesArrList.size() - 1)) > 0) {

                                    Log.d("Query -ONCreate- wala", "Inside special case for key : " + genHash(s1[0] + " Actual port :" + s1[0]));
//                                insertNode = nodesArrList.get(0);
                                    //hashed values of 5554/6..
                                    sibligsQ.add(nodesArrList.get(0));
                                    sibligsQ.add(nodesArrList.get(k + 1));
                                    sibligsQ.add(nodesArrList.get(k + 2));

                                }
                            } else {
                                if (genHash(s1[0]).compareTo(nodesArrList.get(k)) < 0 && genHash(s1[0]).compareTo(nodesArrList.get(k - 1)) > 0) {

//                                insertNode = nodesArrList.get(i);
                                    Log.d("Query -ONCreate- wala@", "Inside Normal case for key : " + genHash(s1[0] + " Actual port :" + s1[0]));

                                    sibligsQ.add(nodesArrList.get(k));
                                    sibligsQ.add(nodesArrList.get(k + 1));
                                    sibligsQ.add(nodesArrList.get(k + 2));
                                }
                            }

                        }

                        Log.d("ONCreate-Query", "This key belongs to :" + sibligsQ.toString());
                        Log.d("ONCreate-Query", "The actual port of mine :" + portStr + " hash is: " + genHash(portStr));

                        if (sibligsQ.contains(genHash(portStr))) {

                            if (keyTSMap.containsKey(s1[0])) {

                                Log.d("ONCreate-Query", "Already contains the key : Printing KeyTSMAp  => : Key :" + s1[0] + "--" + keyTSMap.get(s1[0]));

                                if (Long.parseLong(t[1]) > Long.parseLong(keyTSMap.get(s1[0]).get(1))) {
                                    keyTSMap.put(s1[0], Arrays.asList(new String[]{t[0], t[1], t[2]}));
                                    Log.d("ONCreate-Query", "New Timestamp is MORE");
                                }
                            } else {
                                Log.d("ONCreate-Query", "Adding key first time to Map");
                                keyTSMap.put(s1[0], Arrays.asList(new String[]{t[0], t[1], t[2]}));
                            }


                        }

                    }

                    //**Forming cursor
                    for (Map.Entry<String, List<String>> entry : keyTSMap.entrySet()) {
                        Log.d("@-Query", "Before forming cursor ourput: Key = " + entry.getKey() + " Value = " + entry.getValue());

                        FileOutputStream outputStreamIn;

                        try {
                            outputStreamIn = getContext().openFileOutput(entry.getKey(), Context.MODE_PRIVATE);
                            String val = entry.getValue().get(0) + "#" + entry.getValue().get(1) + "#" + entry.getValue().get(2);
                            outputStreamIn.write(val.getBytes());
                            outputStreamIn.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                Thread.sleep(500);
                flag=true;

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {

            while (flag==false){
                Log.d("Query", "Waiting for AVDs to get the updates from others ");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

            // TODO Auto-generated method stub
            String key = selection;
            String keyhash = "";
            BlockingQueue<List<String>> blockingQ;
            MyList<String> values ;
            ArrayList<String> sibligs= new ArrayList<String>();
            try {
                keyhash = genHash(key);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if (selection.equals("*")){

                BlockingQueue<String> blockingQStar;
                File[] filesList = getContext().getFilesDir().listFiles();
                StringBuilder bigSB = new StringBuilder();

                if(filesList != null){
                    for (File file : filesList) {
                        try {
                            String line;
                            FileInputStream in = getContext().openFileInput(file.getName());
                            InputStreamReader in1 = new InputStreamReader(in);
                            BufferedReader br = new BufferedReader(in1);
                            StringBuilder sb = new StringBuilder();

                            while ((line = br.readLine()) != null) {
                                sb.append(line.trim());
                            }

                            Log.d("*-Query", file.getName()+" "+ sb.toString());
                            bigSB.append(file.getName()+"|"+sb.toString()+"--");


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<String> replicaList = new ArrayList<String>(nodesArrList);
                try {
                    replicaList.remove(genHash(portStr));
                    Log.d("*-Query", "Replica list : "+ replicaList);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                blockingQStar = new ArrayBlockingQueue<String>(1);

                new queryStar().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, replicaList, blockingQStar);

                try {

                    String temp = blockingQStar.take();
                    Log.d("*-Query", "* Queues data for queryReplica: " +temp);

                    bigSB.append(temp);

                    Log.d("*-Query", "* Final BIG sb data: " +bigSB);

//                Map<String, String> keyTSMap = new HashMap<String, String>();
                    Map<String, List<String>> keyTSMap = new HashMap<String, List<String>>();
                    String bigSBString = bigSB.toString();
                    String[] s= bigSBString.split("--");

                    for (int i=0; i< s.length; i++){
                        Log.d("*-Query", "Inside for loop => s: " +s[i]);
                        String[] s1= s[i].split("\\|");
                        String[] t= s1[1].split("#");

                        if (keyTSMap.containsKey(s1[0])) {

                            Log.d("*-Query", "Printing KeyTSMAp  => : Key :" + s1[0]+"--"+keyTSMap.get(s1[0]));

                        }else{
                            keyTSMap.put(s1[0], Arrays.asList(new String[]{t[0], t[1], t[2]}));
                        }
                    }


                    //**Forming cursor
                    MatrixCursor tempCur = new MatrixCursor(new String[]{"key", "value"});
                    for( Map.Entry<String,List<String>> entry : keyTSMap.entrySet()){
                        Log.d("*-Query", "Before forming cursor ourput: " + entry.getKey() + " Value : " + entry.getValue());

                        tempCur.addRow(new Object[]{entry.getKey(), entry.getValue().get(0)});

                    }

                    return tempCur;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
            else if (selection.equals("@")) {
                BlockingQueue<String> blockingQAt;
                File[] filesList = getContext().getFilesDir().listFiles();
                StringBuilder bigSB = new StringBuilder();

                if(filesList != null){
                    for (File file : filesList) {
                        try {
                            String line;
                            FileInputStream in = getContext().openFileInput(file.getName());
                            InputStreamReader in1 = new InputStreamReader(in);
                            BufferedReader br = new BufferedReader(in1);
                            StringBuilder sb = new StringBuilder();

                            while ((line = br.readLine()) != null) {
                                sb.append(line.trim());
                            }

                            Log.d("@-Query", file.getName()+" "+ sb.toString());
                            bigSB.append(file.getName()+"|"+sb.toString()+"--");


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<String> replicaList = new ArrayList<String>(nodesArrList);
                try {
                    replicaList.remove(genHash(portStr));
                    Log.d("@-Query", "Replica list : "+ replicaList);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                blockingQAt = new ArrayBlockingQueue<String>(1);

                new queryAt().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, replicaList, blockingQAt);

                try {

                    String temp = blockingQAt.take();
                    Log.d("@-Query", "@ Queues data for queryReplica: " +temp);

                    if (temp.equals("none") && bigSB.length()==0){
                        return null;
                    }
                    String bigSBString = "";

                    if (temp.equals("none") )
                        bigSBString = bigSB.substring(0, bigSB.length() - 2);
                    else {
                        bigSB.append(temp);
                        bigSBString = bigSB.toString();
                    }
                    Log.d("@-Query", "@ Final BIG sb data: " +bigSB);

//                Map<String, String> keyTSMap = new HashMap<String, String>();
                    Map<String, List<String>> keyTSMap = new HashMap<String, List<String>>();
                    String[] s= bigSBString.split("--");

                    for (int i=0; i< s.length; i++){
                        Log.d("@-Query", "Inside for loop => s: " +s[i]);
                        String[] s1= s[i].split("\\|");
                        String[] t= s1[1].split("#");
                        ArrayList<String> sibligsQ= new ArrayList<String>();

                        for(int k=0; k<nodesArrList.size(); k++){

                            if(k==0) {
                                if (genHash(s1[0]).compareTo(nodesArrList.get(k)) < 0 || genHash(s1[0]).compareTo(nodesArrList.get(nodesArrList.size() - 1)) > 0) {

                                    Log.d("Query - wala@", "Inside special case for key : "+ genHash(s1[0]+" Actual port :"+ s1[0]));
//                                insertNode = nodesArrList.get(0);
                                    //hashed values of 5554/6..
                                    sibligsQ.add(nodesArrList.get(0));
                                    sibligsQ.add(nodesArrList.get(k+1));
                                    sibligsQ.add(nodesArrList.get(k+2));

                                }
                            }else {
                                if (genHash(s1[0]).compareTo(nodesArrList.get(k))<0 && genHash(s1[0]).compareTo(nodesArrList.get(k-1))>0){

//                                insertNode = nodesArrList.get(i);
                                    Log.d("Query - wala@", "Inside Normal case for key : "+ genHash(s1[0]+" Actual port :"+ s1[0]));

                                    sibligsQ.add(nodesArrList.get(k));
                                    sibligsQ.add(nodesArrList.get(k+1));
                                    sibligsQ.add(nodesArrList.get(k+2));
                                }
                            }

                        }

                        Log.d("@-Query", "This key belongs to :" + sibligsQ.toString());
                        Log.d("@-Query", "The actual port of mine :"+ portStr + " hash is: "+ genHash(portStr));

                        if (sibligsQ.contains(genHash(portStr))){

                            if (keyTSMap.containsKey(s1[0])) {

                                Log.d("@-Query", "Already contains the key : Printing KeyTSMAp  => : Key :" + s1[0]+"--"+keyTSMap.get(s1[0]));

                                if (Long.parseLong(t[1]) > Long.parseLong(keyTSMap.get(s1[0]).get(1))) {
                                    keyTSMap.put(s1[0], Arrays.asList(new String[]{t[0], t[1], t[2]}));
                                    Log.d("@-Query","New Timestamp is MORE");
                                }
                            }else{
                                Log.d("@-Query","Adding key first time to Map");
                                keyTSMap.put(s1[0], Arrays.asList(new String[]{t[0], t[1], t[2]}));
                            }


                        }

                    }

                    //**Forming cursor
                    MatrixCursor tempCur = new MatrixCursor(new String[]{"key", "value"});
                    for( Map.Entry<String, List<String>> entry : keyTSMap.entrySet()){
                        Log.d("@-Query", "Before forming cursor ourput: Key = " + entry.getKey() + " Value = " + entry.getValue());

                        tempCur.addRow(new Object[]{entry.getKey(), entry.getValue().get(0)});

                    }

                    return tempCur;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }else {

                //**Get sibligs of the co-ordinator
                for (int i = 0; i < nodesArrList.size(); i++) {

                    if (i == 0) {
                        if (keyhash.compareTo(nodesArrList.get(i)) < 0 || keyhash.compareTo(nodesArrList.get(nodesArrList.size() - 1)) > 0) {

                            insertNode = nodesArrList.get(0);

                            //hashed values of 5554/6..
                            sibligs.add(insertNode);
                            sibligs.add(nodesArrList.get(i + 1));
                            sibligs.add(nodesArrList.get(i + 2));

                        }
                    } else {
                        if (keyhash.compareTo(nodesArrList.get(i)) < 0 && keyhash.compareTo(nodesArrList.get(i - 1)) > 0) {

                            insertNode = nodesArrList.get(i);
                            sibligs.add(insertNode);
                            sibligs.add(nodesArrList.get(i + 1));
                            sibligs.add(nodesArrList.get(i + 2));
                        }
                    }
                }

                Log.d("Query", "My Port : " + portStr);

                //**If this node is in (Co-ordinator || replicas) => query this node first and then query remaining ones
                if (portStr.compareTo(nodesHashes.get(sibligs.get(0))[0]) == 0 || portStr.compareTo(nodesHashes.get(sibligs.get(1))[0]) == 0 || portStr.compareTo(nodesHashes.get(sibligs.get(2))[0]) == 0) {
                    FileOutputStream outputStreamIn;

                    StringBuilder stringBuilder = new StringBuilder();
                    try {

                        FileInputStream input = getContext().openFileInput(key);
                        InputStreamReader input_reader = new InputStreamReader(input);
                        BufferedReader br = new BufferedReader(input_reader);
                        String line;
                        while ((line = br.readLine()) != null) {
                            stringBuilder.append(line.trim());
                        }
                        values = new MyList<String>();
                        if (stringBuilder.length()!=0) {
                            values.add(stringBuilder.toString());
                        }

                    } catch (Exception e) {

                    }

                    List<String> replicaList = new ArrayList<String>(sibligs);
                    try {
                        replicaList.remove(genHash(portStr));
                        Log.d("Query", "My port hash :" + genHash(portStr));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    blockingQ = new ArrayBlockingQueue<List<String>>(1);
                    Log.d("Query", "Replica List of 2 nodes : " + replicaList.toString());


                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "queryReplica", key, portStr, replicaList.get(0), replicaList.get(1), blockingQ);
                    //***Querying other nodes for same key


                    try {

                        List<String> t = blockingQ.take();
                        Log.d("Query", "Queues data for queryReplica: " + t);
                        String finalValue = null;
                        long TS = 0;

//                    String[] temp = t.split("--");
//                    values.add(temp[0]);
//                    values.add(temp[1]);


                        //**To compare timestamps and get the Value whose timestamp is largest
                        Log.d("Query", "Received value form Queue : " + t);
                        for (int i = 0; i < t.size(); i++) {

                            String[] objs = t.get(i).split("#");
                            Log.d("Query", "Received value form Objects : " + objs.length);


                            if (objs.length>1) {
                                if (Long.parseLong(objs[1]) > TS) {
                                    TS = Long.parseLong(objs[1]);
                                    finalValue = objs[0];
                                }
                            }

                        }
                        //**Forming cursor
                        MatrixCursor tempCur = new MatrixCursor(new String[]{"key", "value"});
                        tempCur.addRow(new Object[]{key, finalValue});
                        Log.d("Query", "Final Output : " + finalValue + " timestamp : " + TS);

                        return tempCur;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                } else {

                    blockingQ = new ArrayBlockingQueue<List<String>>(1);

                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "queryAll", key, portStr, sibligs.get(0), sibligs.get(1), sibligs.get(2), blockingQ);

                    try {

                        List<String> t = blockingQ.take();
                        Log.d("Query", "Queues data for queryAll: " + t);
                        String finalValue = null;
                        long TS = 0;
//                    String[] temp = t.split("--");
//                    values.add(temp[0]);
//                    values.add(temp[1]);
//                    values.add(temp[2]);
                        Log.d("Query", "Received value form Queue : " + t);


                        //**To compare timestamps and get the Value whose timestamp is largest
                        for (int i = 0; i < t.size(); i++) {

                            String[] objs = t.get(i).split("#");
                            Log.d("Query", " Index out of wala issue : "+ objs.length);

                            if (objs.length>1) {
                                if (Long.parseLong(objs[1]) > TS) {
                                    TS = Long.parseLong(objs[1]);
                                    finalValue = objs[0];
                                }
                            }

                        }
                        //**Forming cursor
                        MatrixCursor tempCur = new MatrixCursor(new String[]{"key", "value"});
                        tempCur.addRow(new Object[]{key, finalValue});
                        Log.d("Query", "Final Output : " + finalValue + "timestamp : " + TS);
                        return tempCur;


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }

            return null;

        }

        @Override
        public int update(Uri uri, ContentValues values, String selection,
                          String[] selectionArgs) {
            // TODO Auto-generated method stub
            return 0;
        }

        private class ServerTask  extends  AsyncTask<ServerSocket, String, Void>{

            @Override
            protected Void doInBackground(ServerSocket... sockets) {

                String inMsg;
                ServerSocket serverSocket=sockets[0];
                Socket appSocket=null;
                DataInputStream in;
                String t="";

                try {
                    while (true) {

                        appSocket = serverSocket.accept();
                        Log.d("Server Logs", "Entered the server side");

                        in = new DataInputStream(appSocket.getInputStream());
                        inMsg = in.readUTF();
                        String[] objs = inMsg.split("#");

                        Log.d("Server Logs", "Message received by server socket: And Message is " + inMsg);

                        if (objs[0].equals("replica")){
                            Log.d("Server Logs", "Inside server inserting in replica : ");
                            DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());
                            String temp = "ack";
                            out.writeUTF(temp.trim());
                            out.flush();

                            FileOutputStream outputStream;
                            outputStream = getContext().openFileOutput(objs[1], Context.MODE_PRIVATE);
                            Date date = new Date();
                            long timeMilli = date.getTime();
                            String val= objs[2]+"#"+String.valueOf(timeMilli)+"#"+objs[3];
                            outputStream.write(val.getBytes());
                            outputStream.close();

                        }

                        if (objs[0].equals("insertAll")){
                            Log.d("Server Logs", "Inside server inserting in replica for insert All : ");
                            DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());
                            String temp = "ack";
                            out.writeUTF(temp.trim());
                            out.flush();
                            FileOutputStream outputStream;
                            outputStream = getContext().openFileOutput(objs[1], Context.MODE_PRIVATE);
                            Date date = new Date();
                            long timeMilli = date.getTime();

                            String val= objs[2]+"#"+timeMilli+"#"+objs[3];
                            outputStream.write(val.getBytes());
                            outputStream.close();

                        }

                        if (objs[0].equals("deleteReplica")){
                            Log.d("ServerDeleteReplica", "Inside server deleting in replica ");

                            getContext().deleteFile(objs[1]);

                            DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());
                            String temp = "deleted";
                            out.writeUTF(temp.trim());
                            out.flush();

                        }

                        if (objs[0].equals("queryReplica")){
                            Log.d("Server Logs", "Inside server Query in replica  ");


                            File[] filesList = getContext().getFilesDir().listFiles();
                            StringBuilder stringBuilder = new StringBuilder();
                            try {

                                FileInputStream input = getContext().openFileInput(objs[1]);
                                InputStreamReader input_reader = new InputStreamReader(input);
                                BufferedReader br = new BufferedReader(input_reader);
                                String line;
                                while ((line = br.readLine()) != null) {
                                    stringBuilder.append(line.trim());
                                }

                            } catch (Exception e) {

                            }
                            Log.d("Server Logs", "Inside server Query in replica # FOund value "+ stringBuilder.toString());

                            DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());

                            out.writeUTF(stringBuilder.toString().trim());
                            out.flush();
                        }

                        if (objs[0].equals("queryAll")){
                            Log.d("Server Logs", "Inside server Query in QueryAll  ");


                            File[] filesList = getContext().getFilesDir().listFiles();
                            StringBuilder stringBuilder = new StringBuilder();
                            try {

                                FileInputStream input = getContext().openFileInput(objs[1]);
                                InputStreamReader input_reader = new InputStreamReader(input);
                                BufferedReader br = new BufferedReader(input_reader);
                                String line;
                                while ((line = br.readLine()) != null) {
                                    stringBuilder.append(line.trim());
                                }

                            } catch (Exception e) {

                            }
                            Log.d("Server Logs", "Inside server Query in Query All # Found value "+ stringBuilder.toString());

                            DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());

                            out.writeUTF(stringBuilder.toString().trim());
                            out.flush();
                        }

                        if (objs[0].equals("@")){
                            Log.d("Server Logs", "Inside server Query in QueryAll  ");

                            File[] filesList = getContext().getFilesDir().listFiles();
                            MyList<String> values= new MyList<String>();

                            if(filesList != null){
                                for (File file : filesList) {
                                    try {
                                        String line;
                                        FileInputStream fileIn = getContext().openFileInput(file.getName());
                                        InputStreamReader in1 = new InputStreamReader(fileIn);
                                        BufferedReader br = new BufferedReader(in1);
                                        StringBuilder sb = new StringBuilder();

                                        while ((line = br.readLine()) != null) {
                                            sb.append(line.trim());
                                        }

                                        Log.d("@Server-Query 4", file.getName()+" "+ sb.toString());
                                        if (file.getName()!=null & sb.length()!=0) {
                                            values.add(file.getName() + "|" + sb.toString());
                                        }

                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                            //Log.d("Server Logs", "Inside server Query in Query All # Found value "+ values.toString());

                            DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());

                            if (values.size()==0)
                                out.writeUTF("none");
                            else
                                out.writeUTF(values.toString()+"--");

                            out.flush();
                        }

                        appSocket.close();

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


                return null;
            }

            private Uri buildUri(String scheme, String authority) {
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.authority(authority);
                uriBuilder.scheme(scheme);
                return uriBuilder.build();
            }

            private final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");

        }


        private class ClientTask extends AsyncTask<Object, Void, Void>{
            @Override
            protected Void doInBackground(Object... msgs) {

                DataOutputStream out;
                DataOutputStream out1;
                DataOutputStream out2;
                DataOutputStream out3;

                Log.d(TAG, "Socket declared Client port : " + msgs[1].toString());

                String[] objs = {};

                try {

                    if (msgs[0].toString().equals("replica")) {

                        for (int i=4; i<=5; i++){

                            try {

                                Log.d("Client-Insert", "Received msgs : " + msgs);

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                        Integer.parseInt(nodesHashes.get(msgs[i].toString())[1]));
                                out = new DataOutputStream(socket.getOutputStream());

                                Log.d("Client-Insert", "In replica");

                                out.writeUTF("replica" + "#" + msgs[1].toString() + "#" + msgs[2].toString() + "#" + msgs[3].toString());

                                out.flush();

                                DataInputStream in = new DataInputStream(socket.getInputStream());

                                String toRead="";

                                if((toRead = in.readLine())!=null)
                                    toRead = toRead.trim();

                                if (toRead.equals("ack")) {
                                    socket.close();
                                }
                                socket.close();

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }

                    if (msgs[0].toString().equals("insertAll")){
                        for (int i=3; i<=5; i++){

                            try{
                                Log.d("Client-Insert", "In Insert All");

                                Log.d("Client-Insert", "Received msgs : "+ msgs);

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                        Integer.parseInt(nodesHashes.get(msgs[i].toString())[1]));
                                out = new DataOutputStream(socket.getOutputStream());



                                out.writeUTF("insertAll" + "#" + msgs[1].toString() + "#" + msgs[2].toString()+"#"+ msgs[3].toString());

                                out.flush();

                                DataInputStream in = new DataInputStream(socket.getInputStream());
                                String toRead="";
                                if (((toRead = in.readUTF()) != null)) {
                                    toRead = toRead.trim();
                                }

                                if (toRead.equals("ack")){
                                    socket.close();
                                }
                                socket.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                    }


                    if (msgs[0].toString().equals("queryReplica")) {

                        MyList<String> values = new MyList<String>();
                        Log.d("Client-Query", "Received msgs : "+ msgs);

                        for (int i=3; i<=4; i++){

                            try{
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                        Integer.parseInt(nodesHashes.get(msgs[i].toString())[1]));
                                out = new DataOutputStream(socket.getOutputStream());
                                Log.d("Client queryReplica", "My Port " + msgs[1].toString());
                                out.writeUTF("queryReplica" + "#" + msgs[1].toString());
                                out.flush();

                                DataInputStream in = new DataInputStream(socket.getInputStream());

                                String x="";
                                if (( x = in.readUTF()) != null) {
                                    values.add(x);
                                }

                                Log.d("Client queryReplica", "Inside Query replica the received value ");


                                socket.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        BlockingQueue<List<String>> blockingQ=(ArrayBlockingQueue<List<String>>)msgs[5];
                        blockingQ.put(values);


                    }

                    if (msgs[0].toString().equals("queryAll")) {
                        MyList<String> values = new MyList<String>();
                        Log.d("Client-Query", "Received msgs : "+ msgs);


                        for (int i=3; i<=5; i++){
                            try{

                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                        Integer.parseInt(nodesHashes.get(msgs[i].toString())[1]));
                                out = new DataOutputStream(socket.getOutputStream());
                                Log.d("Client queryAll", "Key to Query " + msgs[1].toString());
                                out.writeUTF("queryAll" + "#" + msgs[1].toString());
                                out.flush();

                                DataInputStream in = new DataInputStream(socket.getInputStream());

                                String x="";
                                if (( x = in.readUTF()) != null) {
                                    values.add(x.trim());
                                }

                                Log.d("Client queryAll", "Inside Query replica for QueryAll the received value ");


                                socket.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        BlockingQueue<List<String>> blockingQ=(ArrayBlockingQueue<List<String>>)msgs[6];
                        blockingQ.put(values);

                    }



                    if (msgs[0].toString().equals("deleteReplica")) {

                        String deleted="";
                        Log.d("ClientDeleteRepl", "Received msgs : "+ msgs);

                        for (int i=3; i<=4; i++){

                            try{
                                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                        Integer.parseInt(nodesHashes.get(msgs[i].toString())[1]));
                                out = new DataOutputStream(socket.getOutputStream());
                                Log.d("ClientDeleteRepl", "My Port " + msgs[1].toString());
                                out.writeUTF("deleteReplica" + "#" + msgs[1].toString());
                                out.flush();

                                DataInputStream in = new DataInputStream(socket.getInputStream());

                                String x="";
                                if (( x = in.readUTF()) != null) {
                                    deleted=x;
                                }
                                Log.d("ClientDeleteRepl", "Inside delete replica the received value " + deleted.toString());

                                socket.close();

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        BlockingQueue<String> blockingQ=(ArrayBlockingQueue<String>)msgs[5];
                        blockingQ.put(deleted);


                    }

                }catch (Exception e){
                    e.printStackTrace();

                }

                return null;
            }
        }

        private class queryOnCreate extends AsyncTask<Object, Void, Void> {
            @Override
            protected Void doInBackground(Object... msgs) {
                StringBuilder sb= new StringBuilder();
                List<String> receiverPorts= new ArrayList<String>((Collection<? extends String>) msgs[0]);

                Log.d("Client-OnCreate Query", "List of Receiver ports to send for query:" + receiverPorts);



                try {
                    for (int i=0; i<4; i++) {

                        try {
                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                    Integer.parseInt(nodesHashes.get(receiverPorts.get(i))[1]));
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());


                            out.writeUTF("@");

                            out.flush();

                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String x="";
                            if (( x = in.readUTF()) != null) {
                                x=x;
                            }
                            if (x.equals("none")) {

                            } else sb.append(x);

                            Log.d("Client-OnCreate Query", "StringBuilder: " + sb.toString());
                            socket.close();
                        }catch (Exception e){
                            Log.d("Client-OnCreate Query", "Yet to come up, Socket exception");
                        }

                    }

                    BlockingQueue<String> blockingQ=(ArrayBlockingQueue<String>)msgs[1];
                    if (sb.length()==0){
                        sb.append("none");
                        blockingQ.put(sb.toString());

                    }else {
                        blockingQ.put(sb.substring(0, sb.length() - 2));

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }

        private class queryAt extends AsyncTask<Object, Void, Void> {
            @Override
            protected Void doInBackground(Object... msgs) {
                StringBuilder sb= new StringBuilder();
                List<String> receiverPorts= new ArrayList<String>((Collection<? extends String>) msgs[0]);

                Log.d("Client-@ Query", "List of Receiver ports to send for query:" + receiverPorts);



                try {
                    for (int i=0; i<4; i++) {
                        try {

                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                    Integer.parseInt(nodesHashes.get(receiverPorts.get(i))[1]));
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());


                            out.writeUTF("@");

                            out.flush();

                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String x="";
                            if (( x = in.readUTF()) != null) {
                                x=x;
                            }
                            if (x.equals("none")) {

                            } else sb.append(x);

                            Log.d("Client-@ Query", "StringBuilder: " + sb.toString());
                            socket.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    BlockingQueue<String> blockingQ=(ArrayBlockingQueue<String>)msgs[1];
                    if (sb.length()==0){
                        sb.append("none");
                        blockingQ.put(sb.toString());

                    }else {
                        blockingQ.put(sb.substring(0, sb.length() - 2));

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }





                return null;
            }
        }

        private class queryStar extends AsyncTask<Object, Void, Void> {
            @Override
            protected Void doInBackground(Object... msgs) {
                StringBuilder sb= new StringBuilder();
                List<String> receiverPorts= new ArrayList<String>((Collection<? extends String>) msgs[0]);

                Log.d("Client-* Query", "List of Receiver ports to send for query:" + receiverPorts);



                try {
                    for (int i=0; i<4; i++) {
                        try{

                            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), // By default chosen by Android system emulator for communication and is directed to 127.0.0.1 which is actual listening ip fro host
                                    Integer.parseInt(nodesHashes.get(receiverPorts.get(i))[1]));
                            DataOutputStream out = new DataOutputStream(socket.getOutputStream());


                            out.writeUTF("@" );

                            out.flush();

                            DataInputStream in = new DataInputStream(socket.getInputStream());
                            String x="";
                            if (( x = in.readUTF()) != null) {
                                x=x;
                            }
                            sb.append(x);
                            Log.d("Client-* Query", "StringBuilder: " + sb.toString());
                            socket.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    BlockingQueue<String> blockingQ=(ArrayBlockingQueue<String>)msgs[1];
                    blockingQ.put(sb.substring(0,sb.length()-2));

                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }

        private String genHash(String input) throws NoSuchAlgorithmException {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] sha1Hash = sha1.digest(input.getBytes());
            Formatter formatter = new Formatter();
            for (byte b : sha1Hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }
}
