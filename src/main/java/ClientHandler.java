import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private Socket socket;
    private int client;

    public ClientHandler(Socket socket, int client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        BufferedReader input;
        PrintWriter output;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            String message;
            while ((message = input.readLine()) != null) {
                if (message.equalsIgnoreCase("STOP")) {
                    output.println("TERMINATE");
                    break;
                }

                String response;
                try {
                    response = clientRequest(message);
                } catch (InvalidInputException e) {
                    response = e.getMessage();
                } catch (IncorrectActionException e) {
                    response = e.getMessage();
                }

                output.println(response);
            }

        } catch (IOException e) {
            System.out.println("Error : " + e.getMessage());
        } finally {
            try {
                System.out.println("\n* Client " + client + " disconnecting... *");
                System.out.println("* Connection closed... *");
                socket.close();
            } catch (IOException e) {
                System.out.println("Unable to close connection: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private final ArrayList<TimetableSlot> timetableSlots = new ArrayList<>();

    public String clientRequest(String request) throws InvalidInputException, IncorrectActionException {
        String response = "";

        if (request.equals("Clear Timetable Slots.")) {
            if (timetableSlots.isEmpty()) {
                throw new InvalidInputException("Error - No Timetable Slots to Clear!");
            }
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

            for (TimetableSlot slot : timetableSlots) {
                if (slot.getDate().equals(date) && slot.getTime().equals(time) && slot.getRoom().equals(room)) {
                    throw new InvalidInputException("Clash: Timetable Slot already exists for that room, date, and time!");
                }
            }

            timetableSlots.add(new TimetableSlot(date, time, room, module));
            response = "LECTURE SUCCESSFULLY ADDED: " + date + "|" + time + "|" + room + "|" + module;
        } else if (option.equals("REMOVE")) {

            TimetableSlot match = null;
            if (timetableSlots.isEmpty()) {
                throw new InvalidInputException("Error - Cannot remove timetable slot! No slots exist.");
            }
            for (TimetableSlot slot : timetableSlots) {
                if (slot.getDate().equals(date) && slot.getTime().equals(time) && slot.getRoom().equals(room) && slot.getModule().equals(module)) {
                    match = slot;
                    break;
                }
            }

            if (match == null) {
                throw new InvalidInputException("Error - Cannot remove timetable slot! Matching slot not found.");
            } else {
                timetableSlots.remove(match);
                response = "LECTURE SUCCESSFULLY REMOVED: " + date + "|" + time + "|" + room + "|" + module;
            }
        } else if (option.equals("DISPLAY")) {
            if (timetableSlots == null || timetableSlots.isEmpty()) {
                throw new InvalidInputException("Error - No timetable slots found!");
            } else {
                StringBuilder sb = new StringBuilder("DISPLAY: ");
                int count = 0;
                for (TimetableSlot slot : timetableSlots) {
                    count++;
                    sb.append(count + ") " + slot.getDate() + "|" + slot.getTime() + "|" + slot.getRoom() + "|" + slot.getModule() + ". ");
                }
                response = sb.toString();
            }
        } else if (option.equals("OTHER")) {
            throw new IncorrectActionException("Error - Server does not support this service!");
        }
        return response;
    }
}
