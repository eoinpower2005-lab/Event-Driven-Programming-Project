import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class ShiftDays extends RecursiveAction {
    private final List<String> dates;
    private String course;
    private final List<TimetableSlot> timetableSlots;
    private final String[] times = {"09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00", "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"};

    public ShiftDays(List<String> dates, List<TimetableSlot> timetableSlots, String course) {
        this.dates = dates;
        this.timetableSlots = timetableSlots;
        this.course = course;
    }

    private boolean shifted = true;

    @Override
    protected void compute() {
        if (dates.size() == 1) {
            shiftToMorning(dates.get(0));
        } else {
            int mid = dates.size() / 2;
            ShiftDays left = new ShiftDays(dates.subList(0, mid), timetableSlots, course);
            ShiftDays right = new ShiftDays(dates.subList(mid, dates.size()), timetableSlots, course);
            left.fork();
            right.compute();
            left.join();
        }
    }

    private void shiftToMorning(String date) {
        synchronized (timetableSlots) {
            List<TimetableSlot> daySlots = new ArrayList<>();
            for (TimetableSlot timetableSlot : timetableSlots) {
                if (timetableSlot.getCourse().equals(course) && timetableSlot.getDate().equals(date)) {
                    daySlots.add(timetableSlot);
                }
            }

            if (daySlots.isEmpty()) {
                return;
            }

            daySlots.sort(Comparator.comparing(TimetableSlot::getStartTime));

            for (int i = 0; i < daySlots.size(); i++) {
                String shiftedTime = times[i];
                String shiftedRoom = daySlots.get(i).getRoom();
                for (TimetableSlot slot : timetableSlots) {
                    if (slot.getDate().equals(date) && slot.getTime().equals(shiftedTime) && slot.getRoom().equals(shiftedRoom) && !daySlots.contains(slot)) {
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

    public boolean isShifted() {
        return shifted;
    }
}
