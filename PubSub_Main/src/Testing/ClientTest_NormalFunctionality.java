package Testing;

import Client.Client;
import Client.RSA_Signature;
import Client.SubscribeListener;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ClientTest_NormalFunctionality {
    static Logger logger = Logger.getLogger("debugLogger");
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
				try {

					while (true) {
                        String publicKey;
						String done="";
						String data="connect";
						String clientID = "test";

						//Socket connection with Broker server
						Socket socket = new Socket("10.142.0.2", 6000);

						//For local testing
//						Socket socket = new Socket("localhost", 6000);

						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

						if (data.equalsIgnoreCase("Connect")) {
							data = "10#12#0004#MQTT#0004#" + clientID;

							dataOutputStream.writeUTF(data);
							dataOutputStream.flush();

							DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
							String ack = dataInputStream.readUTF();

							if (ack.equals("20")) {
							    //Socket connection with Broker for Subscriber's listening
								Socket subSocket = new Socket("10.142.0.2", 6000);
//								Socket subSocket = new Socket("localhost", 6000);

								logger.info(subSocket.getLocalAddress());
								logger.info(subSocket.getInetAddress());

								logger.info(subSocket.getPort());
								logger.info(subSocket.getLocalPort());

								DataOutputStream subOutputStream = new DataOutputStream(subSocket.getOutputStream());

								DataInputStream subInputStream = new DataInputStream(subSocket.getInputStream());
								SubscribeListener subscribeListener = new SubscribeListener(subSocket, logger);
								subscribeListener.setDataInputStream(subInputStream);
								subscribeListener.setLogger(logger);

								//To get back the missed backlog and update the Snapshot
                                publicKey = onInit(subOutputStream, subInputStream);
								subscribeListener.start();

                                boolean allUp = false;

								while (true) {
									String publishPacket;
									String info;
									StringBuffer topic= new StringBuffer();
									String[] pubAck;

                                    String options = args[0];

                                    //For Publishers
                                    if (options.equalsIgnoreCase("1")) {

                                        dataOutputStream.writeUTF("TopicList");
                                        dataOutputStream.flush();
                                        String in = dataInputStream.readUTF();
                                        logger.info("Topic List: "+ in);

                                        String[] tl = in.trim().split("#");



                                        if (tl.length==1){
                                            logger.info("No topic present as of now");
                                            topic = new StringBuffer("TestTopic1");
                                        }else {
                                            List<String> tl_List = new ArrayList<>(Arrays.asList(tl[1].split(",")));
                                            Collections.sort(tl_List);
                                            Collections.reverse(tl_List);
                                            logger.info(tl_List);
                                            topic = new StringBuffer("TestTopic");
                                            topic.append(Integer.parseInt(String.valueOf(tl_List.get(0).charAt(9)))+Integer.valueOf(1));

                                        }

                                        if ( topic.length()>0 && Integer.parseInt(String.valueOf(topic.toString().charAt(9)))>52){
                                            logger.info("Inside Break for TestTopic53");
                                            done = "Done";
                                            break;
                                        }

                                        logger.info("New topic to be published: "+ topic.toString());

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

										//Waiting for pre decided number of subscribers to come up
                                        //49 here
                                        while(!allUp){

                                            logger.info("Inside All UP");
                                            dataOutputStream.writeUTF("AllUP#");
                                            dataOutputStream.flush();
                                            if (dataInputStream.readUTF().trim().equalsIgnoreCase("49")) {
                                                allUp=true;
                                                break;
                                            }
                                            Thread.sleep(1000);
                                        }

										for (int i=0; i<10; ) {

											 info = String.valueOf(i)+System.currentTimeMillis();
//											 publishPacket = "30#12#0007#" + topic.toString() + "#" + info;

											publishPacket = "30#12#0007#" + topic + "#" + info;
											String signature = "";

											signature = gk.sign(info, gk);

											logger.info("Publishing : "+topic+"--"+info);
											System.out.println("Publishing : "+topic+"--"+info);

                                            publishPacket += "#" + signature + "#" + publicKey;



											dataOutputStream.writeUTF(publishPacket);
											dataOutputStream.flush();
											pubAck = dataInputStream.readUTF().trim().split("#");
//
											if (pubAck[0].equalsIgnoreCase("40")) {
												logger.info("PubAck: Message got Published successfully");
												System.out.println("PubAck: Message got Published successfully");
												i++;
											}else if(pubAck[0].equalsIgnoreCase("43")){
												logger.info("Invalid Public Key. You have been rejected.");
											}
											else if (pubAck[0].equalsIgnoreCase("99")) {
												logger.info(pubAck[1]);
											} else if (pubAck[0].equalsIgnoreCase("44")) {
												logger.info("Since no subscribers yet trying again to send same data");
                                                System.out.println("Since no subscribers yet trying again to send same data");
												Thread.sleep(5000);
											}

											Thread.sleep(500);
										}

										Thread.sleep(2000);


									}

									//For Subscribers
									if(options.equalsIgnoreCase("2")){
									    StringBuffer sb = new StringBuffer();
									    for (int i=1; i<=52; i++){
									        sb.append("TestTopic");
									        sb.append(i);
									        sb.append(",");
                                        }


                                        String subPacket = "82#0D#0001#" + sb.substring(0, sb.length()-1).toString() + "#00";
                                        System.out.println(sb.substring(0, sb.length()-1).toString());

                                        subOutputStream.writeUTF(subPacket);
                                        subOutputStream.flush();
                                        if (!subscribeListener.isAlive()) {
                                            subscribeListener.start();
                                        }

                                        done = "Done";
                                        break;

                                    }

								}
								if (done.equalsIgnoreCase("Done"))
								    break;

							} else socket.close();

						} else socket.close();


					}

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
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

    //Prints Backlog
    private static synchronized String onInit(DataOutputStream subOutputStream, DataInputStream subInputStream) {
        logger.info("Inside Init");
        String publicKey = "";
        try {
            gk.createKeys(); // generate one set pf keys for each node
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
                        logger.info(topic + ": " + stuff[0]);
                        System.out.println(topic + ": " + stuff[0]);
                    }
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

        return publicKey;
    }


}
