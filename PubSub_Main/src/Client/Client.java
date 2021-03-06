package Client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.Scanner;


public class Client{
    static boolean flag = false;
    static Logger logger = Logger.getLogger("debugLogger");
    //    static RSA_Signature gk = new RSA_Signature(2048);
    static RSA_Signature gk;

    static {
        try {
            gk = new RSA_Signature(2048);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    public  static void main(String args[]) throws InterruptedException {
        PropertyConfigurator.configure(Client.class.getResourceAsStream("log4j.info"));

        new Thread() {

            @Override
            public void run() {
                String publicKey;
                try {

                    while (true) {
                        Scanner scanner = new Scanner(System.in);
                        System.out.println("To connect type Connect and hit enter ");
                        String data = scanner.nextLine();
                        System.out.println("Enter name of ClientID you want to give to this client");
                        String clientID = scanner.nextLine();

                        Socket socket = new Socket("10.142.0.2", 6000);
//                         Socket socket = new Socket("localhost", 6000);

                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        if (data.equalsIgnoreCase("Connect")) {
                            data = "10#12#0004#MQTT#0004#" + clientID;

                            dataOutputStream.writeUTF(data);
                            dataOutputStream.flush();

                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                            String ack = dataInputStream.readUTF();

                            if (ack.equals("20")) {
                                Socket subSocket = new Socket("10.142.0.2", 6000);
//                                 Socket subSocket = new Socket("localhost", 6000);

                                System.out.println(subSocket.getLocalAddress());
                                System.out.println(subSocket.getInetAddress());

                                System.out.println(subSocket.getPort());
                                System.out.println(subSocket.getLocalPort());

                                DataOutputStream subOutputStream = new DataOutputStream(subSocket.getOutputStream());
                                DataInputStream subInputStream = new DataInputStream(subSocket.getInputStream());

                                publicKey = onInit(subOutputStream, subInputStream);
                                SubscribeListener subscribeListener = new SubscribeListener(subSocket, logger);

                                subscribeListener.setDataInputStream(subInputStream);
                                subscribeListener.setDataOutputStream(subOutputStream);

                                if (!subscribeListener.isAlive()) {
                                    subscribeListener.start();
                                }

                                subscribeListener.setLogger(logger);

                                System.out.println("Your are now connected; Below are your options");


                                while (true) {


                                    System.out.println("To Publish a new topic enter 1");
                                    System.out.println("To Publish a data for some topic enter 2");
                                    System.out.println("To get the list of Topic to subscribe enter 3");
                                    System.out.println("To Unsubscribe to Topics enter 4");
                                    System.out.println("To Reset enter 0");
                                    String options = scanner.nextLine();

                                    //Publish name of the Topic
                                    if (options.equalsIgnoreCase("1")) {
                                        System.out.println("Enter the name of the topic to be published");
                                        String topic = scanner.nextLine();
                                        String publishPacket = "33#12#0007#" + topic.trim();

                                        dataOutputStream.writeUTF(publishPacket);
                                        dataOutputStream.flush();
//                                              clientListener.setFlag(true);
//                                              clientListener.start();

//                                              wait();
//                                              notify();
                                        String[] pubAck = dataInputStream.readUTF().trim().split("#");
//
                                        if (pubAck[0].equalsIgnoreCase("36")) {
                                            System.out.println("PubAck: Topic added in the Topic list successfully");
                                        }

                                    }

                                    //Publish data for a topic
                                    if (options.equalsIgnoreCase("2")) {
                                        System.out.println("Enter the name of the topic and the data or info to be published for this Topic:");
                                        String topic = scanner.nextLine();
                                        String info = scanner.nextLine();

                                        String publishPacket = "30#12#0007#" + topic + "#" + info;
                                        String signature = "";

                                        signature = gk.sign(info, gk);

                                        publishPacket += "#" + signature + "#" + publicKey;

                                        dataOutputStream.writeUTF(publishPacket);
                                        dataOutputStream.flush();
//                                              clientListener.setFlag(true);
//                                              clientListener.start();

//                                              wait();
//                                              notify();
                                        String[] pubAck = dataInputStream.readUTF().trim().split("#");
//
                                        if (pubAck[0].equalsIgnoreCase("40")) {
                                            System.out.println("PubAck: Message got Published successfully");
                                        }else if(pubAck[0].equalsIgnoreCase("43")){
                                            logger.info("Invalid Public Key. You have been rejected.");
                                        }
                                        else if (pubAck[0].equalsIgnoreCase("99")) {
                                            System.out.println(pubAck[1]);
                                        } else if (pubAck[0].equalsIgnoreCase("44")) {
                                            System.out.println("Added the Topic in the Topic Pool; But data was not published being a new topic; Republish the dat using Option 2");
                                        }


                                    }

                                    //Get list of available Topics and Subscribe
                                    if (options.equalsIgnoreCase("3")) {
                                        dataOutputStream.writeUTF("TopicList");
                                        dataOutputStream.flush();
                                        String[] s = dataInputStream.readUTF().trim().split("#");
                                        if (s.length==1){
                                            System.out.println("No topic present as of now");
                                            continue;
                                        }
                                        System.out.println(s[1]);

                                        System.out.println("Enter the names of the topic to Subscribe to separated by comma");
                                        String topicList = scanner.nextLine();

                                        String subPacket = "82#0D#0001#" + topicList + "#00";

                                        subOutputStream.writeUTF(subPacket);
                                        subOutputStream.flush();

                                        subscribeListener.setDataInputStream(subInputStream);
                                        subscribeListener.setLogger(logger);
                                        subscribeListener.setDataOutputStream(subOutputStream);

                                        if (!subscribeListener.isAlive()) {
                                            subscribeListener.start();
                                        }
                                    }

                                    //Un subscribe to topics
                                    if (options.equalsIgnoreCase("4")) {

                                        System.out.println("Enter topic names separated by comma to unsubscribe");
                                        String unsubList = scanner.nextLine();

                                        String publishPacket = "83#12#0007#"+unsubList;

                                        subOutputStream.writeUTF(publishPacket);
                                        subOutputStream.flush();
                                    }

                                    //Reset
                                    if (options.equalsIgnoreCase("0")) {

                                        String publishPacket = "14#12#0007#";

                                        subOutputStream.writeUTF(publishPacket);
                                        subOutputStream.flush();


                                    }

                                    //disconnect from network
                                    if (options.equalsIgnoreCase("007")) {

                                        //sockets closing
                                        try {
                                            subSocket.close();
                                            Thread.sleep(1000);

                                            socket.close();
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        } finally {
                                            socket.close();
                                        }
                                        break;

                                    }

                                }


                            } else socket.close();

                        } else socket.close();

                    }

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();

    }

    //Prints Backlog
    private static synchronized String onInit(DataOutputStream subOutputStream, DataInputStream subInputStream) {
        logger.info("Inside Init");
        String publicKey = "";
        try {
            gk.createKeys(); // generate one set of keys for each node
            publicKey = gk.storePublicKey(gk.getPublicKey());
            subOutputStream.writeUTF("00#" + publicKey);
            subOutputStream.flush();
            String initAck = subInputStream.readUTF();
            if (initAck.equals("null"))
            {
                return publicKey;
            }
            else
            {
                logger.debug("Backlog Data: ");
                String [] bklg = initAck.split("##"); // missed_stuff#data1##data2##etc
                int len = bklg.length;
                String [] stuff;
                String topic;
                for(int i = 0; i<len; i++)
                {
                    stuff = bklg[i].split("-");
                    topic = stuff[0];
                    stuff = stuff[1].split("~~~~");

                    if(gk.verifySignature(stuff[0], stuff[1], gk.loadPublicKey(stuff[2])))
                    {
                        logger.info(topic + ": "+ stuff[0]);
                        System.out.println(topic + ": "+ stuff[0]);
                    }
                    else
                    {
                        logger.info("The data was modified!");
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return publicKey;
    }

}
