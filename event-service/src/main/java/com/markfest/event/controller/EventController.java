package com.markfest.event.controller;

import com.markfest.event.model.Event;
import com.markfest.event.repository.EventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventRepository repo;

    public EventController(EventRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<Event> create(@RequestBody Event e) {
        return ResponseEntity.ok(repo.save(e));
    }

    @GetMapping
    public ResponseEntity<List<Event>> list() { return ResponseEntity.ok(repo.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<Event> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
