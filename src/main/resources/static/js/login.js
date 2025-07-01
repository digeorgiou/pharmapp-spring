    function togglePassword(fieldId) {
        const field = document.getElementById(fieldId);
        const icon = event.target;

        if (field.type === 'password') {
            field.type = 'text';
            icon.className = 'bi bi-eye-slash-fill text-gray-400 hover:text-gray-600';
        } else {
            field.type = 'password';
            icon.className = 'bi bi-eye-fill text-gray-400 hover:text-gray-600';
        }
    }

    // Handle reset form submission
    document.getElementById('resetForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const username = document.getElementById('resetUsername').value;
        document.getElementById('resetLink').value = resetLink;
        document.getElementById('resetLinkContainer').classList.remove('hidden');
    });