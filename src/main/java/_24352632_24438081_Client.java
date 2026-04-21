import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;

public class _24352632_24438081_Client extends Application {
    Socket socket = null;
    BufferedReader sInput = null;
    PrintWriter sOutput = null;
    private boolean serverConnected = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        ComboBox<String> box1 = new ComboBox(FXCollections.observableArrayList("ADD", "REMOVE", "DISPLAY", "EARLY LECTURES", "OTHER"));
        box1.setPromptText("Choose an Option");
        box1.setMaxWidth(140);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(140);

        ComboBox<String> box2 = new ComboBox(FXCollections.observableArrayList("09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"));
        box2.setPromptText("Choose a Timeslot");
        box2.setMaxWidth(140);

        TextField roomID = new TextField();
        roomID.setPromptText("Enter Room ID");
        roomID.setMaxWidth(140);

        ComboBox<String> box3 = new ComboBox(FXCollections.observableArrayList("CS4076", "CS4006", "CS4115", "MA4413", "CS4815"));
        box3.setPromptText("Choose a Module");
        box3.setMaxWidth(140);

        Button b1 = new Button("Send Request");
        b1.setMaxWidth(140);

        Button b2 = new Button("STOP");
        b2.setMaxWidth(140);

        Button b3 = new Button("Clear");
        b3.setMaxWidth(140);

        Label statusLabel = new Label("Status: Client and Server Connecting.");

        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(true);

        TableView<TimetableSlot> tableView = new TableView();

        TableColumn<TimetableSlot, String> col1 = new TableColumn<>("Date");
        col1.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDate().toString()));

        TableColumn<TimetableSlot, String> col2 = new TableColumn<>("Time");
        col2.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTime().toString()));

        TableColumn<TimetableSlot, String> col3 = new TableColumn<>("Room");
        col3.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().toString()));

        TableColumn<TimetableSlot, String> col4 = new TableColumn<>("Module");
        col4.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getModule().toString()));

        tableView.getColumns().addAll(col1, col2, col3, col4);
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ObservableList<TimetableSlot> slots = FXCollections.observableArrayList();
        tableView.setItems(slots);

        try {
            socket = new Socket(InetAddress.getLocalHost(), 1234);
            sInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sOutput = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            serverConnected = true;
            statusLabel.setText("Status: Connection Established.");
            ta.appendText("Status: Connection Established." + "\n");
        } catch (IOException e) {
            statusLabel.setText("Status: Connection between Client and Server could not be established.");
            ta.appendText("Status: Connection between Client and Server could not be established." + "\n");
            b1.setDisable(true);
            b2.setDisable(true);
            b3.setDisable(true);
        }

        EventHandler<ActionEvent> button1Event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!serverConnected) {
                    displayError("Server is not connected.");
                    return;
                }
                try {
                    String option = box1.getValue();
                    String room = roomID.getText();
                    String date = String.valueOf(datePicker.getValue());
                    String time = box2.getValue();
                    String module = box3.getValue();

                    if (option == null) {
                        throw new InvalidInputException("You must select an option!");
                    }

                    String request = "";
                    if (option.equals("ADD") || option.equals("REMOVE")) {
                        if (date == null || date.isEmpty() || time == null || time.isEmpty() || room == null || room.isEmpty() || module == null || module.isEmpty()) {
                            throw new InvalidInputException("Data fields cannot be empty!");
                        }
                        request = option + "|" + date + "|" + time + "|" + room + "|" + module;
                    } else if (option.equals("DISPLAY")) {
                        request = option + "||||";
                    } else if (option.equals("EARLY LECTURES")) {
                        request = option + "||||";
                    } else if (option.equals("OTHER")) {
                        request = option + "||||";
                    }

                    ta.appendText("CLIENT: " + request + "\n");

                    final String r = request;
                    Task<String> task = new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            sOutput.println(r);
                            return sInput.readLine();
                        }
                    };


                    task.setOnSucceeded(succeeded -> {
                        String message = task.getValue();
                        if (message == null) {
                            ta.appendText("SERVER: Connection Closed.");
                            serverConnected = false;
                            statusLabel.setText("Status: Connection Closed.");
                            b1.setDisable(true);
                            b2.setDisable(true);
                            b3.setDisable(true);
                            return;
                        }
                        ta.appendText("SERVER: " + message + "\n");

                        if (message.startsWith("LECTURE SUCCESSFULLY ADDED: ")) {
                            slots.add(new TimetableSlot(date, time, room, module));
                        } else if (message.startsWith("LECTURE SUCCESSFULLY REMOVED: ")) {
                            TimetableSlot match = null;
                            for (TimetableSlot slot : slots) {
                                if (slot.getDate().equals(date) && slot.getRoom().equals(room) && slot.getModule().equals(module) && slot.getTime().equals(time)) {
                                    match = slot;
                                    break;
                                }
                            }
                            if (match != null) {
                                slots.remove(match);
                            }
                        } else if (message.startsWith("DISPLAY: ") || message.startsWith("DISPLAY EARLY LECTURES: ")) {
                            slots.clear();
                            String text = "";
                            if (message.startsWith("DISPLAY: ")) {
                                text = message.substring(9);
                            } else {
                                text = message.substring(24);
                            }
                            String[] tSlots = text.split("\\.");
                            for (String tSlot : tSlots) {
                                if (tSlot.isEmpty()) {
                                    continue;
                                }
                                String[] parts = tSlot.split("\\|", -1);
                                if (parts.length == 4) {
                                    slots.add(new TimetableSlot(parts[0], parts[1], parts[2], parts[3]));
                                }
                            }
                        } else if (message.startsWith("Clash") || message.startsWith("Error")) {
                            displayError(message);
                        }
                    });

                    task.setOnFailed(failed -> {
                        Throwable ex = task.getException();
                        displayError("Closing Connection: " + ex.getMessage());
                        ta.appendText("SERVER: Connection Closed.");
                        statusLabel.setText("Status: Connection Closed.");
                        b1.setDisable(true);
                        b2.setDisable(true);
                        b3.setDisable(true);
                    });

                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                } catch (InvalidInputException e) {
                    displayError(e.getMessage());
                }
            }
        };

        EventHandler<ActionEvent> button2Event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (serverConnected) {
                    ta.appendText("CLIENT: STOP. \n");

                    Task<String> task = new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            sOutput.println("STOP");
                            return sInput.readLine();
                        }
                    };

                    task.setOnSucceeded(succeeded -> {
                        String message = task.getValue();
                        if (message != null) {
                            ta.appendText("SERVER: " + message + "\n");
                        } else {
                            ta.appendText("SERVER: No Client Response. \n");
                        }
                        serverConnected = false;
                        ta.appendText("SERVER: Connection Closed.");
                        statusLabel.setText("Status: Connection Closed.");

                        try {
                            if (sInput != null) {
                                sOutput.close();
                            }
                            if (sOutput != null) {
                                sOutput.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            displayError("Closing Connection: " + e.getMessage());
                        }

                    });

                    task.setOnFailed(failed -> {
                        Throwable ex = task.getException();
                        displayError("Closing Connection: " + ex.getMessage());
                        ta.appendText("SERVER: Connection Closed.");
                        statusLabel.setText("Status: Connection Closed.");

                        try {
                            if (sInput != null) {
                                sOutput.close();
                            }
                            if (sOutput != null) {
                                sOutput.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            displayError("Closing Connection: " + e.getMessage());
                        }
                    });

                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    b1.setDisable(true);
                    b2.setDisable(true);
                    b3.setDisable(true);
                    ta.appendText("Status: Connection Closed. \n");
                    statusLabel.setText("Status: Connection Closed.");
                }
            }
        };

        EventHandler<ActionEvent> button3Event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (serverConnected) {
                    ta.appendText("CLIENT: Clear Timetable Slots. \n");

                    Task<String> task = new Task<String>() {
                        @Override
                        protected String call() throws Exception {
                            sOutput.println("Clear Timetable Slots.");
                            return sInput.readLine();
                        }
                    };

                    task.setOnSucceeded(succeeded -> {
                        String message = task.getValue();
                        if (message != null && message.startsWith("Error")) {
                            ta.appendText("SERVER: " + message + "\n");
                            displayError(message);
                        } else {
                            ta.appendText("SERVER: " + message + "\n");
                            slots.clear();
                        }
                    });

                    task.setOnFailed(failed -> {
                        Throwable ex = task.getException();
                        displayError("Closing Connection: " + ex.getMessage());
                    });

                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                }
            }
        };

        b1.setOnAction(button1Event);
        b2.setOnAction(button2Event);
        b3.setOnAction(button3Event);

        BorderPane pane = new BorderPane();
        VBox vbox = new VBox(box1, datePicker, box2, roomID, box3, b1, b2, b3, statusLabel);
        pane.setLeft(vbox);
        pane.setCenter(tableView);
        pane.setBottom(ta);
        vbox.setSpacing(3);

        Scene scene = new Scene(pane, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Lecture Scheduler");
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void displayError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

