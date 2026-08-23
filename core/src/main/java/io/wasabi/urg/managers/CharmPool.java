package io.wasabi.urg.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import io.wasabi.urg.elements.charm.*;

public class CharmPool {
    private static final List<Supplier<Charm>> CHARM_SUPPLIERS = List.of(
        BlackCharm::new,
        EraserCharm::new,
        GoldCharm::new,
        MetallicCharm::new,
        MirrorCharm::new,
        RedCharm::new,
        ScrambledCharm::new,
        StrengthCharm::new,
        WeakeningCharm::new
    );

    private final Random random = new Random();

    private final List<Class<? extends Charm>> checkedOut = new ArrayList<>();

    public Charm getRandomCharm() {
        List<Supplier<Charm>> available = new ArrayList<>();
        for (Supplier<Charm> supplier : CHARM_SUPPLIERS) {
            Charm sample = supplier.get();
            if (!checkedOut.contains(sample.getClass())) {
                available.add(supplier);
            }
        }
        if (available.isEmpty()) {
            return null;
        }
        Supplier<Charm> supplier = available.get(random.nextInt(available.size()));
        Charm charm = supplier.get();
        checkedOut.add(charm.getClass());
        return charm;
    }

    public void returnCharm(Charm charm) {
        if (charm != null) {
            checkedOut.remove(charm.getClass());
        }
    }
}
