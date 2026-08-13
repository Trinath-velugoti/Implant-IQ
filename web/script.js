const API = "http://10.68.228.120:8080/api";

// Helper for Secure Headers
const getHeaders = () => ({
    'Authorization': localStorage.getItem('auth_token'),
    'Content-Type': 'application/json'
});

function showAuth(scr) {
    document.querySelectorAll('#auth-overlay .screen').forEach(s => s.classList.remove('active'));
    document.getElementById('auth-overlay').classList.remove('hidden');
    document.getElementById('sidebar').style.display = 'none';
    const target = document.getElementById('scr-' + scr);
    if(target) target.classList.add('active');
}

function navTo(scr) {
    document.getElementById('auth-overlay').classList.add('hidden');
    document.getElementById('sidebar').style.display = 'flex';
    document.querySelectorAll('.main-view > .screen').forEach(s => s.classList.remove('active'));
    const target = document.getElementById('scr-' + scr);
    if(target) target.classList.add('active');

    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    const navId = { 'dash': 'nav-dash', 'patients': 'nav-pats', 'predict': 'nav-pred', 'reps': 'nav-reps', 'settings': 'nav-sets' }[scr];
    if(navId) document.getElementById(navId).classList.add('active');

    if(scr === 'dash') loadDashboard();
    if(scr === 'patients') loadPatientRegistry();
}

// REAL AUTHENTICATION
async function performLogin() {
    const email = document.getElementById('l-email').value;
    const password = document.getElementById('l-pass').value;
    if(!email || !password) { alert("Enter clinical credentials"); return; }

    try {
        const r = await fetch(`${API}/auth/login`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, password})
        });
        const d = await r.json();

        if(d.status === 'success') {
            localStorage.setItem('auth_token', d.token);
            localStorage.setItem('doctor_name', d.user.username);
            navTo('dash');
        } else {
            alert(d.error || "Invalid Credentials");
        }
    } catch(e) { alert("Clinical Server Offline. Check IP connection."); }
}

async function performSignup() {
    const username = document.getElementById('s-name').value;
    const email = document.getElementById('s-email').value;
    const password = document.getElementById('s-pass').value;
    if(!username || !email || !password) return alert("Fill all clinical fields");

    try {
        const r = await fetch(`${API}/auth/signup`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({username: email.split('@')[0], email, password})
        });
        const d = await r.json();

        if(d.status === 'otp_sent') {
            localStorage.setItem('temp_email', email);
            alert("Verification Protocol Initiated. Check your clinical inbox.");
            showAuth('otp');
        } else { alert(d.error); }
    } catch(e) { alert("Registration Error"); }
}

async function verifyOTP() {
    const email = localStorage.getItem('temp_email');
    const otp = document.getElementById('o-code').value;
    try {
        const r = await fetch(`${API}/auth/verify-otp`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, otp})
        });
        if(r.ok) { alert("Identity Verified. Proceeding to Login."); showAuth('login'); }
        else { alert("Invalid Clinical Code"); }
    } catch(e) { alert("System Error during verification"); }
}

function performLogout() {
    localStorage.clear();
    location.reload();
}

// REAL DATA FETCHING
async function loadDashboard() {
    const name = localStorage.getItem('doctor_name') || "Doctor";
    document.getElementById('doc-name-display').innerText = name;

    try {
        const r = await fetch(`${API}/dashboard/statistics`, { headers: getHeaders() });
        const d = await r.json();

        if(r.status === 401) { performLogout(); return; }

        document.getElementById('c-pats').innerText = d.totalPatients || 0;
        document.getElementById('c-acc').innerText = d.successRate || "0%";
        document.getElementById('c-active').innerText = d.activeToday || 0;
        document.getElementById('c-risks').innerText = d.criticalRisks || 0;

        const list = document.getElementById('dash-recent-list');
        if(list) list.innerHTML = `<p style="color:var(--text-secondary); text-align:center; padding:1rem;">All Clinical Metrics Synchronized ✓</p>`;
    } catch(e) { console.error("Sync Failed"); }
}

async function loadPatientRegistry() {
    const container = document.getElementById('full-patient-list');
    container.innerHTML = `<p style="text-align:center; color:var(--text-secondary);">Accessing Clinical Registry...</p>`;
    try {
        const r = await fetch(`${API}/patients`, { headers: getHeaders() });
        const data = await r.json();
        container.innerHTML = "";
        if(data.length === 0) {
            container.innerHTML = `<p style="text-align:center; color:var(--text-secondary);">No patients found in your registry.</p>`;
            return;
        }
        data.forEach(p => {
            const card = document.createElement('div');
            card.className = "item-card";
            card.style.cursor = "default";
            card.innerHTML = `
                <div><strong>${p.name}</strong><br><small style="color:var(--text-secondary)">ID: ${p.patient_id}</small></div>
                <div style="text-align:right">Age: ${p.age}<br><span class="status-pill" style="background:var(--success); color:white">Analyzed</span></div>`;
            container.appendChild(card);
        });
    } catch(e) { container.innerHTML = "Failed to load patient database."; }
}

// X-RAY PROTOCOL
async function handleFileUpload(input) {
    const status = document.getElementById('up-status');
    if(input.files && input.files[0]) {
        status.innerText = "Verifying Dental Protocol...";
        status.style.color = "var(--accent)";

        const formData = new FormData();
        formData.append('file', input.files[0]);
        formData.append('patientId', 'PAT-TEMP');

        try {
            const upR = await fetch(`${API}/xray/upload`, {
                method: 'POST',
                headers: { 'Authorization': localStorage.getItem('auth_token') },
                body: formData
            });
            const upD = await upR.json();

            const valR = await fetch(`${API}/xray/validate`, {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ xrayId: upD.xrayId })
            });
            const valD = await valR.json();

            if (valD.valid) {
                const extR = await fetch(`${API}/xray/extract`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: JSON.stringify({ xrayId: upD.xrayId })
                });
                const extD = await extR.json();

                document.getElementById('in-den').value = extD.bone_density;
                document.getElementById('in-h').value = extD.bone_height;
                document.getElementById('in-w').value = extD.bone_width;
                status.innerText = "X-Ray Analyzed ✓ (Panoramic Verified)";
                status.style.color = "var(--success)";
            } else {
                status.innerText = "❌ Invalid Dental X-Ray. Please upload a panoramic image.";
                status.style.color = "var(--error)";
                input.value = "";
            }
        } catch(e) { status.innerText = "Check Clinical Connection"; }
    }
}

async function runAnalysis() {
    const name = document.getElementById('in-name').value;
    if(!name) return alert("Enter Patient Name");

    // Simulate Prediction for demonstration
    navTo('result');
    document.getElementById('res-name').innerText = name;
}

function downloadReport(format) {
    alert("Downloading Clinical Report as " + format.toUpperCase());
}

window.onload = () => {
    if(localStorage.getItem('auth_token')) navTo('dash');
    else showAuth('splash');
};
