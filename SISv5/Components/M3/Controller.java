import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.*;
import java.io.*;
import java.net.*;

public class Controller {

    // Variables for admin.fxml
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

    // Variable for Vote.fxml
    @FXML
    private TitledPane messagesTitle;

    @FXML
    private ListView<Message> messages;

    @FXML
    private Button send;

    @FXML
    private Button load;

    @FXML
    private Button clear;

    @FXML
    private TextField phoneNum;

    @FXML
    private TextField candID;

    @FXML
    private Button castButton;

    @FXML
    private Button terminateButton;

    @FXML
    private TextArea infoArea;

    private static ObservableList<Message> data = FXCollections
            .observableArrayList();

    private static ObservableList<Field> toSendData = FXCollections
            .observableArrayList();

    private static ObservableList<Field> receivedData = FXCollections
            .observableArrayList();

    private Dialog<String> dialog = new Dialog<>();

    /*
    end of FXML variables
     */


    /*
    other variables
     */

    //table for storing the user accounts


    /*
    end of other variables
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
    void loginRequest(ActionEvent event) throws Exception {
        //ready to check the security information
        String user = username.getText();
        String pass = password.getText();

        Hashtable<String, String> userTable = new Hashtable<String, String>();
        String user1 = "user1";
        String passcode = "password";
        String admin_user = "admin";
        String admin_passcode = "1234";
        userTable.put(user1,passcode);
        userTable.put(admin_user, admin_passcode);

        information.setText("Checking the passcode...");
        //if the password is correct, connect to the server
        information.appendText("\nConnecting to server....");


        if (user.equals(admin_user)) {
          //terminateButton.setDisable(false);
        } else {
            if (pass.equals(userTable.get(user))) {
                connectToServer();
                Stage window;
                window = (Stage) logButton.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("Vote.fxml"));
                Scene voteScene = new Scene(root,950,600);
                window.setScene(voteScene);
                window.setTitle("Welcome to Voting System");

                //we display the GUI
                window.show();
                //terminateButton.setDisable(false);

            } else {
                information.appendText("\nInvalid user");
            }
        }

    }

    public void handlerClear() {
        data.clear();
    }

    public void handlerSend() {
        ObservableList<Message> selectedItems = messages.getSelectionModel()
                .getSelectedItems();
        // System.out.println(selectedItems.size());
        for (Message message : selectedItems) {
            String path = message.getPath();

            try {
                KeyValueList kvList = XMLUtil.extractToKV(new File(path));
                encoder.sendMsg(kvList);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                // data.remove(message);
                dialog.setTitle("Error");
                dialog.setHeaderText("Fail to send message");
                dialog.setContentText(message.getName() + "\n\n"
                        + message.getPath());
                dialog.showAndWait();
            }
        }
    }

    public void handlerLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load XML messages / Script");
        List<String> exts = new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 5932481899696458622L;

            {
                add("*.xml");
                add("*.txt");
            }
        };
        fileChooser
                .setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML, TXT", exts));
        Stage primaryStage = (Stage) castButton.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

        if (files != null) {
            for (File file : files) {
                String extension = file.getName().toLowerCase();
                if (extension.endsWith(".xml")) {
                    loadOneXML(file, null);
                } else if (extension.endsWith(".txt")) {
                    try {
                        List<String> paths = Files.readAllLines(FileSystems
                                .getDefault().getPath(file.getAbsolutePath()));
                        for (String p : paths) {
                            File temp = new File(p);
                            String ext = temp.getName().toLowerCase();
                            if (ext.endsWith(".xml")) {
                                loadOneXML(temp, null);
                            }
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        // e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadOneXML(File file, File base) {
        if (!file.exists()) {
            String prefix = base.getParent();
            prefix += "\\" + file.getName();
            file = new File(prefix);
            if (!file.exists()) {
                return;
            }
        }
        Message aMessage = new Message(file.getName(), file.getAbsolutePath());
        if (!data.contains(aMessage)) {
            data.add(aMessage);
        }
    }

    public void handlerMessagesHint() {
        messagesTitle.setText("Hold 'Ctrl' To Select");
    }

    public void handlerMessagesHintExit() {
        messagesTitle.setText("Message(s)");
    }

    @FXML
    public void handlerTerminate() throws Exception {
      // try to establish a connection to SISServer
      universal = connect();

      //bind the message writer to outputstream of the socket
      encoder = new MsgEncoder(universal.getOutputStream());


      KeyValueList reg = new KeyValueList();

      reg.putPair("Scope", "SIS.Scope1");
      reg.putPair("MessageType", "25");
      reg.putPair("Passcode", "1234");
      reg.putPair("SecurityLevel", "3");
      reg.putPair("Sender", "SISServer");
      reg.putPair("Receiver", "Compo");
      encoder.sendMsg(reg);

      infoArea.appendText("Voting has been terminated! Awaiting results...\n");
    }

    @FXML
    public void handlerVote() throws Exception{
      // try to establish a connection to SISServer
      universal = connect();

      //bind the message writer to outputstream of the socket
      encoder = new MsgEncoder(universal.getOutputStream());


      KeyValueList reg = new KeyValueList();

      reg.putPair("Scope", "SIS.Scope1");
      reg.putPair("MessageType", "701");
      reg.putPair("VoterPhoneNo", phoneNum.getText());
      reg.putPair("CandidateID", candID.getText());
      reg.putPair("Sender", "SISServer");
      reg.putPair("Receiver", "Compo");
      encoder.sendMsg(reg);

      infoArea.appendText("Vote has been cast!\n");
    }



}
