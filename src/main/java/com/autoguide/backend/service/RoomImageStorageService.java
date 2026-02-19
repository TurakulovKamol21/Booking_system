package com.autoguide.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class RoomImageStorageService {

    private final Path roomUploadsDirectory;
    private final String publicBasePath;

    public RoomImageStorageService(
            @Value("${app.uploads.base-path:${user.dir}/uploads}") String uploadsBasePath,
            @Value("${app.uploads.public-base-path:/uploads}") String publicBasePath
    ) {
        this.roomUploadsDirectory = Paths.get(uploadsBasePath, "rooms").toAbsolutePath().normalize();
        this.publicBasePath = normalizePublicBasePath(publicBasePath);
    }

    public Mono<String> storeRoomImage(FilePart imageFile) {
        if (imageFile == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageFile is required"));
        }

        MediaType mediaType = imageFile.headers().getContentType();
        if (mediaType == null || !"image".equalsIgnoreCase(mediaType.getType())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed"));
        }

        String extension = resolveExtension(mediaType, imageFile.filename());
        String fileName = UUID.randomUUID() + extension;
        Path targetPath = roomUploadsDirectory.resolve(fileName).normalize();

        return Mono.fromCallable(() -> {
                    Files.createDirectories(roomUploadsDirectory);
                    return targetPath;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(path -> imageFile.transferTo(path).thenReturn(buildPublicUrl(fileName)));
    }

    private String buildPublicUrl(String fileName) {
        return publicBasePath + "/rooms/" + fileName;
    }

    private String resolveExtension(MediaType mediaType, String originalFilename) {
        String subtype = mediaType.getSubtype().toLowerCase(Locale.ROOT);
        int plusIndex = subtype.indexOf('+');
        if (plusIndex > 0) {
            subtype = subtype.substring(0, plusIndex);
        }
        if ("jpeg".equals(subtype)) {
            subtype = "jpg";
        }
        subtype = subtype.replaceAll("[^a-z0-9]", "");
        if (!subtype.isBlank()) {
            return "." + subtype;
        }

        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(dotIndex + 1)
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]", "");
                if (!extension.isBlank()) {
                    return "." + extension;
                }
            }
        }

        return ".img";
    }

    private String normalizePublicBasePath(String value) {
        String normalized = (value == null || value.isBlank()) ? "/uploads" : value.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
