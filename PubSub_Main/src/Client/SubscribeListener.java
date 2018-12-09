package Client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.*;


public class SubscribeListener extends Thread{


	Socket socket;
	DataInputStream dataInputStream=null;
	DataOutputStream dataOutputStream=null;
    //this is logger for local logging of comments
    static Logger logger = Logger.getLogger("debugLogger");

    //this logger is for logging into remmote file at broker. Please refer the log4j.info for details
    static Logger logger2 = Logger.getLogger("reportsLogger");

    static {
        PropertyConfigurator.configure(SubscribeListener.class.getResourceAsStream("log4j.info"));

    }


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

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    private HashMap<String, List<String>> resultTesting = new HashMap<>();


    public SubscribeListener(Socket socket, Logger logger) {
		// TODO Auto-generated constructor stub
		this.socket = socket;
		this.logger = logger;
	}

	
	@Override 
	public void run() {
		try {
		    int countMsg=0;
            RSA_Signature gk = new RSA_Signature(2048);

            while (true) {
                        String input="";
                        logger.info("Subscribe");
                        logger.info("inside client listener");

                try {
                             input = dataInputStream.readUTF();
                        }catch (Exception e){
                            logger.info("Subscribe socket closed");
                            break;
                        }
                        String[] s  = input.split("#");

                                if (s[0].equalsIgnoreCase("90")) {

                                    logger.info("SubAck: Subscribed to Topics successfully");
                                    System.out.println("SubAck: Subscribed to Topics successfully");

                                } else if (s[0].equalsIgnoreCase("99")) {

                                    //Sign
                                    String[] stuff = s[1].split("-");
                                    String topic = stuff[0];
                                    stuff = stuff[1].split("~~~~");

                                    if (gk.verifySignature(stuff[0], stuff[1], gk.loadPublicKey(stuff[2])))
                                    {
                                        //For latency test
                                        if ( topic.equalsIgnoreCase("TestTopic1") && stuff[0].charAt(0)=='1'){
                                            logger2.error("Latency for TestTopic1-1: "+ (System.currentTimeMillis()- Double.parseDouble(stuff[0].substring(1))));
                                        }else if ( topic.equalsIgnoreCase("TestTopic2") && stuff[0].charAt(0)=='1'){
                                            logger2.error("Latency for TestTopic10-1: "+ (System.currentTimeMillis()- Double.parseDouble(stuff[0].substring(1))));
                                        }else if ( topic.equalsIgnoreCase("TestTopic3") && stuff[0].charAt(0)=='1'){
                                            logger2.error("Latency for TestTopic-30: "+ (System.currentTimeMillis()- Double.parseDouble(stuff[0].substring(1))));
                                        }else if ( topic.equalsIgnoreCase("TestTopic4") && stuff[0].charAt(0)=='1'){
                                            logger2.error("Latency for TestTopic48-1: "+ (System.currentTimeMillis()- Double.parseDouble(stuff[0].substring(1))));
                                        }
                                        System.out.println("New Data Received");
                                        System.out.println("Topic: " + topic);
                                        System.out.println("Data: " + stuff[0]);
                                        System.out.println();


                                        ArrayList<String> l = new ArrayList<>(resultTesting.getOrDefault(topic,new ArrayList<>()));
                                        l.add(stuff[0]);
                                        resultTesting.put(topic,l );
                                        countMsg++;

                                        //For message delivery testing
                                        if (countMsg==40){

                                            //Sending email test report
//                                          sendReport("Machine IP: "+ socket +"Received All!");

                                            //Logging into remote broker log file
                                            logger2.error("Subscriber: "+ socket.getLocalAddress()+" -- All received!");

                                        }
                                    }
                                    else
                                        System.out.println("Failed to get the Data:: The data was modified!");


                                }else if (s[0].equalsIgnoreCase("93")) {
                                    logger.info("Unsubcribed successfully!");
                                }else if (s[0].equalsIgnoreCase("dis")){
                                    break;
                                }

                    }

    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    //Not used
    //To send an email
    private void sendReport(String body) {
         // Recipient's email ID needs to be mentioned.

        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");
        final String username = "parikshitparikshit@gmail.com";//
        final String password = "*****";
        try{
            Session session = Session.getDefaultInstance(props,
                    new Authenticator(){
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }});

            // -- Create a new message --
            Message msg = new MimeMessage(session);

            // -- Set the FROM and TO fields --
            msg.setFrom(new InternetAddress("parikshitparikshit@gmail.com"));
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("pdeshmuk@buffalo.edu",false));
            msg.setSubject("PubSub Test Report");
            msg.setText(body);
            msg.setSentDate(new Date());
            Transport.send(msg);
            System.out.println("Message sent.");
        }catch (MessagingException e){
            e.printStackTrace();
        }
    }

}
