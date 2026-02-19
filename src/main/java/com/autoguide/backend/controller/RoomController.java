package com.autoguide.backend.controller;

import com.autoguide.backend.dto.CreateRoomMultipartRequest;
import com.autoguide.backend.dto.CreateRoomRequest;
import com.autoguide.backend.dto.RoomResponse;
import com.autoguide.backend.dto.UpdateRoomRequest;
import com.autoguide.backend.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<RoomResponse> createJson(@Valid @RequestBody CreateRoomRequest request) {
        return roomService.create(request);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<RoomResponse> createMultipart(
            @Valid @ModelAttribute CreateRoomMultipartRequest request,
            @RequestPart("imageFile") FilePart imageFile
    ) {
        return roomService.createWithUploadedImage(request.toCreateRoomRequest(), imageFile);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<RoomResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateRoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> delete(@PathVariable UUID id) {
        return roomService.delete(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Flux<RoomResponse> getAll() {
        return roomService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATOR')")
    public Mono<RoomResponse> getById(@PathVariable UUID id) {
        return roomService.getById(id);
    }
}
