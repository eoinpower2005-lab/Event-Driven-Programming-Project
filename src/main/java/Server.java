import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        Server server = new Server();
        server.beginServer();
    }
    private final ObservableList<TimetableSlot> timetableSlots = FXCollections.observableArrayList();

    public void beginServer() {
        ServerSocket servSock = null;
        try {
            servSock = new ServerSocket(1234);
            System.out.println("Listening on port 1234!");

            while (true) {
                Socket socket = servSock.accept();
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                    String message;
                    while ((message = input.readLine()) != null) {
                        System.out.println("CLIENT: " + message);
                        if (message.equalsIgnoreCase("STOP")) {
                            output.println("TERMINATE");
                            break;
                        }

                        String response;
                        try {
                            response = clientRequest(message);
                        } catch (IncorrectActionException e) {
                            response = e.getMessage();
                        }

                        output.println(response);
                    }
                    System.out.println("Client Disconnected!");
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error : " + e.getMessage());
                } finally {
                    try {
                        if (servSock != null) {
                            servSock.close();
                        }
                    } catch (IOException e) {
                        System.out.println("Error : " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String clientRequest(String request) throws IncorrectActionException {
        String response = "";

        if (request.equals("Clear Timetable Slots.")) {
            timetableSlots.clear();
            response = "Timetable Slots Cleared.";
            return response;
        }
        String[] fields = request.split("\\|", -1);
        String option = fields[0];
        String date = fields[1];
        String time = fields[2];
        String room = fields[3];
        String module = fields[4];

        if (option.equals("ADD")) {
            if (date == null || date.isEmpty() || time == null || time.isEmpty() || module == null || module.isEmpty() || room == null || room.isEmpty()) {
                throw new IncorrectActionException("Data fields cannot be empty!");
            }

            boolean exists = false;
            for (TimetableSlot slot : timetableSlots) {
                if (slot.getDate().equals(date) && slot.getTime().equals(time) && slot.getRoom().equals(room)) {
                    throw new IncorrectActionException("Clash: Timetable Slot already exists for that room, date, and time!");
                }
            }

            timetableSlots.add(new TimetableSlot(date, time, room, module));
            response = "LECTURE SUCCESSFULLY ADDED: " + date + "|" + time + "|" + room + "|" + module;
        } else if (option.equals("REMOVE")) {
            if (date == null || date.isEmpty() || time == null || time.isEmpty() || module == null || module.isEmpty() || room == null || room.isEmpty()) {
                throw new IncorrectActionException("Data fields cannot be empty!");
            }
            TimetableSlot match = null;
            if (timetableSlots.isEmpty()) {
                throw new IncorrectActionException("Cannot remove timetable slot! No slots exist.");
            }
            for (TimetableSlot slot : timetableSlots) {
                if (slot.getDate().equals(date) && slot.getTime().equals(time) && slot.getRoom().equals(room) && slot.getModule().equals(module)) {
                    match = slot;
                    break;
                }
            }

            if (match == null) {
                throw new IncorrectActionException("Cannot remove timetable slot! Matching slot not found.");
            } else {
                timetableSlots.remove(match);
            response = "LECTURE SUCCESSFULLY REMOVED: " + date + "|" + time + "|" + room + "|" + module;
            }
        } else if (option.equals("DISPLAY")) {
            if (timetableSlots == null || timetableSlots.isEmpty()) {
                throw new IncorrectActionException("No timetable slots found!");
            } else {
                StringBuilder sb = new StringBuilder("DISPLAY: ");
                int count = 0;
                for (TimetableSlot slot : timetableSlots) {
                    count++;
                    sb.append(count + ") " + slot.getDate() + "|" + slot.getTime() + "|" + slot.getRoom() + "|" + slot.getModule() + ". ");
                }
                response = sb.toString();
            }
        }
        return response;
    }

    public ObservableList<TimetableSlot> getTimetableSlots() {
        return timetableSlots;
    }
}

