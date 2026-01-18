// Rolling Stock Rostering - Frontend Application
let autoRefreshIntervalId = null;
let loadedSchedule = null;
let currentView = "trains";

$(document).ready(function () {
    console.log("Rolling Stock Rostering UI initializing...");
    
    $("#solveButton").click(function () {
        solve();
    });
    
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    
    $("#analyzeButton").click(function () {
        analyzeConstraints();
    });
    
    $("#refreshButton").click(function () {
        refreshSchedule();
    });
    
    // View switchers
    $("#viewTrains").click(function () {
        currentView = "trains";
        refreshSchedule();
    });
    
    $("#viewTrips").click(function () {
        currentView = "trips";
        refreshSchedule();
    });
    
    setupAjax();
    refreshSchedule();
    
    // Auto-refresh every 2 seconds when solving
    setInterval(function() {
        if (loadedSchedule && loadedSchedule.solverStatus === "SOLVING_ACTIVE") {
            refreshSchedule();
        }
    }, 2000);
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        error: function(xhr, status, error) {
            console.error("AJAX Error:", status, error);
            console.error("Response:", xhr.responseText);
        }
    });

    // Extend jQuery to support $.put() and $.delete()
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
    console.log("Fetching schedule...");
    
    $.getJSON("/schedule", function (schedule) {
        console.log("Schedule loaded successfully:", schedule);
        loadedSchedule = schedule;
        renderSchedule(schedule);
    })
    .fail(function (xhr, ajaxOptions, thrownError) {
        console.error("Error fetching schedule:", xhr.status, thrownError);
        console.error("Response:", xhr.responseText);
        showError("Neizdevās ielādēt grafiku", xhr);
        refreshSolvingButtons(false);
    });
}

function renderSchedule(schedule) {
    console.log("Rendering schedule...");
    
    // Update solving buttons
    const isSolving = schedule.solverStatus === "SOLVING_ACTIVE";
    refreshSolvingButtons(isSolving);
    
    // Update score display
    renderScore(schedule);
    
    // Update statistics
    renderStatistics(schedule);
    
    // Render main content based on view
    if (currentView === "trains") {
        renderTrainView(schedule);
    } else if (currentView === "trips") {
        renderTripView(schedule);
    }
}

function renderScore(schedule) {
    const scoreCard = $("#scoreCard");
    scoreCard.empty();
    
    if (!schedule.score) {
        scoreCard.html(`
            <div class="score-card">
                <h3>Rezultāts</h3>
                <p class="text-muted">Nav aprēķināts. Nospiediet "Sākt optimizāciju".</p>
            </div>
        `);
        return;
    }
    
    const scoreStr = schedule.score.toString();
    const parts = scoreStr.match(/(-?\d+)hard\/(-?\d+)soft/);
    
    if (parts) {
        const hardScore = parseInt(parts[1]);
        const softScore = parseInt(parts[2]);
        const isFeasible = hardScore === 0;
        
        scoreCard.html(`
            <div class="score-card">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h3>Rezultāts</h3>
                    ${isFeasible ? 
                        '<span class="badge bg-success">✓ Pieņemams</span>' : 
                        '<span class="badge bg-danger">✗ Nav pieņemams</span>'}
                </div>
                <div class="row">
                    <div class="col-6">
                        <div class="text-center">
                            <small class="text-muted">Hard ierobežojumi</small>
                            <div class="score-hard">${hardScore}</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="text-center">
                            <small class="text-muted">Soft ierobežojumi</small>
                            <div class="score-soft">${softScore}</div>
                        </div>
                    </div>
                </div>
            </div>
        `);
    } else {
        scoreCard.html(`
            <div class="score-card">
                <h3>Rezultāts</h3>
                <p class="fs-4">${scoreStr}</p>
            </div>
        `);
    }
}

function renderStatistics(schedule) {
    const statsCard = $("#statisticsCard");
    
    const vilcieniCount = schedule.vilcieni ? schedule.vilcieni.length : 0;
    const braucieniCount = schedule.braucieni ? schedule.braucieni.length : 0;
    const stacijasCount = schedule.stacijas ? schedule.stacijas.length : 0;
    const marsrutiCount = schedule.marsruti ? schedule.marsruti.length : 0;
    
    // Count assigned vs unassigned trips
    let assignedCount = 0;
    let unassignedCount = 0;
    
    if (schedule.braucieni) {
        schedule.braucieni.forEach(brauciens => {
            if (brauciens.vilciens) {
                assignedCount++;
            } else {
                unassignedCount++;
            }
        });
    }
    
    statsCard.html(`
        <div class="score-card">
            <h3>Statistika</h3>
            <div class="row g-3">
                <div class="col-6 col-md-3">
                    <div class="text-center">
                        <i class="fas fa-train fs-3 text-primary"></i>
                        <div class="fs-4 fw-bold">${vilcieniCount}</div>
                        <small class="text-muted">Vilcieni</small>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="text-center">
                        <i class="fas fa-route fs-3 text-info"></i>
                        <div class="fs-4 fw-bold">${braucieniCount}</div>
                        <small class="text-muted">Braucieni</small>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="text-center">
                        <i class="fas fa-check-circle fs-3 text-success"></i>
                        <div class="fs-4 fw-bold">${assignedCount}</div>
                        <small class="text-muted">Piešķirti</small>
                    </div>
                </div>
                <div class="col-6 col-md-3">
                    <div class="text-center">
                        <i class="fas fa-exclamation-circle fs-3 text-danger"></i>
                        <div class="fs-4 fw-bold">${unassignedCount}</div>
                        <small class="text-muted">Nepiešķirti</small>
                    </div>
                </div>
            </div>
            <div class="row g-3 mt-2">
                <div class="col-6">
                    <div class="text-center">
                        <i class="fas fa-map-marker-alt fs-3 text-warning"></i>
                        <div class="fs-4 fw-bold">${stacijasCount}</div>
                        <small class="text-muted">Stacijas</small>
                    </div>
                </div>
                <div class="col-6">
                    <div class="text-center">
                        <i class="fas fa-map fs-3 text-secondary"></i>
                        <div class="fs-4 fw-bold">${marsrutiCount}</div>
                        <small class="text-muted">Maršruti</small>
                    </div>
                </div>
            </div>
        </div>
    `);
}

function renderTrainView(schedule) {
    const content = $("#mainContent");
    content.empty();
    
    if (!schedule.vilcieni || schedule.vilcieni.length === 0) {
        content.html('<div class="alert alert-info">Nav pieejami vilcienu dati.</div>');
        return;
    }
    
    // Group trips by train
    const tripsByTrain = new Map();
    if (schedule.braucieni) {
        schedule.braucieni.forEach(brauciens => {
            if (brauciens.vilciens) {
                if (!tripsByTrain.has(brauciens.vilciens)) {
                    tripsByTrain.set(brauciens.vilciens, []);
                }
                tripsByTrain.get(brauciens.vilciens).push(brauciens);
            }
        });
    }
    
    let html = '<div class="row">';
    
    // Display each train
    schedule.vilcieni.forEach(vilciens => {
        const trips = tripsByTrain.get(vilciens.id) || [];
        const tripCount = trips.length;
        
        // Sort trips by start time
        trips.sort((a, b) => {
            const timeA = a.sakumaLaiks || "";
            const timeB = b.sakumaLaiks || "";
            return timeA.localeCompare(timeB);
        });
        
        html += `
            <div class="col-12 col-lg-6 mb-3">
                <div class="score-card">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h4><i class="fas fa-train text-primary"></i> ${vilciens.nosaukums || 'Vilciens #' + vilciens.id}</h4>
                        <span class="badge bg-primary">${tripCount} braucieni</span>
                    </div>
                    <div class="mb-2">
                        <small class="text-muted">Kapacitāte:</small> <strong>${vilciens.kapacitate} pasažieri</strong>
                    </div>
        `;
        
        if (trips.length > 0) {
            html += '<div class="mt-3"><h6>Braucieni:</h6><div class="list-group">';
            trips.forEach(brauciens => {
                const marsruts = schedule.marsruti ? 
                    schedule.marsruti.find(m => m.id === brauciens.marsrutsId) : null;
                const marsrutsNosaukums = marsruts ? marsruts.nosaukums : 'Nezināms maršruts';
                
                html += `
                    <div class="list-group-item">
                        <div class="d-flex justify-content-between">
                            <div>
                                <i class="fas fa-route text-info"></i> 
                                <strong>${marsrutsNosaukums}</strong>
                            </div>
                            <small class="text-muted">${formatTime(brauciens.sakumaLaiks)} - ${formatTime(brauciens.beiguLaiks)}</small>
                        </div>
                    </div>
                `;
            });
            html += '</div></div>';
        } else {
            html += '<div class="alert alert-warning mt-3"><small>Nav piešķirtu braucienu</small></div>';
        }
        
        html += '</div></div>';
    });
    
    html += '</div>';
    content.html(html);
}

function renderTripView(schedule) {
    const content = $("#mainContent");
    content.empty();
    
    if (!schedule.braucieni || schedule.braucieni.length === 0) {
        content.html('<div class="alert alert-info">Nav pieejami braucienu dati.</div>');
        return;
    }
    
    // Sort trips by start time
    const sortedTrips = [...schedule.braucieni].sort((a, b) => {
        const timeA = a.sakumaLaiks || "";
        const timeB = b.sakumaLaiks || "";
        return timeA.localeCompare(timeB);
    });
    
    // Group by assigned/unassigned
    const assigned = sortedTrips.filter(b => b.vilciens != null);
    const unassigned = sortedTrips.filter(b => b.vilciens == null);
    
    let html = '<div class="row">';
    
    // Unassigned trips
    if (unassigned.length > 0) {
        html += `
            <div class="col-12 mb-4">
                <div class="score-card">
                    <h4 class="text-danger"><i class="fas fa-exclamation-triangle"></i> Nepiešķirti braucieni (${unassigned.length})</h4>
                    <div class="list-group mt-3">
        `;
        
        unassigned.forEach(brauciens => {
            html += renderTripCard(brauciens, schedule, true);
        });
        
        html += '</div></div></div>';
    }
    
    // Assigned trips
    if (assigned.length > 0) {
        html += `
            <div class="col-12">
                <div class="score-card">
                    <h4 class="text-success"><i class="fas fa-check-circle"></i> Piešķirti braucieni (${assigned.length})</h4>
                    <div class="list-group mt-3">
        `;
        
        assigned.forEach(brauciens => {
            html += renderTripCard(brauciens, schedule, false);
        });
        
        html += '</div></div></div>';
    }
    
    html += '</div>';
    content.html(html);
}

function renderTripCard(brauciens, schedule, isUnassigned) {
    const marsruts = schedule.marsruti ? 
        schedule.marsruti.find(m => m.id === brauciens.marsrutsId) : null;
    const marsrutsNosaukums = marsruts ? marsruts.nosaukums : 'Nezināms maršruts';
    
    const vilciens = brauciens.vilciens && schedule.vilcieni ? 
        schedule.vilcieni.find(v => v.id === brauciens.vilciens) : null;
    const vilciensNosaukums = vilciens ? vilciens.nosaukums || 'Vilciens #' + vilciens.id : 'Nav piešķirts';
    
    const bgClass = isUnassigned ? 'bg-danger bg-opacity-10' : '';
    
    return `
        <div class="list-group-item ${bgClass}">
            <div class="row align-items-center">
                <div class="col-12 col-md-4">
                    <i class="fas fa-route text-info"></i> <strong>${marsrutsNosaukums}</strong>
                </div>
                <div class="col-6 col-md-3">
                    <small class="text-muted">Laiks:</small><br>
                    ${formatTime(brauciens.sakumaLaiks)} - ${formatTime(brauciens.beiguLaiks)}
                </div>
                <div class="col-6 col-md-3">
                    <small class="text-muted">Vilciens:</small><br>
                    ${isUnassigned ? 
                        '<span class="text-danger">Nav piešķirts</span>' : 
                        '<span class="text-success"><i class="fas fa-train"></i> ' + vilciensNosaukums + '</span>'}
                </div>
                <div class="col-12 col-md-2 text-end">
                    <span class="badge ${isUnassigned ? 'bg-danger' : 'bg-success'}">
                        ${isUnassigned ? 'Nepiešķirts' : 'Piešķirts'}
                    </span>
                </div>
            </div>
        </div>
    `;
}

function formatTime(timeStr) {
    if (!timeStr) return 'N/A';
    // Format like "06:00" or "14:30"
    return timeStr;
}

function solve() {
    console.log("Starting solver...");
    $.post("/schedule/solve", function () {
        console.log("Solver started successfully");
        refreshSolvingButtons(true);
        setTimeout(refreshSchedule, 500); // Refresh after a moment
    }).fail(function (xhr, ajaxOptions, thrownError) {
        console.error("Error starting solver:", xhr.status, thrownError);
        showError("Neizdevās sākt optimizāciju", xhr);
        refreshSolvingButtons(false);
    });
}

function stopSolving() {
    console.log("Stopping solver...");
    $.post("/schedule/stop", function () {
        console.log("Solver stopped successfully");
        refreshSolvingButtons(false);
        setTimeout(refreshSchedule, 500);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        console.error("Error stopping solver:", xhr.status, thrownError);
        showError("Neizdevās apturēt optimizāciju", xhr);
    });
}

function analyzeConstraints() {
    if (!loadedSchedule) {
        alert("Vispirms ielādējiet grafiku!");
        return;
    }
    
    if (!loadedSchedule.score) {
        alert("Vispirms jāveic optimizācija, lai būtu rezultāts, ko analizēt!");
        return;
    }
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('constraintAnalysisModal'));
    modal.show();
    
    const modalContent = $("#constraintAnalysisContent");
    modalContent.html('<div class="text-center"><div class="spinner-border" role="status"></div><p>Analizē ierobežojumus...</p></div>');
    
    // Analyze constraints manually
    const analysis = performConstraintAnalysis(loadedSchedule);
    
    let html = `
        <div class="alert ${analysis.isFeasible ? 'alert-success' : 'alert-danger'}">
            <h5>${analysis.isFeasible ? '✓ Risinājums ir pieņemams' : '✗ Risinājums nav pieņemams'}</h5>
            <p>Hard ierobežojumu pārkāpumi: ${analysis.hardViolations}</p>
            <p>Soft ierobežojumu pārkāpumi: ${analysis.softViolations}</p>
        </div>
    `;
    
    if (analysis.violations.length > 0) {
        html += '<h5 class="mt-4">Atklātie pārkāpumi:</h5><div class="list-group">';
        
        analysis.violations.forEach(violation => {
            const badgeClass = violation.type === 'hard' ? 'bg-danger' : 'bg-warning';
            html += `
                <div class="list-group-item">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h6>${violation.constraint}</h6>
                            <p class="mb-1">${violation.description}</p>
                            <small class="text-muted">Skaits: ${violation.count}</small>
                        </div>
                        <span class="badge ${badgeClass}">${violation.type.toUpperCase()}</span>
                    </div>
                </div>
            `;
        });
        
        html += '</div>';
    } else {
        html += '<div class="alert alert-success">Nav atrastu ierobežojumu pārkāpumu!</div>';
    }
    
    modalContent.html(html);
}

function performConstraintAnalysis(schedule) {
    const violations = [];
    let hardViolations = 0;
    let softViolations = 0;
    
    // Check for unassigned trips (hard constraint)
    const unassignedTrips = schedule.braucieni.filter(b => !b.vilciens);
    if (unassignedTrips.length > 0) {
        violations.push({
            type: 'hard',
            constraint: 'Visi braucieni jāpiešķir',
            description: `${unassignedTrips.length} braucieni nav piešķirti nevienam vilcienam`,
            count: unassignedTrips.length
        });
        hardViolations += unassignedTrips.length;
    }
    
    // Check train capacities
    const tripsByTrain = new Map();
    schedule.braucieni.forEach(brauciens => {
        if (brauciens.vilciens) {
            if (!tripsByTrain.has(brauciens.vilciens)) {
                tripsByTrain.set(brauciens.vilciens, []);
            }
            tripsByTrain.get(brauciens.vilciens).push(brauciens);
        }
    });
    
    // Check for time overlaps (trains can't be in two places at once)
    let overlapCount = 0;
    tripsByTrain.forEach((trips, trainId) => {
        trips.sort((a, b) => (a.sakumaLaiks || "").localeCompare(b.sakumaLaiks || ""));
        
        for (let i = 0; i < trips.length - 1; i++) {
            const current = trips[i];
            const next = trips[i + 1];
            
            if (current.beiguLaiks && next.sakumaLaiks) {
                if (current.beiguLaiks > next.sakumaLaiks) {
                    overlapCount++;
                }
            }
        }
    });
    
    if (overlapCount > 0) {
        violations.push({
            type: 'hard',
            constraint: 'Laiku konflikti',
            description: `Vilcieniem ir braucieni, kas pārklājas laikā`,
            count: overlapCount
        });
        hardViolations += overlapCount;
    }
    
    // Soft constraints
    // Check train utilization
    const avgTripsPerTrain = schedule.braucieni.length / schedule.vilcieni.length;
    tripsByTrain.forEach((trips, trainId) => {
        if (trips.length < avgTripsPerTrain * 0.5) {
            violations.push({
                type: 'soft',
                constraint: 'Vilciena izmantošana',
                description: `Vilciens ${trainId} ir maz izmantots (tikai ${trips.length} braucieni)`,
                count: 1
            });
            softViolations++;
        }
    });
    
    const isFeasible = hardViolations === 0;
    
    return {
        isFeasible,
        hardViolations,
        softViolations,
        violations
    };
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        $("#statusBadge").removeClass("d-none").addClass("badge bg-warning").text("⟳ Optimizē...");
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        $("#statusBadge").addClass("d-none");
    }
}

function showError(message, xhr) {
    let details = "";
    if (xhr && xhr.responseText) {
        try {
            const error = JSON.parse(xhr.responseText);
            details = error.message || error.error || xhr.responseText;
        } catch (e) {
            details = xhr.responseText;
        }
    }
    
    const errorHtml = `
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <strong>Kļūda!</strong> ${message}
            ${details ? '<br><small>' + details + '</small>' : ''}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    $("#errorContainer").html(errorHtml);
    setTimeout(() => {
        $("#errorContainer").empty();
    }, 5000);
}
