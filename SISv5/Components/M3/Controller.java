import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.*;
import java.io.*;
import java.net.*;

public class Controller {


    @FXML
    private VBox vbox;

    @FXML
    private MenuBar menuBar;

    @FXML
    private Menu aboutButton;

    @FXML
    private Menu exitButton;

    @FXML
    private Label logLabel;

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    @FXML
    private Button logButton;

    @FXML
    private TextArea information;



    /*
    end of FXML variables
     */


    /*
    Network Variables
     */

    // socket for connection to SISServer
    private Socket universal;
    // message writer
    private MsgEncoder encoder;
    // message reader
    private MsgDecoder decoder;
    // scope of this component
    // private static final String SCOPE = "SIS";
    // name of this component
    private final String NAME = "Voting GUI";

    private String SCOPE = "SIS.Scope1";

    private int port = 53217;

    private Socket connect() throws Exception {
        Socket socket = new Socket("127.0.0.1", port);
        return socket;
    }

    private void connectToServer() {

        try {

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
            conn.putPair("Role", "Basic");
            conn.putPair("Name", NAME);
            encoder.sendMsg(conn);

            conn = new KeyValueList();
            conn.putPair("Scope", SCOPE);
            conn.putPair("MessageType", "Connect");
            conn.putPair("Role", "Basic");
            conn.putPair("Name", NAME);
            encoder.sendMsg(conn);

            //print out the connect message
            information.appendText("\nSuccessfully Connected!");

        } catch (Exception e) {
            //print the error message
            e.printStackTrace();

            //try to coonect again
            try {
                //wait for 1 second to try
                Thread.sleep(1000);
            }

            //catch sleep error
            catch (Exception e1) {
                e1.printStackTrace();
            }

            //then try to connect again
            System.out.println("Try to reconnect");
            try {
                universal = connect();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }


    @FXML
    void loginRequest(ActionEvent event) {
        //ready to check the security information
        String user = username.getText();
        String pass = password.getText();
        information.setText("Checking the passcode...");
        //if the password is correct, connect to the server
        information.appendText("\nConnecting to server....");
        connectToServer();


    }

}
