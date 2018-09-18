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

        public ServerThread(Socket appSocket){

        }
    }
}
