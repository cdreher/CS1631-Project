import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Hashtable;
import java.util.*;

public class Compo {

    //socket for connection to SISServer
    static Socket universal;
    private static int port = 53217;

    //message writer and reader
    static MsgEncoder encoder;
    static MsgDecoder decoder;

    //scope of this component
    private static final String SCOPE = "SIS.Scope1";

    //name of this component
    private static final String NAME = "Compo";

    //messages types that can be handled by this component
    private static final List<String> TYPES = new ArrayList<String>(
            Arrays.asList(new String[] { "Reading", "Alert", "Confirm",
                    "Connect", "Setting", "21" }));

    //Date & refresh information
    private static int refreshRate = 500, max = 40, min = 15;
    private static Date startDate = new Date(), endDate = new Date();
    private static Timer timer = new Timer();

    //shared by all kinds of records that can be generated by this component
    private static KeyValueList record = new KeyValueList();
    //shated by all kinds of alerts that can be generated by this component
    private static KeyValueList alert = new KeyValueList();

    private static String adminPassword = "1234";
    private static String securityLevel = "3";

    private static Hashtable<String, Integer> tallyTable = new Hashtable<String, Integer>();
    private static Hashtable<String, String> voterTable = new Hashtable<String, String>();
    private static List<String> candidateList = new ArrayList<String>(
            Arrays.asList(new String[] { "001", "002", "003", "004", "005", "006" }));

    /*
     *  Main program
     */

    public static void main(String[] args){

        while(true){
            try
            {

                // try to establish a connection to SISServer
                universal = connect();

                // bind the message reader to inputstream of the socket
                decoder = new MsgDecoder(universal.getInputStream());

                //bind the message writer to outputstream of the socket
                encoder = new MsgEncoder(universal.getOutputStream());


                /*
                    construct a connection message to establish the connection
                 */
                KeyValueList conn = new KeyValueList();
                conn.putPair("Scope", SCOPE);
                conn.putPair("MessageType", "Register");
                conn.putPair("Role","Basic");
                conn.putPair("Name", NAME);
                encoder.sendMsg(conn);

                conn = new KeyValueList();
                conn.putPair("Scope", SCOPE);
                conn.putPair("MessageType", "Connect");
                conn.putPair("Role","Basic");
                conn.putPair("Name", NAME);
                encoder.sendMsg(conn);

                initRecord();

                //KeyValueList for inward messages, see KeyValueList for details
                KeyValueList kvList;
                while(true){
                    // attempt to read and decode a message, see MsgDecoder for details
                    kvList = decoder.getMsg();

                    //process that message
                    ProcessMsg(kvList);
                }
            }

            catch (Exception e){
                //print the error message
                e.printStackTrace();

                //try to coonect again
                try{
                    //wait for 1 second to try
                    Thread.sleep(1000);
                }

                //catch sleep error
                catch (Exception e1){
                    e1.printStackTrace();
                }

                //then try to connect again
                System.out.println("Try to reconnect");
                try{
                    universal = connect();
                }
                catch (Exception e2){
                    e2.printStackTrace();
                }
            }
        }



    }


    /*
        used for connect(reconnect) to SISServer
     */
    public static Socket connect() throws IOException
    {
        Socket socket = new Socket("127.0.0.1",port);
        return socket;
    }

    public static void initRecord()
    {
        record.putPair("Scope", SCOPE);
        record.putPair("MessageType", "Reading");
        record.putPair("Sender", NAME);

        //Receiver may be different for each message, so we don't specify that

        alert.putPair("Scope", SCOPE);
        alert.putPair("MessageType","Alert");
        alert.putPair("Sender",NAME);
        alert.putPair("Purpose", "TestAlert");
    }

    private static void ProcessMsg(KeyValueList kvList) throws Exception
    {
        //following cases are errors
        /*String scope = kvList.getValue("Scope");
        if (!SCOPE.startsWith(scope))
        {
            return;
        }

        String messageType = kvList.getValue("MessageType");
        if(!TYPES.contains(messageType))
        {
            return;
        }*/

        String sender = kvList.getValue("Sender");
        String type = kvList.getValue("MessageType");
        String receiver = kvList.getValue("Receiver");

        System.out.println("Message successfully received");
        System.out.println("Sender: " + sender);
        System.out.println("Receiver: " + receiver);

        //now we send back to server to confirm message
        KeyValueList back = new KeyValueList();
        back.putPair("Scope", SCOPE);
        back.putPair("MessageType", "Confirm");
        back.putPair("Role","Basic");
        back.putPair("Name", NAME);
        encoder.sendMsg(back);

        switch(type){
            case "21":
                if(adminPassword.equals(kvList.getValue("Passcode")) && securityLevel.equals(kvList.getValue("SecurityLevel"))){
                  System.out.println("Admin successfully logged in.");
                  back = new KeyValueList();
                  back.putPair("Scope", SCOPE);
                  back.putPair("MessageType", "21");
                  back.putPair("Sender",NAME);
                  back.putPair("Receiver", "Voting GUI");
                  encoder.sendMsg(back);

                  System.out.println("VotingSoftware created successfully.\n");
                }
                else{
                  System.out.println("Admin password not correct.");
                }

                break;

            case "25":
                if(adminPassword.equals(kvList.getValue("Passcode")) && securityLevel.equals(kvList.getValue("SecurityLevel"))){
                  System.out.println("Admin successfully logged in.");
                  System.out.println("Voting has been terminated. The voting results are as follows:\n");

                  ArrayList<Map.Entry<String, Integer>> l = new ArrayList(tallyTable.entrySet());
                  Collections.sort(l, new Comparator<Map.Entry<String, Integer>>(){
                     public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }});

                  System.out.println(l);

                  back = new KeyValueList();
                  back.putPair("Scope", SCOPE);
                  back.putPair("MessageType", "25");
                  back.putPair("Sender",NAME);
                  back.putPair("Receiver", "Voting GUI");
                  encoder.sendMsg(back);

                  System.exit(0);
                }
                break;

            case "701":
                boolean voterHasVoted;

                //If voterTable does NOT contain VoterPhoneNo key yet.
                if(!voterTable.containsKey(kvList.getValue("VoterPhoneNo"))){
                  if(candidateList.contains(kvList.getValue("CandidateID"))){
                    voterTable.put(kvList.getValue("VoterPhoneNo"), kvList.getValue("CandidateID"));
                    voterHasVoted = false;
                  }
                  else{
                    back = new KeyValueList();
                    back.putPair("Scope", SCOPE);
                    back.putPair("MessageType", "711");
                    back.putPair("Sender",NAME);
                    back.putPair("Receiver", "Voting GUI");
                    back.putPair("Status", "2");
                    encoder.sendMsg(back);
                    System.out.println("Invalid candidate. Vote is not counted.\n");
                    voterHasVoted = true;
                  }

                }
                //If voterTable DOES contain VoterPhoneNo.
                else {
                  System.out.println("Vote can not be counted. This user has voted already.\n");
                  voterHasVoted = true;
                  back = new KeyValueList();
                  back.putPair("Scope", SCOPE);
                  back.putPair("MessageType", "711");
                  back.putPair("Sender",NAME);
                  back.putPair("Receiver", "Voting GUI");
                  back.putPair("Status", "1");
                  encoder.sendMsg(back);
                }

                if(!voterHasVoted){
                  //If tallyTable does NOT contain CandidateID key yet.
                  if(!tallyTable.containsKey(kvList.getValue("CandidateID"))){
                    tallyTable.put(kvList.getValue("CandidateID"), 1);
                  }
                  //If tallyTable DOES contain CandidateID.
                  else {
                    Integer n = tallyTable.get(kvList.getValue("CandidateID"));
                    n++;
                    tallyTable.replace(kvList.getValue("CandidateID"), n);
                  }

                  Integer n = tallyTable.get(kvList.getValue("CandidateID"));
                  System.out.println("vote count: " + n);

                  System.out.println("Your vote has been cast!\n");

                  back = new KeyValueList();
                  back.putPair("Scope", SCOPE);
                  back.putPair("MessageType", "711");
                  back.putPair("Sender",NAME);
                  back.putPair("Receiver", "Voting GUI");
                  back.putPair("Status", "3");
                  encoder.sendMsg(back);

                }
                break;

            case "Confirm":
                System.out.println("Successfully connect to SISServer");
                break;
        }


    }



}
