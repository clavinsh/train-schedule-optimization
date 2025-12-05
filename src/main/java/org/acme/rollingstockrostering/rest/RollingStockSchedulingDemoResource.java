package org.acme.rollingstockrostering.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.rollingstockrostering.domain.RollingStockSchedule;

/**
 * RollingStockSchedulingDemoResource - Demo endpoint for generating test data
 */
@Path("/demo")
@Produces(MediaType.APPLICATION_JSON)
public class RollingStockSchedulingDemoResource {
    
    @Inject
    DemoDataGenerator demoDataGenerator;
    
    /**
     * GET /demo/rolling-stock-schedule
     * Returns a demo schedule with test data
     */
    @GET
    @Path("/rolling-stock-schedule")
    public RollingStockSchedule getDemoSchedule() {
        return demoDataGenerator.generateDemoData();
    }
}
