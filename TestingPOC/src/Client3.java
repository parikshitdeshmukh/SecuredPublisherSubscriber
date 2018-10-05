import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client3 {

    public static void main(String[] args) throws IOException
    {
        int i=1;
        String type="";
        try
        {
            Scanner scn = new Scanner(System.in);

            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            Socket s = new Socket(ip, 5056);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while (true)
            {
//                System.out.println(dis.readUTF());
                if(i==1 || type.equals("Pub")) {
                    System.out.println("Inside Pub of Client");

                    String tosend = scn.nextLine();
                    if(i==1)
                        type = tosend;
                    dos.writeUTF(tosend);
                    dos.flush();

                    // If client sends exit,close this connection
                    // and then break from the while loop
                    if (tosend.equals("Exit")) {
                        System.out.println("Closing this connection : " + s);
                        s.close();
                        System.out.println("Connection closed");
                        break;
                    }

                    // printing date or time as requested by client
//                    String received = dis.readUTF();
//                    System.out.println(received);
                    i++;
                }else if(type.equals("Sub")){
                    System.out.println("Inside Sub of Client");

                    String received = dis.readUTF();
                    if(dis.readUTF()==null)
                        dis.close();
                    System.out.println(received);

                }
            }

            // closing resources
            scn.close();
            dis.close();
            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
