package com.gestion.eventos.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestion.eventos.Service.FileStorageService;

@RestController
@RequestMapping("/archivos")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/view/{folder}/{evento}/{filename}")
    public ResponseEntity<Resource> view(
            @PathVariable String folder,
            @PathVariable String evento,
            @PathVariable String filename) {

        String path = folder + "/" + evento + "/" + filename;
        Resource file = fileStorageService.loadAsResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @GetMapping("/download/{folder}/{evento}/{filename}")
    public ResponseEntity<Resource> download(
            @PathVariable String folder,
            @PathVariable String evento,
            @PathVariable String filename) {

        String path = folder + "/" + evento + "/" + filename;
        Resource file = fileStorageService.loadAsResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

}
