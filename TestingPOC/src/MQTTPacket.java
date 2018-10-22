class MQTTPacket
{
    public static void main(String[] args)
    {
        String connect_packet, control, length, variable_header, payload;

//      connect packet
        control = "0x10"; // control for connect
//      23 bytes length - length field is of a maximum of 4 bytes - its LSB and MSB in order
//      with first bit called continuity bit
        length = "0x17" + "0x00";

//      variable header with protocol set to mqtt, connect flags only clean session set,
//      keep alive set to 60 seconds (< is 60 in ascii)
        variable_header = "0x04MQTT" + "0x04" + "0x02" + "0x00<";

//      payload has length 11 (0x0b) from message 'python_test'
//      adding the limiter '~~~~' at the beginning of the payload to separate it from the rest of the packet
        payload = "~~~~" + "0x000x0bpython_test";

        connect_packet = control + length + variable_header + payload;
        System.out.println("\n Connect Packet: \n" + connect_packet);

//      connect ack packet
        control = "0x20";
        length = "0x01" + "0x00";
        variable_header = "0x00";
        payload = "~~~~" + " ";
        String connect_ack_packet;
        connect_ack_packet = control + length + variable_header + payload;
        System.out.println("\n Connect ACK packet: \n" + connect_ack_packet);

//      publish message packet
        control = "0x30";  // it'll be '0x32' for QoS enabled packet (at least once mode)
        length = "0x02" + "0x01"; // 18 bytes

//      first is topic name length, then is the topic name, followed by packet identifier in case of QoS
        variable_header = "0x05" + "topic" + "0x00" + "0x00"; // for now packet identifier is 0000

//      payload may or may not be empty, length not included in packet; can be calculated from 'length'
        payload = "~~~~" + "content";
        String publish_message_packet;
        publish_message_packet = control + length + variable_header + payload;
        System.out.println("\n Publish message packet: \n" + publish_message_packet);

//      publish ACK packet (in case of QoS level 1 (at least once)
        control = "0x40";
        length = "0x03" + "0x00";
        variable_header = "0x00" + "0x00"; // packet identifier from publish message packet.
        payload = "~~~~" + " ";
        String publish_ACK_packet;
        publish_ACK_packet = control + length + variable_header + payload;
        System.out.println("\n Publlsh ACK packet: \n" + publish_ACK_packet);

//      subscribe packet
        control = "0x82";
        variable_header = "0x00" + "0x00";  // packet identifier - 0000 for now
        length = "0x04" + "0x01"; // number of topic filters in packet (20 bytes)
        payload = "~~~~" + "0x00" + "0x0c" + "topic filter"; // length of topic filter followed by the topic filter
        payload += "0x00"; // reserved - do not touch - this is followed by 4 bits having 00XX format, where XX is QoS (10 for at least once)
        payload += "0x02"; // this can be followed by multiple
        String subscribe_packet;
        subscribe_packet = control + length + variable_header + payload;
        System.out.println("\n Subscribe Packet: \n" + subscribe_packet);

//      subscribe ACK packet
        control = "0x90";
        length = "0x07" + "0x00";
        variable_header = "0x00" + "0x00"; // packet identifier of subscribe packet
        payload = "~~~~" + "0x00" + "0x02"; // return code - 02 for QoS 2 (at least once)
        String subscribe_ACK_packet;
        subscribe_ACK_packet = control + length + variable_header + payload;


//      trying out the packet_parser function
        packet_parser(connect_packet);
        packet_parser(connect_ack_packet);
        packet_parser(publish_message_packet);
        packet_parser(publish_ACK_packet);
        packet_parser(subscribe_packet);
        packet_parser(subscribe_ACK_packet);
    }

    static void packet_parser(String packet)
    {

        String arr[] = packet.split("0x");
        String control, length, variable_header, payload;
        control = arr[1]; // first value is a blank string so starting from index 1
        length = arr[2] + arr[3];
        String temp = "";
        for(int i = 4; i< arr.length; i++)
        {
            temp += arr[i];
        }

        arr = temp.split("~~~~");
        variable_header = arr[0];
        payload = arr[1];

        System.out.println("\n control: " + control + "\nlength: " + length + "\nVariable Header: " + variable_header + "\nPayload: " + payload + "\n");
    }
}
