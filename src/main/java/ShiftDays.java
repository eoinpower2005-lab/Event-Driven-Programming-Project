import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class ShiftDays extends RecursiveAction {
    private List<String> dates;
    private final List<TimetableSlot> timetableSlots;
    private final String[] times = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"};

    public ShiftDays(List<String> dates, List<TimetableSlot> timetableSlots) {
        this.dates = dates;
        this.timetableSlots = timetableSlots;
    }

    private boolean shifted = true;

    @Override
    protected void compute() {
        if (dates.size() == 1) {
            shiftToMorning(dates.get(0));
        } else {
            int mid = dates.size() / 2;
            ShiftDays left = new ShiftDays(dates.subList(0, mid), timetableSlots);
            ShiftDays right = new ShiftDays(dates.subList(mid, dates.size()), timetableSlots);
            left.fork();
            right.compute();
            left.join();

            if (!left.shifted || !right.shifted) {
                shifted = false;
            }
        }
    }

    private void shiftToMorning(String date) {
        synchronized (timetableSlots) {
            List<TimetableSlot> daySlots = new ArrayList<>();
            for (TimetableSlot timetableSlot : timetableSlots) {
                if (timetableSlot.getDate().equals(date)) {
                    daySlots.add(timetableSlot);
                }
            }

            if (daySlots.isEmpty()) {
                return;
            }

            daySlots.sort(Comparator.comparing(TimetableSlot::getStartTime));

            for (int i = 0; i < daySlots.size(); i++) {
                String shiftedTime = times[i];
                for (TimetableSlot slot : timetableSlots) {
                    if (slot.getDate().equals(date) && slot.getTime().equals(shiftedTime) && !daySlots.contains(slot)) {
                        shifted = false;
                        return;
                    }
                }
            }

            for (int i = 0; i < daySlots.size(); i++) {
                daySlots.get(i).setTime(times[i]);
            }
        }
    }

    public boolean shifted() {
        return shifted;
    }
}
