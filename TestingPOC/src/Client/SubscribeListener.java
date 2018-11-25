package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

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


                    dataInputStream = new DataInputStream(socket.getInputStream());
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
                                    System.out.println("New Data Published for Topic: " + s[1]);
//                                    dataOutputStream.writeUTF("PubAck");

                                }

                    }




    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
