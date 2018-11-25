package Client;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientListener extends Thread{


	Socket socket;
	DataInputStream dataInputStream;
	DataOutputStream dataOutputStream;
	 private boolean flag;

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public ClientListener(Socket socket, boolean flag, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
		// TODO Auto-generated constructor stub
		this.socket = socket;
		this.flag = flag;
		this.dataInputStream = dataInputStream;
		this.dataOutputStream = dataOutputStream;
	}

	
	@Override 
	public void run() {
		try {


//                    dataInputStream = new DataInputStream(socket.getInputStream());
                    while (true) {

                        if (flag) {
                             synchronized (this) {

                                System.out.println("inside client listener");

                                String[] s = dataInputStream.readUTF().split("#");
                                if (dataInputStream.readUTF().trim().equalsIgnoreCase("40")) {
                                    System.out.println("PubAck: Message got Published successfully");
                                    flag = false;
                                } else if (s[0].equalsIgnoreCase("90")) {
                                    System.out.println("SubAck: Subscribed to Topics successfully");
                                } else if (s[0].equalsIgnoreCase("99")) {
//                        Thread.sleep(2000);
                                    System.out.println("New Data Published fot Topic: " + s[1]);
                                } else if (s[0].equalsIgnoreCase("TL")) {
//                        Thread.sleep(1000);
                                    System.out.println("inside client handler for Topic List");
                                    System.out.println(s[1]);
//                        wait();
//                        notifyAll();

                                }

                                wait();
                                notify();
                            }
                        }
                    }




    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
