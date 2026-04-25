package com.br.zetta.fire.controller;

import com.br.zetta.fire.data.dto.response.FireEventResponseDTO;
import com.br.zetta.fire.service.FireService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("fire-event")
public class FireEventController {
    private final FireService fireService;

    public FireEventController(FireService fireService) {
        this.fireService = fireService;
    }

    @GetMapping("/all")
    public ResponseEntity<Page<FireEventResponseDTO>> getAllFireEvent(
            @PageableDefault(page = 0, size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(fireService.listAll(pageable));
    }

}
