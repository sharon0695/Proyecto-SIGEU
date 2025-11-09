package com.gestion.eventos.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Repository.IUsuarioRepository;
import com.gestion.eventos.Service.IEvaluacionService;
import com.gestion.eventos.Service.IEventoService;

@RestController
@RequestMapping ("/rutaEva")
public class EvaluacionController {
    @Autowired IEvaluacionService evaluacionService;
    @Autowired IEventoService eventoService;
    @Autowired IUsuarioRepository usuarioRepository;
    
    @PostMapping ("/ruta1")
    public ResponseEntity<EvaluacionModel> crearEvaluacion(@RequestBody EvaluacionModel evaluacion){
        return new ResponseEntity<>(evaluacionService.guardarEvaluacion(evaluacion),HttpStatus.CREATED);
    }
    @GetMapping ("/ruta2")
    public ResponseEntity<List<EvaluacionModel>> listarEvaluaciones(){
        return new ResponseEntity<>(evaluacionService.listarEvaluaciones(), HttpStatus.OK);
    }
    @GetMapping("/pendientes")
    public ResponseEntity<List<Map<String, Object>>> listarEventosPendientes() {
        List<EventoModel> pendientes = evaluacionService.listarPorEstado(EventoModel.estado.enviado);
        List<Map<String, Object>> respuesta = pendientes.stream().map(e -> {
            String organizadorNombre = usuarioRepository.findById(e.getIdUsuarioRegistra())
                .map(u -> (u.getNombre() != null ? u.getNombre() : "") + " " + (u.getApellido() != null ? u.getApellido() : ""))
                .orElse("").trim();
            Map<String, Object> map = new HashMap<>();
            map.put("codigo", e.getCodigo());
            map.put("nombre", e.getNombre());
            map.put("descripcion", e.getDescripcion());
            map.put("tipo", e.getTipo());
            map.put("fecha", e.getFecha());
            map.put("hora_inicio", e.getHora_inicio());
            map.put("hora_fin", e.getHora_fin());
            map.put("estado", e.getEstado() != null ? e.getEstado().name() : null);
            map.put("organizadorNombre", organizadorNombre);
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(respuesta);
    }

    public static class EvaluacionRequest {
        private Integer codigoEvento;
        private Integer idSecretaria;
        private String observaciones;
        private org.springframework.web.multipart.MultipartFile actaComite;

        public Integer getCodigoEvento() { return codigoEvento; }
        public void setCodigoEvento(Integer codigoEvento) { this.codigoEvento = codigoEvento; }
        
        public Integer getIdSecretaria() { return idSecretaria; }
        public void setIdSecretaria(Integer idSecretaria) { this.idSecretaria = idSecretaria; }
        
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
        
        public org.springframework.web.multipart.MultipartFile getActaComite() { return actaComite; }
        public void setActaComite(org.springframework.web.multipart.MultipartFile actaComite) { this.actaComite = actaComite; }
    }

    @PutMapping(value = "/aprobar", consumes = "multipart/form-data")
    public ResponseEntity<?> aprobarEvento(@ModelAttribute EvaluacionRequest request) {
        EventoModel evento = evaluacionService.aprobarEvento(
            request.getCodigoEvento(), 
            request.getIdSecretaria(),
            request.getObservaciones(), 
            request.getActaComite()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("mensaje", "Evento aprobado correctamente");
        body.put("nuevoEstado", evento.getEstado().toString());
        return ResponseEntity.ok(body);
    }

    @PutMapping(value = "/rechazar", consumes = "multipart/form-data")
    public ResponseEntity<?> rechazarEvento(@ModelAttribute EvaluacionRequest request) {
        EventoModel evento = evaluacionService.rechazarEvento(
            request.getCodigoEvento(),
            request.getIdSecretaria(),
            request.getObservaciones(),
            request.getActaComite()
        );
        Map<String, Object> body = new HashMap<>();
        body.put("mensaje", "Evento rechazado correctamente");
        body.put("nuevoEstado", evento.getEstado().toString());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/detalle/{codigo}")
    public ResponseEntity<EventoCompletoResponse> obtenerDetalleParaSecretaria(@PathVariable Integer codigo) {
        EventoCompletoResponse detalle = eventoService.obtenerEventoCompleto(codigo);
        return ResponseEntity.ok(detalle);
    }
}   
