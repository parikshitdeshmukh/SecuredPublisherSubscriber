package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
    static boolean flag = false;

    public  static void main(String args[]) throws InterruptedException {
        new Thread() {

            @Override
            public void run() {
                try {

                    while (true) {
                        Scanner scanner = new Scanner(System.in);
                        System.out.println("To connect type Connect and hit enter ");
                        String data = scanner.nextLine();
                        System.out.println("Enter name of ClientID you want to give to this client");
                        String clientID = scanner.nextLine();

                        Socket socket = new Socket("10.142.0.2", 6000);
//						Socket socket = new Socket("localhost", 6000);

                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                        if (data.equalsIgnoreCase("Connect")) {
                            data = "10#12#0004#MQTT#0004#" + clientID;

                            dataOutputStream.writeUTF(data);
                            dataOutputStream.flush();

                            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                            String ack = dataInputStream.readUTF();

                            if (ack.equals("20")) {
                                Socket subSocket = new Socket("10.142.0.2", 6000);
//								Socket subSocket = new Socket("localhost", 6000);

                                System.out.println(subSocket.getLocalAddress());
                                System.out.println(subSocket.getInetAddress());

                                System.out.println(subSocket.getPort());
                                System.out.println(subSocket.getLocalPort());

                                DataOutputStream subOutputStream = new DataOutputStream(subSocket.getOutputStream());

                                DataInputStream subInputStream = new DataInputStream(subSocket.getInputStream());
                                SubscribeListener subscribeListener = new SubscribeListener(subSocket);
                                subscribeListener.setDataInputStream(subInputStream);
//								subscribeListener.setDataOutputStream(subOutputStream);

                                onInit(subOutputStream, subInputStream);
                                subscribeListener.start();

                                System.out.println("Your are now connected; Below are your options");

                                while (true) {

                                    System.out.println("To Publish a new topic enter 1");
                                    System.out.println("To Publish a data for some topic enter 2");
                                    System.out.println("To get the list of Topic to subscribe enter 3");
                                    System.out.println("To Unsubscribe to Topics enter 4");
                                    System.out.println("To Reset enter 0");
                                    String options = scanner.nextLine();

                                    if (options.equalsIgnoreCase("1")) {
                                        System.out.println("Enter the name of the topic to be published");
                                        String topic = scanner.nextLine();
//										System.out.println("Enter the data or info to be published for this Topic: ");
//										String info = scanner.nextLine();

                                        String publishPacket = "33#12#0007#" + topic.trim();

                                        dataOutputStream.writeUTF(publishPacket);
                                        dataOutputStream.flush();
//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
                                        String[] pubAck = dataInputStream.readUTF().trim().split("#");
//
                                        if (pubAck[0].equalsIgnoreCase("36")) {
                                            System.out.println("PubAck: Topic added in the Topic list successfully");
                                        }

                                    }

                                    if (options.equalsIgnoreCase("2")) {
                                        System.out.println("Enter the name of the topic and the data or info to be published for this Topic:");
                                        String topic = scanner.nextLine();
//										System.out.println("Enter the data or info to be published for this Topic: ");
                                        String info = scanner.nextLine();

                                        String publishPacket = "30#12#0007#" + topic + "#" + info;

                                        dataOutputStream.writeUTF(publishPacket);
                                        dataOutputStream.flush();
//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
                                        String[] pubAck = dataInputStream.readUTF().trim().split("#");
//
                                        if (pubAck[0].equalsIgnoreCase("40")) {
                                            System.out.println("PubAck: Message got Published successfully");
                                        } else if (pubAck[0].equalsIgnoreCase("99")) {
                                            System.out.println(pubAck[1]);
                                        }else if (pubAck[0].equalsIgnoreCase("44")){
                                            System.out.println("Added the Topic in the Topic Pool; But data was not published being a new topic; Republish the dat using Option 2");
                                        }

                                    }

                                    if (options.equalsIgnoreCase("3")) {
                                        dataOutputStream.writeUTF("TopicList");
                                        dataOutputStream.flush();
                                        String[] s = dataInputStream.readUTF().trim().split("#");
                                        if (s.length==1){
                                            System.out.println("No topic present as of now");
                                            continue;
                                        }
                                        System.out.println(s[1]);
//												wait();
//												notify();
//												Thread.sleep(2000);
//												yield();

                                        System.out.println("Enter the names of the topic to Subscribe to separated by comma");
                                        String topicList = scanner.nextLine();

                                        String subPacket = "82#0D#0001#" + topicList + "#00";

                                        subOutputStream.writeUTF(subPacket);
                                        subOutputStream.flush();
                                        if (!subscribeListener.isAlive()) {
                                            subscribeListener.start();
                                        }
//										subscribeListener.setDataOutputStream(subOutputStream);


//												if (dataInputStream.readUTF().trim().equalsIgnoreCase("90")) {
//													System.out.println("SubAck: Subscribed to Topics successfully");
//												}

                                    }
                                    if (options.equalsIgnoreCase("4")) {

                                        System.out.println("Enter topic names separated by comma to unsubscribe");
                                        String unsubList = scanner.nextLine();

                                        String publishPacket = "83#12#0007#"+unsubList;

                                        subOutputStream.writeUTF(publishPacket);
                                        subOutputStream.flush();


//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
//										String[] disAck = dataInputStream.readUTF().trim().split("#");
//										if (disAck[0].equalsIgnoreCase("144")){
//                                            System.out.println("Client Disconnected!");
//                                        }


                                    }
                                    if (options.equalsIgnoreCase("0")) {

                                        String publishPacket = "14#12#0007#";

                                        subOutputStream.writeUTF(publishPacket);
                                        subOutputStream.flush();



//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
//										String[] disAck = dataInputStream.readUTF().trim().split("#");
//										if (disAck[0].equalsIgnoreCase("144")){
//                                            System.out.println("Client Disconnected!");
//                                        }

                                        //Socket closing

//										try {
//											subSocket.close();
//											Thread.sleep(1000);
//
//											socket.close();
//											Thread.sleep(1000);
//										} catch (InterruptedException e) {
//											e.printStackTrace();
//										} finally {
//											socket.close();
//										}
//										break;

                                    }


                                }
//								socket.close();


                            } else socket.close();


                            //else disconnect
                        } else socket.close();


                    }

                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }.start();

    }

    private static synchronized void onInit(DataOutputStream subOutputStream, DataInputStream subInputStream) {
        System.out.println("Inside Init");

        try {
            subOutputStream.writeUTF("00#");
            subOutputStream.flush();
//			subOutputStream.close();
            String initAck = subInputStream.readUTF();
            if (initAck.equals("null")){
                return;
            }else {
                System.out.println(initAck);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
