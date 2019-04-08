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
    private PasswordField password;

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

    @FXML
    private Stage primaryStage;

    private ObservableList<Message> data = FXCollections
            .observableArrayList();

    private ObservableList<Field> toSendData = FXCollections
            .observableArrayList();

    private ObservableList<Field> receivedData = FXCollections
            .observableArrayList();

    private Dialog<String> dialog = new Dialog<>();

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

    private DebuggerProc proc;

    private boolean runProcessMsg;

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
        userTable.put(user1, passcode);
        userTable.put(admin_user, admin_passcode);

        information.setText("Checking the passcode...");

        Stage window = (Stage) logButton.getScene().getWindow();
        primaryStage = window;
        information.appendText("\nConnecting to server....");

        if (user.equals(admin_user)) {
            proc = new DebuggerProc();
            //if the password is correct, connect to the server
            connectToServer();

            //print out the connect message
            information.appendText("\nSuccessfully Connected!");
            Parent root = FXMLLoader.load(getClass().getResource("Admin.fxml"));
            Scene voteScene = new Scene(root, 950, 600);
            window.setScene(voteScene);
            window.setTitle("Welcome to Voting System");

            //we display the GUI
            window.show();
        } else {
            if (pass.equals(userTable.get(user))) {
                proc = new DebuggerProc();
                //if the password is correct, connect to the server
                connectToServer();

                //print out the connect message
                information.appendText("\nSuccessfully Connected!");
                Parent root = FXMLLoader.load(getClass().getResource("Vote.fxml"));
                Scene voteScene = new Scene(root, 950, 600);
                window.setScene(voteScene);
                window.setTitle("Welcome to Voting System");

                //we display the GUI
                window.show();
            } else {
                information.appendText("\nInvalid user");
            }
        }


    }

    @FXML
    public void handlerClear() {
        data.clear();
    }

    @FXML
    public void handlerSend() throws Exception {

        connectToServer();

        ObservableList<Message> selectedItems = messages.getSelectionModel()
                .getSelectedItems();
        //System.out.println(selectedItems.size());
        for (Message message : selectedItems) {
            String path = message.getPath();

            try {
                KeyValueList kvList = XMLUtil.extractToKV(new File(path));
                encoder.sendMsg(kvList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        runProcessMsg = true;
        KeyValueList kvList;
        while (runProcessMsg) {
            // attempt to read and decode a message, see MsgDecoder for details
            try {
                kvList = decoder.getMsg();
                //process that message
                ProcessMsg(kvList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handlerLoad() {
        connectToServer();
        //fake constructor
        try {
            messages.setItems(data);
            messages.setCellFactory(new Callback<ListView<Message>, ListCell<Message>>() {

                public ListCell<Message> call(ListView<Message> param) {
                    final ListCell<Message> cell = new ListCell<Message>() {
                        @Override
                        public void updateItem(Message item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setText(item.getName());
                                setTooltip(new Tooltip(item.getPath()));
                            }
                        }
                    }; // ListCell
                    return cell;
                }
            }); // setCellFactory

            messages.getSelectionModel().selectedItemProperty()
                    .addListener(new ChangeListener<Message>() {
                        public void changed(
                                ObservableValue<? extends Message> observable,
                                Message oldValue, Message newValue) {
                            toSendData.clear();

                            try {
                                if (observable != null
                                        && observable.getValue() != null) {
                                    String path = observable.getValue()
                                            .getPath();
                                    KeyValueList kvList = XMLUtil
                                            .extractToKV(new File(path));

                                    List<String> table = kvList.tableOutput();
                                    for (String string : table) {
                                        String[] parts = string.split(" ");
                                        toSendData.add(new Field(parts[0],
                                                parts[1]));
                                    }
                                }

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                // data.remove(newValue);
                                // messages.setItems(null);
                                // messages.setItems(data);
                            }
                        }
                    });

            messages.getSelectionModel().setSelectionMode(
                    SelectionMode.MULTIPLE);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        //end of fake constructor


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

    @FXML
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

    @FXML
    public void handlerMessagesHint() {
        messagesTitle.setText("Hold 'Ctrl' To Select");
    }

    @FXML
    public void handlerMessagesHintExit() {
        messagesTitle.setText("Message(s)");
    }

    @FXML
    public void handlerTerminate() throws Exception {
        connectToServer();
        infoArea.appendText("Voting has been terminated! Awaiting results...\n");

        KeyValueList back = new KeyValueList();
        back.putPair("Scope", "SIS.Scope1");
        back.putPair("MessageType", "25");
        back.putPair("Passcode", "1234");
        back.putPair("SecurityLevel", "3");
        back.putPair("Sender",NAME);
        back.putPair("Receiver", "Compo");
        encoder.sendMsg(back);

    }

    @FXML

    public void handlerVote() throws Exception {
        connectToServer();
        KeyValueList reg = new KeyValueList();

        reg.putPair("Scope", "SIS.Scope1");
        reg.putPair("MessageType", "701");
        reg.putPair("VoterPhoneNo", phoneNum.getText());
        reg.putPair("CandidateID", candID.getText());
        reg.putPair("Sender", "SISServer");
        reg.putPair("Receiver", "Compo");
        encoder.sendMsg(reg);

        runProcessMsg = true;
        KeyValueList kvList;
        while (runProcessMsg) {
            // attempt to read and decode a message, see MsgDecoder for details
            try {
                kvList = decoder.getMsg();
                //process that message
                ProcessMsg(kvList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DebuggerProc {
        private Thread thread;
        private DebuggerTask debuggerTask;

        public DebuggerProc() {
            debuggerTask = new DebuggerTask();
            thread = new Thread(debuggerTask);
            thread.start();
        }

        public void pause() {
            debuggerTask.pause();
        }

        public void resume() {
            debuggerTask.resume();
        }

        public void close() {
            try {
                debuggerTask.terminate();
                thread.join();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    class DebuggerTask extends Task<Void> {

        private volatile boolean running = true;
        private volatile boolean pause = false;

        public void terminate() throws Exception {
            // TODO Auto-generated method stub
            universal.shutdownInput();
            universal.shutdownOutput();
            universal.close();
            running = false;
        }

        public void pause() {
            pause = true;
        }

        public void resume() {
            pause = false;
        }

        @Override
        protected Void call() throws Exception {
            // TODO Auto-generated method stub
            while (running) {
                if (!pause) {
                    try {
                        KeyValueList msg = decoder.getMsg();
                        if (msg != null) {
                            receivedData.clear();
                            List<String> table = msg.tableOutput();
                            for (String string : table) {
                                String[] parts = string.split(" ");
                                receivedData.add(new Field(parts[0], parts[1]));
                            }
                        }
                        Thread.sleep(3000);
                        // TABLEVIEW
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        // e.printStackTrace();
                        break;
                    }
                }
            }
            return null;
        }
    }


    private void ProcessMsg(KeyValueList kvList) throws Exception {

        String sender = kvList.getValue("Sender");
        String type = kvList.getValue("MessageType");
        String receiver = kvList.getValue("Receiver");

        switch (type) {
            case "711":
                String status = kvList.getValue("Status");
                if (status.equals("2")) {
                    infoArea.appendText("Invalid candidate. Vote is not counted.\n");
                } else if (status.equals("1")) {
                    infoArea.appendText("Vote can not be counted. This user has voted already.\n");
                } else {
                    infoArea.appendText("Your vote has been cast!\n");
                }
                runProcessMsg = false;
                break;
            case "25":
                infoArea.appendText("Voting has been terminated! Awaiting results...\n");

                runProcessMsg = false;
                break;
            case "21":
                infoArea.appendText("VotingSoftware created successfully.\n");
                runProcessMsg = false;
                break;
        }


    }

}






