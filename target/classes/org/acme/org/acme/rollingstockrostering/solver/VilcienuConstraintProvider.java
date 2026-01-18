package org.acme.rollingstockrostering.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.acme.rollingstockrostering.domain.Brauciens;
import org.acme.rollingstockrostering.domain.Vilciens;

public class VilcienuConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            // Hard constraints
            trainCannotBeInTwoPlacesAtOnce(factory),
            trainContinuity(factory),
            firstTripMustStartFromDepo(factory),
            lastTripMustEndAtDepo(factory),
            
            // Soft constraints
            maximizePassengers(factory),
            minimizeTrainsUsed(factory)
        };
    }

    /**
     * HARD: A train cannot be assigned to two overlapping trips.
     */
    Constraint trainCannotBeInTwoPlacesAtOnce(ConstraintFactory factory) {
        return factory.forEachUniquePair(Brauciens.class,
                Joiners.equal(Brauciens::getVilciens))
            .filter((b1, b2) -> {
                if (b1.getVilciens() == null) return false;
                return b1.overlapsInTime(b2);
            })
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Vilciens nevar būt divās vietās vienlaicīgi");
    }

    /**
     * HARD: Train must be at correct location for next trip.
     * Checks that for any two trips assigned to the same train,
     * if they are consecutive (no other trips in between),
     * the first trip must end where the second trip starts.
     */
    Constraint trainContinuity(ConstraintFactory factory) {
        return factory.forEachUniquePair(Brauciens.class,
                Joiners.equal(Brauciens::getVilciens))
            .filter((b1, b2) -> {
                if (b1.getVilciens() == null) return false;
                
                // Determine which trip comes first
                Brauciens first = b1.getSakumaLaiks().isBefore(b2.getSakumaLaiks()) ? b1 : b2;
                Brauciens second = b1.getSakumaLaiks().isBefore(b2.getSakumaLaiks()) ? b2 : b1;
                
                // If trips overlap in time, this is already caught by another constraint
                if (first.overlapsInTime(second)) return false;
                
                // If second trip starts before first ends, skip (impossible)
                if (second.getSakumaLaiks().isBefore(first.getBeidzasLaiks())) return false;
                
                // Check if they can transition (must end where next starts)
                return !first.canTransitionTo(second);
            })
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Vilciena nepārtrauktība - jābūt pareizajā stacijā");
    }
    
    /**
     * HARD: Train's first trip MUST start from its depo station.
     * This is a hard requirement - every train must begin at its assigned depot.
     */
    Constraint firstTripMustStartFromDepo(ConstraintFactory factory) {
        return factory.forEach(Brauciens.class)
            .filter(b -> b.getVilciens() != null)
            // Check if this trip does NOT start from depo
            .filter(b -> !b.getSakumaStacijaId().equals(b.getVilciens().getDepoStacijaId()))
            // Check if there's no earlier trip for this train
            .ifNotExists(Brauciens.class,
                Joiners.equal(Brauciens::getVilciens, Brauciens::getVilciens),
                Joiners.lessThan(Brauciens::getSakumaLaiks, Brauciens::getSakumaLaiks))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Pirmajam braucienam jāsākas no depo");
    }
    
    /**
     * HARD: Train's last trip MUST end at its depo station.
     * This is a hard requirement - every train must return to its assigned depot.
     */
    Constraint lastTripMustEndAtDepo(ConstraintFactory factory) {
        return factory.forEach(Brauciens.class)
            .filter(b -> b.getVilciens() != null)
            // Check if this trip does NOT end at depo
            .filter(b -> !b.getBeidzasStacijaId().equals(b.getVilciens().getDepoStacijaId()))
            // Check if there's no later trip for this train
            .ifNotExists(Brauciens.class,
                Joiners.equal(Brauciens::getVilciens, Brauciens::getVilciens),
                Joiners.greaterThan(Brauciens::getSakumaLaiks, Brauciens::getSakumaLaiks))
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Pēdējam braucienam jābeidzas depo");
    }

    /**
     * SOFT: Maximize passengers served.
     */
    Constraint maximizePassengers(ConstraintFactory factory) {
        return factory.forEach(Brauciens.class)
            .filter(b -> b.getVilciens() != null)
            .reward(HardSoftScore.ONE_SOFT, Brauciens::getKopejaisPasazieru)
            .asConstraint("Pasažieru maksimizācija");
    }

    /**
     * SOFT: Minimize number of trains used.
     */
    Constraint minimizeTrainsUsed(ConstraintFactory factory) {
        return factory.forEach(Brauciens.class)
            .filter(b -> b.getVilciens() != null)
            .groupBy(Brauciens::getVilciens)
            .penalize(HardSoftScore.ofSoft(500))
            .asConstraint("Vilcienu skaita minimizācija");
    }
}
