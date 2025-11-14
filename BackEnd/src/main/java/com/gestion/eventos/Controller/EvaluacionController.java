package com.gestion.eventos.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gestion.eventos.DTO.EventoCompletoResponse;
import com.gestion.eventos.Model.EvaluacionModel;
import com.gestion.eventos.Model.EventoModel;
import com.gestion.eventos.Repository.IUsuarioRepository;
import com.gestion.eventos.Service.IEvaluacionService;
import com.gestion.eventos.Service.IEventoService;

@RestController
@RequestMapping ("/evaluacion")
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
    @GetMapping("/pendientes/{idSecre}")
    public ResponseEntity<List<Map<String, Object>>> listarEventosPendientes(@PathVariable Integer idSecre) {
        List<EventoModel> pendientes = evaluacionService.listarPorEstado(EventoModel.estado.enviado, idSecre);
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


    @PostMapping("/aprobar/{idEvento}")
    public ResponseEntity<?> aprobarEvento(
            @PathVariable Integer idEvento,
            @RequestParam String decision,
            @RequestParam("idSecretaria") Integer idSecretaria,
            @RequestParam("actaComite") MultipartFile actaComite) {

            evaluacionService.aprobarEvento(idEvento, decision, idSecretaria, actaComite);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Evento aprobado correctamente.");
            return ResponseEntity.ok(response);       
    }

    @PostMapping("/rechazar/{idEvento}")
    public ResponseEntity<?> rechazarEvento(
            @PathVariable Integer idEvento,
            @RequestParam String decision,
            @RequestParam("idSecretaria") Integer idSecretaria,
            @RequestParam("observaciones") String observaciones) {

            evaluacionService.rechazarEvento(idEvento, decision, idSecretaria, observaciones);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Evento rechazado correctamente.");
            return ResponseEntity.ok(response);     
    }

    @GetMapping("/detalle/{codigo}")
    public ResponseEntity<EventoCompletoResponse> obtenerDetalleParaSecretaria(@PathVariable Integer codigo) {
        EventoCompletoResponse detalle = eventoService.obtenerEventoCompleto(codigo);
        return ResponseEntity.ok(detalle);
    }
}   
