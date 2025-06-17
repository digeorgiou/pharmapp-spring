document.addEventListener('DOMContentLoaded', function() {

            const form = document.getElementById('registerForm');
            const password = document.getElementById('password');
            const confirmPassword = document.getElementById('confirmPassword');
            const passwordMatch = document.getElementById('passwordMatch');
            const passwordMismatch = document.getElementById('passwordMismatch');
            const strengthBar = document.querySelector('.password-strength-bar');

            // Toggle password visibility
            document.querySelectorAll('.toggle-password').forEach(button => {
                button.addEventListener('click', function() {
                    const input = this.parentNode.querySelector('input');
                    const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                    input.setAttribute('type', type);
                    this.querySelector('i').classList.toggle('bi-eye-fill');
                    this.querySelector('i').classList.toggle('bi-eye-slash-fill');
                });
            });


            // Password strength indicator
            password.addEventListener('input', function() {
                const strength = calculatePasswordStrength(this.value);
                strengthBar.style.width = strength.percentage + '%';
                strengthBar.style.backgroundColor = strength.color;
            });

            // Password match checking
            confirmPassword.addEventListener('input', function() {
                if (password.value && this.value) {
                    if (password.value === this.value) {
                        passwordMatch.style.display = 'block';
                        passwordMismatch.style.display = 'none';
                        confirmPassword.setCustomValidity('');
                    } else {
                        passwordMatch.style.display = 'none';
                        passwordMismatch.style.display = 'block';
                        confirmPassword.setCustomValidity('Passwords must match');
                    }
                } else {
                    passwordMatch.style.display = 'none';
                    passwordMismatch.style.display = 'none';
                }
            });


            // Calculate password strength
            function calculatePasswordStrength(password) {
                let strength = 0;

                // Length contributes up to 50%
                strength += Math.min(50, (password.length / 12) * 50);

                // Mixed case contributes up to 20%
                if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)) {
                    strength += 20;
                }

                // Numbers contribute up to 20%
                if (password.match(/([0-9])/)) {
                    strength += 20;
                }

                // Special characters contribute up to 10%
                if (password.match(/([!,%,&,@,#,$,^,*,?,_,~])/)) {
                    strength += 10;
                }

                // Determine color based on strength
                let color;
                if (strength < 40) {
                    color = '#dc3545'; // Red
                } else if (strength < 70) {
                    color = '#fd7e14'; // Orange
                } else {
                    color = '#28a745'; // Green
                }

                return {
                    percentage: strength,
                    color: color
                };
            }
        });