document.addEventListener('DOMContentLoaded', function() {

        // Toggle password visibility
        document.querySelectorAll('.toggle-password').forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const input = this.closest('.input-group').querySelector('input');
                if (input) {
                    const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                    input.setAttribute('type', type);
                    const icon = this.querySelector('i');
                    if (icon) {
                        icon.classList.toggle('bi-eye-fill');
                        icon.classList.toggle('bi-eye-slash-fill');
                    }
                }
            });
        });
    });