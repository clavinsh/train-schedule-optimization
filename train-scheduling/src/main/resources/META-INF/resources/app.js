// Train Schedule Optimization - Frontend Application

let autoRefreshIntervalId = null;
let scheduleId = null;
let loadedSchedule = null;
let viewType = "train";

// Timeline configurations
const timelineOptions = {
    timeAxis: { scale: "hour", step: 2 },
    orientation: { axis: "top" },
    stack: true,
    xss: { disabled: true },
    zoomMin: 1000 * 60 * 60 * 2,  // 2 hours
    zoomMax: 1000 * 60 * 60 * 24, // 24 hours
    margin: { item: 5 },
    // Subtle animations for smoother updates during solving
    animation: {
        duration: 200,  // Reduced from default 500ms
        easingFunction: 'linear'
    }
};

// Timeline instances
let trainTimelinePanel, trainTimeline, trainGroupData, trainItemData;
let routeTimelinePanel, routeTimeline, routeGroupData, routeItemData;

// Color palette for routes
const routeColors = [
    '#3498db', '#e74c3c', '#2ecc71', '#9b59b6', '#f39c12',
    '#1abc9c', '#e67e22', '#34495e', '#16a085', '#c0392b'
];

$(document).ready(function () {
    initializeTimelines();
    setupEventHandlers();
    setupAjax();
    refreshSchedule();
});

function initializeTimelines() {
    // Train timeline
    trainTimelinePanel = document.getElementById("trainTimeline");
    trainGroupData = new vis.DataSet();
    trainItemData = new vis.DataSet();
    trainTimeline = new vis.Timeline(trainTimelinePanel, trainItemData, trainGroupData, timelineOptions);

    // Route timeline
    routeTimelinePanel = document.getElementById("routeTimeline");
    routeGroupData = new vis.DataSet();
    routeItemData = new vis.DataSet();
    routeTimeline = new vis.Timeline(routeTimelinePanel, routeItemData, routeGroupData, timelineOptions);
}

function setupEventHandlers() {
    $("#solveButton").click(solve);
    $("#stopSolvingButton").click(stopSolving);
    $("#analyzeButton").click(analyze);
    $("#exportButton").click(exportScheduleJson);
    $("#importButton").click(() => $("#importFileInput").click());
    $("#importFileInput").change(importScheduleJson);

    $("#byTrainTab").click(function () {
        viewType = "train";
        setTimeout(() => trainTimeline.redraw(), 100);
    });

    $("#byRouteTab").click(function () {
        viewType = "route";
        setTimeout(() => routeTimeline.redraw(), 100);
    });

    // Redraw timelines when tabs are shown
    $('#byTrainTab').on('shown.bs.tab', () => trainTimeline.redraw());
    $('#byRouteTab').on('shown.bs.tab', () => routeTimeline.redraw());
}

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain'
        }
    });

    // Extend jQuery for PUT and DELETE
    jQuery.each(["put", "delete"], function (i, method) {
        jQuery[method] = function (url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });
}

function refreshSchedule() {
    let path = scheduleId ? `/schedules/${scheduleId}` : "/demo-data";

    $.getJSON(path, function (schedule) {
        loadedSchedule = schedule;
        renderSchedule(schedule);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Getting the schedule has failed.", xhr);
        refreshSolvingButtons(false);
    });
}

function renderSchedule(schedule) {
    // Update stats
    $("#trainCount").text(schedule.trains ? schedule.trains.length : 0);
    $("#routeCount").text(schedule.routes ? schedule.routes.length : 0);
    $("#departureCount").text(schedule.departureTimes ? schedule.departureTimes.length : 0);
    $("#stationCount").text(schedule.stations ? schedule.stations.length : 0);

    // Update score
    const scoreText = schedule.score ? schedule.score : "Not calculated";
    $("#score").text(`Score: ${scoreText}`);
    if (schedule.score && schedule.score.includes("-")) {
        $("#score").removeClass("bg-success bg-secondary").addClass("bg-warning");
    } else if (schedule.score && !schedule.score.includes("-")) {
        $("#score").removeClass("bg-warning bg-secondary").addClass("bg-success");
    } else {
        $("#score").removeClass("bg-success bg-warning").addClass("bg-secondary");
    }

    // Check solver status
    const isSolving = schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING";
    refreshSolvingButtons(isSolving);

    // Render visualizations
    renderTrainTimeline(schedule);
    renderRouteTimeline(schedule);
    renderUnassignedDepartures(schedule);
}

function renderTrainTimeline(schedule) {
    trainGroupData.clear();
    trainItemData.clear();

    if (!schedule.trains || !schedule.departureTimes) return;

    // Create groups for each train with trip count
    schedule.trains.forEach(train => {
        const assignedTrips = schedule.departureTimes.filter(d => d.train && d.train.id === train.id).length;
        trainGroupData.add({
            id: train.id,
            content: `<div class="fw-bold"><i class="fas fa-train me-1"></i>Train ${train.id}</div>
                      <small class="text-muted">Cap: ${train.capacity} | Trips: ${assignedTrips}</small>`
        });
    });

    // Add "Unassigned" group
    const unassignedCount = schedule.departureTimes.filter(d => !d.train || !d.departureTime).length;
    trainGroupData.add({
        id: "unassigned",
        content: `<div class="fw-bold text-danger"><i class="fas fa-exclamation-circle me-1"></i>Unassigned (${unassignedCount})</div>`
    });

    // Create route color map
    const routeColorMap = {};
    if (schedule.routes) {
        schedule.routes.forEach((route, idx) => {
            routeColorMap[route.id] = routeColors[idx % routeColors.length];
        });
    }

    // Add departure items with realistic trip durations
    schedule.departureTimes.forEach((departure, idx) => {
        const groupId = departure.train ? departure.train.id : "unassigned";
        const routeColor = departure.route ? routeColorMap[departure.route.id] || '#6c757d' : '#6c757d';
        const routeName = departure.route ? departure.route.name : 'Unknown';

        // Calculate trip duration based on route length (3 minutes per station)
        const stationCount = departure.route && departure.route.stations ? departure.route.stations.length : 10;
        const tripDurationMinutes = stationCount * 3;

        // Get first and last station names for tooltip
        let firstStation = 'Unknown', lastStation = 'Unknown';
        if (departure.route && departure.route.stations && departure.route.stations.length > 0) {
            firstStation = departure.route.stations[0].name;
            lastStation = departure.route.stations[departure.route.stations.length - 1].name;
        }

        // Create time for display - use today's date with the departure time
        let startTime, endTime;
        if (departure.departureTime) {
            const today = new Date();
            const timeParts = departure.departureTime.split(':');
            const hours = parseInt(timeParts[0], 10);
            const minutes = parseInt(timeParts[1] || '0', 10);
            startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), hours, minutes);
            endTime = new Date(startTime.getTime() + tripDurationMinutes * 60000);
        } else {
            // For unassigned, spread them out across the day
            const today = new Date();
            startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 6 + (idx % 16), 0);
            endTime = new Date(startTime.getTime() + tripDurationMinutes * 60000);
        }

        // Format times for display
        const formatTime = (date) => {
            return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
        };

        const tooltip = `Route: ${routeName}
From: ${firstStation}
To: ${lastStation}
Departure: ${departure.departureTime || 'Not set'}
Arrival: ${departure.departureTime ? formatTime(endTime) : 'N/A'}
Duration: ${tripDurationMinutes} min (${stationCount} stations)
Train capacity: ${departure.train ? departure.train.capacity : 'N/A'}`;

        trainItemData.add({
            id: departure.id,
            group: groupId,
            content: `<small><b>${routeName}</b></small>`,
            title: tooltip,
            start: startTime,
            end: endTime,
            style: `background-color: ${routeColor}; color: white; border-color: ${routeColor};`
        });
    });

    // Set timeline window to today
    const today = new Date();
    const start = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 5, 0);
    const end = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 0);
    trainTimeline.setWindow(start, end);
}

function renderRouteTimeline(schedule) {
    routeGroupData.clear();
    routeItemData.clear();

    if (!schedule.routes || !schedule.departureTimes) return;

    // Create groups for each route with trip counts
    schedule.routes.forEach((route, idx) => {
        const color = routeColors[idx % routeColors.length];
        const stationCount = route.stations ? route.stations.length : 0;
        const tripCount = schedule.departureTimes.filter(d => d.route && d.route.id === route.id).length;
        const assignedCount = schedule.departureTimes.filter(d => d.route && d.route.id === route.id && d.train && d.departureTime).length;

        // Get first and last station
        let routeRange = '';
        if (route.stations && route.stations.length >= 2) {
            routeRange = `${route.stations[0].name} → ${route.stations[route.stations.length - 1].name}`;
        }

        routeGroupData.add({
            id: route.id,
            content: `<div class="fw-bold" style="color: ${color};">
                        <i class="fas fa-route me-1"></i>${route.name}
                      </div>
                      <small class="text-muted">${stationCount} stops | ${assignedCount}/${tripCount} trips</small>
                      <br><small class="text-muted">${routeRange}</small>`
        });
    });

    // Add departure items grouped by route
    schedule.departureTimes.forEach((departure, idx) => {
        if (!departure.route) return;

        const routeIdx = schedule.routes.findIndex(r => r.id === departure.route.id);
        const routeColor = routeColors[routeIdx % routeColors.length];
        const trainId = departure.train ? departure.train.id : 'N/A';
        const isAssigned = departure.train != null && departure.departureTime != null;

        // Calculate trip duration based on route length (3 minutes per station)
        const stationCount = departure.route.stations ? departure.route.stations.length : 10;
        const tripDurationMinutes = stationCount * 3;

        // Get first and last station for tooltip
        let firstStation = 'Unknown', lastStation = 'Unknown';
        if (departure.route.stations && departure.route.stations.length > 0) {
            firstStation = departure.route.stations[0].name;
            lastStation = departure.route.stations[departure.route.stations.length - 1].name;
        }

        // Create time for display
        let startTime, endTime;
        if (departure.departureTime) {
            const today = new Date();
            const timeParts = departure.departureTime.split(':');
            const hours = parseInt(timeParts[0], 10);
            const minutes = parseInt(timeParts[1] || '0', 10);
            startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), hours, minutes);
            endTime = new Date(startTime.getTime() + tripDurationMinutes * 60000);
        } else {
            const today = new Date();
            startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 6 + (idx % 16), (idx * 5) % 60);
            endTime = new Date(startTime.getTime() + tripDurationMinutes * 60000);
        }

        const bgColor = isAssigned ? routeColor : '#dc3545';

        // Format times for tooltip
        const formatTime = (date) => {
            return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
        };

        const tooltip = `Train: ${trainId}${departure.train ? ` (capacity: ${departure.train.capacity})` : ''}
From: ${firstStation}
To: ${lastStation}
Departure: ${departure.departureTime || 'Not set'}
Arrival: ${departure.departureTime ? formatTime(endTime) : 'N/A'}
Duration: ${tripDurationMinutes} min`;

        routeItemData.add({
            id: `route-${departure.id}`,
            group: departure.route.id,
            content: `<small><b>T${trainId}</b> ${departure.departureTime || '?'}</small>`,
            title: tooltip,
            start: startTime,
            end: endTime,
            style: `background-color: ${bgColor}; color: white; border-color: ${bgColor};`
        });
    });

    // Set timeline window
    const today = new Date();
    const start = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 5, 0);
    const end = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 0);
    routeTimeline.setWindow(start, end);
}

function renderUnassignedDepartures(schedule) {
    const container = $("#unassignedDepartures");
    container.empty();

    if (!schedule.departureTimes) {
        container.append(createSuccessBanner("No departure data available."));
        return;
    }

    const unassigned = schedule.departureTimes.filter(d => !d.train || !d.departureTime);

    if (unassigned.length === 0) {
        container.append(createSuccessBanner("All departures have been assigned!"));
        return;
    }

    // Show first 12 unassigned departures
    unassigned.slice(0, 12).forEach(departure => {
        const stationName = departure.station ? departure.station.name : 'Unknown';
        const routeName = departure.route ? departure.route.name : 'Unknown';
        const missingTrain = !departure.train;
        const missingTime = !departure.departureTime;

        const card = $(`
            <div class="col">
                <div class="card h-100 border-danger">
                    <div class="card-body">
                        <h6 class="card-title">
                            <i class="fas fa-map-marker-alt text-primary me-1"></i>${stationName}
                        </h6>
                        <p class="card-text small mb-1">
                            <i class="fas fa-route me-1"></i>${routeName}
                        </p>
                        <p class="card-text small mb-0">
                            ${missingTrain ? '<span class="badge bg-danger me-1">No Train</span>' : ''}
                            ${missingTime ? '<span class="badge bg-warning">No Time</span>' : ''}
                        </p>
                    </div>
                </div>
            </div>
        `);
        container.append(card);
    });

    if (unassigned.length > 12) {
        container.append($(`
            <div class="col-12">
                <p class="text-muted text-center">... and ${unassigned.length - 12} more unassigned departures</p>
            </div>
        `));
    }
}

function createSuccessBanner(message) {
    return $(`
        <div class="col-12">
            <div class="alert alert-success d-flex align-items-center justify-content-center" role="alert">
                <i class="fas fa-check-circle me-2"></i>
                <span>${message}</span>
            </div>
        </div>
    `);
}

function solve() {
    $.post("/schedules", JSON.stringify(loadedSchedule), function (data) {
        scheduleId = data;
        refreshSolvingButtons(true);
        showSuccess("Solving started!", `Job ID: ${scheduleId}`);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
        refreshSolvingButtons(false);
    }, "text");
}

function stopSolving() {
    if (!scheduleId) return;

    $.delete(`/schedules/${scheduleId}`, function () {
        refreshSolvingButtons(false);
        refreshSchedule();
        showSuccess("Solving stopped.", "The solver has been terminated.");
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function analyze() {
    const modal = new bootstrap.Modal("#scoreAnalysisModal");
    modal.show();

    const content = $("#scoreAnalysisModalContent");
    content.empty();

    if (!loadedSchedule.score) {
        content.html('<p class="text-muted">No score to analyze yet. Please solve first.</p>');
        return;
    }

    $("#scoreAnalysisScoreLabel").text(loadedSchedule.score);

    if (!scheduleId) {
        content.html('<p class="text-muted">Submit the schedule for solving to enable score analysis.</p>');
        return;
    }

    content.html('<div class="text-center"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Loading analysis...</p></div>');

    $.getJSON(`/schedules/${scheduleId}/score-analysis`, function (analysis) {
        renderScoreAnalysis(content, analysis);
    }).fail(function (xhr) {
        content.html(`<p class="text-danger">Failed to load score analysis: ${xhr.statusText}</p>`);
    });
}

function renderScoreAnalysis(container, analysis) {
    container.empty();

    if (!analysis.constraints || analysis.constraints.length === 0) {
        container.html('<p class="text-muted">No constraint data available.</p>');
        return;
    }

    const table = $(`
        <table class="table table-sm">
            <thead>
                <tr>
                    <th></th>
                    <th>Constraint</th>
                    <th class="text-center">Matches</th>
                    <th class="text-end">Score Impact</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    `);

    const tbody = table.find('tbody');

    analysis.constraints.forEach(constraint => {
        const score = constraint.score || "0";
        const isHard = score.includes("hard");
        const isNegative = score.includes("-");
        const matchCount = constraint.matches ? constraint.matches.length : 0;

        let icon = '';
        let rowClass = '';
        if (isHard && isNegative) {
            icon = '<i class="fas fa-exclamation-triangle text-danger"></i>';
            rowClass = 'table-danger';
        } else if (matchCount === 0) {
            icon = '<i class="fas fa-check-circle text-success"></i>';
        } else if (isNegative) {
            icon = '<i class="fas fa-minus-circle text-warning"></i>';
            rowClass = 'table-warning';
        } else {
            icon = '<i class="fas fa-plus-circle text-info"></i>';
        }

        tbody.append(`
            <tr class="${rowClass}">
                <td>${icon}</td>
                <td>${constraint.name}</td>
                <td class="text-center">${matchCount}</td>
                <td class="text-end"><code>${score}</code></td>
            </tr>
        `);
    });

    container.append(table);
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (!autoRefreshIntervalId) {
            autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function showError(title, xhr) {
    let message = "Unknown error";
    if (xhr.responseJSON) {
        message = xhr.responseJSON.message || JSON.stringify(xhr.responseJSON);
    } else if (xhr.statusText) {
        message = `${xhr.status}: ${xhr.statusText}`;
    }

    showToast(title, message, "danger");
}

function showSuccess(title, message) {
    showToast(title, message, "success");
}

function showToast(title, message, type) {
    const toast = $(`
        <div class="toast" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="toast-header bg-${type} text-white">
                <strong class="me-auto">${title}</strong>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
            <div class="toast-body">${message}</div>
        </div>
    `);

    $("#notificationPanel").append(toast);
    const bsToast = new bootstrap.Toast(toast, { delay: 5000 });
    bsToast.show();

    toast.on('hidden.bs.toast', () => toast.remove());
}

function copyTextToClipboard(id) {
    const text = document.getElementById(id).textContent.trim();
    navigator.clipboard.writeText(text).then(() => {
        showSuccess("Copied!", "Command copied to clipboard.");
    }).catch(err => {
        console.error("Failed to copy:", err);
    });
}

function exportScheduleJson() {
    if (!loadedSchedule) {
        showError("Export failed", { statusText: "No schedule loaded" });
        return;
    }

    const dataStr = JSON.stringify(loadedSchedule, null, 2);
    const blob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `train-schedule-${new Date().toISOString().slice(0, 10)}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    showSuccess("Exported!", "Schedule JSON downloaded.");
}

function importScheduleJson(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const schedule = JSON.parse(e.target.result);

            // Basic validation
            if (!schedule.trains || !schedule.routes || !schedule.departureTimes) {
                showError("Import failed", { statusText: "Invalid schedule format. Must contain trains, routes, and departureTimes." });
                return;
            }

            // Reset solver state
            scheduleId = null;
            loadedSchedule = schedule;

            // Render the imported schedule
            renderSchedule(schedule);
            refreshSolvingButtons(false);

            showSuccess("Imported!", `Loaded ${schedule.departureTimes.length} departures, ${schedule.trains.length} trains, ${schedule.routes.length} routes.`);
        } catch (err) {
            showError("Import failed", { statusText: "Invalid JSON: " + err.message });
        }
    };
    reader.readAsText(file);

    // Reset file input so same file can be re-imported
    event.target.value = '';
}
