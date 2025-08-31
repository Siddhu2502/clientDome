document.addEventListener('DOMContentLoaded', () => {
    // --- Element References ---
    const profileCard = document.getElementById('profile-card');
    const statusEl = document.getElementById('verification-status');
    const idEl = document.getElementById('kmkm-id');
    const reasonEl = document.getElementById('verification-reason');

    // --- 1. THE FIX: Parse URL Parameters Correctly ---
    const params = new URLSearchParams(window.location.search);
    const status = params.get('status');
    const kmkmId = params.get('kmkmId');
    const reason = params.get('reason');

    // --- 2. Populate the Card with Data ---
    if (status) {
        statusEl.textContent = status;
        if (status === 'APPROVED') {
            profileCard.classList.add('approved');
            idEl.textContent = kmkmId || 'ID not generated';
        } else {
            profileCard.classList.add('rejected');
            idEl.textContent = 'N/A';
        }
    }
    if (reason) {
        reasonEl.textContent = reason;
    }

    // --- 3. THE GENERATIVE LAVA LAMP LOGIC ---
    if (kmkmId) {
        generateLampStyleFromId(kmkmId);
    }
    
    /**
     * Takes a unique ID string and uses it as a seed to generate
     * unique visual properties for the lava lamp blobs.
     * @param {string} id The KMKM ID string.
     */
    function generateLampStyleFromId(id) {
        console.log("Generating unique lamp style from ID:", id);
        const blobs = document.querySelectorAll('.blob');
        // A simple hashing function to turn a string into a number
        const stringToHash = s => s.split('').reduce((a, b) => ((a << 5) - a + b.charCodeAt(0)) | 0, 0);

        blobs.forEach((blob, index) => {
            // Use different parts of the ID for each blob to ensure variety
            const seedString = id.substring(index * 4, (index + 1) * 4 + 5);
            const hash = Math.abs(stringToHash(seedString));

            // Generate values within reasonable ranges using the hash
            const speed = (hash % 25) + 15; // Speed between 15s and 40s
            const size = (hash % 30) + 20;  // Radius between 20 and 50
            const delay = -1 * (hash % 10); // Negative delay to start immediately
            const skewX = (hash % 10) - 5;  // Skew between -5 and 5

            // Apply the unique, generated styles to the blob
            blob.style.setProperty('--speed', `${speed}s`);
            blob.setAttribute('r', size); // Set radius directly for circles
            blob.style.setProperty('--delay', `${delay}s`);
            blob.style.setProperty('--skewX', skewX);
        });
    }
});