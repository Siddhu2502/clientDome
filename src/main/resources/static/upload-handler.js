document.addEventListener('DOMContentLoaded', () => {
    // === NEW: DEFINE THE GOAL AND TRACK STATE ===
    // This is our client-side "checklist". It must match the backend.
    // For your test, it's just 'aadhar'. Later, add the others back.
    const REQUIRED_DOCS = ['aadhar'];
    
    // A Set is an efficient way to store which docs we've successfully uploaded.
    const uploadedDocs = new Set();
    
    // Get a reference to the button we need to enable.
    const processButton = document.getElementById('process-button');
    // === END NEW ===

    const fileInput = document.getElementById('file-upload-input');
    const uploadSlots = document.querySelectorAll('.upload-slot');
    let currentDocType = null;

    uploadSlots.forEach(slot => {
        slot.addEventListener('click', () => {
            currentDocType = slot.getAttribute('data-doc-type');
            fileInput.click(); // Trigger the hidden file input
        });
    });

    fileInput.addEventListener('change', async (event) => {
        const file = event.target.files[0];
        if (!file || !currentDocType) return;

        const slot = document.querySelector(`.upload-slot[data-doc-type='${currentDocType}']`);
        const statusElement = slot.querySelector('.upload-status');
        statusElement.textContent = '⏳';

        const formData = new FormData();
        formData.append('file', file);
        formData.append('docType', currentDocType);

        try {
            const response = await fetch('/api/upload', {
                method: 'POST',
                body: formData,
            });

            if (!response.ok) throw new Error('Upload failed');
            
            const result = await response.json();
            console.log('Upload successful:', result);

            // Update the UI for this specific slot
            statusElement.textContent = '✅';
            slot.style.borderColor = '#2a9d8f';

            // === NEW: TRACK PROGRESS AND CHECK FOR COMPLETION ===
            // Add the successfully uploaded doc type to our Set.
            uploadedDocs.add(result.docType);
            
            // Run the check to see if we're done.
            checkCompletion();
            // === END NEW ===

        } catch (error) {
            console.error('Error uploading file:', error);
            statusElement.textContent = '❌';
            slot.style.borderColor = '#e76f51';
        }
        
        event.target.value = '';
    });

    // === NEW: THE COMPLETION CHECKER FUNCTION ===
    function checkCompletion() {
        // The .every() method checks if ALL items in our required list
        // are present in our 'uploadedDocs' Set.
        const allDocsUploaded = REQUIRED_DOCS.every(doc => uploadedDocs.has(doc));

        if (allDocsUploaded) {
            console.log('All required documents have been uploaded. Enabling process button.');
            processButton.disabled = false;
        }
    }
    // === END NEW ===
});