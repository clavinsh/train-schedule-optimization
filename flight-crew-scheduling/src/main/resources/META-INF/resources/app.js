// Rolling Stock Schedule Optimization - Frontend Application

let currentSchedule = null;
let currentJobId = null;
let currentAnalysis = null;
let pollingInterval = null;
let countdownInterval = null;
let solveStartTime = null;
let selectedRideId = null;
let isSolving = false; // Flag to track solving state
const POLL_INTERVAL_MS = 2000;
const SOLVER_TIMEOUT_SECONDS = 30; // From rollingStockSolverConfig.xml

// Lookup maps for resolving JSON identity references
let routeMap = new Map();
let trainMap = new Map();
let stationMap = new Map();
let rideMap = new Map();

// Map from ride ID to constraint violations
let rideViolations = new Map();
// Map from route ID to constraint violations
let routeViolations = new Map();

// DOM Elements
const btnLoadDemo = document.getElementById('btn-load-demo');
const btnSolve = document.getElementById('btn-solve');
const btnStop = document.getElementById('btn-stop');
const solvingIndicator = document.getElementById('solving-indicator');
const scoreValue = document.getElementById('score-value');
const statusBadge = document.getElementById('status-badge');
const routeFilter = document.getElementById('route-filter');
const timetableContainer = document.getElementById('timetable-container');
const constraintContainer = document.getElementById('constraint-container');

// Statistics elements
const statRoutes = document.getElementById('stat-routes');
const statTrains = document.getElementById('stat-trains');
const statRides = document.getElementById('stat-rides');
const statAssigned = document.getElementById('stat-assigned');
const statUnassigned = document.getElementById('stat-unassigned');

// Event Listeners
btnLoadDemo.addEventListener('click', loadDemoData);
btnSolve.addEventListener('click', startSolving);
btnStop.addEventListener('click', stopSolving);
routeFilter.addEventListener('change', renderTimetable);

// Close modal when clicking outside
document.addEventListener('click', (e) => {
    const modal = document.getElementById('ride-modal');
    if (modal && e.target === modal) {
        closeRideModal();
    }
});

// Build lookup maps from schedule data
function buildLookupMaps() {
    routeMap.clear();
    trainMap.clear();
    stationMap.clear();
    rideMap.clear();

    if (!currentSchedule) return;

    // Build route map
    (currentSchedule.routes || []).forEach(route => {
        if (route && route.id) {
            routeMap.set(route.id, route);
        }
    });

    // Build train map
    (currentSchedule.trains || []).forEach(train => {
        if (train && train.id) {
            trainMap.set(train.id, train);
        }
    });

    // Build station map
    (currentSchedule.stations || []).forEach(station => {
        if (station && station.id) {
            stationMap.set(station.id, station);
        }
    });

    // Build ride map
    (currentSchedule.rides || []).forEach(ride => {
        if (ride && ride.id) {
            rideMap.set(ride.id, ride);
        }
    });
}

// Build ride and route violations maps from analysis
function buildRideViolationsMap() {
    rideViolations.clear();
    routeViolations.clear();

    if (!currentAnalysis || !currentAnalysis.constraints) return;

    currentAnalysis.constraints.forEach(constraint => {
        // Skip constraints with no violations
        if (constraint.score.startsWith('0')) return;
        if (!constraint.matches) return;

        constraint.matches.forEach(match => {
            const violation = {
                constraintName: constraint.name,
                score: match.score,
                justification: match.justification
            };

            // Add to ride violations
            if (match.rideIds && match.rideIds.length > 0) {
                match.rideIds.forEach(rideId => {
                    if (!rideViolations.has(rideId)) {
                        rideViolations.set(rideId, []);
                    }
                    rideViolations.get(rideId).push(violation);
                });
            }

            // Add to route violations
            if (match.routeIds && match.routeIds.length > 0) {
                match.routeIds.forEach(routeId => {
                    if (!routeViolations.has(routeId)) {
                        routeViolations.set(routeId, []);
                    }
                    routeViolations.get(routeId).push(violation);
                });
            }
        });
    });

    // For route coverage violations, mark the specific unassigned rides as having violations
    // These rides are "causing" the route coverage violation by not having trains assigned
    if (currentSchedule && currentSchedule.rides) {
        routeViolations.forEach((violations, routeId) => {
            // Find all unassigned rides on this route
            currentSchedule.rides.forEach(ride => {
                const rideRouteId = getRouteId(ride.route);
                if (rideRouteId === routeId && !ride.train) {
                    // This unassigned ride is contributing to the route coverage violation
                    violations.forEach(v => {
                        if (!rideViolations.has(ride.id)) {
                            rideViolations.set(ride.id, []);
                        }
                        // Add with a marker that it's from route coverage
                        rideViolations.get(ride.id).push({
                            ...v,
                            isRouteCoverage: true
                        });
                    });
                }
            });
        });
    }
}

// Get route ID from a route reference (handles both object and string ID)
function getRouteId(routeRef) {
    if (!routeRef) return null;
    if (typeof routeRef === 'string') return routeRef;
    if (typeof routeRef === 'object' && routeRef.id) return routeRef.id;
    return null;
}

// Get train ID from a train reference
function getTrainId(trainRef) {
    if (!trainRef) return null;
    if (typeof trainRef === 'string') return trainRef;
    if (typeof trainRef === 'object' && trainRef.id) return trainRef.id;
    return null;
}

// Get station name from a station reference
function getStationName(stationRef) {
    if (!stationRef) return '?';
    if (typeof stationRef === 'string') {
        const station = stationMap.get(stationRef);
        return station ? station.name : stationRef;
    }
    if (typeof stationRef === 'object') {
        return stationRef.name || stationRef.id || '?';
    }
    return '?';
}

// API Functions
async function loadDemoData() {
    try {
        btnLoadDemo.disabled = true;
        btnLoadDemo.innerHTML = '<div class="spinner"></div> Loading...';

        const response = await fetch('/rolling-stock/demo-data');
        if (!response.ok) throw new Error('Failed to load demo data');

        currentSchedule = await response.json();
        currentJobId = null;

        buildLookupMaps();
        updateUI();
        btnSolve.disabled = false;

        // Also analyze the initial schedule
        await analyzeSchedule();

    } catch (error) {
        console.error('Error loading demo data:', error);
        alert('Failed to load demo data: ' + error.message);
    } finally {
        btnLoadDemo.disabled = false;
        btnLoadDemo.innerHTML = `
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/>
            </svg>
            Load Demo Data
        `;
    }
}

async function startSolving() {
    console.log('startSolving called, currentSchedule:', !!currentSchedule);
    if (!currentSchedule) return;

    try {
        isSolving = true;
        btnSolve.disabled = true;
        btnSolve.classList.add('hidden');
        btnStop.classList.remove('hidden');
        btnStop.disabled = false;
        solvingIndicator.classList.remove('hidden');
        solvingIndicator.classList.add('flex');

        updateStatus('SOLVING_ACTIVE');

        const response = await fetch('/rolling-stock', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(currentSchedule)
        });

        if (!response.ok) throw new Error('Failed to start solving');

        currentJobId = await response.text();
        solveStartTime = Date.now();
        console.log('Solver started, job ID:', currentJobId, 'timeout:', SOLVER_TIMEOUT_SECONDS, 'seconds');

        // Start polling and countdown
        startPolling();
        startCountdown();

    } catch (error) {
        console.error('Error starting solver:', error);
        alert('Failed to start solving: ' + error.message);
        resetSolvingUI();
    }
}

async function stopSolving() {
    if (!currentJobId || !isSolving) return;

    // Set flag immediately to prevent multiple calls
    isSolving = false;
    console.log('Stopping solver for job:', currentJobId);

    try {
        btnStop.disabled = true;

        await fetch(`/rolling-stock/${currentJobId}`, { method: 'DELETE' });

        stopPolling();
        stopCountdown();
        resetSolvingUI();

        // Small delay to let solver finish terminating
        await new Promise(resolve => setTimeout(resolve, 500));

        // Fetch final solution and update UI
        await fetchSolution();
        await analyzeSchedule();

    } catch (error) {
        console.error('Error stopping solver:', error);
        // Reset flag on error so user can retry
        isSolving = false;
    }
}

function startPolling() {
    if (pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(pollStatus, POLL_INTERVAL_MS);
    // Also poll immediately
    pollStatus();
}

function stopPolling() {
    if (pollingInterval) {
        clearInterval(pollingInterval);
        pollingInterval = null;
    }
}

function startCountdown() {
    console.log('startCountdown called');
    if (countdownInterval) clearInterval(countdownInterval);
    updateCountdown();
    countdownInterval = setInterval(updateCountdown, 1000);
}

function stopCountdown() {
    if (countdownInterval) {
        clearInterval(countdownInterval);
        countdownInterval = null;
    }
}

function updateCountdown() {
    if (!solveStartTime) return;

    const elapsed = Math.floor((Date.now() - solveStartTime) / 1000);
    const remaining = Math.max(0, SOLVER_TIMEOUT_SECONDS - elapsed);

    // Log every 5 seconds to avoid spam
    if (remaining % 5 === 0 || remaining <= 3) {
        console.log('Countdown:', remaining, 'seconds remaining, isSolving:', isSolving);
    }

    const indicatorText = solvingIndicator.querySelector('span');
    if (indicatorText) {
        indicatorText.textContent = `Solving... ${remaining}s remaining`;
    }

    if (remaining <= 0 && isSolving) {
        console.log('Timer expired, stopping solver...');
        stopCountdown();
        stopSolving();
    }
}

async function pollStatus() {
    if (!currentJobId) return;

    try {
        // Fetch full solution to get real-time updates
        const response = await fetch(`/rolling-stock/${currentJobId}`);
        if (!response.ok) throw new Error('Failed to get solution');

        const solution = await response.json();
        currentSchedule = solution;
        buildLookupMaps();

        // Update score
        if (solution.score) {
            scoreValue.textContent = solution.score;
            scoreValue.className = getScoreClass(solution.score);
        }

        // Update timetable and statistics
        updateStatistics();

        // Also update constraint analysis during solving
        await analyzeSchedule();

        renderTimetable();

        // Check if solving is complete
        if (solution.solverStatus === 'NOT_SOLVING') {
            console.log('Solver finished (detected by poll)');
            stopPolling();
            stopCountdown();
            resetSolvingUI();
        }

    } catch (error) {
        console.error('Error polling status:', error);
    }
}

async function fetchSolution() {
    if (!currentJobId) return;

    try {
        const response = await fetch(`/rolling-stock/${currentJobId}`);
        if (!response.ok) throw new Error('Failed to fetch solution');

        currentSchedule = await response.json();
        buildLookupMaps();
        updateUI();

    } catch (error) {
        console.error('Error fetching solution:', error);
    }
}

async function analyzeSchedule() {
    if (!currentSchedule) return;

    try {
        let response;

        // If we have a job ID, use the job-based analysis endpoint for consistency
        // This avoids JSON serialization issues with @JsonIdentityInfo
        if (currentJobId) {
            response = await fetch(`/rolling-stock/${currentJobId}/analysis`);
        } else {
            // For initial analysis (before solving), send the schedule
            response = await fetch('/rolling-stock/analyze', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(currentSchedule)
            });
        }

        if (!response.ok) throw new Error('Failed to analyze schedule');

        currentAnalysis = await response.json();
        buildRideViolationsMap();
        renderConstraints(currentAnalysis);

    } catch (error) {
        console.error('Error analyzing schedule:', error);
    }
}

// UI Functions
function updateUI() {
    if (!currentSchedule) return;

    updateStatistics();
    updateScore();
    updateRouteFilter();
    renderTimetable();
}

function updateStatistics() {
    const routes = currentSchedule.routes || [];
    const trains = currentSchedule.trains || [];
    const rides = currentSchedule.rides || [];

    const assignedRides = rides.filter(r => r.train != null);
    const unassignedRides = rides.filter(r => r.train == null);

    statRoutes.textContent = routes.length;
    statTrains.textContent = trains.length;
    statRides.textContent = rides.length;
    statAssigned.textContent = assignedRides.length;
    statUnassigned.textContent = unassignedRides.length;
}

function updateScore() {
    const score = currentSchedule.score;
    if (score) {
        scoreValue.textContent = score;
        scoreValue.className = getScoreClass(score);
    } else {
        scoreValue.textContent = '-';
        scoreValue.className = 'text-lg font-mono font-bold text-gray-400';
    }

    const status = currentSchedule.solverStatus || 'NOT_SOLVING';
    updateStatus(status);
}

function updateStatus(status) {
    statusBadge.textContent = formatStatus(status);
    statusBadge.className = 'px-3 py-1 rounded-full text-sm font-medium ' + getStatusClass(status);
}

function formatStatus(status) {
    switch (status) {
        case 'NOT_SOLVING': return 'Ready';
        case 'SOLVING_ACTIVE': return 'Solving...';
        case 'SOLVING_SCHEDULED': return 'Scheduled';
        default: return status;
    }
}

function getStatusClass(status) {
    switch (status) {
        case 'SOLVING_ACTIVE': return 'bg-blue-100 text-blue-800';
        case 'SOLVING_SCHEDULED': return 'bg-yellow-100 text-yellow-800';
        default: return 'bg-gray-200 text-gray-700';
    }
}

function getScoreClass(score) {
    const baseClass = 'text-lg font-mono font-bold ';
    if (!score) return baseClass + 'text-gray-400';
    if (score.includes('-') && score.includes('hard')) {
        return baseClass + 'text-red-600';
    }
    if (score.startsWith('0hard')) {
        return baseClass + 'text-green-600';
    }
    return baseClass + 'text-yellow-600';
}

function updateRouteFilter() {
    const routes = currentSchedule.routes || [];
    routeFilter.innerHTML = '<option value="all">All Routes</option>';

    routes.forEach(route => {
        const option = document.createElement('option');
        option.value = route.id;
        option.textContent = route.name;
        routeFilter.appendChild(option);
    });
}

function renderTimetable() {
    if (!currentSchedule) return;

    const selectedRoute = routeFilter.value;
    const routes = currentSchedule.routes || [];
    const rides = currentSchedule.rides || [];

    // Filter routes
    const filteredRoutes = selectedRoute === 'all'
        ? routes
        : routes.filter(r => r.id === selectedRoute);

    if (filteredRoutes.length === 0) {
        timetableContainer.innerHTML = `
            <div class="text-center text-gray-500 py-12">
                <p>No routes to display</p>
            </div>
        `;
        return;
    }

    timetableContainer.innerHTML = '';

    filteredRoutes.forEach(route => {
        // Match rides to route - handle both object and string ID references
        const routeRides = rides.filter(r => {
            const rideRouteId = getRouteId(r.route);
            return rideRouteId === route.id;
        });
        const routeSection = createRouteSection(route, routeRides);
        timetableContainer.appendChild(routeSection);
    });
}

function createRouteSection(route, routeRides) {
    const section = document.createElement('div');
    const hasRouteViolation = routeViolations.has(route.id);

    // Style the section based on whether the route has violations
    if (hasRouteViolation) {
        section.className = 'border-2 border-red-400 rounded-lg p-4 bg-red-50 mb-4';
    } else {
        section.className = 'border rounded-lg p-4 bg-gray-50 mb-4';
    }

    // Route header
    const header = document.createElement('div');
    header.className = 'flex justify-between items-center mb-3';

    const violations = routeViolations.get(route.id) || [];
    const violationBadge = hasRouteViolation
        ? `<span class="ml-2 px-2 py-1 text-xs bg-red-500 text-white rounded">${violations.length} violation${violations.length > 1 ? 's' : ''}</span>`
        : '';

    header.innerHTML = `
        <div class="flex items-center">
            <h3 class="font-semibold ${hasRouteViolation ? 'text-red-700' : 'text-gray-800'}">${route.name}</h3>
            ${violationBadge}
        </div>
        <span class="text-sm text-gray-500">${routeRides.length} rides</span>
    `;
    section.appendChild(header);

    // Group rides by train
    const ridesByTrain = new Map();
    const unassignedRides = [];

    routeRides.forEach(ride => {
        const trainId = getTrainId(ride.train);
        if (trainId) {
            if (!ridesByTrain.has(trainId)) {
                ridesByTrain.set(trainId, []);
            }
            ridesByTrain.get(trainId).push(ride);
        } else {
            unassignedRides.push(ride);
        }
    });

    // Render train rows
    const trainRows = document.createElement('div');
    trainRows.className = 'space-y-2';

    if (ridesByTrain.size === 0 && unassignedRides.length === 0) {
        trainRows.innerHTML = '<p class="text-sm text-gray-400 italic">No rides for this route</p>';
    }

    ridesByTrain.forEach((trainRides, trainId) => {
        const train = trainMap.get(trainId);
        const trainRow = createTrainRow(trainId, train, trainRides);
        trainRows.appendChild(trainRow);
    });

    // Render unassigned rides
    if (unassignedRides.length > 0) {
        const unassignedRow = createUnassignedRow(unassignedRides);
        trainRows.appendChild(unassignedRow);
    }

    section.appendChild(trainRows);
    return section;
}

function createTrainRow(trainId, train, rides) {
    const row = document.createElement('div');
    row.className = 'train-row flex items-center gap-2 bg-white p-2 rounded border';

    // Train label
    const label = document.createElement('div');
    label.className = 'w-24 flex-shrink-0 font-medium text-sm text-gray-700';
    label.textContent = `Train-${trainId}`;
    if (train) {
        label.title = `Capacity: ${train.capacity}`;
    }
    row.appendChild(label);

    // Rides container
    const ridesContainer = document.createElement('div');
    ridesContainer.className = 'flex flex-wrap gap-1 flex-1';

    // Sort rides by departure time
    rides.sort((a, b) => {
        if (!a.departureTime || !b.departureTime) return 0;
        return a.departureTime.localeCompare(b.departureTime);
    });

    rides.forEach(ride => {
        const hasViolation = rideViolations.has(ride.id);
        const status = hasViolation ? 'violation' : 'assigned';
        const rideBlock = createRideBlock(ride, status);
        ridesContainer.appendChild(rideBlock);
    });

    row.appendChild(ridesContainer);
    return row;
}

function createUnassignedRow(rides) {
    const row = document.createElement('div');
    row.className = 'train-row flex items-center gap-2 bg-red-50 p-2 rounded border border-red-200';

    // Label
    const label = document.createElement('div');
    label.className = 'w-24 flex-shrink-0 font-medium text-sm text-red-600';
    label.textContent = 'Unassigned';
    row.appendChild(label);

    // Rides container
    const ridesContainer = document.createElement('div');
    ridesContainer.className = 'flex flex-wrap gap-1 flex-1';

    rides.forEach(ride => {
        // Check if this unassigned ride has violations (from route coverage constraints)
        const hasViolation = rideViolations.has(ride.id);
        const status = hasViolation ? 'violation' : 'unassigned';
        const rideBlock = createRideBlock(ride, status);
        ridesContainer.appendChild(rideBlock);
    });

    row.appendChild(ridesContainer);
    return row;
}

function createRideBlock(ride, status) {
    const block = document.createElement('div');
    block.className = `ride-block ride-${status} cursor-pointer`;
    block.dataset.rideId = ride.id;

    const depStation = getStationName(ride.departureStation);
    const arrStation = getStationName(ride.arrivalStation);
    const depTime = ride.departureTime ? formatTime(ride.departureTime) : '';

    block.textContent = `${depTime} ${depStation} → ${arrStation}`;

    // Enhanced tooltip
    let tooltipText = `Ride ${ride.id}\n${depStation} → ${arrStation}\n${ride.departureTime || ''} - ${ride.arrivalTime || ''}`;

    // Show violations
    const violations = rideViolations.get(ride.id);
    if (violations && violations.length > 0) {
        tooltipText += '\n\nViolations:';
        violations.forEach(v => {
            tooltipText += `\n- ${v.constraintName}: ${v.score}`;
        });
    }

    block.title = tooltipText;

    // Click handler to show detailed constraint info
    block.addEventListener('click', () => showRideDetails(ride.id));

    return block;
}

function formatTime(isoDateTime) {
    if (!isoDateTime) return '';
    try {
        const date = new Date(isoDateTime);
        return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
    } catch {
        return '';
    }
}

function showRideDetails(rideId) {
    selectedRideId = rideId;
    const ride = rideMap.get(rideId);

    if (!ride) {
        console.error('Ride not found:', rideId);
        return;
    }

    const depStation = getStationName(ride.departureStation);
    const arrStation = getStationName(ride.arrivalStation);
    const trainId = getTrainId(ride.train);
    const train = trainId ? trainMap.get(trainId) : null;
    const violations = rideViolations.get(rideId) || [];

    // Create modal if it doesn't exist
    let modal = document.getElementById('ride-modal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'ride-modal';
        modal.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
        document.body.appendChild(modal);
    }

    const hasViolations = violations.length > 0;
    const headerColor = hasViolations ? 'bg-red-600' : 'bg-blue-600';

    modal.innerHTML = `
        <div class="bg-white rounded-lg shadow-xl max-w-lg w-full mx-4 max-h-[80vh] overflow-hidden">
            <div class="${headerColor} text-white p-4">
                <div class="flex justify-between items-center">
                    <h3 class="text-lg font-semibold">Ride ${rideId}</h3>
                    <button onclick="closeRideModal()" class="text-white hover:text-gray-200">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
            </div>
            <div class="p-4 overflow-y-auto max-h-[60vh]">
                <!-- Ride Details -->
                <div class="mb-4">
                    <h4 class="font-medium text-gray-700 mb-2">Details</h4>
                    <div class="bg-gray-50 rounded p-3 space-y-1 text-sm">
                        <div class="flex justify-between">
                            <span class="text-gray-500">Route:</span>
                            <span>${depStation} → ${arrStation}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-500">Departure:</span>
                            <span>${ride.departureTime || '-'}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-500">Arrival:</span>
                            <span>${ride.arrivalTime || '-'}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="text-gray-500">Assigned Train:</span>
                            <span>${trainId ? `Train-${trainId} (capacity: ${train?.capacity || '?'})` : 'Not assigned'}</span>
                        </div>
                    </div>
                </div>

                <!-- Constraint Violations -->
                <div>
                    <h4 class="font-medium text-gray-700 mb-2">Constraint Analysis</h4>
                    ${!hasViolations
                        ? '<div class="bg-green-50 border border-green-200 rounded p-3 text-green-700 text-sm">No constraint violations for this ride</div>'
                        : `<div class="space-y-2">
                            ${violations.map(v => `
                                <div class="bg-red-50 border border-red-200 rounded p-3">
                                    <div class="flex justify-between items-start">
                                        <span class="font-medium text-red-700">${v.constraintName}</span>
                                        <span class="text-sm font-mono text-red-600">${v.score}</span>
                                    </div>
                                    ${v.justification ? `<div class="text-xs text-gray-500 mt-1 break-all">${escapeHtml(v.justification)}</div>` : ''}
                                </div>
                            `).join('')}
                        </div>`
                    }
                </div>
            </div>
            <div class="border-t p-3 flex justify-end">
                <button onclick="closeRideModal()" class="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded text-gray-700">
                    Close
                </button>
            </div>
        </div>
    `;

    modal.classList.remove('hidden');
}

function closeRideModal() {
    const modal = document.getElementById('ride-modal');
    if (modal) {
        modal.classList.add('hidden');
    }
    selectedRideId = null;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function renderConstraints(analysis) {
    if (!analysis || !analysis.constraints) {
        constraintContainer.innerHTML = `
            <div class="text-center text-gray-500 py-8">
                <p class="text-sm">No constraint data available</p>
            </div>
        `;
        return;
    }

    constraintContainer.innerHTML = '';

    // Overall score
    const scoreHeader = document.createElement('div');
    scoreHeader.className = 'mb-4 p-3 bg-gray-100 rounded-lg';
    scoreHeader.innerHTML = `
        <div class="text-sm text-gray-500">Overall Score</div>
        <div class="text-xl font-mono font-bold ${getScoreClass(analysis.score)}">${analysis.score}</div>
    `;
    constraintContainer.appendChild(scoreHeader);

    // Sort constraints: hard violations first, then soft
    const constraints = [...analysis.constraints].sort((a, b) => {
        const aIsHard = a.score.includes('hard');
        const bIsHard = b.score.includes('hard');
        const aHasViolation = !a.score.startsWith('0');
        const bHasViolation = !b.score.startsWith('0');

        if (aIsHard && aHasViolation && (!bIsHard || !bHasViolation)) return -1;
        if (bIsHard && bHasViolation && (!aIsHard || !aHasViolation)) return 1;
        return b.matchCount - a.matchCount;
    });

    constraints.forEach(constraint => {
        const item = createConstraintItem(constraint);
        constraintContainer.appendChild(item);
    });
}

function createConstraintItem(constraint) {
    const isHard = constraint.score.includes('hard');
    const hasViolation = !constraint.score.startsWith('0');

    let itemClass = 'constraint-ok';
    if (hasViolation) {
        itemClass = isHard ? 'constraint-hard' : 'constraint-soft';
    }

    const item = document.createElement('div');
    item.className = `constraint-item ${itemClass} p-3 rounded-r-lg mb-2`;

    const icon = hasViolation
        ? (isHard ? '&#x26A0;' : '&#x26A0;')
        : '&#x2714;';

    // Build match details if available
    let matchDetails = '';
    if (constraint.matches && constraint.matches.length > 0 && hasViolation) {
        const matchItems = constraint.matches.slice(0, 5).map(match => {
            const rideLinks = (match.rideIds || []).map(id =>
                `<span class="text-blue-600 hover:underline cursor-pointer" onclick="showRideDetails('${id}')">Ride ${id}</span>`
            ).join(', ');
            return `<div class="text-xs text-gray-600">• ${rideLinks || 'Unknown rides'}: ${match.score}</div>`;
        }).join('');

        const moreCount = constraint.matches.length - 5;
        matchDetails = `
            <div class="mt-2 pl-6 space-y-1">
                ${matchItems}
                ${moreCount > 0 ? `<div class="text-xs text-gray-400">... and ${moreCount} more</div>` : ''}
            </div>
        `;
    }

    item.innerHTML = `
        <div class="flex justify-between items-start">
            <div>
                <span class="mr-2">${icon}</span>
                <span class="font-medium">${constraint.name}</span>
            </div>
            <span class="text-sm font-mono ${hasViolation ? (isHard ? 'text-red-600' : 'text-yellow-600') : 'text-green-600'}">${constraint.score}</span>
        </div>
        <div class="text-sm text-gray-500 mt-1 ml-6">
            ${constraint.matchCount} match${constraint.matchCount !== 1 ? 'es' : ''}
        </div>
        ${matchDetails}
    `;

    return item;
}

function resetSolvingUI() {
    isSolving = false;
    btnSolve.disabled = false;
    btnSolve.classList.remove('hidden');
    btnStop.classList.add('hidden');
    btnStop.disabled = true;
    solvingIndicator.classList.add('hidden');
    solvingIndicator.classList.remove('flex');

    // Reset countdown text
    const indicatorText = solvingIndicator.querySelector('span');
    if (indicatorText) {
        indicatorText.textContent = 'Solving...';
    }

    solveStartTime = null;
    updateStatus('NOT_SOLVING');
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('Rolling Stock Schedule Optimization UI initialized');
});
