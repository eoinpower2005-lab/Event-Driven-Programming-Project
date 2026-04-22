import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class SchedulingTask extends Task<String> {
    private final List<TimetableSlot> timetableSlots;
    private final String course;

    public SchedulingTask(List<TimetableSlot> timetableSlots, String course) {
        this.timetableSlots = timetableSlots;
        this.course = course;
    }

    @Override
    public String call() {
        List<String> slotDates = new ArrayList<>();
        synchronized (timetableSlots) {
            for (TimetableSlot slot : timetableSlots) {
                if (slot.getCourse().equals(course) && !slotDates.contains(slot.getDate())) {
                    slotDates.add(slot.getDate());
                }
            }
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ShiftDays sd = new ShiftDays(slotDates, timetableSlots, course);
        try {
            forkJoinPool.invoke(sd);
        } finally {
            forkJoinPool.shutdown();
        }

        if (!sd.isShifted()) {
            return "Error - Some lectures could not be shifted. Morning slots are already occupied!";
        }

        StringBuilder sb = new StringBuilder("DISPLAY EARLY LECTURES: ");
        synchronized (timetableSlots) {
            for (TimetableSlot slot : timetableSlots) {
                if (slot.getCourse().equals(course)) {
                    sb.append(slot.getCourse()).append("|").append(slot.getDate()).append("|").append(slot.getTime()).append("|").append(slot.getRoom()).append("|").append(slot.getModule()).append(".");
                }
            }
        }
        return sb.toString();
    }
}
