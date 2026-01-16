package org.acme.rollingstockrostering.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Konfiguracija (Configuration) - Problem Fact
 *
 * Global configuration parameters for the scheduling problem.
 * All constraint parameters should be defined here to avoid magic numbers.
 *
 * Parameters are organized into categories:
 * 1. Time parameters - intervals, durations
 * 2. Penalty weights - soft constraint weights
 * 3. Capacity parameters - thresholds for capacity constraints
 */
public class Konfiguracija {

    // ========== TIME PARAMETERS ==========

    /**
     * Minimum time interval required between two trains at the same station.
     * Prevents trains from being scheduled too close together.
     */
    private Duration minIntervalBetweenTrains;

    /**
     * Duration a train stops at each station for passenger boarding/alighting.
     */
    private Duration stationStopDuration;

    /**
     * Average travel time between adjacent stations on a route.
     * In a real system, this would be per-station-pair, but simplified here.
     */
    private Duration travelTimeBetweenStations;

    /**
     * Minimum time a train needs to prepare for the next trip after completing one.
     * This includes turnaround time at terminal stations.
     */
    private Duration turnaroundTime;

    // ========== PENALTY WEIGHTS (Soft Constraints) ==========

    /**
     * Penalty weight for each empty trip (zero passengers).
     * Higher values strongly discourage running empty trains.
     */
    private int emptyTripPenalty;

    /**
     * Reward weight for each passenger transported.
     * Higher values prioritize maximizing ridership.
     */
    private int passengerReward;

    /**
     * Penalty weight per minute of delay from scheduled time.
     * Higher values prioritize punctuality.
     */
    private int delayPenaltyPerMinute;

    /**
     * Penalty for each passenger left behind due to capacity.
     * Encourages matching capacity to demand.
     */
    private int leftBehindPassengerPenalty;

    /**
     * Bonus for using fewer unique trains (encourages fleet efficiency).
     * Applied per trip that reuses an already-active train.
     */
    private int trainReuseBonus;

    // ========== CAPACITY PARAMETERS ==========

    /**
     * Percentage of capacity at which crowding penalty starts.
     * E.g., 0.8 means penalty starts at 80% capacity.
     */
    private double crowdingThreshold;

    /**
     * Penalty per passenger when train is above crowding threshold.
     */
    private int crowdingPenaltyPerPassenger;

    // ========== CONSTRUCTORS ==========

    public Konfiguracija() {
        // Set sensible defaults
        this.minIntervalBetweenTrains = Duration.ofMinutes(5);
        this.stationStopDuration = Duration.ofMinutes(2);
        this.travelTimeBetweenStations = Duration.ofMinutes(5);
        this.turnaroundTime = Duration.ofMinutes(15);

        this.emptyTripPenalty = 100;
        this.passengerReward = 1;
        this.delayPenaltyPerMinute = 5;
        this.leftBehindPassengerPenalty = 10;
        this.trainReuseBonus = 50;

        this.crowdingThreshold = 0.85;
        this.crowdingPenaltyPerPassenger = 2;
    }

    /**
     * Constructor with basic time parameters (backward compatible).
     */
    public Konfiguracija(Duration minIntervalBetweenTrains, Duration stationStopDuration) {
        this();
        this.minIntervalBetweenTrains = minIntervalBetweenTrains;
        this.stationStopDuration = stationStopDuration;
    }

    /**
     * Full constructor with all parameters.
     */
    public Konfiguracija(Duration minIntervalBetweenTrains, Duration stationStopDuration,
                         Duration travelTimeBetweenStations, Duration turnaroundTime,
                         int emptyTripPenalty, int passengerReward, int delayPenaltyPerMinute,
                         int leftBehindPassengerPenalty, int trainReuseBonus,
                         double crowdingThreshold, int crowdingPenaltyPerPassenger) {
        this.minIntervalBetweenTrains = minIntervalBetweenTrains;
        this.stationStopDuration = stationStopDuration;
        this.travelTimeBetweenStations = travelTimeBetweenStations;
        this.turnaroundTime = turnaroundTime;
        this.emptyTripPenalty = emptyTripPenalty;
        this.passengerReward = passengerReward;
        this.delayPenaltyPerMinute = delayPenaltyPerMinute;
        this.leftBehindPassengerPenalty = leftBehindPassengerPenalty;
        this.trainReuseBonus = trainReuseBonus;
        this.crowdingThreshold = crowdingThreshold;
        this.crowdingPenaltyPerPassenger = crowdingPenaltyPerPassenger;
    }

    // ========== HELPER METHODS ==========

    /**
     * Get minimum interval between trains in minutes.
     */
    public long getMinIntervalMinutes() {
        return minIntervalBetweenTrains.toMinutes();
    }

    /**
     * Get station stop duration in minutes.
     */
    public long getStopDurationMinutes() {
        return stationStopDuration.toMinutes();
    }

    /**
     * Get travel time between stations in minutes.
     */
    public long getTravelTimeMinutes() {
        return travelTimeBetweenStations.toMinutes();
    }

    /**
     * Get turnaround time in minutes.
     */
    public long getTurnaroundMinutes() {
        return turnaroundTime.toMinutes();
    }

    // ========== GETTERS AND SETTERS ==========

    public Duration getMinIntervalBetweenTrains() {
        return minIntervalBetweenTrains;
    }

    public void setMinIntervalBetweenTrains(Duration minIntervalBetweenTrains) {
        this.minIntervalBetweenTrains = minIntervalBetweenTrains;
    }

    public Duration getStationStopDuration() {
        return stationStopDuration;
    }

    public void setStationStopDuration(Duration stationStopDuration) {
        this.stationStopDuration = stationStopDuration;
    }

    public Duration getTravelTimeBetweenStations() {
        return travelTimeBetweenStations;
    }

    public void setTravelTimeBetweenStations(Duration travelTimeBetweenStations) {
        this.travelTimeBetweenStations = travelTimeBetweenStations;
    }

    public Duration getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(Duration turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getEmptyTripPenalty() {
        return emptyTripPenalty;
    }

    public void setEmptyTripPenalty(int emptyTripPenalty) {
        this.emptyTripPenalty = emptyTripPenalty;
    }

    public int getPassengerReward() {
        return passengerReward;
    }

    public void setPassengerReward(int passengerReward) {
        this.passengerReward = passengerReward;
    }

    public int getDelayPenaltyPerMinute() {
        return delayPenaltyPerMinute;
    }

    public void setDelayPenaltyPerMinute(int delayPenaltyPerMinute) {
        this.delayPenaltyPerMinute = delayPenaltyPerMinute;
    }

    public int getLeftBehindPassengerPenalty() {
        return leftBehindPassengerPenalty;
    }

    public void setLeftBehindPassengerPenalty(int leftBehindPassengerPenalty) {
        this.leftBehindPassengerPenalty = leftBehindPassengerPenalty;
    }

    public int getTrainReuseBonus() {
        return trainReuseBonus;
    }

    public void setTrainReuseBonus(int trainReuseBonus) {
        this.trainReuseBonus = trainReuseBonus;
    }

    public double getCrowdingThreshold() {
        return crowdingThreshold;
    }

    public void setCrowdingThreshold(double crowdingThreshold) {
        this.crowdingThreshold = crowdingThreshold;
    }

    public int getCrowdingPenaltyPerPassenger() {
        return crowdingPenaltyPerPassenger;
    }

    public void setCrowdingPenaltyPerPassenger(int crowdingPenaltyPerPassenger) {
        this.crowdingPenaltyPerPassenger = crowdingPenaltyPerPassenger;
    }

    // Backward compatibility aliases
    public Duration getAttalumsStarpVilcieniem() {
        return minIntervalBetweenTrains;
    }

    public void setAttalumsStarpVilcieniem(Duration attalumsStarpVilcieniem) {
        this.minIntervalBetweenTrains = attalumsStarpVilcieniem;
    }

    public Duration getStavesanasLaiks() {
        return stationStopDuration;
    }

    public void setStavesanasLaiks(Duration stavesanasLaiks) {
        this.stationStopDuration = stavesanasLaiks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Konfiguracija that = (Konfiguracija) o;
        return emptyTripPenalty == that.emptyTripPenalty &&
               passengerReward == that.passengerReward &&
               Objects.equals(minIntervalBetweenTrains, that.minIntervalBetweenTrains) &&
               Objects.equals(stationStopDuration, that.stationStopDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minIntervalBetweenTrains, stationStopDuration, emptyTripPenalty, passengerReward);
    }

    @Override
    public String toString() {
        return "Konfiguracija{" +
                "minIntervalBetweenTrains=" + minIntervalBetweenTrains +
                ", stationStopDuration=" + stationStopDuration +
                ", travelTimeBetweenStations=" + travelTimeBetweenStations +
                ", turnaroundTime=" + turnaroundTime +
                ", emptyTripPenalty=" + emptyTripPenalty +
                ", passengerReward=" + passengerReward +
                ", delayPenaltyPerMinute=" + delayPenaltyPerMinute +
                '}';
    }
}
