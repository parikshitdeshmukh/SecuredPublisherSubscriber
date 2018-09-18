import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client2 {

    public static void main(String...args){

        Socket socket = null;

        try {
            socket = new Socket("192.168.1.56", 4444);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Sending to Server---");

            while (true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                // Reading data using readLine
                String name = reader.readLine();

                out.writeUTF(name.trim());
                out.flush();

                DataInputStream in = new DataInputStream(socket.getInputStream());
                String read;
                System.out.println("Received from Server---- ");
                if ((read = in.readUTF()) != null) {

                    System.out.println(read);
                }
            }

//                socket.close();

        } catch (Exception e) {
            System.out.println("Connection issue");
            e.printStackTrace();
        }


    }

}
