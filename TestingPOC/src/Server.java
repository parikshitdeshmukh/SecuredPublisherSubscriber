import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


    public static void main(String...args){

        Socket appSocket = null;
        ServerSocket serverSocket = null;
        System.out.println("Server Listening------");

        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            e.printStackTrace();

            System.out.println("Server Hug diya!");
        }

        while(true){

            try {
                appSocket = serverSocket.accept();

                System.out.println("Connected !!");

                ServerThread st= new ServerThread(appSocket);
                st.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    private static class ServerThread extends Thread {

        Socket appSocket = null;
        DataInputStream in ;

        public ServerThread(Socket appSocket){
            this.appSocket = appSocket;
        }

        public void run(){

            try {
                in = new DataInputStream(appSocket.getInputStream());
                String serverReceived = in.readUTF();
                System.out.println("Server received form Client-- "+ serverReceived);

                System.out.println("Server Sending to Client-- "+ serverReceived);

                DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());
                out.writeUTF("Hello from Server");
                out.flush();
                appSocket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
