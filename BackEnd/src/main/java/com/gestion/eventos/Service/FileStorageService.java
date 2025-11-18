package com.gestion.eventos.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
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
     // ==== ELIMINAR CARPETA ====
    public boolean deleteFolderByPath(String relativePath) {
        try {
            // 1. Resolver la ruta absoluta (uploads/ruta/relativa)
            Path folderPath = this.rootLocation.resolve(relativePath).normalize();
            
            // 2. Verificar si existe y es un directorio
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
                
                // 3. Recorrer el directorio, eliminar archivos y subdirectorios de forma inversa
                Files.walk(folderPath)
                    .sorted(Comparator.reverseOrder()) // Empezar por los archivos más profundos
                    .map(Path::toFile)
                    .forEach(File::delete);
                
                // 4. Intenta eliminar el directorio principal (debería estar vacío ahora)
                return Files.deleteIfExists(folderPath);
            }
            return false; // El directorio no existía
        } catch (IOException e) {
            System.err.println("Error I/O al eliminar carpeta: " + relativePath + ". " + e.getMessage());
            // Lanzamos una excepción para que el método eliminarEvento en EventoServiceImp 
            // pueda hacer rollback de la base de datos si la eliminación falla por permisos, etc.
            throw new RuntimeException("Error al eliminar carpeta en el sistema de archivos: " + relativePath, e);
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
