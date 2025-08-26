// src/main/resources/static/chat.js

document.addEventListener('DOMContentLoaded', () => {
    const chatWindow = document.getElementById('chat-window');
    const chatForm = document.getElementById('chat-form');
    const messageInput = document.getElementById('message-input');

    chatForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const userMessage = messageInput.value.trim();
        if (userMessage === '') return;

        appendMessage(userMessage, 'user');
        messageInput.value = '';

        const thinkingIndicator = appendMessage('...', 'thinking');

        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: userMessage }),
            });

            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            const data = await response.json();
            const botResponse = data.response;

            thinkingIndicator.remove();
            appendMessage(botResponse, 'bot');

        } catch (error) {
            console.error('Error:', error);
            thinkingIndicator.remove();
            appendMessage('Sorry, I encountered an error. Please try again.', 'bot error');
        }
    });

    function appendMessage(text, type) {
        const messageDiv = document.createElement('div');
        // Add the base 'message' class to all
        messageDiv.classList.add('message');

        // --- FIX PART 2: The Helper Function Logic ---
        // This is the robust way to handle different message types.
        if (type === 'thinking') {
            // A "thinking" message is a bot message with an extra class for animation.
            messageDiv.classList.add('bot-message', 'thinking-message');
        } else if (type === 'bot error') {
            // An error message is a bot message with a special error class.
            messageDiv.classList.add('bot-message', 'error-message');
        } else {
            // For standard 'user' or 'bot' messages.
            messageDiv.classList.add(`${type}-message`);
        }
        
        const p = document.createElement('p');
        p.textContent = text;
        messageDiv.appendChild(p);
        
        chatWindow.appendChild(messageDiv);
        chatWindow.scrollTop = chatWindow.scrollHeight;
        return messageDiv;
    }
});