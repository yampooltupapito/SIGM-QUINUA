/**
 * SIGM-QUINUA - Funcionalidades interactivas de las pantallas de autenticación institucional
 */

document.addEventListener('DOMContentLoaded', function () {
    // Inicializar Lucide Icons
    if (window.lucide) {
        window.lucide.createIcons();
    }

    // 1. Alternar visualización de contraseñas (Toggle Password Visibility)
    document.querySelectorAll('.toggle-password-btn').forEach(button => {
        button.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (!input) return;

            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';

            const iconOpen = this.querySelector('.icon-eye');
            const iconClosed = this.querySelector('.icon-eye-off');

            if (iconOpen && iconClosed) {
                if (isPassword) {
                    iconOpen.classList.add('hidden');
                    iconClosed.classList.remove('hidden');
                    this.setAttribute('aria-label', 'Ocultar contraseña');
                } else {
                    iconOpen.classList.remove('hidden');
                    iconClosed.classList.add('hidden');
                    this.setAttribute('aria-label', 'Mostrar contraseña');
                }
            }
        });
    });

    // 2. Botones de autocompletado rápido de credenciales institucionales
    document.querySelectorAll('.quick-cred-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const user = this.getAttribute('data-fill-user');
            const pass = this.getAttribute('data-fill-pass');
            const userInput = document.getElementById('username');
            const passInput = document.getElementById('password');
            if (userInput && passInput && user && pass) {
                userInput.value = user;
                passInput.value = pass;
                userInput.focus();
            }
        });
    });

    // 3. Control de estado de envío en formularios de autenticación (Anti-doble envío)
    document.querySelectorAll('.auth-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            const submitBtn = this.querySelector('.btn-submit');
            if (submitBtn) {
                if (submitBtn.disabled) {
                    e.preventDefault();
                    return;
                }
                const spinner = submitBtn.querySelector('.btn-spinner');
                const textNormal = submitBtn.querySelector('.btn-text-normal');
                const textLoading = submitBtn.querySelector('.btn-text-loading');

                submitBtn.disabled = true;
                if (spinner) spinner.classList.remove('hidden');
                if (textNormal) textNormal.classList.add('hidden');
                if (textLoading) textLoading.classList.remove('hidden');
            }
        });
    });

    // 3. Medidor de fortaleza y coincidencia de contraseña en reset-password
    const newPasswordInput = document.getElementById('nuevaContrasena');
    const confirmPasswordInput = document.getElementById('confirmarContrasena');
    const strengthBar = document.getElementById('passwordStrengthBar');
    const strengthText = document.getElementById('passwordStrengthText');
    const matchErrorText = document.getElementById('passwordMatchError');

    if (newPasswordInput && strengthBar && strengthText) {
        newPasswordInput.addEventListener('input', function () {
            const val = this.value;
            let score = 0;

            if (val.length >= 8) score++;
            if (/[A-Z]/.test(val) && /[a-z]/.test(val)) score++;
            if (/[0-9]/.test(val)) score++;
            if (/[^A-Za-z0-9]/.test(val)) score++;

            strengthBar.className = 'strength-meter-bar';
            if (val.length === 0) {
                strengthBar.style.width = '0%';
                strengthText.textContent = '';
            } else if (score <= 1) {
                strengthBar.style.width = '25%';
                strengthBar.style.backgroundColor = '#ef4444'; // Red
                strengthText.textContent = 'Débil (mínimo 8 caracteres requeridos)';
                strengthText.className = 'text-[11px] text-rose-400 mt-1';
            } else if (score === 2) {
                strengthBar.style.width = '50%';
                strengthBar.style.backgroundColor = '#f59e0b'; // Amber
                strengthText.textContent = 'Moderada (agregue números y símbolos)';
                strengthText.className = 'text-[11px] text-amber-400 mt-1';
            } else if (score === 3) {
                strengthBar.style.width = '75%';
                strengthBar.style.backgroundColor = '#3b82f6'; // Blue
                strengthText.textContent = 'Fuerte';
                strengthText.className = 'text-[11px] text-blue-400 mt-1';
            } else {
                strengthBar.style.width = '100%';
                strengthBar.style.backgroundColor = '#10b981'; // Emerald
                strengthText.textContent = 'Excelente y segura';
                strengthText.className = 'text-[11px] text-emerald-400 mt-1';
            }

            checkPasswordsMatch();
        });
    }

    if (confirmPasswordInput && matchErrorText) {
        confirmPasswordInput.addEventListener('input', checkPasswordsMatch);
    }

    function checkPasswordsMatch() {
        if (!newPasswordInput || !confirmPasswordInput || !matchErrorText) return;
        const val1 = newPasswordInput.value;
        const val2 = confirmPasswordInput.value;

        if (val2.length > 0 && val1 !== val2) {
            matchErrorText.classList.remove('hidden');
        } else {
            matchErrorText.classList.add('hidden');
        }
    }
});
