package MySQL.Donators;

import General.CustomObservableList;
import General.CustomObservableMap;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

public class DonatorBean extends Observable {

    private CustomObservableMap<Long, DonatorBeanSlot> slots;

    public DonatorBean(HashMap<Long, DonatorBeanSlot> slots) { this.slots = new CustomObservableMap<>(slots); }


    /* Getters */

    public CustomObservableMap<Long, DonatorBeanSlot> getMap() { return slots; }

    public DonatorBeanSlot get(long userId) {
        return slots.computeIfAbsent(userId, key -> new DonatorBeanSlot(
                userId,
                LocalDate.now()
        ));
    }

}