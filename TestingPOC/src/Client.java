

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
    static StringBuffer buffer = new StringBuffer();
    static boolean flag = false;

    public static void main(String...args){

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        System.out.println("Client Active------");

        try {
            serverSocket = new ServerSocket(4448);

        } catch (IOException e) {
            e.printStackTrace();

            System.out.println("Client ka server Hug diya!");
        }

        while(true){

            try {

                ClientThread ct = new ClientThread(4444);
                ct.start();

                Thread.sleep(10000);
                if (flag == true) {
                    ServerThread st = new ServerThread(serverSocket);
                    st.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }

    private static class ClientThread extends Thread{

        Socket clientSocket=null;
        DataOutputStream out;
        int clientport;
        public ClientThread(int clientPort) {

            this.clientport = clientPort ;
        }

        public void run(){
            try{


                clientSocket = new Socket(InetAddress.getLocalHost(), 4444);

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                System.out.println("Inside Client of Client");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                // Reading data using readLine
                String name = reader.readLine();

                out.writeUTF(name.trim());
                flag = true;
                out.flush();

//                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
//                String read;
//                System.out.println("Received from Server---- ");
//                if ((read = in.readUTF()) != null) {
//
//                    System.out.println(read);
//                }


//                socket.close();

            } catch (Exception e) {
                System.out.println("Connection issue");
                e.printStackTrace();
            }
        }

    }

    private static class ServerThread extends Thread {

        ServerSocket serverSocket = null;
        Socket appSocket=null;
        DataInputStream in ;

        public ServerThread(ServerSocket serverSocket) throws IOException {
            this.serverSocket = serverSocket;

        }

        public void run(){

            try {
                while(true){

                    appSocket = serverSocket.accept();

                    System.out.println("Connected !!");
                    System.out.println("inet address: " + appSocket.getInetAddress());
                    System.out.println("local address: " + appSocket.getLocalAddress());
                    System.out.println("local port: " + appSocket.getLocalPort());
                    System.out.println("port: " + appSocket.getPort());

                    System.out.println("Inside Server of client");
                    in = new DataInputStream(appSocket.getInputStream());
//                    String serverReceived = in.readUTF();
                    buffer.append(in.readUTF());
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

//        Socket socket = null;
//
//            try {
////                socket = new Socket("192.168.1.56", 4444);
//                socket = new Socket(InetAddress.getLocalHost(), 4444);
//                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//                System.out.println("Sending to Server---");
//
//                while (true) {
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//
//                    // Reading data using readLine
//                    String name = reader.readLine();
//
//                    out.writeUTF(name.trim());
//                    out.flush();
//
//                    DataInputStream in = new DataInputStream(socket.getInputStream());
//                    String read;
//                    System.out.println("Received from Server---- ");
//                    if ((read = in.readUTF()) != null) {
//
//                        System.out.println(read);
//                    }
//                }
//
////                socket.close();
//
//            } catch (Exception e) {
//                System.out.println("Connection issue");
//                e.printStackTrace();
//            }
//




}
