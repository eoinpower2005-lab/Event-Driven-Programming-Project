import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class SchedulingTask extends Task<String> {
    private final List<TimetableSlot> timetableSlots;

    public SchedulingTask(List<TimetableSlot> timetableSlots) {
        this.timetableSlots = timetableSlots;
    }

    @Override
    public String call() {
        List<String> slotDates = new ArrayList<>();
        synchronized (timetableSlots) {
            for (TimetableSlot slot : timetableSlots) {
                if (!slotDates.contains(slot.getDate())) {
                    slotDates.add(slot.getDate());
                }
            }
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ShiftDays sd = new ShiftDays(slotDates, timetableSlots);
        try {
            forkJoinPool.invoke(sd);
        } finally {
            forkJoinPool.shutdown();
        }

        if (!sd.isShifted()) {
            return "Error - Lectures could not be shifted. Morning slots are already occupied!";
        }

        StringBuilder sb = new StringBuilder("DISPLAY EARLY LECTURES: ");
        synchronized (timetableSlots) {
            for (TimetableSlot slot : timetableSlots) {
                sb.append(slot.getDate()).append("|").append(slot.getTime()).append("|").append(slot.getRoom()).append("|").append(slot.getModule()).append(".");
            }
        }
        return sb.toString();
    }
}
