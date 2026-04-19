import javafx.beans.property.StringProperty;

public class TimetableSlot {
    private String date;
    private String time;
    private String room;
    private String module;

    public TimetableSlot(String date, String time, String room, String module) {
        this.date = date;
        this.time = time;
        this.room = room;
        this.module = module;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public String getStartTime() {
        String[] parts = time.split("-");
        return parts[0];
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
