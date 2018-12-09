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
import java.util.Scanner;


//This class was created for showing the continuously running demo of application
public class ClientTest_NormalRun {
	static boolean flag = false;
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

                        logger.info("started again");

						String done="";
						Scanner scanner = new Scanner(System.in);
						String data="connect";
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
								SubscribeListener subscribeListener = new SubscribeListener(subSocket, logger);
								subscribeListener.setDataInputStream(subInputStream);
								subscribeListener.setLogger(logger);

                                publicKey = onInit(subOutputStream, subInputStream);
								subscribeListener.start();

                                boolean allUp = true;

								while (true) {
									String publishPacket;
									String info;
									StringBuffer topic= new StringBuffer();
									String[] pubAck;
                                    String options = args[0];

                                    //Acts as a Publisher for publishing random health tips for topic "Health"
                                    if (options.equalsIgnoreCase("0")) {

                                        while(allUp){

                                            logger.info("Inside All UP");
                                            dataOutputStream.writeUTF("AllUP#");
                                            dataOutputStream.flush();
                                            if (dataInputStream.readUTF().trim().equalsIgnoreCase("2")) {
                                                allUp=false;
                                                break;
                                            }
                                            Thread.sleep(1000);
                                        }


                                        ArrayList<String> tips = new ArrayList<>(Arrays.asList(new String[]{"Develop relationships with health care practitioners you trust",
                                        "Eat fruits and veggies",
                                        "Rest when you are sick or tired",
                                                "Be as kind to yourself as you are to everyone else",
                                        "Laugh out loud",
                                                "Do something that scares you",
                                        "Trust your body",
                                                "Floss",
                                                "Maintain a healthy weight",
                                                "Drink water",
                                                "Quit smoking",
                                                "Ask for help",
                                                "Offer help",
                                                "Meditate",
                                        "Visit the dentist",
                                                "Walk barefoot",
                                                "Slow down",
                                                "Breathe Deeply",
                                                "Sit Still",
                                                "Spend time alone",
                                        "Spend time with friends",
                                        "Eat Colors not Calories",
                                        "Get regular screenings",
                                                "Sleep well",
                                                "Drink Tea"}));

                                        for (int i=20; i<100; ) {
                                                topic = new StringBuffer("Health");
                                                info = tips.get(i%tips.size()).toString();


                                            publishPacket = "30#12#0007#" + topic + "#" + info;
                                            String signature = "";
                                            signature = gk.sign(info, gk);

                                            logger.info("Publishing : "+topic+"--"+info);
                                            System.out.println("Publishing : "+topic+"--"+info);

                                            publishPacket += "#" + signature + "#" + publicKey;

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

                                            Thread.sleep(6000);
                                        }

                                        Thread.sleep(2000);


                                    }

                                    //Acts as a Publisher pblishing random data from Temperature and Humidity topics
                                    if (options.equalsIgnoreCase("1")) {

                                        while(allUp){

                                            logger.info("Inside All UP");
                                            dataOutputStream.writeUTF("AllUP#");
                                            dataOutputStream.flush();
                                            if (dataInputStream.readUTF().trim().equalsIgnoreCase("2")) {
                                                allUp=false;
                                                break;
                                            }
                                            Thread.sleep(1000);
                                        }

                                        boolean th=false;

										for (int i=20; i<100; ) {
										    if (th) {
										        topic = new StringBuffer("Temperature");
                                                info = String.valueOf(i)+"C";
										        th = false;
                                            }
										    else {
										        topic = new StringBuffer("Humidity");
                                                info = String.valueOf(i)+"%";
										        th = true;
                                            }



											publishPacket = "30#12#0007#" + topic + "#" + info;
											String signature = "";

											signature = gk.sign(info, gk);

											logger.info("Publishing : "+topic+"--"+info);
											System.out.println("Publishing : "+topic+"--"+info);

                                            publishPacket += "#" + signature + "#" + publicKey;



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

											Thread.sleep(6000);
										}

										Thread.sleep(2000);


									}

									//Acts as a Subscriber who has subscribed to all 3 topics
									if(options.equalsIgnoreCase("2")){
									    StringBuffer sb = new StringBuffer();


                                        String subPacket = "82#0D#0001#" + "Temperature,Humidity,Health" + "#00";

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
                // System.out.println(initAck);
                logger.debug("Backlog Data: ");
                System.out.println();
                System.out.println("Backlog data: ");
                // DBConnectionFactory db = new DBConnectionFactory();
                String [] bklg = initAck.split("##"); // missed_stuff#data1##data2##etc
                int len = bklg.length;
                String [] stuff;
                String topic;
                for(int i = 0; i<len; i++)
                {
                    stuff = bklg[i].split("-");
                    topic = stuff[0];
                    stuff = stuff[1].split("~~~~");

                    // publicKey = db.retrieveKey(stuff[2]);
                    if(gk.verifySignature(stuff[0], stuff[1], gk.loadPublicKey(stuff[2])))
                    {
                        logger.info(topic + ": " + stuff[0]);
                        System.out.println(topic + ": " + stuff[0]);
                        System.out.println();
                    }
                    else
                        logger.info("The data was modified!");
                }

            }

            System.out.println();
            System.out.println();

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
