import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TimetableRow {
    private String time = "";
    private String monday = "";
    private String tuesday = "";
    private String wednesday = "";
    private String thursday = "";
    private String friday = "";

    public TimetableRow(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getMonday() {
        return monday;
    }

    public String getTuesday() {
        return tuesday;
    }

    public String getWednesday() {
        return wednesday;
    }

    public String getThursday() {
        return thursday;
    }

    public String getFriday() {
        return friday;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setMonday(String text) {
        this.monday = text;
    }

    public void setTuesday(String text) {
        this.tuesday = text;
    }

    public void setWednesday(String text) {
        this.wednesday = text;
    }

    public void setThursday(String text) {
        this.thursday = text;
    }

    public void setFriday(String text) {
        this.friday = text;
    }
}
