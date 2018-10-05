

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    static StringBuffer buffer = new StringBuffer();
    static boolean flag = false;

    public static void main(String...args){

        Socket appSocket = null;
        ServerSocket serverSocket = null;
        System.out.println("Broker Active------");

        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            e.printStackTrace();

            System.out.println("Broker ka server Hug diya!");
        }



        try {

            ServerThread st= new ServerThread(serverSocket);
            st.start();

            if (buffer.length()!=0) {
                Socket clientSocket = new Socket(InetAddress.getLocalHost(), 4448);
                ClientThread ct = new ClientThread(clientSocket);
                ct.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    static class ClientThread extends Thread{

        Socket clientSocket=null;
        DataOutputStream out;
        public ClientThread(Socket clientSocket) {

            this.clientSocket = clientSocket;
        }

        public void run(){
            try{

                while (true){
                    System.out.println("Inside Client of Broker");
                    if(buffer.length()!=0) {
                        out = new DataOutputStream(clientSocket.getOutputStream());
                        out.writeUTF(buffer.toString());
                        out.flush();
                    }

                }

            }catch (Exception e){
                System.out.println("Exception while sending back to client ");
                e.printStackTrace();
            }
        }

    }

    static class ServerThread extends Thread {

        ServerSocket serverSocket = null;
        Socket appSocket=null;
        DataInputStream in ;

        public ServerThread(ServerSocket serverSocket){
            this.serverSocket = serverSocket;
        }

        public void run(){

            try {
                while(true){
//                    System.out.println("Inside server of Broker, waiting to accept new conn");

                    appSocket = serverSocket.accept();

                    System.out.println("Connected !!");
                    System.out.println("inet address: " + appSocket.getInetAddress());
                    System.out.println("local address: " + appSocket.getLocalAddress());
                    System.out.println("local port: " + appSocket.getLocalPort());
                    System.out.println("port: " + appSocket.getPort());

                    System.out.println("Inside Server of Broker");
                    in = new DataInputStream(appSocket.getInputStream());
//                    String serverReceived = in.readUTF();
                    buffer.append(in.readUTF().toString());
                    System.out.println("Server received from Client-- "+ buffer.toString());

                    System.out.println("Server Sending to Client-- "+ buffer);

//                    DataOutputStream out = new DataOutputStream(appSocket.getOutputStream());
//                    out.writeUTF(buffer.toString());
//                    out.flush();

//                    buffer.append(serverReceived);
//                    buffer.setLength(0);

                    System.out.println("port: " + appSocket.getPort());
//                appSocket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
