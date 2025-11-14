package com.gestion.eventos.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() throws IOException {
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
    }

    // ==== GUARDAR ARCHIVO EN SUBCARPETA ====
    public String storeFile(MultipartFile file, String subfolder) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("El archivo está vacío");
            }

            // Crear carpeta destino: uploads/organizaciones/evento_40
            Path destinationFolder = rootLocation.resolve(subfolder);
            Files.createDirectories(destinationFolder);

            // Nombre único
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path destinationFile = destinationFolder.resolve(filename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Retornar ruta que GUARDAS EN BD:
            return subfolder + "/" + filename;

        } catch (Exception e) {
            throw new RuntimeException("Error guardando archivo: " + e.getMessage());
        }
    }

    // ==== ELIMINAR ARCHIVO ====
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    // ==== OBTENER ARCHIVO PARA STREAMING ====
    public Resource loadAsResource(String relativePath) {
        try {
            Path filePath = rootLocation.resolve(relativePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + relativePath);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error cargando archivo " + relativePath + ": " + e.getMessage());
        }
    }
}
