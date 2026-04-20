import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
                } catch (InterruptedException e) {
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

    private static final ArrayList<TimetableSlot> timetableSlots = new ArrayList<>();

    public String clientRequest(String request) throws InvalidInputException, IncorrectActionException, InterruptedException {
        String response = "";

        if (request.equals("Clear Timetable Slots.")) {
            synchronized (timetableSlots) {
                if (timetableSlots.isEmpty()) {
                    throw new InvalidInputException("Error - No Timetable Slots to Clear!");
                }
                timetableSlots.clear();
            }
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

            synchronized (timetableSlots) {
                for (TimetableSlot slot : timetableSlots) {
                    if (slot.getDate().equals(date) && slot.getTime().equals(time) && slot.getRoom().equals(room)) {
                        throw new InvalidInputException("Clash: Timetable Slot already exists for that room, date, and time!");
                    } else if (slot.getDate().equals(date) && slot.getTime().equals(time)) {
                        throw new InvalidInputException("Clash: Timetable slot for a course module already exists for that date and time!");
                    }
                }

                timetableSlots.add(new TimetableSlot(date, time, room, module));
            }
            response = "LECTURE SUCCESSFULLY ADDED: " + date + "|" + time + "|" + room + "|" + module;
        } else if (option.equals("REMOVE")) {

            TimetableSlot match = null;
            synchronized (timetableSlots) {
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
                }
            }

            response = "LECTURE SUCCESSFULLY REMOVED: " + date + "|" + time + "|" + room + "|" + module;
        } else if (option.equals("EARLY LECTURES")) {
            synchronized (timetableSlots) {
                if (timetableSlots.isEmpty()) {
                    throw new InvalidInputException("Error - Cannot shift timetable slots! No slots exist.");
                }
            }

            try {
                SchedulingTask task = new SchedulingTask(timetableSlots);
                response = task.call();
            } catch (Exception e) {
                response = e.getMessage();
            }

        } else if (option.equals("DISPLAY")) {
            synchronized (timetableSlots) {
                if (timetableSlots == null || timetableSlots.isEmpty()) {
                    throw new InvalidInputException("Error - No timetable slots found!");
                } else {
                    StringBuilder sb = new StringBuilder("DISPLAY: ");
                    for (TimetableSlot slot : timetableSlots) {
                        sb.append(slot.getDate()).append("|").append(slot.getTime()).append("|").append(slot.getRoom()).append("|").append(slot.getModule()).append(". ");
                    }
                    response = sb.toString();
                }
            }

        } else if (option.equals("OTHER")) {
            throw new IncorrectActionException("Error - Server does not support this service!");
        }
        return response;
    }
}
