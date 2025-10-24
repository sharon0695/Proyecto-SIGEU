package com.gestion.eventos.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gestion.eventos.Service.FileStorageService;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/archivos")
public class FileController {

    @Autowired FileStorageService fileStorageService;

    @GetMapping("/ver")
    public ResponseEntity<Resource> viewFile(@RequestParam String tipo, @RequestParam String archivo) {
        try {
            System.out.println("=== SOLICITUD VER ARCHIVO ===");
            System.out.println("Tipo: " + tipo);
            System.out.println("Archivo: " + archivo);
            
            // Construir la ruta completa
            String filePath = tipo + "/" + archivo;
            System.out.println("Ruta completa: " + filePath);
            
            // Cargar el archivo
            Path file = fileStorageService.loadFileAsPath(filePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/pdf";
                }

                System.out.println("✓ Archivo encontrado - Tipo: " + contentType);
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                System.out.println("✗ Archivo NO encontrado o no legible");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/descargar")
    public ResponseEntity<Resource> downloadFile(@RequestParam String tipo, @RequestParam String archivo) {
        try {
            System.out.println("=== SOLICITUD DESCARGAR ARCHIVO ===");
            System.out.println("Tipo: " + tipo);
            System.out.println("Archivo: " + archivo);
            
            String filePath = tipo + "/" + archivo;
            System.out.println("Ruta completa: " + filePath);
            
            Path file = fileStorageService.loadFileAsPath(filePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                System.out.println("✓ Archivo encontrado para descarga");
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
            } else {
                System.out.println("✗ Archivo NO encontrado para descarga");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
        } catch (Exception e) {
            System.out.println("✗ ERROR descarga: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}