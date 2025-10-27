package com.gestion.eventos.Service;

import com.gestion.eventos.DTO.LoginRequest;
import com.gestion.eventos.DTO.LoginResponse;
import com.gestion.eventos.DTO.MensajeResponse;
import com.gestion.eventos.DTO.UsuarioRegistroRequest;
import com.gestion.eventos.Model.UsuarioModel;
import com.gestion.eventos.Repository.IFacultadRepository;
import com.gestion.eventos.Repository.IProgramaRepository;
import com.gestion.eventos.Repository.IUnidadAcademicaRepository;
import com.gestion.eventos.Repository.IUsuarioRepository;
import com.gestion.eventos.Security.JwtUtil;
import com.gestion.eventos.Security.PasswordPolicy;
import com.gestion.eventos.Security.TokenBlacklistService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioServiceImp implements IUsuarioService {
    
    @Autowired IUsuarioRepository usuarioRepository;  
    @Autowired JavaMailSender mailSender;
    @Autowired JwtUtil jwtUtil;
    @Autowired TokenBlacklistService tokenBlacklistService;
    @Autowired IProgramaRepository programaRepository;
    @Autowired IUnidadAcademicaRepository unidadRepository;
    @Autowired IFacultadRepository facultadRepository;
   
    @Override
    public MensajeResponse guardarUsuario(UsuarioRegistroRequest request) {
        // Validación de campos obligatorios
         if(request.getIdentificacion()==null || request.getNombre()==null || request.getApellido()==null ||
        request.getCorreoInstitucional()==null || request.getContrasena()==null || request.getRol()==null){
            throw new IllegalArgumentException("Hay campos obligatorios vacíos");
        }
        //Validar correo institucional
        if(!request.getCorreoInstitucional().endsWith("@uao.edu.co")){
            throw new IllegalArgumentException("Solo se permite el uso del correo institucional");
        }
        // Validación de usuario único
        if(usuarioRepository.findByIdentificacion(request.getIdentificacion()).isPresent()){
            throw new IllegalArgumentException("Ya existe un usuario con ese número de identificación");
        }
        if(usuarioRepository.findByCorreoInstitucional(request.getCorreoInstitucional()).isPresent()){
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo");
        }
        if(!PasswordPolicy.isValid(request.getContrasena())){
            throw new IllegalArgumentException(PasswordPolicy.requirementsMessage());
        }        
        UsuarioModel usuario = new UsuarioModel();
        usuario.setIdentificacion(request.getIdentificacion());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreoInstitucional(request.getCorreoInstitucional());
        usuario.setContrasena(request.getContrasena());
        usuario.setRol(UsuarioModel.rol.valueOf(request.getRol())); // Enum

        //Campos opcionales según el rol
        switch (usuario.getRol()) {
            case estudiante -> {
                if (request.getCodigo() == null || request.getCodigoPrograma() == null || request.getCodigoPrograma().isBlank()) {
                throw new RuntimeException("El estudiante debe llenar su código y código del programa");
                }
                usuario.setCodigo(request.getCodigo());
                var programa = programaRepository.findById(request.getCodigoPrograma())
                    .orElseThrow(() -> new RuntimeException("Programa no encontrado"));
                usuario.setCodigo_programa(programa);
            }
            case docente -> {
                if (request.getCodigoUnidad() == null || request.getCodigoUnidad().isBlank()) {
                throw new RuntimeException("El docente debe estar asociado a una unidad académica");
                }
                var unidad = unidadRepository.findById(request.getCodigoUnidad())
                    .orElseThrow(() -> new RuntimeException("Unidad académica no encontrada"));
                usuario.setCodigo_unidad(unidad);
            }
            case secretaria_academica -> {
                if (request.getIdFacultad() == null || request.getIdFacultad().isBlank()) {
                throw new RuntimeException("La secretaría debe tener una facultad asociada");
                }
                var facultad = facultadRepository.findById(request.getIdFacultad())
                    .orElseThrow(() -> new RuntimeException("Facultad no encontrada"));
                usuario.setId_facultad(facultad);
            }
            default -> { }
        }
        if(request.getCodigo() != null && usuarioRepository.findByCodigo(request.getCodigo()).isPresent()){
            throw new IllegalArgumentException("El código ya se encuentra en uso");
        }
        usuarioRepository.save(usuario);
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(usuario.getCorreoInstitucional());
            helper.setSubject("Nuevo registro de cuenta");

            // 🔗 URL donde se inicia sesión (ajusta según tu entorno)
            String urlLogin = "http://localhost:4200/login";

            // 💌 Cuerpo del mensaje con HTML y botón
            String contenidoHtml =
                "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<h2>¡Hola, " + usuario.getNombre() + "!</h2>" +
                "<p>Has creado una cuenta en el <b>Sistema de Gestión de Eventos Universitarios</b>.</p>" +
                "<p>Este es tu correo registrado: <b>" + usuario.getCorreoInstitucional() + "</b></p>" +
                "<br>" +
                "<p>Para iniciar sesión, haz clic en el siguiente botón:</p>" +
                "<a href='" + urlLogin + "' " +
                "style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>" +
                "Iniciar sesión" +
                "</a>" +
                "<br><br>" +
                "<p style='font-size: 12px; color: gray;'>Si no creaste esta cuenta, ignora este mensaje.</p>" +
                "</body>" +
                "</html>";

            helper.setText(contenidoHtml, true); // true = HTML habilitado

            mailSender.send(mimeMessage);
            System.out.println("✅ Correo de registro enviado a " + usuario.getCorreoInstitucional());
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("❌ Error al enviar el correo: " + e.getMessage());
        }
        return new MensajeResponse("Usuario creado con éxito");         

}
 

    @Override
    public List<UsuarioModel> listarUsuario() {
        return usuarioRepository.findAll();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String correo = request.getCorreoInstitucional();
        String contrasena = request.getContrasena();

        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        Optional<UsuarioModel> usuarioOpt = usuarioRepository.findByCorreoInstitucional(correo);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        UsuarioModel usuario = usuarioOpt.get();

        if (!contrasena.equals(usuario.getContrasena())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = jwtUtil.generateToken(usuario);

        return new LoginResponse(
            token,
            "Bearer",
            jwtUtil.getExpirationMillis(),
            usuario.getIdentificacion(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getCorreoInstitucional(),
            usuario.getRol().name()
        );
    }

    @Override
    public void enviarCredencialesPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }

        Optional<UsuarioModel> usuarioOpt = usuarioRepository.findByCorreoInstitucional(correo);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("No existe un usuario con ese correo");
        }

        UsuarioModel usuario = usuarioOpt.get();
        String contrasena = usuario.getContrasena();

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Recuperación de credenciales");
        mensaje.setText("Hola " + usuario.getNombre() + 
                        ",\n\nTu contraseña registrada en el sistema es: " + contrasena +
                        "\n\nPor seguridad, te recomendamos cambiarla después de iniciar sesión.");

        
                        
        mailSender.send(mensaje);
    }
    @Override
    public UsuarioModel actualizarPerfil(Integer identificacion, String contrasenaActual, String contrasenaNueva, String celular, MultipartFile fotoPerfil) throws IOException {
        UsuarioModel usuario = usuarioRepository.findById(identificacion)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar contraseña
        if (contrasenaNueva != null && !contrasenaNueva.trim().isEmpty()) {
            if (contrasenaActual == null || contrasenaActual.trim().isEmpty()) {
                throw new RuntimeException("Debe proporcionar la contraseña actual para cambiarla");
            }
            if (!contrasenaActual.equals(usuario.getContrasena())) {
                throw new RuntimeException("La contraseña actual no coincide");
            }
            if (!PasswordPolicy.isValid(contrasenaNueva)) {
                throw new RuntimeException(PasswordPolicy.requirementsMessage());
            }
            usuario.setContrasena(contrasenaNueva);
        }

        // Validar y actualizar celular (solo números y 10 dígitos)
        if (celular != null && !celular.trim().isEmpty()) {
            if (!celular.matches("\\d{10}")) {
                throw new RuntimeException("El número de celular debe contener exactamente 10 dígitos");
            }
            usuario.setCelular(celular);
        }

        // Guardar imagen si viene
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            String contentType = fotoPerfil.getContentType();
            if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
                throw new RuntimeException("Solo se permiten imágenes PNG y JPG");
            }

            String directorio = "src/main/resources/static/uploads/perfiles/";
            String nombreArchivo = usuario.getIdentificacion() + "_" + fotoPerfil.getOriginalFilename();
            Path rutaArchivo = Paths.get(directorio, nombreArchivo);
            Files.createDirectories(rutaArchivo.getParent());
            fotoPerfil.transferTo(rutaArchivo.toFile());

            usuario.setFotoPerfil("/uploads/perfiles/" + nombreArchivo);
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public ResponseEntity<Resource> obtenerFoto(Integer id) throws IOException {
        UsuarioModel usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getFotoPerfil() == null) {
            return ResponseEntity.notFound().build();
        }

        Path ruta = Paths.get("src/main/resources/static" + usuario.getFotoPerfil());
        Resource recurso = new UrlResource(ruta.toUri());

        if (!recurso.exists() || !recurso.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(recurso);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token);
        }
    }

}






