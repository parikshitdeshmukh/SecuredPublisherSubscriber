package Parsers;


import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MQTTParsers {


    //User's data for connect
    static String clientID = "Client1";
    static String connectPacket = "10#12#0004#MQTT#0004#"+clientID;

    //User's data for publish
    static String topic = "Weather";
    static String data = "Cloudy, drizzles";
    static String publishPacket = "30#12#0007#"+topic+"#"+data;

    static LogManager lgmngr = LogManager.getLogManager();
    static Logger log = lgmngr.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public synchronized String initialParsing(String packet){
        String[] dataArray = packet.split("#");

        switch (dataArray[0]){
            case "10":
                log.log(Level.INFO,"Into Connect Case");
                return connect(dataArray);
            case "30":
                log.log(Level.INFO, "Into Publish packet");
                return publish(dataArray);
            case "82":
                log.log(Level.INFO, "Into Subscribe Packet");
                return subscribe(dataArray);
            default:
                log.log(Level.WARNING,"It's a bad packet");
                break;
        }

        return null;

    }

    private synchronized String subscribe(String[] dataArray) {

        return "90";


    }

    private synchronized String publish(String[] dataArray) {

        log.log(Level.INFO, "Topic: "+dataArray[3]+", Data: "+dataArray[4]);

        return "40";




    }

    private synchronized static String connect(String[] dataArray){
        log.log(Level.INFO, " Connect Packet Info-- Protocol name: "+dataArray[3] +", Client ID: "+dataArray[5] );

        return "20";

    }

    public static void main(String[] args) {
        System.out.println(connectPacket);
    }



}
