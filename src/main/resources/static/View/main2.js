  const profileBtn = document.getElementById('profileBtn');
  const profileModal = document.getElementById('profileModal');
  const closeModal = document.getElementById('closeModal');
  const cancelBtn = document.getElementById('cancelBtn');
  const profileForm = document.getElementById('profileForm');
  const profileMsg = document.getElementById('profileMsg');

  // Abrir / cerrar modal
  function openModal() {
    profileModal.classList.add('open');
    profileModal.setAttribute('aria-hidden','false');
  }
  function closeModalFn() {
    profileModal.classList.remove('open');
    profileModal.setAttribute('aria-hidden','true');
    profileMsg.textContent = '';
  }
  profileBtn.onclick = openModal;
  closeModal.onclick = closeModalFn;
  cancelBtn.onclick = closeModalFn;

  // Formulario submit
  profileForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    profileMsg.textContent = '';

    const formData = new FormData();
    formData.append("celular", document.getElementById('celular').value || "");
    formData.append("password", document.getElementById('password').value || "");
    if (document.getElementById('foto').files[0]) {
      formData.append("foto", document.getElementById('foto').files[0]);
    }

    try {
      const res = await fetch('/usuarios/editar', {
        method: 'PUT',
        body: formData // importante para subir foto
      });

      if (res.ok) {
        profileMsg.textContent = "Perfil actualizado correctamente.";
        profileMsg.className = "form-msg success";
        setTimeout(closeModalFn, 1000);
      } else {
        const txt = await res.text();
        profileMsg.textContent = "Error: " + txt;
        profileMsg.className = "form-msg error";
      }
    } catch (err) {
      profileMsg.textContent = "No se pudo conectar con el servidor.";
      profileMsg.className = "form-msg error";
    }
  });