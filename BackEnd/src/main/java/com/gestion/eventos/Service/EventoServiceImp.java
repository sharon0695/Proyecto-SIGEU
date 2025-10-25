package com.gestion.eventos.Service;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.DTO.EventoEdicionCompleto;
import com.gestion.eventos.DTO.EventoRegistroCompleto;
import com.gestion.eventos.Model.ColaboracionModel;
import com.gestion.eventos.Model.EspacioModel;
import com.gestion.eventos.Model.EventoModel;
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
import java.util.List;
import java.util.Optional;

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
        
        if (request.getOrganizaciones() != null && !request.getOrganizaciones().isEmpty()) {
            procesarOrganizaciones(request.getOrganizaciones(), evento, request.getId_usuario_registra());
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

    private void procesarOrganizaciones(List<EventoRegistroCompleto.OrganizacionDTO> organizacionesDTO, EventoModel evento, Integer idUsuarioRegistra) {
        int contador = 0;
        for (EventoRegistroCompleto.OrganizacionDTO orgDTO : organizacionesDTO) {
            contador++;
            final int numeroOrganizacion = contador;
            
            if (orgDTO.getNit() == null || orgDTO.getNit().trim().isEmpty()) {
                throw new IllegalArgumentException("El NIT de la organización " + numeroOrganizacion + " es obligatorio");
            }
        
            Optional<OrganizacionModel> orgExistente = organizacionRepository.findByNit(orgDTO.getNit());
            
            OrganizacionModel organizacion;
            
            if (orgExistente.isPresent()) {
                organizacion = orgExistente.get();
            } else {
                validarDatosOrganizacion(orgDTO, numeroOrganizacion);
                
                organizacion = new OrganizacionModel();
                organizacion.setNit(orgDTO.getNit());
                organizacion.setNombre(orgDTO.getNombre());
                organizacion.setRepresentante_legal(orgDTO.getRepresentante_legal());
                organizacion.setUbicacion(orgDTO.getUbicacion());
                organizacion.setTelefono(orgDTO.getTelefono());
                organizacion.setSector_economico(orgDTO.getSector_economico());
                organizacion.setActividad_principal(orgDTO.getActividad_principal());
                
                UsuarioModel usuario = usuarioRepository.findById(idUsuarioRegistra)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario registrador no encontrado"));
                organizacion.setUsuario(usuario);
                
                organizacion = organizacionRepository.save(organizacion);
            }
            
            if (orgDTO.getCertificado_participacion() != null && 
                !orgDTO.getCertificado_participacion().trim().isEmpty() &&
                !orgDTO.getCertificado_participacion().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("El certificado de la organización " + numeroOrganizacion + " debe ser un archivo PDF");
            }
            
            if (orgDTO.getRepresentante_alterno() != null && 
                !orgDTO.getRepresentante_alterno().trim().isEmpty()) {
                if (orgDTO.getRepresentante_alterno().length() < 3) {
                    throw new IllegalArgumentException("El nombre del representante alterno de la organización " + numeroOrganizacion + " debe tener al menos 3 caracteres");
                }
            }
            
            ColaboracionModel colaboracion = new ColaboracionModel();
            colaboracion.setNitOrganizacion(organizacion);
            colaboracion.setCodigoEvento(evento);
            colaboracion.setCertificado_participacion(orgDTO.getCertificado_participacion());
            colaboracion.setRepresentante_alterno(orgDTO.getRepresentante_alterno());
            
            colaboracionRepository.save(colaboracion);
        }
    }

    private void validarDatosOrganizacion(EventoRegistroCompleto.OrganizacionDTO orgDTO, int numeroOrg) {
        if (orgDTO.getNit() == null || orgDTO.getNit().trim().isEmpty()) {
            throw new IllegalArgumentException("El NIT de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getNombre() == null || orgDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getRepresentante_legal() == null || orgDTO.getRepresentante_legal().trim().isEmpty()) {
            throw new IllegalArgumentException("El representante legal de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getUbicacion() == null || orgDTO.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación de la organización " + numeroOrg + " es obligatoria");
        }
        if (orgDTO.getTelefono() == null || orgDTO.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono de la organización " + numeroOrg + " es obligatorio");
        }
        if (!orgDTO.getTelefono().matches("\\d+")) {
            throw new IllegalArgumentException("El teléfono de la organización " + numeroOrg + " solo debe contener números");
        }
        if (orgDTO.getSector_economico() == null || orgDTO.getSector_economico().trim().isEmpty()) {
            throw new IllegalArgumentException("El sector económico de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getActividad_principal() == null || orgDTO.getActividad_principal().trim().isEmpty()) {
            throw new IllegalArgumentException("La actividad principal de la organización " + numeroOrg + " es obligatoria");
        }
    }

    private void procesarResponsables(List<EventoRegistroCompleto.ResponsableDTO> responsablesDTO, EventoModel evento) {
        int contador = 0;
        for (EventoRegistroCompleto.ResponsableDTO respDTO : responsablesDTO) {
            contador++;
            final int numeroResponsable = contador;
            
            if (respDTO.getId_usuario() == null) {
                throw new IllegalArgumentException("El responsable " + numeroResponsable + " no ha sido seleccionado");
            }
            
            UsuarioModel usuario = usuarioRepository.findById(respDTO.getId_usuario())
                .orElseThrow(() -> new IllegalArgumentException("El responsable " + numeroResponsable + " no existe en el sistema"));
            
            if (respDTO.getDocumentoAval() != null && 
                !respDTO.getDocumentoAval().trim().isEmpty() &&
                !respDTO.getDocumentoAval().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("El documento de aval del responsable " + numeroResponsable + " debe ser un archivo PDF");
            }
            
            ResponsableEventoModel responsable = new ResponsableEventoModel();
            responsable.setId_usuario(usuario);
            responsable.setCodigoEvento(evento);
            responsable.setDocumentoAval(respDTO.getDocumentoAval());
            
            // Solo asignar tipoAval si viene en el request y es válido
            if (respDTO.getTipoAval() != null && !respDTO.getTipoAval().trim().isEmpty()) {
                try {
                    responsable.setTipoAval(
                        ResponsableEventoModel.tipo_aval.valueOf(respDTO.getTipoAval().toLowerCase())
                    );
                } catch (IllegalArgumentException e) {
                }
            }
            
            responsableEventoRepository.save(responsable);
        }
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
        // Validar que el evento existe
        EventoModel eventoExistente = eventoRepository.findById(request.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("El evento a editar no existe"));

        // Validar que el evento esté en estado borrador o rechazado para permitir edición
        if (!eventoExistente.getEstado().equals(EventoModel.estado.borrador) && 
            !eventoExistente.getEstado().equals(EventoModel.estado.rechazado)) {
            throw new IllegalArgumentException("Solo se pueden editar eventos en estado 'borrador' o 'rechazado'");
        }

        // Validar campos del evento
        validarCamposEventoEdicion(request);

        // Validar usuario registrador
        if (request.getId_usuario_registra() == null) {
            throw new IllegalArgumentException("El ID del usuario que edita el evento es obligatorio");
        }
        usuarioRepository.findById(request.getId_usuario_registra())
                .orElseThrow(() -> new IllegalArgumentException("El usuario editor no existe en el sistema"));

        // Validaciones específicas para relaciones obligatorias
        if (request.getResponsables() == null || request.getResponsables().isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un responsable al evento");
        }
        
        if (request.getReservaciones() == null || request.getReservaciones().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un espacio para el evento");
        }

        // Actualizar datos básicos del evento
        eventoExistente.setNombre(request.getNombre());
        eventoExistente.setDescripcion(request.getDescripcion());
        eventoExistente.setTipo(request.getTipo());
        eventoExistente.setFecha(request.getFecha());
        eventoExistente.setHora_inicio(request.getHora_inicio());
        eventoExistente.setHora_fin(request.getHora_fin());

        EventoModel eventoActualizado = eventoRepository.save(eventoExistente);

        // Eliminar relaciones existentes
        eliminarRelacionesExistentes(eventoExistente.getCodigo());

        // Procesar nuevas relaciones - ahora sabemos que responsables y reservaciones no son null/vacíos
        procesarOrganizacionesEdicion(request.getOrganizaciones(), eventoActualizado, request.getId_usuario_registra());
        procesarResponsablesEdicion(request.getResponsables(), eventoActualizado);
        procesarReservacionesEdicion(request.getReservaciones(), eventoActualizado);

        return eventoActualizado;
    }

    private void procesarOrganizacionesEdicion(List<EventoEdicionCompleto.OrganizacionDTO> organizacionesDTO, 
                                            EventoModel evento, Integer idUsuarioRegistra) {
        // Organizaciones son opcionales - si es null o vacío, simplemente no se crean
        if (organizacionesDTO == null || organizacionesDTO.isEmpty()) {
            return;
        }

        // Procesar las organizaciones proporcionadas
        int contador = 0;
        for (EventoEdicionCompleto.OrganizacionDTO orgDTO : organizacionesDTO) {
            contador++;
            final int numeroOrganizacion = contador;
            
            if (orgDTO.getNit() == null || orgDTO.getNit().trim().isEmpty()) {
                throw new IllegalArgumentException("El NIT de la organización " + numeroOrganizacion + " es obligatorio");
            }
            
            Optional<OrganizacionModel> orgExistente = organizacionRepository.findByNit(orgDTO.getNit());
            OrganizacionModel organizacion;
            
            if (orgExistente.isPresent()) {
                organizacion = orgExistente.get();
            } else {
                validarDatosOrganizacionE(orgDTO, numeroOrganizacion);
                organizacion = new OrganizacionModel();
                organizacion.setNit(orgDTO.getNit());
                organizacion.setNombre(orgDTO.getNombre());
                organizacion.setRepresentante_legal(orgDTO.getRepresentante_legal());
                organizacion.setUbicacion(orgDTO.getUbicacion());
                organizacion.setTelefono(orgDTO.getTelefono());
                organizacion.setSector_economico(orgDTO.getSector_economico());
                organizacion.setActividad_principal(orgDTO.getActividad_principal());
                
                UsuarioModel usuario = usuarioRepository.findById(idUsuarioRegistra)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario registrador no encontrado"));
                organizacion.setUsuario(usuario);
                organizacion = organizacionRepository.save(organizacion);
            }

            if (orgDTO.getCertificado_participacion() != null && 
                !orgDTO.getCertificado_participacion().trim().isEmpty() && 
                !orgDTO.getCertificado_participacion().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("El certificado de la organización " + numeroOrganizacion + " debe ser un archivo PDF");
            }

            if (orgDTO.getRepresentante_alterno() != null && !orgDTO.getRepresentante_alterno().trim().isEmpty()) {
                if (orgDTO.getRepresentante_alterno().length() < 3) {
                    throw new IllegalArgumentException("El nombre del representante alterno de la organización " + numeroOrganizacion + " debe tener al menos 3 caracteres");
                }
            }

            ColaboracionModel colaboracion = new ColaboracionModel();
            colaboracion.setNitOrganizacion(organizacion);
            colaboracion.setCodigoEvento(evento);
            colaboracion.setCertificado_participacion(orgDTO.getCertificado_participacion());
            colaboracion.setRepresentante_alterno(orgDTO.getRepresentante_alterno());
            colaboracionRepository.save(colaboracion);
        }
    }

    private void procesarResponsablesEdicion(List<EventoEdicionCompleto.ResponsableDTO> responsablesDTO, EventoModel evento) {
        // Responsables son obligatorios - ya validamos que no son null/vacíos, así que procesamos directamente
        int contador = 0;
        for (EventoEdicionCompleto.ResponsableDTO respDTO : responsablesDTO) {
            contador++;
            final int numeroResponsable = contador;
            
            if (respDTO.getId_usuario() == null) {
                throw new IllegalArgumentException("El responsable " + numeroResponsable + " no ha sido seleccionado");
            }
            
            UsuarioModel usuario = usuarioRepository.findById(respDTO.getId_usuario())
                    .orElseThrow(() -> new IllegalArgumentException("El responsable " + numeroResponsable + " no existe en el sistema"));

            if (respDTO.getDocumentoAval() != null && 
                !respDTO.getDocumentoAval().trim().isEmpty() && 
                !respDTO.getDocumentoAval().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("El documento de aval del responsable " + numeroResponsable + " debe ser un archivo PDF");
            }

            ResponsableEventoModel responsable = new ResponsableEventoModel();
            responsable.setId_usuario(usuario);
            responsable.setCodigoEvento(evento);
            responsable.setDocumentoAval(respDTO.getDocumentoAval());
            
            // Solo asignar tipoAval si viene en el request y es válido
            if (respDTO.getTipoAval() != null && !respDTO.getTipoAval().trim().isEmpty()) {
                try {
                    responsable.setTipoAval(
                        ResponsableEventoModel.tipo_aval.valueOf(respDTO.getTipoAval().toLowerCase())
                    );
                } catch (IllegalArgumentException e) {
                    // Si el tipo no es válido, se deja null
                }
            }
            
            responsableEventoRepository.save(responsable);
        }
    }

    private void validarDatosOrganizacionE(EventoEdicionCompleto.OrganizacionDTO orgDTO, int numeroOrg) {
        if (orgDTO.getNit() == null || orgDTO.getNit().trim().isEmpty()) {
            throw new IllegalArgumentException("El NIT de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getNombre() == null || orgDTO.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getRepresentante_legal() == null || orgDTO.getRepresentante_legal().trim().isEmpty()) {
            throw new IllegalArgumentException("El representante legal de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getUbicacion() == null || orgDTO.getUbicacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación de la organización " + numeroOrg + " es obligatoria");
        }
        if (orgDTO.getTelefono() == null || orgDTO.getTelefono().trim().isEmpty()) {
            throw new IllegalArgumentException("El teléfono de la organización " + numeroOrg + " es obligatorio");
        }
        if (!orgDTO.getTelefono().matches("\\d+")) {
            throw new IllegalArgumentException("El teléfono de la organización " + numeroOrg + " solo debe contener números");
        }
        if (orgDTO.getSector_economico() == null || orgDTO.getSector_economico().trim().isEmpty()) {
            throw new IllegalArgumentException("El sector económico de la organización " + numeroOrg + " es obligatorio");
        }
        if (orgDTO.getActividad_principal() == null || orgDTO.getActividad_principal().trim().isEmpty()) {
            throw new IllegalArgumentException("La actividad principal de la organización " + numeroOrg + " es obligatoria");
        }
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
        // Eliminar responsables existentes
        List<ResponsableEventoModel> responsablesExistentes = 
            responsableEventoRepository.findAllByCodigoEvento_Codigo(codigoEvento);
        if (!responsablesExistentes.isEmpty()) {
            responsableEventoRepository.deleteAll(responsablesExistentes);
        }

        // Eliminar colaboraciones existentes
        List<ColaboracionModel> colaboracionesExistentes = 
            colaboracionRepository.findAllByCodigoEvento_Codigo(codigoEvento);
        if (!colaboracionesExistentes.isEmpty()) {
            colaboracionRepository.deleteAll(colaboracionesExistentes);
        }

        // Eliminar reservaciones existentes
        List<ReservacionModel> reservacionesExistentes = 
            reservacionRepository.findAllByCodigoEvento_Codigo(codigoEvento);
        if (!reservacionesExistentes.isEmpty()) {
            reservacionRepository.deleteAll(reservacionesExistentes);
        }
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
        respResponse.setId_usuario(responsable.getId_usuario().getIdentificacion());
        
        // Obtener nombre del usuario
        String nombreCompleto = responsable.getId_usuario().getNombre() + " " + 
                            responsable.getId_usuario().getApellido();
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
    public Optional<EventoModel> buscarPorCodigo(Integer codigo) {
        return eventoRepository.findById(codigo);
    }

    @Override
    @Transactional
    public void eliminarEvento(Integer codigo) {
    // Buscar el evento
    EventoModel evento = eventoRepository.findById(codigo)
            .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

    // Validar estado permitido para eliminar
    String estado = evento.getEstado().name();
    if (!estado.equalsIgnoreCase("borrador") && !estado.equalsIgnoreCase("rechazado")) {
        throw new RuntimeException("Solo se pueden eliminar eventos en estado 'borrador' o 'rechazado'");
    }
    // 🔹 Eliminar responsables asociados al evento
    List<ResponsableEventoModel> responsables = responsableEventoRepository.findAllByCodigoEvento_Codigo(codigo);
    if (!responsables.isEmpty()) {
    responsableEventoRepository.deleteAll(responsables);
}

    // 🔹 Eliminar colaboraciones asociadas al evento
    List<ColaboracionModel> colaboraciones = colaboracionRepository.findAllByCodigoEvento_Codigo(codigo);
    if (!colaboraciones.isEmpty()) {
        colaboracionRepository.deleteAll(colaboraciones);
    }

    // 🔹 Eliminar reservaciones asociadas al evento
    List<ReservacionModel> reservaciones = reservacionRepository.findAllByCodigoEvento_Codigo(codigo);
    if (!reservaciones.isEmpty()) {
        reservacionRepository.deleteAll(reservaciones);
    }

    // 🔹 Finalmente eliminar el evento
    eventoRepository.delete(evento);
    }

}