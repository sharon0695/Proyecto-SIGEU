package com.gestion.eventos.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.gestion.eventos.Config.FileStorageProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio para guardar los archivos.", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            // Validar tamaño máximo permitido
            if (file.getSize() > MAX_FILE_SIZE) {
                double tamanioMB = (double) file.getSize() / (1024 * 1024);
                double limiteMB = (double) MAX_FILE_SIZE / (1024 * 1024);
                throw new IllegalArgumentException(
                    String.format("El archivo excede el tamaño máximo permitido (%.2f MB / máximo %.2f MB)",
                            tamanioMB, limiteMB)
                );
            }

            // Validar que sea PDF
            if (!"application/pdf".equals(file.getContentType())) {
                throw new IllegalArgumentException("Solo se permiten archivos PDF");
            }

            // Crear subdirectorio si no existe
            Path subDirPath = this.fileStorageLocation.resolve(subDirectory);
            Files.createDirectories(subDirPath);

            // Generar nombre único para el archivo
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Copiar archivo al directorio destino
            Path targetLocation = subDirPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Retornar la ruta relativa para guardar en BD
            return subDirectory + "/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo " + file.getOriginalFilename() + ". Por favor intenta de nuevo.", ex);
        }
    }

    // Método para cargar archivo como Path (NECESARIO para el controller)
    public Path loadFileAsPath(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            if (Files.exists(file) && Files.isReadable(file)) {
                return file;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + filePath);
            }
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo cargar el archivo: " + filePath, ex);
        }
    }

    // Método para cargar archivo como Resource
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = loadFileAsPath(filePath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + filePath);
            }
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo cargar el archivo: " + filePath, ex);
        }
    }

    // Método para cargar bytes del archivo (si lo necesitas)
    public byte[] loadFile(String filePath) {
        try {
            Path file = loadFileAsPath(filePath);
            return Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo cargar el archivo: " + filePath, ex);
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return false;
            }
            
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            boolean deleted = Files.deleteIfExists(file);
            
            if (deleted) {
                System.out.println("✓ Archivo eliminado: " + filePath);
            } else {
                System.out.println("ℹ️ Archivo no existía: " + filePath);
            }
            
            return deleted;
            
        } catch (IOException ex) {
            System.out.println("⚠ Error eliminando archivo " + filePath + ": " + ex.getMessage());
            return false;
        }
    }

    public boolean fileExists(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            return Files.exists(file) && Files.isReadable(file);
        } catch (Exception ex) {
            return false;
        }
    }

    // Método para obtener la ruta completa del archivo
    public String getFullPath(String filePath) {
        return this.fileStorageLocation.resolve(filePath).normalize().toString();
    }
}