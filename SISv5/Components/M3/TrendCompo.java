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
import java.text.DecimalFormat;

public class TrendCompo {

    //socket for connection to SISServer
    static Socket universal;
    private static int port = 53217;

    //message writer and reader
    static MsgEncoder encoder;
    static MsgDecoder decoder;

    //scope of this component
    private static final String SCOPE = "SIS.Scope1";

    //name of this component
    private static final String NAME = "TrendCompo";

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
            case "25": // NEED TO CHANGE BACK TO 25
                 
                String all_posters= kvList.getValue("CandidateID");
                System.out.println(all_posters + " \n");
                System.out.println("Poster IDs received.");
                


                //Subjects
                //001 - AI
                //002 - Literature
                //003 - Math
                //004 - History
                //005 - Philosophy
                //006 - Science
                
                all_posters = all_posters.replace("}","");
                all_posters = all_posters.replace("{","");
                all_posters = all_posters.replace(" ","");
                all_posters = all_posters.replace("001","AI");
                all_posters = all_posters.replace("002","Literature");
                all_posters = all_posters.replace("003","Math");
                all_posters = all_posters.replace("004","Music");
                all_posters = all_posters.replace("005","Philosophy");
                all_posters = all_posters.replace("006","Science");

                System.out.println(all_posters + " \n");


                double AITotal =0,LiteratureTotal=0,MathTotal=0, MusicTotal=0,PhilosophyTotal=0,ScienceTotal=0;
                double total=0;
                String[] tokens = all_posters.split(",");
                for(String t: tokens){
                    String [] sep_tokens = t.split("=");
                    // for(String t2: sep_tokens){
                        for(int i = 0; i<sep_tokens.length; i++)
                        {   
                            if(sep_tokens[i].equals("AI"))
                            {
                                AITotal += Integer.parseInt(sep_tokens[i+1]);
                                total += AITotal;
                            }
                            if(sep_tokens[i].equals("Literature"))
                            {
                                LiteratureTotal += Integer.parseInt(sep_tokens[i+1]);
                                total += LiteratureTotal;
                            }
                            if(sep_tokens[i].equals("MathTotal"))
                            {
                                MathTotal += Integer.parseInt(sep_tokens[i+1]);
                                total += MathTotal;
                            }
                            if(sep_tokens[i].equals("Music"))
                            {
                                MusicTotal += Integer.parseInt(sep_tokens[i+1]);
                                total += MusicTotal;
                            }
                            if(sep_tokens[i].equals("Philosophy"))
                            {
                                PhilosophyTotal += Integer.parseInt(sep_tokens[i+1]);
                                total += PhilosophyTotal;
                            }
                            if(sep_tokens[i].equals("Science"))
                            {
                                ScienceTotal += Integer.parseInt(sep_tokens[i+1]);
                                total += ScienceTotal;
                            }
                        }
                        //System.out.println("sep: " + t2);
                    // }
                }
                
                //for this year, rank the subjects - which is more popular than another

                DecimalFormat df = new DecimalFormat("##.##");

                System.out.println("\nPercentages from this year's voting - ");
                System.out.println("Votes for AI: "+ df.format((AITotal/total*100))+"%");
                System.out.println("Votes for Literature: " + df.format((LiteratureTotal/total*100))+"%");
                System.out.println("Votes for Math: " + df.format((MathTotal/total*100))+"%");
                System.out.println("Votes for Music: " + df.format((MusicTotal/total*100)) +"%");
                System.out.println("Votes for Philosophy: " + df.format((PhilosophyTotal/total*100)) +"%");
                System.out.println("Votes for Science: " + df.format((ScienceTotal/total*100)) +"%");
                

                //load multiple years of data - predict trend

                try{
                    file = new File("pastyear1.txt");
                    reader = new BufferedReader(new FileReader(file));
                    String text = null;
                    String [] split;
                    int pastTotal = 0;
                    while((text = reader.readline()) != null){
                        split = text.split(",");
                        //checks each line to see which category total it should add to
                        if(split[0] == 001)
                        {
                            AITotal += Integer.parseInt(split[1]);
                            pastTotal += Integer.parseInt(split[1]);
                        }
                        if(split[0] == 002)
                        {
                            LiteratureTotal += Integer.parseInt(split[1]);
                            pastTotal += Integer.parseInt(split[1]);
                        }
                        if(split[0] == 003)
                        {
                            MathTotal += Integer.parseInt(split[1]);
                            pastTotal += Integer.parseInt(split[1]);
                        }
                        if(split[0] == 004)
                        {
                            MusicTotal += Integer.parseInt(split[1]);
                            pastTotal += Integer.parseInt(split[1]);
                        }
                        if(split[0] == 005)
                        {
                            PhilosophyTotal += Integer.parseInt(split[1]);
                            pastTotal += Integer.parseInt(split[1]);
                        }
                        if(split[0] == 006)
                        {
                            ScienceTotal += Integer.parseInt(split[1]);
                            pastTotal += Integer.parseInt(split[1]);
                        }
                    }
                }
                catch(FileNotFoundException e){
                    e.printStackTrace();
                }

                //Calculate total amount of votes from previous and current year
                total += pastTotal;

                System.out.println("\nPrediction based off previous year and current year - ");
                System.out.println("Votes for AI: "+ df.format((AITotal/total*100))+"%");
                System.out.println("Votes for Literature: " + df.format((LiteratureTotal/total*100))+"%");
                System.out.println("Votes for Math: " + df.format((MathTotal/total*100))+"%");
                System.out.println("Votes for Music: " + df.format((MusicTotal/total*100)) +"%");
                System.out.println("Votes for Philosophy: " + df.format((PhilosophyTotal/total*100)) +"%");
                System.out.println("Votes for Science: " + df.format((ScienceTotal/total*100)) +"%");


                /*
                back = new KeyValueList();
                back.putPair("Scope", SCOPE);
                back.putPair("MessageType", "666");
                back.putPair("Sender",NAME);
                back.putPair("Receiver", "PrjGUI");
                encoder.sendMsg(back);
                */   
                break;

            case "Confirm":
                System.out.println("Successfully connect to SISServer");
                break;


        }


    }



}
