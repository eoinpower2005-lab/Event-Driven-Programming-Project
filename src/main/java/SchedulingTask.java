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
    public String call() throws Exception {
        updateMessage("Shifting Scheduled Lectures to Morning Slots");
        List<String> slotDates = new ArrayList<>();
        synchronized (timetableSlots) {
            for (TimetableSlot slot : timetableSlots) {
                if (!slotDates.contains(slot.getDate())) {
                    slotDates.add(slot.getDate());
                }
            }
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        try {
            forkJoinPool.invoke(new ShiftDays(slotDates, timetableSlots));
        } finally {
            forkJoinPool.shutdown();
        }
        updateMessage("Scheduled Lectures Shifted to Morning Slots");
        return "Lectures Shifted to Morning Slots";
    }
}
