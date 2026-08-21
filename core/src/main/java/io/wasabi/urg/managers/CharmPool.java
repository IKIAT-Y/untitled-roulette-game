package io.wasabi.urg.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import io.wasabi.urg.elements.charm.AbstractCharm;
import io.wasabi.urg.elements.charm.BlackCharm;
import io.wasabi.urg.elements.charm.RedCharm;
import io.wasabi.urg.elements.charm.ScrambledCharm;

public class CharmPool {
    private static final List<Supplier<AbstractCharm>> CHARM_SUPPLIERS = List.of(
        BlackCharm::new,
        RedCharm::new,
        ScrambledCharm::new
    );

    private final Random random = new Random();

    private final List<Class<? extends AbstractCharm>> checkedOut = new ArrayList<>();

    public AbstractCharm getRandomCharm() {
        List<Supplier<AbstractCharm>> available = new ArrayList<>();
        for (Supplier<AbstractCharm> supplier : CHARM_SUPPLIERS) {
            AbstractCharm sample = supplier.get();
            if (!checkedOut.contains(sample.getClass())) {
                available.add(supplier);
            }
        }
        if (available.isEmpty()) {
            return null;
        }
        Supplier<AbstractCharm> supplier = available.get(random.nextInt(available.size()));
        AbstractCharm charm = supplier.get();
        checkedOut.add(charm.getClass());
        return charm;
    }

    public void returnCharm(AbstractCharm charm) {
        if (charm != null) {
            checkedOut.remove(charm.getClass());
        }
    }
}
