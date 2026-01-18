package com.example.domain;

import java.time.LocalTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Setter @Getter @NoArgsConstructor
public class Trip implements Standstill {

    @PlanningId
    private Long id;

    private Route route;
    private Station station;
    private int stationIndex;

    @PlanningVariable(valueRangeProviderRefs = "standstillRange", graphType = PlanningVariableGraphType.CHAINED)
    private Standstill previousStandstill;

    @PlanningVariable(valueRangeProviderRefs = "timeRange")
    private LocalTime departureTime;

    @JsonIgnore
    @ShadowVariable(variableListenerClass = NextTripVariableListener.class, sourceVariableName = "previousStandstill")
    private Trip nextTrip;

    // @JsonIgnore
    @ShadowVariable(variableListenerClass = TrainVariableListener.class, sourceVariableName = "previousStandstill")
    private Train train;

    @ShadowVariable(variableListenerClass = ArrivalTimeVariableListener.class, sourceVariableName = "previousStandstill")
    @ShadowVariable(variableListenerClass = ArrivalTimeVariableListener.class, sourceVariableName = "departureTime")
    private LocalTime arrivalTime;

    @ShadowVariable(variableListenerClass = PassengerCountVariableListener.class, sourceVariableName = "previousStandstill")
    @ShadowVariable(variableListenerClass = PassengerCountVariableListener.class, sourceVariableName = "departureTime")
    private Integer passengerCount;

    public Trip(Long id, Route route, int stationIndex) {
        this.id = id;
        this.route = route;
        this.station = route.getStations().get(stationIndex);
        this.stationIndex = stationIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
