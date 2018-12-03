package Client;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;


public class SubscribeListener extends Thread{


    Socket socket;
    DataInputStream dataInputStream=null;
    DataOutputStream dataOutputStream=null;
    public static boolean flag = false;
    private Logger logger;

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void setDataInputStream(DataInputStream dataInputStream) {
        this.dataInputStream = dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }


    public SubscribeListener(Socket socket) {
        // TODO Auto-generated constructor stub
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            int countMsg=0;


//                    dataInputStream = new DataInputStream(socket.getInputStream());
//                dataOutputStream = new DataOutputStream(socket.getOutputStream());
//            BufferedReader bufferedReaderer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            BufferedOutputStream outputStream = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));
//            PrintWriter writer = new PrintWriter(outputStream);
//            DataOutputStream op = new DataOutputStream(socket.getOutputStream());

            RSA_Signature gk = new RSA_Signature(2048);

            while (true) {
                String input="";
                logger.info("Subscribe");
                System.out.println("Inside Subscriber listener");

                logger.info("inside client listener");

//                        if (countMsg==4){
//                            dataInputStream.close();
//                            socket.close();
//                            break;
//                        }


                try {
                    input = dataInputStream.readUTF();
//                             input = bufferedReader.readLine();
                }catch (Exception e){
                    logger.info("Subscribe socket closed");
                    break;
                }
                String[] s  = input.split("#");

                if (s[0].equalsIgnoreCase("90")) {
                    logger.info("SubAck: Subscribed to Topics successfully");
                    System.out.println("SubAck: Subscribed to Topics successfully");
                } else if (s[0].equalsIgnoreCase("99")) {
                    countMsg++;
//                        Thread.sleep(2000);
                    logger.info("New Data Published for Topic: " + s[1]);
//                                    System.out.println("New Data Published for Topic: " + s[1]);


                    String[] stuff = s[1].split("-");
                    String topic = stuff[0];
                    // System.out.println("s[1].split(-)[0] / topic: "+ topic);
                    stuff = stuff[1].split("~~~~");
                    // System.out.println("stuff: \n[0]: "+ stuff[0] + "\n[1]: " + stuff[1] + "\n[2]: " + gk.loadPublicKey(stuff[2]));
                    // msg, sig, public key
                    // System.out.println(gk.verifySignature(stuff[0], stuff[1], gk.loadPublicKey(stuff[2])));
                    if (gk.verifySignature(stuff[0], stuff[1], gk.loadPublicKey(stuff[2])))
                        System.out.println("New Data Published for Topic: " + topic + "-" + stuff[0]);
                    else
                        System.out.println("The data was modified!");

//                                    dataOutputStream.writeUTF("PubAck"+"\n");
//                                    Thread.sleep(500);
//                                    dataOutputStream.flush();
//                                    dataOutputStream.close();


                }else if (s[0].equalsIgnoreCase("93")) {
                    logger.info("Unsubcribed successfully!");
                }else if (s[0].equalsIgnoreCase("dis")){
                    break;
                }

            }
//                    socket.close();



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
