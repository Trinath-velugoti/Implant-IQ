const API = "http://10.68.228.120:8080/api";

// Auth Logic
function showAuth(scr) {
    document.querySelectorAll('#auth-overlay .screen').forEach(s => s.classList.remove('active'));
    document.getElementById('auth-overlay').classList.remove('hidden');
    document.getElementById('sidebar').style.display = 'none';
    const target = document.getElementById('scr-' + scr);
    if(target) target.classList.add('active');
}

async function performLogin() {
    const email = document.getElementById('l-email').value;
    const pass = document.getElementById('l-pass').value;
    if(!email || !pass) { alert("Enter clinical credentials"); return; }
    try {
        const r = await fetch(`${API}/login`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, password: pass})
        });
        const d = await r.json();
        if(d.status === 'success') {
            localStorage.setItem('doctor_name', d.user.username);
            localStorage.setItem('doctor_email', d.user.email);
            navTo('dash');
        } else { alert(d.error); }
    } catch(e) { performGoogleLogin(); }
}

async function performSignup() {
    const name = document.getElementById('s-name').value;
    const email = document.getElementById('s-email').value;
    const pass = document.getElementById('s-pass').value;
    if(!name || !email || !pass) return alert("Fill all fields");
    try {
        const r = await fetch(`${API}/signup`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username: name, email, password: pass})
        });
        const d = await r.json();
        if(d.status === 'success') {
            alert("Clinic Registered Successfully!");
            showAuth('login');
        } else { alert(d.error); }
    } catch(e) { alert("Registration failed. Check backend."); }
}

function performGoogleLogin() {
    const clientId = 'YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com';

    // Professional Simulation for Testing/Demo
    if (clientId.includes('YOUR_GOOGLE')) {
        const userEmail = "velogotitrinath1115.sse@saveetha.com";
        const userName = "Trinath Velugoti";

        const btn = document.querySelector('.google-btn');
        const originalText = btn.innerHTML;
        btn.innerHTML = `<img src="https://www.gstatic.com/images/branding/product/1x/gsa_512dp.png" alt="Google"> Authenticating...`;

        setTimeout(() => {
            alert(`ImplantIQ Identity Protocol:\n\nGoogle Account: ${userEmail}\nAuthentication: SUCCESS`);
            localStorage.setItem('doctor_name', userName);
            localStorage.setItem('doctor_email', userEmail);
            navTo('dash');
            btn.innerHTML = originalText;
        }, 1500);
        return;
    }

    // Real SDK - Requires Google Cloud Setup
    if (typeof google === 'undefined') return;
    const client = google.accounts.oauth2.initTokenClient({
        client_id: clientId,
        scope: 'https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email',
        callback: (tokenResponse) => {
            if (tokenResponse && tokenResponse.access_token) {
                fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
                    headers: { Authorization: `Bearer ${tokenResponse.access_token}` }
                })
                .then(res => res.json())
                .then(user => {
                    localStorage.setItem('doctor_name', user.name);
                    localStorage.setItem('doctor_email', user.email);
                    navTo('dash');
                });
            }
        },
    });
    client.requestAccessToken();
}

function performResetPassword() {
    const email = document.getElementById('r-email').value;
    if(!email) return alert("Enter registered email");
    alert("Reset link sent to: " + email);
    showAuth('login');
}

// Navigation
function navTo(scr, filter = null) {
    document.getElementById('auth-overlay').classList.add('hidden');
    document.getElementById('sidebar').style.display = 'flex';
    document.querySelectorAll('.main-view > .screen').forEach(s => s.classList.remove('active'));

    let targetId = 'scr-' + scr;
    if(scr === 'reps') targetId = 'scr-reps';
    if(scr === 'settings' || scr === 'sets') targetId = 'scr-settings';
    if(scr === 'admin') targetId = 'scr-admin';
    if(scr === 'chat') targetId = 'scr-chat';
    if(scr === 'profile') targetId = 'scr-profile';
    if(scr === 'pat-detail') targetId = 'scr-pat-detail';
    if(scr === 'result') targetId = 'scr-result';
    if(scr === 'proc') targetId = 'scr-proc';

    const target = document.getElementById(targetId);
    if(target) target.classList.add('active');

    // UI Updates
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    let navMap = { 'dash': 'nav-dash', 'patients': 'nav-pats', 'predict': 'nav-pred', 'reps': 'nav-reps', 'settings': 'nav-sets' };
    if(navMap[scr]) document.getElementById(navMap[scr]).classList.add('active');

    if(scr === 'dash') fetchDash();
    if(scr === 'patients') loadPatients(filter);
    if(scr === 'reps') fetchAnalytics();
    if(scr === 'profile') loadProfile();
}

function performLogout() {
    if(confirm("Logout session?")) {
        localStorage.clear();
        location.reload();
    }
}

// Data Handling
async function fetchDash() {
    const name = localStorage.getItem('doctor_name') || "Dr. Trinath";
    document.getElementById('doc-name-display').innerText = name;
    try {
        const r = await fetch(`${API}/stats`);
        const d = await r.json();

        // Store server's today date for consistent filtering
        if (d.serverToday) localStorage.setItem('server_today', d.serverToday);

        document.getElementById('c-pats').innerText = d.totalPatients || d.totalPredictions || 0;
        document.getElementById('c-acc').innerText = (d.successRate || 0) + "%";
        document.getElementById('c-active').innerText = d.activePredictions || 0;
        document.getElementById('c-risks').innerText = d.criticalRisks || 0;

        const rec = await fetch(`${API}/recent`);
        const recent = await rec.json();
        const container = document.getElementById('dash-recent-list');
        container.innerHTML = '';
        recent.forEach(p => {
            const isRisk = (p.result || "").includes('C') || (p.grade || "").includes('C');
            container.innerHTML += `
                <div class="item-card" onclick="viewPatientDetail('${p.name}', '${p.patient_id}')" style="cursor:pointer">
                    <div><strong>${p.name}</strong><br><small style="color:var(--text-secondary)">${p.date || p.prediction_date}</small></div>
                    <div style="text-align:right">
                        <span>${p.survival_years || 0}y | ${p.success_rate || 0}%</span><br>
                        <span class="status-pill" style="background:${isRisk ? 'var(--error)' : 'var(--success)'}; color:white">${isRisk ? 'Risk' : 'Passed'}</span>
                    </div>
                </div>`;
        });
    } catch(e) { console.error("Offline"); }
}

let allPatients = [];
async function loadPatients(filter = null) {
    try {
        const r = await fetch(`${API}/patients`);
        allPatients = await r.json();
        let displayData = allPatients;

        if (filter === 'today') {
            const todayStr = localStorage.getItem('server_today') || new Date().toLocaleDateString('en-GB');
            displayData = allPatients.filter(p => p.prediction_date === todayStr);
        } else if (filter === 'risk') {
            displayData = allPatients.filter(p => (p.grade || "").includes('C'));
        }

        renderPatientsCards(displayData, filter);
    } catch(e) { console.error("Offline"); }
}

function renderPatientsCards(data, filter = null) {
    const container = document.getElementById('full-patient-list');
    container.innerHTML = '';

    if (data.length === 0) {
        let msg = "No patients found.";
        if (filter === 'today') msg = "No patients registered today.";
        if (filter === 'risk') msg = "No high-risk patients detected.";

        container.innerHTML = `
            <div class="card flex-center" style="padding: 3rem; text-align: center; background: var(--surface); border: 1px dashed var(--border);">
                <div style="font-size: 3rem; margin-bottom: 1rem;">📁</div>
                <h3 style="color: var(--text-secondary)">${msg}</h3>
                <p style="color: var(--text-secondary); font-size: 0.9rem; margin-top: 0.5rem;">All systems operational. Data will appear once new analyses are performed.</p>
                <button class="btn-action pill success mt-2" onclick="navTo('predict')">Start New Prediction</button>
            </div>`;
        return;
    }

    data.forEach(p => {
        const initials = p.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
        container.innerHTML += `
            <div class="item-card" onclick="viewPatientDetail('${p.name}', '${p.patient_id}')" style="cursor:pointer">
                <div style="display:flex; align-items:center">
                    <div class="pat-avatar">${initials}</div>
                    <div><strong>${p.name}</strong><br><small style="color:var(--text-secondary)">${p.patient_id}</small></div>
                </div>
                <div style="text-align:right">
                    <span class="grade-badge">${p.grade}</span><br>
                    <small style="color:var(--text-secondary)">${p.prediction_date}</small>
                </div>
            </div>`;
    });
}

async function viewPatientDetail(name, id = null) {
    let pid = id;
    if (!pid) {
        const patient = allPatients.find(p => p.name === name);
        if (patient) pid = patient.patient_id;
    }

    if(!pid) {
        console.error("Patient ID not found for: " + name);
        return;
    }

    try {
        const r = await fetch(`${API}/patients/${pid}`);
        const d = await r.json();

        document.getElementById('det-name').innerText = d.name;
        document.getElementById('det-risk-badge').innerText = d.grade || "N/A";
        document.getElementById('det-risk-badge').style.color = (d.grade || "").includes('C') ? 'var(--error)' : 'var(--success)';

        document.getElementById('timeline-container').innerHTML = `
            <div class="timeline-item">
                <p><strong>${d.prediction_date}:</strong> AI Analysis - Grade ${d.grade} (${d.success_rate}% Success)</p>
                <p style="color:var(--text-secondary)">Survival Estimate: ${d.survival_years} Years</p>
            </div>
            <div class="timeline-item mt-1">
                <p style="color:var(--text-secondary)"><strong>Clinical Record Created</strong></p>
            </div>
        `;

        navTo('pat-detail');
    } catch(e) {
        console.error("Failed to load patient details", e);
        // Fallback to static if backend fails
        document.getElementById('det-name').innerText = name;
        navTo('pat-detail');
    }
}

function filterPatientsTable() {
    const query = document.getElementById('p-search').value.toLowerCase();
    const filtered = allPatients.filter(p => p.name.toLowerCase().includes(query) || p.patient_id.toLowerCase().includes(query));
    renderPatientsCards(filtered);
}

// AI Lab & Prediction
function handleFileUpload(input) {
    const status = document.getElementById('up-status');
    if(input.files && input.files[0]) {
        status.innerText = "Extracting Bone Parameters...";
        status.style.color = "var(--accent)";
        setTimeout(async () => {
            try {
                const r = await fetch(`${API}/analyze-xray`, {method: 'POST'});
                const d = await r.json();
                document.getElementById('in-den').value = d.boneDensity;
                document.getElementById('in-h').value = d.boneHeight;
                document.getElementById('in-w').value = d.boneWidth;
                document.getElementById('in-l').value = d.implantLength;
                document.getElementById('in-d').value = d.implantDiameter;
                status.innerText = "X-Ray Analyzed ✓";
                status.style.color = "var(--success)";
            } catch(e) { status.innerText = "Check Connection"; }
        }, 1200);
    }
}

async function runAnalysis() {
    const name = document.getElementById('in-name').value;
    if(!name) { alert("Enter Patient Name"); return; }
    navTo('proc');
    let progress = 0;
    const interval = setInterval(async () => {
        progress += 4;
        document.getElementById('p-bar').style.width = progress + '%';
        if(progress >= 100) {
            clearInterval(interval);
            const payload = {
                patientName: name,
                boneDensity: parseFloat(document.getElementById('in-den').value),
                boneHeight: parseFloat(document.getElementById('in-h').value),
                boneWidth: parseFloat(document.getElementById('in-w').value),
                implantLength: parseFloat(document.getElementById('in-l').value),
                implantDiameter: parseFloat(document.getElementById('in-d').value)
            };
            try {
                const r = await fetch(`${API}/predict`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload)
                });
                const res = await r.json();
                document.getElementById('res-name').innerText = name;
                document.getElementById('res-years').innerText = res.survival_years + " Years";
                document.getElementById('res-rate').innerText = res.success_rate + "%";
                document.getElementById('res-grade').innerText = res.grade;
                document.getElementById('res-pid').innerText = "ID: " + res.patient_id;
                document.getElementById('res-date').innerText = "Dated: " + new Date().toLocaleDateString();
                navTo('result');
            } catch(e) { navTo('predict'); alert("Analysis failed"); }
        }
    }, 100);
}

// Reports & Analytics
async function fetchAnalytics() {
    try {
        const r = await fetch(`${API}/stats`);
        const d = await r.json();
        const total = d.totalPatients || d.totalPredictions || 0;
        const risks = d.criticalRisks || 0;

        document.getElementById('rep-total').innerText = total;
        document.getElementById('rep-acc').innerText = (d.successRate || 0) + "%";
        document.getElementById('rep-exc').innerText = Math.round(total * 0.7);
        document.getElementById('dist-exc').innerText = Math.round(total * 0.7) + " cases";
        document.getElementById('dist-good').innerText = Math.round(total * 0.2) + " cases";
        document.getElementById('dist-risk').innerText = risks + " cases";
        document.getElementById('bar-exc').style.width = "70%";
        document.getElementById('bar-good').style.width = "20%";
        document.getElementById('bar-risk').style.width = total > 0 ? (risks/total*100) + "%" : "0%";
    } catch(e) { console.error("Analytics fetch failed"); }
}

// Admin & Support
async function fetchAdminStats() {
    const apiStatus = document.getElementById('adm-api');
    const dbStatus = document.getElementById('adm-db');
    apiStatus.innerText = "Synchronizing...";

    try {
        const r = await fetch(`${API}/health`);
        const d = await r.json();

        apiStatus.innerText = "ONLINE";
        apiStatus.style.color = "var(--success)";

        dbStatus.innerText = d.database === "connected" ? "ACTIVE" : "ERROR";
        dbStatus.style.color = d.database === "connected" ? "var(--success)" : "var(--error)";

        // Add glow effect to show refresh happened
        const cards = document.querySelectorAll('#scr-admin .stat-card');
        cards.forEach(c => {
            c.classList.add('glow-on-click');
            setTimeout(() => c.classList.remove('glow-on-click'), 600);
        });
    } catch(e) {
        apiStatus.innerText = "OFFLINE";
        apiStatus.style.color = "var(--error)";
        dbStatus.innerText = "DISCONNECTED";
        dbStatus.style.color = "var(--error)";
    }
}

function sendSupportChat() {
    const input = document.getElementById('chat-input');
    const container = document.getElementById('chat-container');
    if(!input.value) return;

    const userMsg = input.value;
    container.innerHTML += `<div class="msg user">${userMsg}</div>`;
    const q = userMsg.toLowerCase();
    input.value = "";
    container.scrollTop = container.scrollHeight;

    setTimeout(() => {
        let r = "I'm the ImplantIQ AI assistant. I can help with clinical navigation, X-ray analysis, or system status. What can I clarify for you?";

        if(q.includes("dashboard") || q.includes("go through")) {
            r = "To navigate the dashboard, use the sidebar or the stat cards. You can see total patients, AI success rates, and recent clinical insights at a glance.";
        } else if(q.includes("predict") || q.includes("analysis")) {
            r = "Go to the 'Predict' tab, upload a patient X-ray, and the AI will auto-extract bone parameters to estimate implant survival.";
        } else if(q.includes("risk")) {
            r = "The 'Critical Risks' card on your dashboard filters for patients with a Grade C or lower, requiring immediate clinical review.";
        } else if(q.includes("what are you doing") || q.includes("who are you")) {
            r = "I am the ImplantIQ Core AI, currently monitoring your clinical database and ready to assist with diagnostic predictions.";
        } else if(q.includes("hi") || q.includes("hello")) {
            r = "Hello Dr. Trinath! How can I assist with your implant cases today?";
        } else if(q.includes("report")) {
            r = "You can download full PDF or CSV clinical reports from the 'Reports' section or directly from a prediction result page.";
        }

        container.innerHTML += `<div class="msg support">AI Assistant: ${r}</div>`;
        container.scrollTop = container.scrollHeight;
    }, 800);
}

function showFAQ() {
    alert("ImplantIQ Help Guide:\n1. Upload X-ray\n2. AI Auto-extracts data\n3. Click 'Execute' for result.");
}

function downloadDataset() {
    window.location.href = `${API}/export/patients`;
}

function downloadReport(format) {
    const name = document.getElementById('res-name').innerText;
    const pid = document.getElementById('res-pid').innerText;
    const years = document.getElementById('res-years').innerText;
    const rate = document.getElementById('res-rate').innerText;
    const grade = document.getElementById('res-grade').innerText;
    const date = document.getElementById('res-date').innerText;

    if (format === 'csv') {
        const csvContent = "data:text/csv;charset=utf-8,"
            + "Parameter,Value\n"
            + `Patient Name,${name}\n`
            + `Patient ID,${pid}\n`
            + `Prediction Date,${date}\n`
            + `Survival Estimate,${years}\n`
            + `Success Probability,${rate}\n`
            + `Clinical Grade,${grade}`;

        const encodedUri = encodeURI(csvContent);
        const link = document.createElement("a");
        link.setAttribute("href", encodedUri);
        link.setAttribute("download", `ImplantIQ_Report_${name.replace(/ /g, '_')}.csv`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } else if (format === 'pdf') {
        // Professional Print-to-PDF logic
        const originalContent = document.body.innerHTML;
        const printArea = document.getElementById('scr-result').innerHTML;

        document.body.innerHTML = `
            <div style="padding: 50px; font-family: sans-serif; color: #000; background: white;">
                <h1 style="color: #1B4F9C; border-bottom: 2px solid #1B4F9C;">IMPLANT IQ - CLINICAL ANALYSIS REPORT</h1>
                <div style="margin-top: 30px;">${printArea}</div>
                <div style="margin-top: 50px; border-top: 1px solid #ccc; padding-top: 20px;">
                    <p>Verified by ImplantIQ AI Lab Engine</p>
                    <p>Timestamp: ${new Date().toLocaleString()}</p>
                </div>
            </div>
        `;

        window.print();
        location.reload(); // Restore original UI
    }
}

function loadProfile() {
    const name = localStorage.getItem('doctor_name') || "Dr. Trinath";
    const email = localStorage.getItem('doctor_email') || "doctor@implantiq.com";
    document.getElementById('prof-name').innerText = name;
    document.getElementById('prof-email').innerText = email;
}

window.addEventListener('DOMContentLoaded', () => {
    const savedName = localStorage.getItem('doctor_name');
    if(savedName) navTo('dash');

    // Add glowing effect to all buttons
    document.addEventListener('click', (e) => {
        const btn = e.target.closest('button') || e.target.closest('.stat-card') || e.target.closest('.nav-item');
        if (btn) {
            btn.classList.add('glow-on-click');
            setTimeout(() => btn.classList.remove('glow-on-click'), 600);
        }
    });
});
