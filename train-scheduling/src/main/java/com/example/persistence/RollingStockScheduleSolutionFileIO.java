package com.example.persistence;

import ai.timefold.solver.jackson.api.TimefoldJacksonModule;
import ai.timefold.solver.persistence.common.api.domain.solution.SolutionFileIO;
import com.example.domain.RollingStockSchedule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;

/**
 * SolutionFileIO implementation for reading and writing RollingStockSchedule
 * to/from JSON files for benchmarking purposes.
 */
public class RollingStockScheduleSolutionFileIO implements SolutionFileIO<RollingStockSchedule> {

    private final ObjectMapper objectMapper;

    public RollingStockScheduleSolutionFileIO() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(TimefoldJacksonModule.createModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String getInputFileExtension() {
        return "json";
    }

    @Override
    public RollingStockSchedule read(File inputSolutionFile) {
        try {
            return objectMapper.readValue(inputSolutionFile, RollingStockSchedule.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read solution from file: " + inputSolutionFile, e);
        }
    }

    @Override
    public void write(RollingStockSchedule solution, File outputSolutionFile) {
        try {
            objectMapper.writeValue(outputSolutionFile, solution);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to write solution to file: " + outputSolutionFile, e);
        }
    }
}
