  const form = document.querySelector("form");

  form.addEventListener("submit", async function (e) {
    e.preventDefault(); 
    
    const data = Object.fromEntries(new FormData(form).entries());

    try {
      const response = await fetch("/usuarios/registrar", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(data)
      });

      if (!response.ok) {
        const errorMsg = await response.text();
        alert("Error: " + errorMsg);  
      } else {
        alert("Usuario creado con éxito!");
        form.reset();
      }
    } catch (err) {
      alert("Error de conexión con el servidor");
    }
  });

