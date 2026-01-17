// package org.acme.RollingStockRosteringOptimization.domain;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Objects;

// import ai.timefold.solver.core.api.domain.lookup.PlanningId;

// import com.fasterxml.jackson.annotation.JsonIdentityInfo;
// import com.fasterxml.jackson.annotation.ObjectIdGenerators;

// /**
//  * A timetable containing the schedule of rides for a specific route.
//  * Groups all rides that operate on the same route.
//  */
// @JsonIdentityInfo(scope = TimeTable.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
// public class TimeTable {

//     @PlanningId
//     private String id;

//     // The route this timetable is for
//     private Route route;

//     // List of scheduled rides on this route
//     private List<Ride> rides;

//     public TimeTable() {
//         this.rides = new ArrayList<>();
//     }

//     public TimeTable(String id) {
//         this.id = id;
//         this.rides = new ArrayList<>();
//     }

//     public TimeTable(String id, Route route) {
//         this.id = id;
//         this.route = route;
//         this.rides = new ArrayList<>();
//     }

//     public TimeTable(String id, Route route, List<Ride> rides) {
//         this.id = id;
//         this.route = route;
//         this.rides = rides != null ? rides : new ArrayList<>();
//     }

//     /**
//      * Add a ride to this timetable.
//      */
//     public void addRide(Ride ride) {
//         if (rides == null) {
//             rides = new ArrayList<>();
//         }
//         rides.add(ride);
//     }

//     /**
//      * Get the total number of rides in this timetable.
//      */
//     public int getRideCount() {
//         return rides != null ? rides.size() : 0;
//     }

//     @Override
//     public String toString() {
//         return "TimeTable-" + id + "(" + (route != null ? route.getName() : "?") + ")";
//     }

//     // ************************************************************************
//     // Simple getters and setters
//     // ************************************************************************

//     public String getId() {
//         return id;
//     }

//     public void setId(String id) {
//         this.id = id;
//     }

//     public Route getRoute() {
//         return route;
//     }

//     public void setRoute(Route route) {
//         this.route = route;
//     }

//     public List<Ride> getRides() {
//         return rides;
//     }

//     public void setRides(List<Ride> rides) {
//         this.rides = rides;
//     }

//     @Override
//     public boolean equals(Object o) {
//         if (this == o)
//             return true;
//         if (!(o instanceof TimeTable timeTable))
//             return false;
//         return Objects.equals(getId(), timeTable.getId());
//     }

//     @Override
//     public int hashCode() {
//         return getId().hashCode();
//     }
// }
