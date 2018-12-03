package Client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.*;


public class ClientTest_NodeFailure_Backlog {
	static boolean flag = false;
    static Logger logger = Logger.getLogger(ClientTest_NodeFailure_Backlog.class);


    public  static void main(String args[]) throws InterruptedException {
    	PropertyConfigurator.configure(ClientTest_NodeFailure_Backlog.class.getResourceAsStream("log4j.info"));
		new Thread() {

			@Override
			public void run() {
				try {

					while (true) {
						logger.info("started again");

						String done="";
						Scanner scanner = new Scanner(System.in);
//						logger.info("To connect type Connect and hit enter ");
//						String data = scanner.nextLine();
						String data="connect";
//						logger.info("Enter name of ClientID you want to give to this client");
//						String clientID = scanner.nextLine();
						String clientID = "a";

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

								logger.info(subSocket.getLocalAddress());
								logger.info(subSocket.getInetAddress());

								logger.info(subSocket.getPort());
								logger.info(subSocket.getLocalPort());

								DataOutputStream subOutputStream = new DataOutputStream(subSocket.getOutputStream());

								DataInputStream subInputStream = new DataInputStream(subSocket.getInputStream());
								SubscribeListener subscribeListener = new SubscribeListener(subSocket);
								subscribeListener.setDataInputStream(subInputStream);
								subscribeListener.setLogger(logger);
//								subscribeListener.setDataOutputStream(subOutputStream);

								onInit(subOutputStream, subInputStream);
								subscribeListener.start();

//								logger.info("Your are now connected; Below are your options");
                                boolean allUp = true;

								while (true) {
									String publishPacket;
									String info;
									StringBuffer topic= new StringBuffer();
									String[] pubAck;

//									logger.info("To Publish a new topic enter 1");
//									logger.info("To Publish a data for some topic enter 2");
//									logger.info("To get the list of Topic to subscribe enter 3");
//									logger.info("To Unsubscribe to Topics enter 4");
//									logger.info("To Reset enter 0");
//									String options = scanner.nextLine();
                                    String options = args[0];

                                    if (options.equalsIgnoreCase("1")) {
//										logger.info("Enter the name of the topic to be published");
//										String topic = scanner.nextLine();

                                        dataOutputStream.writeUTF("TopicList");
                                        dataOutputStream.flush();
                                        String in = dataInputStream.readUTF();
                                        logger.info("Topic List: "+ in);

                                        String[] tl = in.trim().split("#");



                                        if (tl.length==1){
                                            logger.info("No topic present as of now");
                                            topic = new StringBuffer("Test1");
                                        }else {
                                            List<String> tl_List = new ArrayList<>(Arrays.asList(tl[1].split(",")));
                                            Collections.sort(tl_List);
                                            Collections.reverse(tl_List);
                                            logger.info(tl_List);
                                            System.out.println(tl_List);
                                            topic = new StringBuffer("Test");
                                            topic.append(Integer.parseInt(String.valueOf(tl_List.get(0).charAt(4)))+Integer.valueOf(1));

                                        }

                                        if ( topic.length()>0 && Integer.parseInt(String.valueOf(topic.toString().charAt(4)))>8){
                                            logger.info("Inside Break for Test9");
                                            done = "Done";
                                            break;
                                        }

//										logger.info("Enter the data or info to be published for this Topic: ");
//										String info = scanner.nextLine();

                                        logger.info("New topic to be published: "+ topic.toString());
                                        System.out.println("New topic to be published: "+ topic.toString());


                                        publishPacket = "33#12#0007#" + topic.toString();

										dataOutputStream.writeUTF(publishPacket);
										dataOutputStream.flush();
//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
										pubAck = dataInputStream.readUTF().trim().split("#");
//
										if (pubAck[0].equalsIgnoreCase("36")) {
											logger.info("PubAck: Topic added in the Topic list successfully");
										}

//										Thread.sleep(3000);
                                        while(allUp){

                                            logger.info("Inside All UP");
                                            System.out.println("inside all up");
                                            dataOutputStream.writeUTF("AllUP#");
                                            dataOutputStream.flush();
                                            if (dataInputStream.readUTF().trim().equalsIgnoreCase("2")) {
                                                allUp=false;
                                                break;
                                            }
                                            Thread.sleep(1000);
                                        }

										for (int i=0; i<100; ) {

											 info = String.valueOf(i);

//											 publishPacket = "30#12#0007#" + topic.toString() + "#" + info;

                                             publishPacket = "30#12#0007#" + topic + "#" + info;
                                            String signature = "";

                                            RSA_Signature gk = new RSA_Signature(2048);
                                            gk.createKeys();
                                            signature = gk.sign(info, gk);

                                            // System.out.println("signature: " + signature);

                                            publishPacket += "#" + signature + "#" + gk.storePublicKey(gk.getPublicKey());



                                            dataOutputStream.writeUTF(publishPacket);
											dataOutputStream.flush();
//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
											pubAck = dataInputStream.readUTF().trim().split("#");
//
											if (pubAck[0].equalsIgnoreCase("40")) {
												logger.info("PubAck: Message got Published successfully");
                                                System.out.println("published msg");
												i++;
											} else if (pubAck[0].equalsIgnoreCase("99")) {
												logger.info(pubAck[1]);
											} else if (pubAck[0].equalsIgnoreCase("44")) {
												logger.info("Since no subscribers yet trying again to send same data");
												Thread.sleep(3000);
											}

											Thread.sleep(2000);
										}

										Thread.sleep(2000);


									}

									if(options.equalsIgnoreCase("2")){

                                        String subPacket = "82#0D#0001#" + "Test1,Test2,Test3,Test4" + "#00";

                                        subOutputStream.writeUTF(subPacket);
                                        subOutputStream.flush();
                                        if (!subscribeListener.isAlive()) {
                                            subscribeListener.start();
                                        }

                                        done = "Done";
                                        break;

                                    }

                                    if (options.equalsIgnoreCase("0")) {

                                        String subPacket = "82#0D#0001#" + "Test1,Test2,Test3,Test4" + "#00";

                                        subOutputStream.writeUTF(subPacket);
                                        subOutputStream.flush();
                                        if (!subscribeListener.isAlive()) {
                                            subscribeListener.start();
                                        }

                                        Thread.sleep(20000);

                                        done = "restart";

                                       // Socket closing

										try {
//											subscribeListener.dataInputStream.close();
//											subscribeListener.setDataInputStream(null);
//											subscribeListener.socket.close();
											subOutputStream.close();
											subInputStream.close();
//                                            subSocket.close();
////											Thread.sleep(1000);
//
//											socket.close();
//											Thread.sleep(1000);
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
										    subSocket.close();
											socket.close();
										}
										break;

                                    }

//									if (options.equalsIgnoreCase("2")) {
//
//                                        String subPacket = "82#0D#0001#" + "test1" + "#00";
//
//                                        subOutputStream.writeUTF(subPacket);
//                                        subOutputStream.flush();
//                                        if (!subscribeListener.isAlive()) {
//                                            subscribeListener.start();
//                                        }
//
//										//Test
//										topic = "test2";
////										logger.info("Enter the data or info to be published for this Topic: ");
////										String info = scanner.nextLine();
//
//										publishPacket = "33#12#0007#" + topic.trim();
//
//										dataOutputStream.writeUTF(publishPacket);
//										dataOutputStream.flush();
////												clientListener.setFlag(true);
////												clientListener.start();
//
////												wait();
////												notify();
//										pubAck = dataInputStream.readUTF().trim().split("#");
////
//										if (pubAck[0].equalsIgnoreCase("36")) {
//											logger.info("PubAck: Topic added in the Topic list successfully");
//										}
//
//                                        Thread.sleep(2000);
//
//
//										for (int i=0; i<10; i++) {
//
//											info = String.valueOf(i);
//											publishPacket = "30#12#0007#" + topic + "#" + info;
//
//											dataOutputStream.writeUTF(publishPacket);
//											dataOutputStream.flush();
////												clientListener.setFlag(true);
////												clientListener.start();
//
////												wait();
////												notify();
//											pubAck = dataInputStream.readUTF().trim().split("#");
////
//											if (pubAck[0].equalsIgnoreCase("40")) {
//												logger.info("PubAck: Message got Published successfully");
//											} else if (pubAck[0].equalsIgnoreCase("99")) {
//												logger.info(pubAck[1]);
//											} else if (pubAck[0].equalsIgnoreCase("44")) {
//												logger.info("Added the Topic in the Topic Pool; But data was not published being a new topic; Republish the dat using Option 2");
//											}
//										}
//
//									}

									if (options.equalsIgnoreCase("3")) {
										dataOutputStream.writeUTF("TopicList");
										dataOutputStream.flush();
										String[] s = dataInputStream.readUTF().trim().split("#");
										if (s.length==1){
											logger.info("No topic present as of now");
											continue;
										}
										logger.info(s[1]);
//												wait();
//												notify();
//												Thread.sleep(2000);
//												yield();

										logger.info("Enter the names of the topic to Subscribe to separated by comma");
										String topicList = scanner.nextLine();

										String subPacket = "82#0D#0001#" + topicList + "#00";

										subOutputStream.writeUTF(subPacket);
										subOutputStream.flush();
										if (!subscribeListener.isAlive()) {
											subscribeListener.start();
										}
//										subscribeListener.setDataOutputStream(subOutputStream);


//												if (dataInputStream.readUTF().trim().equalsIgnoreCase("90")) {
//													logger.info("SubAck: Subscribed to Topics successfully");
//												}

									}
									if (options.equalsIgnoreCase("4")) {

										logger.info("Enter topic names separated by comma to unsubscribe");
										String unsubList = scanner.nextLine();

										 publishPacket = "83#12#0007#"+unsubList;

											subOutputStream.writeUTF(publishPacket);
											subOutputStream.flush();


//												clientListener.setFlag(true);
//												clientListener.start();

//												wait();
//												notify();
//										String[] disAck = dataInputStream.readUTF().trim().split("#");
//										if (disAck[0].equalsIgnoreCase("144")){
//                                            logger.info("Client Disconnected!");
//                                        }


									}



								}
								if (done.equalsIgnoreCase("Done"))
								    break;
								if (done.equalsIgnoreCase("restart")) {
//								    socket.close();
//								    subSocket.close();
								    //wait for some time before restart
								    Thread.sleep(20000);
								    args[0] = "2";
                                }


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
				} catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
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
            }

			}.start();

	}

//	private static synchronized void onInit(DataOutputStream subOutputStream, DataInputStream subInputStream) {
//		logger.info("Inside Init");
//
//		try {
//			subOutputStream.writeUTF("00#");
//			subOutputStream.flush();
////			subOutputStream.close();
//			String initAck = subInputStream.readUTF();
//			if (initAck.equals("null")){
//				return;
//			}else {
//				logger.info(initAck);
//				System.out.println(initAck);
//
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//
//
//	}


    private static synchronized void onInit(DataOutputStream subOutputStream, DataInputStream subInputStream) {
        System.out.println("Inside Init");
        logger.info("Inside Init");

        try {
            subOutputStream.writeUTF("00#");
            subOutputStream.flush();
//			subOutputStream.close();
            String initAck = subInputStream.readUTF();
            if (initAck.equals("null")){
                return;
            }else {
                // System.out.println(initAck);
                System.out.println("Backlog Data: ");
                RSA_Signature gk = new RSA_Signature(2048);
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
                        logger.info(topic + ": "+ stuff[0]);
                    else
                        logger.info("The data was modified!");
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


    }

}
