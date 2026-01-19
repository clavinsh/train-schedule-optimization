package com.example.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.example.domain.TrainSchedule;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo Data", description = "Demo data generation for train scheduling")
@Path("demo-data")
public class DemoDataResource {

    @Inject
    DemoDataGenerator demoDataGenerator;

    @Operation(summary = "Get demo schedule data")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Demo schedule data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TrainSchedule.class)))})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TrainSchedule getDemoData() {
        return demoDataGenerator.generateDemoData();
    }
}
