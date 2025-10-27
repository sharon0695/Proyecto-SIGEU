package com.gestion.eventos.Service;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.Model.ColaboracionModel;
import com.gestion.eventos.Model.EspacioModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Model.NotificacionModel;
import com.gestion.eventos.Model.OrganizacionModel;
import com.gestion.eventos.Model.ReservacionModel;
import com.gestion.eventos.Model.ResponsableEventoModel;
import com.gestion.eventos.Model.UsuarioModel;
import com.gestion.eventos.Repository.IColaboracionRepository;
import com.gestion.eventos.Repository.IEspacioRepository;
import com.gestion.eventos.Repository.IEventoRepository;
import com.gestion.eventos.Repository.IOrganizacionRepository;
import com.gestion.eventos.Repository.IReservacionRepository;
import com.gestion.eventos.Repository.IResponsableEventoRepository;
import com.gestion.eventos.Repository.IUsuarioRepository;
import com.gestion.eventos.Repository.INotificacionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        EventoModel evento = new EventoModel();
        evento.setNombre(request.getNombre());
        evento.setDescripcion(request.getDescripcion());
        evento.setTipo(request.getTipo());
        evento.setFecha(request.getFecha());
        evento.setHora_inicio(request.getHora_inicio());
        evento.setHora_fin(request.getHora_fin());
        evento.setEstado(EventoModel.estado.borrador);
        
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
            
            // Validar y guardar nuevo archivo
            if (!colabDTO.getCertificado_participacion().getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("El certificado de participación debe ser un archivo PDF");
            }
            
            certificadoPath = fileStorageService.storeFile(
                colabDTO.getCertificado_participacion(), 
                "organizaciones/evento_" + codigoEvento
            );
            System.out.println("✓ Certificado de colaboración guardado: " + certificadoPath);
        }
        
        return certificadoPath;
    }

    private String procesarArchivoResponsableRegistro(EventoRegistroCompleto.ResponsableDTO respDTO, Integer codigoEvento) {
        String documentoAvalPath = null;
        
        if (respDTO.getDocumentoAval() != null && 
            !respDTO.getDocumentoAval().isEmpty()) {
            
            // Validar y guardar nuevo archivo
            if (!respDTO.getDocumentoAval().getContentType().equals("application/pdf")) {
                throw new IllegalArgumentException("El documento de aval del responsable debe ser un archivo PDF");
            }
            
            documentoAvalPath = fileStorageService.storeFile(
                respDTO.getDocumentoAval(), 
                "responsables/evento_" + codigoEvento
            );
            System.out.println("✓ Documento de aval de responsable guardado: " + documentoAvalPath);
        }
        
        return documentoAvalPath;
    }
    private void procesarReservaciones(List<EventoRegistroCompleto.ReservacionDTO> reservacionesDTO, EventoModel evento) {
        int contador = 0;
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
            
            ReservacionModel reservacion = new ReservacionModel();
            reservacion.setCodigoEvento(evento);
            reservacion.setCodigo_espacio(espacio);
            reservacion.setHora_inicio(resDTO.getHora_inicio());
            reservacion.setHora_fin(resDTO.getHora_fin());
            
            reservacionRepository.save(reservacion);
        }
    }

    @Override
    @Transactional
    public EventoModel editarEventoCompleto(EventoEdicionCompleto request) {
        try {
            System.out.println("=== INICIANDO EDICIÓN DEL EVENTO " + request.getCodigo() + " ===");
            
            // Validar que el evento existe y es editable
            EventoModel eventoExistente = eventoRepository.findById(request.getCodigo())
                    .orElseThrow(() -> new IllegalArgumentException("El evento a editar no existe"));

            if (!eventoExistente.getEstado().equals(EventoModel.estado.borrador) && 
                !eventoExistente.getEstado().equals(EventoModel.estado.rechazado)) {
                throw new IllegalArgumentException("Solo se pueden editar eventos en estado 'borrador' o 'rechazado'");
            }

            // Validar campos básicos
            validarCamposEventoEdicion(request);

            // 🔴 PASO 1: Obtener archivos existentes ANTES de cualquier eliminación
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

            // 🔴 PASO 2: Actualizar datos básicos del evento
            System.out.println("Actualizando evento básico...");
            eventoExistente.setNombre(request.getNombre());
            eventoExistente.setDescripcion(request.getDescripcion());
            eventoExistente.setTipo(request.getTipo());
            eventoExistente.setFecha(request.getFecha());
            eventoExistente.setHora_inicio(request.getHora_inicio());
            eventoExistente.setHora_fin(request.getHora_fin());

            EventoModel eventoActualizado = eventoRepository.save(eventoExistente);

            // 🔴 PASO 3: Eliminar relaciones existentes usando queries nativas (MÁS SEGURO)
            System.out.println("Eliminando relaciones existentes...");
            eliminarRelacionesExistentes(request.getCodigo());

            // 🔴 PASO 4: Pausa pequeña para asegurar que las eliminaciones se completen
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 🔴 PASO 5: Crear nuevas relaciones
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

                // 🔴 VERIFICAR que no existe ya esta colaboración (por si las moscas)
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

                // 🔴 VERIFICAR que no existe ya este responsable
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

            ReservacionModel reservacion = new ReservacionModel();
            reservacion.setCodigoEvento(evento);
            reservacion.setCodigo_espacio(espacio);
            reservacion.setHora_inicio(resDTO.getHora_inicio());
            reservacion.setHora_fin(resDTO.getHora_fin());
            reservacionRepository.save(reservacion);
        }
    }

    private void validarCamposEventoEdicion(EventoEdicionCompleto request) {
        if (request.getCodigo() == null) {
            throw new IllegalArgumentException("El código del evento es obligatorio para editar");
        }
        
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
    }

    private void eliminarRelacionesExistentes(Integer codigoEvento) {
        try {
            System.out.println("=== ELIMINANDO RELACIONES DEL EVENTO " + codigoEvento + " ===");
            
            // 🔴 ORDEN CORRECTO: Primero las tablas que NO tienen FKs a otras tablas de evento
            // 1. Primero eliminar reservaciones (depende solo de evento y espacio)
            List<ReservacionModel> reservacionesExistentes = 
                reservacionRepository.findAllByCodigoEvento_Codigo(codigoEvento);
            if (!reservacionesExistentes.isEmpty()) {
                System.out.println("Eliminando " + reservacionesExistentes.size() + " reservaciones");
                reservacionRepository.deleteAll(reservacionesExistentes);
                reservacionRepository.flush(); // Forzar commit inmediato
            }

            // 2. Luego eliminar responsables (depende de evento y usuario)
            List<ResponsableEventoModel> responsablesExistentes = 
                responsableEventoRepository.findAllByCodigoEvento_Codigo(codigoEvento);
            if (!responsablesExistentes.isEmpty()) {
                System.out.println("Eliminando " + responsablesExistentes.size() + " responsables");
                responsableEventoRepository.deleteAll(responsablesExistentes);
                responsableEventoRepository.flush();
            }

            // 3. Finalmente eliminar colaboraciones (depende de evento y organización)
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
    public void enviarEventoAValidacion(Integer codigoEvento) {
        // Buscar el evento por su código
        EventoModel evento = eventoRepository.findById(codigoEvento)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    
        // Verificar si el evento tiene los campos obligatorios completos
        if (evento.getNombre() == null || evento.getDescripcion() == null || evento.getFecha() == null) {
            throw new RuntimeException("No se puede enviar el evento. Faltan campos obligatorios.");
        }
    
        // Buscar el responsable asociado al evento
        ResponsableEventoModel responsable = responsableEventoRepository.findAllByCodigoEvento_Codigo(evento);
        if (responsable == null) {
            throw new RuntimeException("No se encontró responsable asociado al evento.");
        }
    
        // Verificar si el documento de aval está cargado
        if (responsable.getDocumentoAval() == null || responsable.getDocumentoAval().isEmpty()) {
            throw new RuntimeException("Debe adjuntar el documento de aval antes de enviar.");
        }
    
        // Verificar que el evento no esté ya enviado, aprobado o publicado
        if (evento.getEstado() == EventoModel.estado.enviado ||
            evento.getEstado() == EventoModel.estado.aprobado ||
            evento.getEstado() == EventoModel.estado.publicado) {
            throw new RuntimeException("El evento ya fue enviado o aprobado, no se puede reenviar.");
        }
    
        // Cambiar el estado del evento a "enviado"
        evento.setEstado(EventoModel.estado.enviado);
        eventoRepository.save(evento);
    
        // Crear la notificación para la Secretaría Académica
        NotificacionModel notificacion = new NotificacionModel();
        notificacion.setRemitente(responsable.getIdUsuario().getIdentificacion());
        notificacion.setDetalles("Nuevo evento enviado a validación: " + evento.getNombre());
        notificacion.setFecha(new java.sql.Date(System.currentTimeMillis()));
        notificacion.setHora(new java.sql.Time(System.currentTimeMillis()));
    
        // Crear usuario destinatario (secretaría académica)
        UsuarioModel destinatario = new UsuarioModel();
        destinatario.setIdentificacion(3); // Cambia este valor al ID real del usuario secretaria
        notificacion.setDestinatario(destinatario);
    
        notificacionRepository.save(notificacion);
    
        System.out.println("Evento " + evento.getCodigo() + " enviado correctamente a validación.");
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
    public List<EventoModel> filtrarPorNombre(String nombre) {
        return eventoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public List<EventoModel> filtrarPorEstado(EventoModel.estado estado) {
        return eventoRepository.findByEstado(estado);
    }

    @Override
    public List<EventoModel> filtrarPorFecha(Date fecha) {
        return eventoRepository.findByFecha(fecha);
    }

    @Override
    public Optional<EventoModel> buscarPorCodigo(Integer codigo) {
        return eventoRepository.findById(codigo);
    }

    @Override
    @Transactional
    public void eliminarEvento(Integer codigo) {
        // Buscar el evento
        EventoModel evento = eventoRepository.findById(codigo)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        String estado = evento.getEstado().name();
        if (!estado.equalsIgnoreCase("borrador") && !estado.equalsIgnoreCase("rechazado")) {
            throw new RuntimeException("Solo se pueden eliminar eventos en estado 'borrador' o 'rechazado'");
        }
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

        eventoRepository.delete(evento);
    }

}