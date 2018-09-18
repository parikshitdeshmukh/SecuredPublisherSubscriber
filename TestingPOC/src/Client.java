import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    public static void main(String...args) throws IOException {

        Socket socket = new Socket("192.168.1.56", 4444);

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        out.writeUTF("Hello, goodbye!");
        out.flush();

        DataInputStream in = new DataInputStream(socket.getInputStream());
        String read;
        if((read=in.readUTF())!=null){

            System.out.println(read);
        }

        socket.close();

    }

}
