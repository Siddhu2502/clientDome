document.addEventListener('DOMContentLoaded', () => {
    // --- STATE AND GOAL DEFINITION ---
    // This checklist must be updated when you add more documents to the HTML.
    const REQUIRED_DOCS = ['aadhar']; 
    const uploadedDocs = new Set();
    
    // --- ELEMENT REFERENCES ---
    const processButton = document.getElementById('process-button');
    const fileInput = document.getElementById('file-upload-input');
    const uploadSlots = document.querySelectorAll('.upload-slot');
    let currentDocType = null;

    // --- EVENT LISTENER 1: Clicking an upload slot ---
    // This triggers the hidden file input.
    uploadSlots.forEach(slot => {
        slot.addEventListener('click', () => {
            currentDocType = slot.getAttribute('data-doc-type');
            fileInput.click();
        });
    });

    // --- EVENT LISTENER 2: Selecting a file ---
    // This handles the actual file upload to the backend.
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

            statusElement.textContent = '✅';
            slot.style.borderColor = '#2a9d8f';

            uploadedDocs.add(result.docType);
            checkCompletion(); // Check if all files are now uploaded

        } catch (error) {
            console.error('Error uploading file:', error);
            statusElement.textContent = '❌';
            slot.style.borderColor = '#e76f51';
        }
        
        event.target.value = ''; // Reset input to allow re-uploading the same file
    });

    // --- COMPLETION CHECKER FUNCTION ---
    // This function checks our progress against our goal.
    function checkCompletion() {
        const allDocsUploaded = REQUIRED_DOCS.every(doc => uploadedDocs.has(doc));
        if (allDocsUploaded) {
            console.log('All required documents uploaded. Enabling process button.');
            processButton.disabled = false;
        }
    }

    // === THIS IS THE NEW PART YOU WERE ASKING ABOUT ===
    // --- EVENT LISTENER 3: Clicking the process button ---
    // This triggers the backend orchestration.
    processButton.addEventListener('click', async () => {
        // Disable button to prevent multiple clicks and show feedback
        processButton.disabled = true;
        processButton.textContent = 'Processing...';

        try {
            const response = await fetch('/api/process-kyc', {
                method: 'POST',
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Server responded with status: ${response.status}`);
            }

            const result = await response.json();
            console.log('Processing result:', result);
            
            processButton.textContent = 'Processing Complete!';
            // In a real app, we'd add the final ID to the chat. For now, an alert is fine.
            alert('KYC Processing initiated successfully! The response from aadharDome is in the server logs and browser console.');

        } catch (error) {
            console.error('Failed to trigger KYC process:', error);
            processButton.textContent = 'Error! Retry?';
            processButton.disabled = false; // Re-enable button on error
            alert(`An error occurred: ${error.message}`);
        }
    });
    // === END OF THE NEW PART ===
});