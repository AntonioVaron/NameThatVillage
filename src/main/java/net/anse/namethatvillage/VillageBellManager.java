package net.anse.namethatvillage;

import net.anse.namethatvillage.block.entity.VillageBellBlockEntity;

import java.util.HashSet;
import java.util.Set;

public class VillageBellManager {
    private static final Set<VillageBellBlockEntity> bells = new HashSet<>();

    public static void registerBell(VillageBellBlockEntity bell) {
        bells.add(bell);
    }

    public static void unregisterBell(VillageBellBlockEntity bell) {
        bells.remove(bell);
    }

    public static Set<VillageBellBlockEntity> getAllBells() {
        return bells;
    }

    public static boolean isTooCloseToOtherBell(VillageBellBlockEntity bell, double radius) {
        for (VillageBellBlockEntity other : getAllBells()) {
            if (other == bell) continue;
            if (other.getLevel() != bell.getLevel()) continue;
            if (other.getBlockPos().closerThan(bell.getBlockPos(), radius)) {
                return true;
            }
        }
        return false;
    }

}
