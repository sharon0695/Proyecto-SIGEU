package com.gestion.eventos.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.sql.Time;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.Model.ColaboracionModel;
import com.gestion.eventos.Model.EspacioModel;
import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.FacultadModel;
import com.gestion.eventos.Model.NotificacionModel;
import com.gestion.eventos.Model.OrganizacionModel;
import com.gestion.eventos.Model.ReservacionModel;
import com.gestion.eventos.Model.ResponsableEventoModel;
import com.gestion.eventos.Model.UsuarioModel;
import com.gestion.eventos.Repository.IColaboracionRepository;
import com.gestion.eventos.Repository.IEspacioRepository;
import com.gestion.eventos.Repository.IEventoRepository;
import com.gestion.eventos.Repository.INotificacionRepository;
import com.gestion.eventos.Repository.IOrganizacionRepository;
import com.gestion.eventos.Repository.IReservacionRepository;
import com.gestion.eventos.Repository.IResponsableEventoRepository;
import com.gestion.eventos.Repository.IUsuarioRepository;

@Service
public class EventoServiceImp implements IEventoService {
    
    @Autowired private IEventoRepository eventoRepository;
    @Autowired private IOrganizacionRepository organizacionRepository;
    @Autowired private IColaboracionRepository colaboracionRepository;
    @Autowired private IResponsableEventoRepository responsableEventoRepository;
    @Autowired private IReservacionRepository reservacionRepository;
    @Autowired private IEspacioRepository espacioRepository;
    @Autowired private IUsuarioRepository usuarioRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private INotificacionRepository notificacionRepository;


    @Override
    @Transactional
    public EventoModel registrarEventoCompleto(EventoRegistroCompleto request) {
        validarCamposEvento(request);
        
        if (request.getId_usuario_registra() == null) {
            throw new IllegalArgumentException("El ID del usuario que registra el evento es obligatorio");
        }
        
        usuarioRepository.findById(request.getId_usuario_registra())
            .orElseThrow(() -> new IllegalArgumentException("El usuario registrador no existe en el sistema"));
        
        // Validar duplicidad de evento
        validarDuplicidadEvento(request);
        
        EventoModel evento = new EventoModel();
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setTipo(request.getTipo());
        evento.setFecha(request.getFecha());
        evento.setHora_inicio(request.getHora_inicio());
        evento.setHora_fin(request.getHora_fin());
        evento.setEstado(EventoModel.estado.borrador);
        evento.setIdUsuarioRegistra(request.getId_usuario_registra());
        
        evento = eventoRepository.save(evento);
        
        if (request.getColaboraciones() != null && !request.getColaboraciones().isEmpty()) {
            procesarColaboraciones(request.getColaboraciones(), evento);
        }
        
        if (request.getResponsables() != null && !request.getResponsables().isEmpty()) {
            procesarResponsables(request.getResponsables(), evento);
        }
        
        if (request.getReservaciones() != null && !request.getReservaciones().isEmpty()) {
            procesarReservaciones(request.getReservaciones(), evento);
        }
        
        return evento;
    }

    private void validarCamposEvento(EventoRegistroCompleto request) {
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del evento es obligatoria");
        }
        if (request.getTipo() == null || request.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo del evento es obligatorio");
        }
        if (!request.getTipo().equalsIgnoreCase("Academico") && !request.getTipo().equalsIgnoreCase("Ludico")) {
            throw new IllegalArgumentException("El tipo de evento debe ser 'Academico' o 'Ludico'");
        }
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        if (request.getHora_inicio() == null) {
            throw new IllegalArgumentException("La hora de inicio del evento es obligatoria");
        }
        if (request.getHora_fin() == null) {
            throw new IllegalArgumentException("La hora de fin del evento es obligatoria");
        }
        
        java.time.LocalDate fechaActual = java.time.LocalDate.now();
        java.time.LocalDate fechaEvento = request.getFecha().toLocalDate();
        
        if (fechaEvento.isBefore(fechaActual)) {
            throw new IllegalArgumentException("La fecha del evento no puede ser anterior a la fecha actual");
        }
        
        if (request.getHora_inicio().equals(request.getHora_fin())) {
            throw new IllegalArgumentException("La hora de inicio y la hora de fin no pueden ser iguales");
        }
        
        if (request.getHora_fin().before(request.getHora_inicio())) {
            throw new IllegalArgumentException("La hora de fin no puede ser anterior a la hora de inicio");
        }
        
        if (request.getReservaciones() == null || request.getReservaciones().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un espacio para el evento");
        }
        
        if (request.getResponsables() == null || request.getResponsables().isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un responsable al evento");
        }
    }

    private void validarDuplicidadEvento(EventoRegistroCompleto request) {
        Optional<EventoModel> eventoExistente = eventoRepository.findByNombreAndFechaAndHoraInicio(
            request.getNombre(), 
            request.getFecha(), 
            request.getHora_inicio()
        );
        
        if (eventoExistente.isPresent()) {
            throw new IllegalArgumentException(
                "Ya existe un evento con el mismo nombre, fecha y hora de inicio. " +
                "Por favor, verifique los datos o modifique el nombre, fecha u hora del evento."
            );
        }
    }
    
    private void validarDuplicidadEventoEdicion(EventoEdicionCompleto request) {
        Optional<EventoModel> eventoExistente = eventoRepository.findByNombreAndFechaAndHoraInicio(
            request.getNombre(), 
            request.getFecha(), 
            request.getHora_inicio()
        );
        
        // Si existe un evento con los mismos datos pero es diferente al que estamos editando, es duplicado
        if (eventoExistente.isPresent() && !eventoExistente.get().getCodigo().equals(request.getCodigo())) {
            throw new IllegalArgumentException(
                "Ya existe otro evento con el mismo nombre, fecha y hora de inicio. " +
                "Por favor, verifique los datos o modifique el nombre, fecha u hora del evento."
            );
        }
    }

    private void procesarColaboraciones(
        List<EventoRegistroCompleto.ColaboracionDTO> colaboracionesDTO, 
        EventoModel evento) {
    
        for (int i = 0; i < colaboracionesDTO.size(); i++) {
            EventoRegistroCompleto.ColaboracionDTO colabDTO = colaboracionesDTO.get(i);
            
            try {
                System.out.println("Procesando colaboración registro " + (i + 1) + ": " + colabDTO.getNit());
                
                if (colabDTO.getNit() == null || colabDTO.getNit().trim().isEmpty()) {
                    throw new IllegalArgumentException("El NIT de la colaboración " + (i + 1) + " es obligatorio");
                }

                // Buscar organización existente
                OrganizacionModel organizacion = organizacionRepository.findByNit(colabDTO.getNit())
                        .orElseThrow(() -> new IllegalArgumentException("La organización con NIT " + colabDTO.getNit() + " no existe"));

                // Procesar archivo
                String certificadoPath = procesarArchivoColaboracionRegistro(colabDTO, evento.getCodigo());

                // Validar representante alterno
                if (colabDTO.getRepresentante_alterno() != null && !colabDTO.getRepresentante_alterno().trim().isEmpty()) {
                    if (colabDTO.getRepresentante_alterno().length() < 3) {
                        throw new IllegalArgumentException("El nombre del representante alterno debe tener al menos 3 caracteres");
                    }
                }

                // Crear colaboración
                ColaboracionModel colaboracion = new ColaboracionModel();
                colaboracion.setNitOrganizacion(organizacion);
                colaboracion.setCodigoEvento(evento);
                colaboracion.setCertificado_participacion(certificadoPath);
                colaboracion.setRepresentante_alterno(colabDTO.getRepresentante_alterno());
                
                colaboracionRepository.save(colaboracion);
                System.out.println("✓ Colaboración de registro creada: " + organizacion.getNit());
                
            } catch (Exception e) {
                System.err.println("✗ Error procesando colaboración registro " + (i + 1) + ": " + e.getMessage());
                throw new RuntimeException("Error en colaboración registro " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
    }


    private void procesarResponsables(
        List<EventoRegistroCompleto.ResponsableDTO> responsablesDTO, 
        EventoModel evento) {
    
        for (int i = 0; i < responsablesDTO.size(); i++) {
            EventoRegistroCompleto.ResponsableDTO respDTO = responsablesDTO.get(i);
            
            try {
                System.out.println("Procesando responsable registro " + (i + 1) + ": " + respDTO.getId_usuario());
                
                if (respDTO.getId_usuario() == null) {
                    throw new IllegalArgumentException("El responsable " + (i + 1) + " no ha sido seleccionado");
                }

                UsuarioModel usuario = usuarioRepository.findById(respDTO.getId_usuario())
                        .orElseThrow(() -> new IllegalArgumentException("El responsable no existe en el sistema"));

                // Procesar archivo
                String documentoAvalPath = procesarArchivoResponsableRegistro(respDTO, evento.getCodigo());

                // Crear responsable
                ResponsableEventoModel responsable = new ResponsableEventoModel();
                responsable.setIdUsuario(usuario);
                responsable.setCodigoEvento(evento);
                responsable.setDocumentoAval(documentoAvalPath);
                
                ResponsableEventoModel.tipo_aval tipoAval = null;
              
                switch (usuario.getRol()) {
                    case estudiante:
                        tipoAval = ResponsableEventoModel.tipo_aval.director_programa;
                        break;
                    case docente:
                        tipoAval = ResponsableEventoModel.tipo_aval.director_docencia;
                        break;
                    default:
                        tipoAval = null; // si no aplica
                        break;
                }

                if (tipoAval != null) {
                    responsable.setTipoAval(tipoAval);
                }

                
                responsableEventoRepository.save(responsable);
                System.out.println("✓ Responsable de registro creado: " + usuario.getIdentificacion());
                
            } catch (Exception e) {
                System.err.println("✗ Error procesando responsable registro " + (i + 1) + ": " + e.getMessage());
                throw new RuntimeException("Error en responsable registro " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
    }
    private String procesarArchivoColaboracionRegistro(EventoRegistroCompleto.ColaboracionDTO colabDTO, Integer codigoEvento) {
    String certificadoPath = null;
    
    if (colabDTO.getCertificado_participacion() != null && 
        !colabDTO.getCertificado_participacion().isEmpty()) {
        
        // Validar tipo de archivo
        if (!colabDTO.getCertificado_participacion().getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("El certificado de participación debe ser un archivo PDF");
        }
        
        // Validar tamaño del archivo (máximo 5MB)
        if (colabDTO.getCertificado_participacion().getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El certificado de participación no debe superar los 5MB");
        }
        
        certificadoPath = fileStorageService.storeFile(
            colabDTO.getCertificado_participacion(), 
            "organizaciones/evento_" + codigoEvento
        );
        System.out.println("✓ Certificado de colaboración guardado: " + certificadoPath);
    } else {
        throw new IllegalArgumentException(
            "Debe adjuntar el certificado de participación en formato PDF. " +
            "Este documento es obligatorio para registrar la colaboración con la organización externa."
        );
    }
    
    return certificadoPath;
}

    private String procesarArchivoResponsableRegistro(EventoRegistroCompleto.ResponsableDTO respDTO, Integer codigoEvento) {
    String documentoAvalPath = null;
    
    if (respDTO.getDocumentoAval() != null && 
        !respDTO.getDocumentoAval().isEmpty()) {
        
        // Validar tipo de archivo
        if (!respDTO.getDocumentoAval().getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("El documento de aval del responsable debe ser un archivo PDF");
        }
        
        // Validar tamaño del archivo (máximo 5MB)
        if (respDTO.getDocumentoAval().getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El documento de aval no debe superar los 5MB");
        }
        
        documentoAvalPath = fileStorageService.storeFile(
            respDTO.getDocumentoAval(), 
            "responsables/evento_" + codigoEvento
        );
        System.out.println("✓ Documento de aval de responsable guardado: " + documentoAvalPath);
    } else {
        throw new IllegalArgumentException(
            "Debe adjuntar el documento de aval en formato PDF. " +
            "Este documento es obligatorio y debe estar firmado por la autoridad correspondiente (Director de Programa o Director de Docencia)."
        );
    }
    
    return documentoAvalPath;
}
   private void procesarReservaciones(List<EventoRegistroCompleto.ReservacionDTO> reservacionesDTO, EventoModel evento) {
    int contador = 0;
    Map<String, Integer> capacidadesPorEspacio = new HashMap<>();
    
    // Primero validar todos los espacios y calcular capacidad total
    for (EventoRegistroCompleto.ReservacionDTO resDTO : reservacionesDTO) {
        contador++;
        final int numeroEspacio = contador;
        
        if (resDTO.getCodigo_espacio() == null || resDTO.getCodigo_espacio().trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar el espacio " + numeroEspacio);
        }
        
        if (resDTO.getHora_inicio() == null) {
            throw new IllegalArgumentException("La hora de inicio del espacio " + numeroEspacio + " es obligatoria");
        }
        
        if (resDTO.getHora_fin() == null) {
            throw new IllegalArgumentException("La hora de fin del espacio " + numeroEspacio + " es obligatoria");
        }
        
        if (resDTO.getHora_inicio().equals(resDTO.getHora_fin())) {
            throw new IllegalArgumentException("La hora de inicio y fin del espacio " + numeroEspacio + " no pueden ser iguales");
        }
        
        if (resDTO.getHora_fin().before(resDTO.getHora_inicio())) {
            throw new IllegalArgumentException("La hora de fin del espacio " + numeroEspacio + " debe ser posterior a la hora de inicio");
        }
        
        final String codigoEspacio = resDTO.getCodigo_espacio();
        EspacioModel espacio = espacioRepository.findById(codigoEspacio)
            .orElseThrow(() -> new IllegalArgumentException("El espacio " + codigoEspacio + " no existe en el sistema"));
        
        // Validar capacidad del espacio
        if (espacio.getCapacidad() == null || espacio.getCapacidad() <= 0) {
            throw new IllegalArgumentException(
                "El espacio " + codigoEspacio + " no tiene una capacidad válida configurada. " +
                "Por favor, contacte al administrador del sistema."
            );
        }
        
        // Acumular capacidad (si es el mismo espacio usado múltiples veces, solo contar una vez)
        capacidadesPorEspacio.putIfAbsent(codigoEspacio, espacio.getCapacidad());
    }
    
    // Calcular capacidad total disponible
    int capacidadTotalDisponible = capacidadesPorEspacio.values().stream()
        .mapToInt(Integer::intValue)
        .sum();
    
    // Si se necesita validar contra una capacidad específica del evento, agregar aquí
    // Por ahora, solo mostramos información en los logs
    System.out.println("📊 Capacidad total disponible en espacios seleccionados: " + capacidadTotalDisponible);
    
    // Validar que no haya solapamiento de horarios en el mismo espacio
    validarSolapamientoReservaciones(reservacionesDTO);
    
    // Crear las reservaciones
    contador = 0;
    for (EventoRegistroCompleto.ReservacionDTO resDTO : reservacionesDTO) {
        contador++;
        final String codigoEspacio = resDTO.getCodigo_espacio();
        EspacioModel espacio = espacioRepository.findById(codigoEspacio).orElse(null);
        
        if (espacio == null) continue;
        
        ReservacionModel reservacion = new ReservacionModel();
        reservacion.setCodigoEvento(evento);
        reservacion.setCodigo_espacio(espacio);
        reservacion.setHora_inicio(resDTO.getHora_inicio());
        reservacion.setHora_fin(resDTO.getHora_fin());
        
        reservacionRepository.save(reservacion);
        System.out.println("✓ Reservación creada para espacio: " + espacio.getNombre() + " (Capacidad: " + espacio.getCapacidad() + ")");
    }
}
    
    private void validarSolapamientoReservaciones(List<EventoRegistroCompleto.ReservacionDTO> reservacionesDTO) {
        // Validar que no haya solapamiento de horarios en el mismo espacio
        for (int i = 0; i < reservacionesDTO.size(); i++) {
            EventoRegistroCompleto.ReservacionDTO res1 = reservacionesDTO.get(i);
            String espacio1 = res1.getCodigo_espacio();
            Time inicio1 = res1.getHora_inicio();
            Time fin1 = res1.getHora_fin();
            
            for (int j = i + 1; j < reservacionesDTO.size(); j++) {
                EventoRegistroCompleto.ReservacionDTO res2 = reservacionesDTO.get(j);
                String espacio2 = res2.getCodigo_espacio();
                Time inicio2 = res2.getHora_inicio();
                Time fin2 = res2.getHora_fin();
                
                // Si es el mismo espacio, verificar solapamiento
                if (espacio1 != null && espacio1.equals(espacio2)) {
                    // Verificar si hay solapamiento: inicio1 < fin2 && inicio2 < fin1
                    if (inicio1.before(fin2) && inicio2.before(fin1)) {
                        throw new IllegalArgumentException(
                            "El espacio " + espacio1 + " tiene horarios solapados. " +
                            "Por favor, ajuste los horarios para evitar conflictos."
                        );
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public EventoModel editarEventoCompleto(EventoEdicionCompleto request) {
        try {
            
            // Validar que el evento existe y es editable
            EventoModel eventoExistente = eventoRepository.findById(request.getCodigo())
                    .orElseThrow(() -> new IllegalArgumentException("El evento a editar no existe"));

            if (!eventoExistente.getEstado().equals(EventoModel.estado.borrador) && 
                !eventoExistente.getEstado().equals(EventoModel.estado.rechazado)) {
                throw new IllegalArgumentException("Solo se pueden editar eventos en estado 'borrador' o 'rechazado'");
            }

            // Validar campos básicos
            validarCamposEventoEdicion(request);
            
            // Validar duplicidad de evento (excluyendo el evento actual)
            validarDuplicidadEventoEdicion(request);

            System.out.println("Obteniendo archivos existentes...");
            List<ColaboracionModel> colaboracionesExistentes = colaboracionRepository.findAllByCodigoEvento_Codigo(request.getCodigo());
            List<ResponsableEventoModel> responsablesExistentes = responsableEventoRepository.findAllByCodigoEvento_Codigo(request.getCodigo());

            Map<String, String> archivosColaboracionesExistentes = new HashMap<>();
            for (ColaboracionModel colab : colaboracionesExistentes) {
                if (colab.getCertificado_participacion() != null) {
                    archivosColaboracionesExistentes.put(colab.getNitOrganizacion().getNit(), colab.getCertificado_participacion());
                }
            }

            Map<Integer, String> archivosResponsablesExistentes = new HashMap<>();
            for (ResponsableEventoModel resp : responsablesExistentes) {
                if (resp.getDocumentoAval() != null) {
                    archivosResponsablesExistentes.put(resp.getIdUsuario().getIdentificacion(), resp.getDocumentoAval());
                }
            }

            System.out.println("Actualizando evento básico...");
            eventoExistente.setNombre(request.getNombre());
            eventoExistente.setDescripcion(request.getDescripcion());
            eventoExistente.setTipo(request.getTipo());
            eventoExistente.setFecha(request.getFecha());
            eventoExistente.setHora_inicio(request.getHora_inicio());
            eventoExistente.setHora_fin(request.getHora_fin());

            EventoModel eventoActualizado = eventoRepository.save(eventoExistente);

            System.out.println("Eliminando relaciones existentes...");
            eliminarRelacionesExistentes(request.getCodigo());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Creando nuevas relaciones...");
            
            if (request.getColaboraciones() != null && !request.getColaboraciones().isEmpty()) {
                procesarColaboracionesEdicion(
                    request.getColaboraciones(), 
                    eventoActualizado, 
                    archivosColaboracionesExistentes
                );
            }

            if (request.getResponsables() != null && !request.getResponsables().isEmpty()) {
                procesarResponsablesEdicion(
                    request.getResponsables(), 
                    eventoActualizado, 
                    archivosResponsablesExistentes
                );
            }

            if (request.getReservaciones() != null && !request.getReservaciones().isEmpty()) {
                procesarReservacionesEdicion(request.getReservaciones(), eventoActualizado);
            }

            System.out.println("✓ Edición completada exitosamente");
            return eventoActualizado;

        } catch (Exception e) {
            System.err.println("✗ Error en edición: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al editar evento: " + e.getMessage(), e);
        }
    }


    private void procesarColaboracionesEdicion(
        List<EventoEdicionCompleto.ColaboracionEdicionDTO> colaboracionesDTO, 
        EventoModel evento,
        Map<String, String> archivosExistentes) {
        
        for (int i = 0; i < colaboracionesDTO.size(); i++) {
            EventoEdicionCompleto.ColaboracionEdicionDTO colabDTO = colaboracionesDTO.get(i);
            
            try {
                System.out.println("Procesando colaboración " + (i + 1) + ": " + colabDTO.getNit());
                
                if (colabDTO.getNit() == null || colabDTO.getNit().trim().isEmpty()) {
                    throw new IllegalArgumentException("El NIT de la colaboración " + (i + 1) + " es obligatorio");
                }

                // Buscar organización existente
                OrganizacionModel organizacion = organizacionRepository.findByNit(colabDTO.getNit())
                        .orElseThrow(() -> new IllegalArgumentException("La organización con NIT " + colabDTO.getNit() + " no existe"));

               
                Optional<ColaboracionModel> colaboracionExistente = colaboracionRepository
                    .findByNitOrganizacionAndCodigoEvento(organizacion, evento);
                
                if (colaboracionExistente.isPresent()) {
                    System.out.println("⚠ Colaboración ya existe, eliminando...");
                    colaboracionRepository.delete(colaboracionExistente.get());
                    colaboracionRepository.flush();
                }

                // Procesar archivo
                String certificadoPath = procesarArchivoColaboracion(colabDTO, organizacion.getNit(), archivosExistentes, evento.getCodigo());

                // Validar representante alterno
                if (colabDTO.getRepresentante_alterno() != null && !colabDTO.getRepresentante_alterno().trim().isEmpty()) {
                    if (colabDTO.getRepresentante_alterno().length() < 3) {
                        throw new IllegalArgumentException("El nombre del representante alterno debe tener al menos 3 caracteres");
                    }
                }

                // Crear NUEVA colaboración
                ColaboracionModel nuevaColaboracion = new ColaboracionModel();
                nuevaColaboracion.setNitOrganizacion(organizacion);
                nuevaColaboracion.setCodigoEvento(evento);
                nuevaColaboracion.setCertificado_participacion(certificadoPath);
                nuevaColaboracion.setRepresentante_alterno(colabDTO.getRepresentante_alterno());
                
                colaboracionRepository.save(nuevaColaboracion);
                colaboracionRepository.flush(); // Forzar persistencia inmediata
                
                System.out.println("✓ Colaboración creada: " + organizacion.getNit());
                
            } catch (Exception e) {
                System.err.println("✗ Error procesando colaboración " + (i + 1) + ": " + e.getMessage());
                throw new RuntimeException("Error en colaboración " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
    }

    private String procesarArchivoColaboracion(EventoEdicionCompleto.ColaboracionEdicionDTO colabDTO, 
                                         String nit, 
                                         Map<String, String> archivosExistentes,
                                         Integer codigoEvento) {
        String certificadoPath = null;
        
        if (colabDTO.getCertificado_participacion() != null && 
            !colabDTO.getCertificado_participacion().isEmpty()) {
            
            // ARCHIVO NUEVO: Eliminar el anterior si existe
            String archivoAnterior = archivosExistentes.get(nit);
            if (archivoAnterior != null) {
                try {
                    boolean eliminado = fileStorageService.deleteFile(archivoAnterior);
                    System.out.println(eliminado ? "✓ Archivo anterior eliminado" : "ℹ️ Archivo anterior no existía");
                } catch (Exception e) {
                    System.out.println("⚠ No se pudo eliminar archivo anterior: " + e.getMessage());
                }
            }
            
            // Validar y guardar nuevo archivo
            if (!colabDTO.getCertificado_participacion().getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("El certificado debe ser un archivo PDF");
            }
            
            certificadoPath = fileStorageService.storeFile(
                colabDTO.getCertificado_participacion(), 
                "organizaciones/evento_" + codigoEvento
            );
            System.out.println("✓ Nuevo archivo guardado: " + certificadoPath);
            
        } else if (colabDTO.getCertificado_existente() != null && 
                !colabDTO.getCertificado_existente().trim().isEmpty()) {
            // MANTENER ARCHIVO EXISTENTE
            certificadoPath = colabDTO.getCertificado_existente();
            System.out.println("✓ Manteniendo archivo existente: " + certificadoPath);
        }
        
        return certificadoPath;
    }


    private void procesarResponsablesEdicion(
        List<EventoEdicionCompleto.ResponsableDTO> responsablesDTO, 
        EventoModel evento,
        Map<Integer, String> archivosExistentes) {
    
        for (int i = 0; i < responsablesDTO.size(); i++) {
            EventoEdicionCompleto.ResponsableDTO respDTO = responsablesDTO.get(i);
            
            try {
                System.out.println("Procesando responsable " + (i + 1) + ": " + respDTO.getId_usuario());
                
                if (respDTO.getId_usuario() == null) {
                    throw new IllegalArgumentException("El responsable " + (i + 1) + " no ha sido seleccionado");
                }

                UsuarioModel usuario = usuarioRepository.findById(respDTO.getId_usuario())
                        .orElseThrow(() -> new IllegalArgumentException("El responsable no existe en el sistema"));

                Optional<ResponsableEventoModel> responsableExistente = responsableEventoRepository
                    .findByIdUsuarioAndCodigoEvento(usuario, evento);
                
                if (responsableExistente.isPresent()) {
                    System.out.println("⚠ Responsable ya existe, eliminando...");
                    responsableEventoRepository.delete(responsableExistente.get());
                    responsableEventoRepository.flush();
                }

                // Procesar archivo
                String documentoAvalPath = procesarArchivoResponsable(respDTO, usuario.getIdentificacion(), archivosExistentes, evento.getCodigo());

                // Crear NUEVO responsable
                ResponsableEventoModel nuevoResponsable = new ResponsableEventoModel();
                nuevoResponsable.setIdUsuario(usuario);
                nuevoResponsable.setCodigoEvento(evento);
                nuevoResponsable.setDocumentoAval(documentoAvalPath);
                
                ResponsableEventoModel.tipo_aval tipoAval = null;
              
                switch (usuario.getRol()) {
                    case estudiante:
                        tipoAval = ResponsableEventoModel.tipo_aval.director_programa;
                        break;
                    case docente:
                        tipoAval = ResponsableEventoModel.tipo_aval.director_docencia;
                        break;
                    default:
                        tipoAval = null; // si no aplica
                        break;
                }

                if (tipoAval != null) {
                    nuevoResponsable.setTipoAval(tipoAval);
                }

                
                responsableEventoRepository.save(nuevoResponsable);
                responsableEventoRepository.flush();
                
                System.out.println("✓ Responsable creado: " + usuario.getIdentificacion());
                
            } catch (Exception e) {
                System.err.println("✗ Error procesando responsable " + (i + 1) + ": " + e.getMessage());
                throw new RuntimeException("Error en responsable " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
    }
    private String procesarArchivoResponsable(EventoEdicionCompleto.ResponsableDTO respDTO, 
                                        Integer idUsuario, 
                                        Map<Integer, String> archivosExistentes,
                                        Integer codigoEvento) {
        String documentoAvalPath = null;
        
        if (respDTO.getDocumentoAval() != null && 
            !respDTO.getDocumentoAval().isEmpty()) {
            
            // ARCHIVO NUEVO: Eliminar el anterior si existe
            String archivoAnterior = archivosExistentes.get(idUsuario);
            if (archivoAnterior != null) {
                try {
                    boolean eliminado = fileStorageService.deleteFile(archivoAnterior);
                    System.out.println(eliminado ? "✓ Archivo anterior de responsable eliminado: " + archivoAnterior 
                                            : "ℹ️ Archivo anterior de responsable no existía: " + archivoAnterior);
                } catch (Exception e) {
                    System.out.println("⚠ No se pudo eliminar archivo anterior del responsable: " + e.getMessage());
                }
            }
            
            // Validar y guardar nuevo archivo
            if (!respDTO.getDocumentoAval().getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("El documento de aval del responsable debe ser un archivo PDF");
            }
            
            documentoAvalPath = fileStorageService.storeFile(
                respDTO.getDocumentoAval(), 
                "responsables/evento_" + codigoEvento
            );
            System.out.println("✓ Nuevo documento de responsable guardado: " + documentoAvalPath);
            
        } else if (respDTO.getDocumento_existente() != null && 
                !respDTO.getDocumento_existente().trim().isEmpty()) {
            // MANTENER ARCHIVO EXISTENTE
            documentoAvalPath = respDTO.getDocumento_existente();
            System.out.println("✓ Manteniendo documento existente del responsable: " + documentoAvalPath);
        } else {
            System.out.println("ℹ️ Sin documento para responsable: " + idUsuario);
        }
        
        return documentoAvalPath;
    }
    private void procesarReservacionesEdicion(List<EventoEdicionCompleto.ReservacionDTO> reservacionesDTO, EventoModel evento) {
        // Reservaciones son obligatorias - ya validamos que no son null/vacíos, así que procesamos directamente
        int contador = 0;
        
        // Primero validar todos los espacios y calcular capacidad total necesaria
        for (EventoEdicionCompleto.ReservacionDTO resDTO : reservacionesDTO) {
            contador++;
            final int numeroEspacio = contador;
            
            if (resDTO.getCodigo_espacio() == null || resDTO.getCodigo_espacio().trim().isEmpty()) {
                throw new IllegalArgumentException("Debe seleccionar el espacio " + numeroEspacio);
            }
            
            if (resDTO.getHora_inicio() == null) {
                throw new IllegalArgumentException("La hora de inicio del espacio " + numeroEspacio + " es obligatoria");
            }
            
            if (resDTO.getHora_fin() == null) {
                throw new IllegalArgumentException("La hora de fin del espacio " + numeroEspacio + " es obligatoria");
            }
            
            if (resDTO.getHora_inicio().equals(resDTO.getHora_fin())) {
                throw new IllegalArgumentException("La hora de inicio y fin del espacio " + numeroEspacio + " no pueden ser iguales");
            }
            
            if (resDTO.getHora_fin().before(resDTO.getHora_inicio())) {
                throw new IllegalArgumentException("La hora de fin del espacio " + numeroEspacio + " debe ser posterior a la hora de inicio");
            }

            final String codigoEspacio = resDTO.getCodigo_espacio();
            EspacioModel espacio = espacioRepository.findById(codigoEspacio)
                    .orElseThrow(() -> new IllegalArgumentException("El espacio " + codigoEspacio + " no existe en el sistema"));
            
            // Validar capacidad del espacio
            if (espacio.getCapacidad() == null || espacio.getCapacidad() <= 0) {
                throw new IllegalArgumentException("El espacio " + codigoEspacio + " no tiene una capacidad válida configurada");
            }
        }
        
        // Validar que no haya solapamiento de horarios en el mismo espacio
        validarSolapamientoReservacionesEdicion(reservacionesDTO);
        
        // Crear las reservaciones
        contador = 0;
        for (EventoEdicionCompleto.ReservacionDTO resDTO : reservacionesDTO) {
            contador++;
            final String codigoEspacio = resDTO.getCodigo_espacio();
            EspacioModel espacio = espacioRepository.findById(codigoEspacio).orElse(null);
            
            if (espacio == null) continue; // Ya validado arriba

            ReservacionModel reservacion = new ReservacionModel();
            reservacion.setCodigoEvento(evento);
            reservacion.setCodigo_espacio(espacio);
            reservacion.setHora_inicio(resDTO.getHora_inicio());
            reservacion.setHora_fin(resDTO.getHora_fin());
            reservacionRepository.save(reservacion);
        }
    }
    
    private void validarSolapamientoReservacionesEdicion(List<EventoEdicionCompleto.ReservacionDTO> reservacionesDTO) {
        // Validar que no haya solapamiento de horarios en el mismo espacio
        for (int i = 0; i < reservacionesDTO.size(); i++) {
            EventoEdicionCompleto.ReservacionDTO res1 = reservacionesDTO.get(i);
            String espacio1 = res1.getCodigo_espacio();
            Time inicio1 = res1.getHora_inicio();
            Time fin1 = res1.getHora_fin();
            
            for (int j = i + 1; j < reservacionesDTO.size(); j++) {
                EventoEdicionCompleto.ReservacionDTO res2 = reservacionesDTO.get(j);
                String espacio2 = res2.getCodigo_espacio();
                Time inicio2 = res2.getHora_inicio();
                Time fin2 = res2.getHora_fin();
                
                // Si es el mismo espacio, verificar solapamiento
                if (espacio1 != null && espacio1.equals(espacio2)) {
                    // Verificar si hay solapamiento: inicio1 < fin2 && inicio2 < fin1
                    if (inicio1.before(fin2) && inicio2.before(fin1)) {
                        throw new IllegalArgumentException(
                            "El espacio " + espacio1 + " tiene horarios solapados. " +
                            "Por favor, ajuste los horarios para evitar conflictos."
                        );
                    }
                }
            }
        }
    }

    private void validarCamposEventoEdicion(EventoEdicionCompleto request) {
        if (request.getCodigo() == null) {
            throw new IllegalArgumentException("El código del evento es obligatorio para editar");
        }
        
        if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (request.getNombre().length() > 40){
            throw new IllegalArgumentException("El número de caracteres para el nombre es de máximo 40");
        }
        
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del evento es obligatoria");
        }
        
        if (request.getTipo() == null || request.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo del evento es obligatorio");
        }
        
        if (!request.getTipo().equalsIgnoreCase("Academico") && !request.getTipo().equalsIgnoreCase("Ludico")) {
            throw new IllegalArgumentException("El tipo de evento debe ser 'Academico' o 'Ludico'");
        }
        
        if (request.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        
        if (request.getHora_inicio() == null) {
            throw new IllegalArgumentException("La hora de inicio del evento es obligatoria");
        }
        
        if (request.getHora_fin() == null) {
            throw new IllegalArgumentException("La hora de fin del evento es obligatoria");
        }        

        java.time.LocalDate fechaActual = java.time.LocalDate.now();
        java.time.LocalDate fechaEvento = request.getFecha().toLocalDate();
        if (fechaEvento.isBefore(fechaActual)) {
            throw new IllegalArgumentException("La fecha del evento no puede ser anterior a la fecha actual");
        }

        if (request.getHora_inicio().equals(request.getHora_fin())) {
            throw new IllegalArgumentException("La hora de inicio y la hora de fin no pueden ser iguales");
        }

        if (request.getHora_fin().before(request.getHora_inicio())) {
            throw new IllegalArgumentException("La hora de fin no puede ser anterior a la hora de inicio");
        }
    }

    private void eliminarRelacionesExistentes(Integer codigoEvento) {
        try {           
     
            List<ReservacionModel> reservacionesExistentes = 
                reservacionRepository.findAllByCodigoEvento_Codigo(codigoEvento);
            if (!reservacionesExistentes.isEmpty()) {
                System.out.println("Eliminando " + reservacionesExistentes.size() + " reservaciones");
                reservacionRepository.deleteAll(reservacionesExistentes);
                reservacionRepository.flush(); // Forzar commit inmediato
            }

         
            List<ResponsableEventoModel> responsablesExistentes = 
                responsableEventoRepository.findAllByCodigoEvento_Codigo(codigoEvento);
            if (!responsablesExistentes.isEmpty()) {
                System.out.println("Eliminando " + responsablesExistentes.size() + " responsables");
                responsableEventoRepository.deleteAll(responsablesExistentes);
                responsableEventoRepository.flush();
            }

            
            List<ColaboracionModel> colaboracionesExistentes = 
                colaboracionRepository.findAllByCodigoEvento_Codigo(codigoEvento);
            if (!colaboracionesExistentes.isEmpty()) {
                System.out.println("Eliminando " + colaboracionesExistentes.size() + " colaboraciones");
                colaboracionRepository.deleteAll(colaboracionesExistentes);
                colaboracionRepository.flush();
            }

            System.out.println("✓ Todas las relaciones eliminadas correctamente");
            
        } catch (Exception e) {
            System.err.println("✗ Error eliminando relaciones: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar relaciones existentes del evento", e);
        }
    }

    @Override
    public EventoModel enviarEventoAValidacion(Integer codigoEvento) {
        // Buscar el evento por su código
        EventoModel evento = eventoRepository.findById(codigoEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        if (evento.getNombre() == null || evento.getDescripcion() == null || evento.getFecha() == null) {
            throw new RuntimeException("No se puede enviar el evento. Faltan campos obligatorios.");
        }

        // Verificar que el evento no esté ya enviado, aprobado o publicado
        if (evento.getEstado() == EventoModel.estado.enviado ||
            evento.getEstado() == EventoModel.estado.aprobado ||
            evento.getEstado() == EventoModel.estado.publicado) {
            throw new RuntimeException("El evento ya fue enviado o aprobado, no se puede reenviar.");
        }

        // Cambiar el estado del evento a "enviado"
        evento.setEstado(EventoModel.estado.enviado);
        evento.setFechaEnvio(LocalDateTime.now());
        eventoRepository.save(evento);

        // Buscar el usuario que registró el evento
        UsuarioModel usuarioRegistra = usuarioRepository.findByIdentificacion(evento.getIdUsuarioRegistra())
    .orElseThrow(() -> new RuntimeException("Usuario que registró el evento no encontrado"));

        FacultadModel facultadUsuario = null;

        if (usuarioRegistra.getCodigo_programa() != null &&
            usuarioRegistra.getCodigo_programa().getIdFacultad() != null) {
            facultadUsuario = usuarioRegistra.getCodigo_programa().getIdFacultad();
        }

        else if (usuarioRegistra.getCodigo_unidad() != null &&
                usuarioRegistra.getCodigo_unidad().getIdFacultad() != null) {
            facultadUsuario = usuarioRegistra.getCodigo_unidad().getIdFacultad();
        }

        // Si no tiene ninguna de las dos, lanzar error
        else {
            throw new RuntimeException("El usuario que registró el evento no tiene una facultad asociada (ni por programa ni por unidad académica).");
        }


        // Buscar la secretaria académica de la misma facultad
        UsuarioModel secretaria = usuarioRepository.findByRolAndIdFacultad(
            UsuarioModel.rol.secretaria_academica,
            facultadUsuario
        ).orElseThrow(() -> new RuntimeException("No existe una secretaria asociada a esta facultad."));

        // Crear la notificación 
        NotificacionModel notificacion = new NotificacionModel();
        notificacion.setRemitente(usuarioRegistra.getIdentificacion());
        notificacion.setDestinatario(secretaria.getIdentificacion());
        notificacion.setDetalles("Nuevo evento enviado a validación: " + evento.getNombre());
        notificacion.setFecha(new java.sql.Date(System.currentTimeMillis()));
        notificacion.setHora(new java.sql.Time(System.currentTimeMillis()));

        notificacionRepository.save(notificacion);

        return evento;
    }

    @Override
    public EventoCompletoResponse obtenerEventoCompleto(Integer codigo) {
        // Buscar el evento principal
        EventoModel evento = eventoRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con código: " + codigo));

        // Obtener organizaciones colaboradoras
        List<ColaboracionModel> colaboraciones = colaboracionRepository.findAllByCodigoEvento_Codigo(codigo);
        List<EventoCompletoResponse.OrganizacionResponse> organizaciones = colaboraciones.stream()
                .map(this::convertirToOrganizacionResponse)
                .toList();

        // Obtener responsables
        List<ResponsableEventoModel> responsables = responsableEventoRepository.findAllByCodigoEvento_Codigo(codigo);
        List<EventoCompletoResponse.ResponsableResponse> responsablesResponse = responsables.stream()
                .map(this::convertirToResponsableResponse)
                .toList();

        // Obtener reservaciones
        List<ReservacionModel> reservaciones = reservacionRepository.findAllByCodigoEvento_Codigo(codigo);
        List<EventoCompletoResponse.ReservacionResponse> reservacionesResponse = reservaciones.stream()
                .map(this::convertirToReservacionResponse)
                .toList();

        // Construir respuesta completa
        return construirEventoCompletoResponse(evento, organizaciones, responsablesResponse, reservacionesResponse);
    }

    private EventoCompletoResponse.OrganizacionResponse convertirToOrganizacionResponse(ColaboracionModel colaboracion) {
        OrganizacionModel organizacion = colaboracion.getNitOrganizacion();
        
        EventoCompletoResponse.OrganizacionResponse orgResponse = new EventoCompletoResponse.OrganizacionResponse();
        orgResponse.setNit(organizacion.getNit());
        orgResponse.setNombre(organizacion.getNombre());
        orgResponse.setRepresentante_legal(organizacion.getRepresentante_legal());
        orgResponse.setUbicacion(organizacion.getUbicacion());
        orgResponse.setTelefono(organizacion.getTelefono());
        orgResponse.setSector_economico(organizacion.getSector_economico());
        orgResponse.setActividad_principal(organizacion.getActividad_principal());
        orgResponse.setCertificado_participacion(colaboracion.getCertificado_participacion());
        orgResponse.setRepresentante_alterno(colaboracion.getRepresentante_alterno());
        
        return orgResponse;
    }

    private EventoCompletoResponse.ResponsableResponse convertirToResponsableResponse(ResponsableEventoModel responsable) {
        EventoCompletoResponse.ResponsableResponse respResponse = new EventoCompletoResponse.ResponsableResponse();
        respResponse.setId_usuario(responsable.getIdUsuario().getIdentificacion());
        
        // Obtener nombre del usuario
        String nombreCompleto = responsable.getIdUsuario().getNombre() + " " + 
                            responsable.getIdUsuario().getApellido();
        respResponse.setNombreUsuario(nombreCompleto.trim());
        
        respResponse.setDocumentoAval(responsable.getDocumentoAval());
        respResponse.setTipoAval(responsable.getTipoAval() != null ? responsable.getTipoAval().name() : null);
        
        return respResponse;
    }

    private EventoCompletoResponse.ReservacionResponse convertirToReservacionResponse(ReservacionModel reservacion) {
        EventoCompletoResponse.ReservacionResponse resResponse = new EventoCompletoResponse.ReservacionResponse();
        resResponse.setCodigo_espacio(reservacion.getCodigo_espacio().getCodigo());
        resResponse.setNombreEspacio(reservacion.getCodigo_espacio().getNombre());
        resResponse.setHora_inicio(reservacion.getHora_inicio());
        resResponse.setHora_fin(reservacion.getHora_fin());
        
        return resResponse;
    }

    private EventoCompletoResponse construirEventoCompletoResponse(
            EventoModel evento,
            List<EventoCompletoResponse.OrganizacionResponse> organizaciones,
            List<EventoCompletoResponse.ResponsableResponse> responsables,
            List<EventoCompletoResponse.ReservacionResponse> reservaciones) {
        
        EventoCompletoResponse response = new EventoCompletoResponse();
        response.setCodigo(evento.getCodigo());
        response.setNombre(evento.getNombre());
        response.setDescripcion(evento.getDescripcion());
        response.setTipo(evento.getTipo());
        response.setFecha(evento.getFecha());
        response.setHora_inicio(evento.getHora_inicio());
        response.setHora_fin(evento.getHora_fin());
        response.setEstado(evento.getEstado().name());
        response.setOrganizaciones(organizaciones);
        response.setResponsables(responsables);
        response.setReservaciones(reservaciones);
        
        return response;
    }

    @Override
    public List<EventoModel> listarEventos() {
        return eventoRepository.findAll();
    }    

    @Override
    public List<EventoModel> listarPorUsuario(Integer idUsuario) {
        return eventoRepository.findByIdUsuarioRegistra(idUsuario);
    }
    @Override
    public Map<String, Object> obtenerDetallesEvaluacion(Integer codigoEvento) {
        EventoModel evento = eventoRepository.findById(codigoEvento)
        .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("estado", evento.getEstado().name());
        detalles.put("nombre", evento.getNombre());
    
        // Buscar la evaluación más reciente del evento
        List<EvaluacionModel> evaluaciones = IEspacioRepository.findByCodigoEvento_Codigo(codigoEvento);
    
            if (!evaluaciones.isEmpty()) {
                // Obtener la evaluación más reciente
                EvaluacionModel evaluacion = evaluaciones.get(evaluaciones.size() - 1);
        
                detalles.put("decision", evaluacion.getDecision());
                detalles.put("observaciones", evaluacion.getObservaciones());
                detalles.put("actaComite", evaluacion.getActa_comite());
        
                // Obtener nombre de la secretaria que evaluó
                if (evaluacion.getIdSecreAcad() != null) {
                    String nombreSecretaria = evaluacion.getIdSecreAcad().getNombre() + " " + 
                                     evaluacion.getIdSecreAcad().getApellido();
                                     detalles.put("evaluadoPor", nombreSecretaria);
                }
            } else {
                detalles.put("decision", null);
                detalles.put("observaciones", "No hay observaciones disponibles");
                detalles.put("actaComite", null);
                detalles.put("evaluadoPor", "No evaluado");
            }
    
        return detalles;
    }

    @Override
    public Optional<EventoModel> buscarPorCodigo(Integer codigo) {
        return eventoRepository.findById(codigo);
    }
    
    @Override
    @Transactional
    public void eliminarEvento(Integer codigo) {
        // 1. Buscar el evento
        EventoModel evento = eventoRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        String estado = evento.getEstado().name();
        if (!estado.equalsIgnoreCase("borrador") && !estado.equalsIgnoreCase("rechazado")) {
            throw new RuntimeException("Solo se pueden eliminar eventos en estado 'borrador' o 'rechazado'");
        }

        // --- INICIO: LÓGICA PARA ELIMINAR ARCHIVOS FÍSICOS ---
        System.out.println("Iniciando eliminación de archivos físicos para evento: " + codigo);
        try {
            // Definimos las rutas relativas de las carpetas a eliminar
            String carpetaOrganizaciones = "organizaciones/evento_" + codigo;
            String carpetaResponsables = "responsables/evento_" + codigo;
            
            // Llamamos al servicio de almacenamiento para eliminar el directorio completo de organizaciones.
            boolean orgEliminada = fileStorageService.deleteFolderByPath(carpetaOrganizaciones);
            System.out.println(orgEliminada ? "✓ Carpeta de organizaciones eliminada correctamente: " + carpetaOrganizaciones 
                                          : "⚠ No se pudo eliminar la carpeta de organizaciones o no existía: " + carpetaOrganizaciones);
            
            // Llamamos al servicio de almacenamiento para eliminar el directorio completo de responsables.
            boolean respEliminada = fileStorageService.deleteFolderByPath(carpetaResponsables);
            System.out.println(respEliminada ? "✓ Carpeta de responsables eliminada correctamente: " + carpetaResponsables 
                                           : "⚠ No se pudo eliminar la carpeta de responsables o no existía: " + carpetaResponsables);

        } catch (Exception e) {
            // Si falla la eliminación de archivos (ej. error de permisos), es un error I/O. 
            System.err.println("ERROR FATAL al intentar eliminar directorios del evento " + codigo + ": " + e.getMessage());
            
            // Lanzamos una RuntimeException para asegurar que la transacción de la base de datos (@Transactional) 
            // haga ROLLBACK, manteniendo la coherencia si los archivos no se pudieron eliminar.
            throw new RuntimeException("Error al eliminar los archivos físicos del evento. La eliminación de la base de datos fue revertida.", e);
        }
        System.out.println("Fin de eliminación de archivos físicos.");
        // 2. Eliminar de la base de datos (relaciones y evento principal)
        // (Esto solo se ejecutará si la eliminación de archivos fue exitosa)
        
        List<ResponsableEventoModel> responsables = responsableEventoRepository.findAllByCodigoEvento_Codigo(codigo);
        if (!responsables.isEmpty()) {
            responsableEventoRepository.deleteAll(responsables);
        }
        
        List<ColaboracionModel> colaboraciones = colaboracionRepository.findAllByCodigoEvento_Codigo(codigo);
        if (!colaboraciones.isEmpty()) {
            colaboracionRepository.deleteAll(colaboraciones);
        }

        List<ReservacionModel> reservaciones = reservacionRepository.findAllByCodigoEvento_Codigo(codigo);
        if (!reservaciones.isEmpty()) {
            reservacionRepository.deleteAll(reservaciones);
        }

        // Finalmente, se elimina el evento principal
        eventoRepository.delete(evento);
        System.out.println("✓ Evento " + codigo + " eliminado de la base de datos.");
    }
}
