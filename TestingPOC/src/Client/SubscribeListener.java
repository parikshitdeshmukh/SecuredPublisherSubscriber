package Client;

import Signature.RSA_Signature;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


public class SubscribeListener extends Thread{


    Socket socket;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    public static boolean flag = false;

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

    public SubscribeListener(Socket socket) {
        // TODO Auto-generated constructor stub
        this.socket = socket;
    }


    @Override
    public void run() {
        try {


//                    dataInputStream = new DataInputStream(socket.getInputStream());
//                dataOutputStream = new DataOutputStream(socket.getOutputStream());

            RSA_Signature gk = new RSA_Signature(2048);
            
            while (true) {
                String input="";

                System.out.println("inside client listener");
                try {
                    input = dataInputStream.readUTF();
                }catch (Exception e){
                    System.out.println("Subscribe socket closed");
                    break;
                }
                String[] s  = input.split("#");

                if (s[0].equalsIgnoreCase("90")) {
                    System.out.println("SubAck: Subscribed to Topics successfully");
                } else if (s[0].equalsIgnoreCase("99")) {
//                        Thread.sleep(2000);
                    
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
//                                    dataOutputStream.writeUTF("PubAck");
//                                    dataOutputStream.flush();
                }else if (s[0].equalsIgnoreCase("93")) {
                    System.out.println("Unsubcribed successfully!");
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
