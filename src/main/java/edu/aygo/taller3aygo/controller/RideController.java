package edu.aygo.taller3aygo.controller;

import edu.aygo.taller3aygo.model.Ride;
import edu.aygo.taller3aygo.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/ride")
@RequiredArgsConstructor
public class RideController {

    private final CrudService<Ride> rideService;

    @GetMapping
    public ResponseEntity<List<Ride>> findAll(){
        return ResponseEntity.ok(rideService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ride> findById(@PathVariable Long id){
        return rideService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ride> save(@RequestBody Ride ride) throws URISyntaxException {
        return ResponseEntity.created(new URI("/ride/" + ride.getId())).body(rideService.upsert(ride));
    }

    @PatchMapping
    public ResponseEntity<Ride> update(@RequestBody Ride ride) {
        return ResponseEntity.ok(rideService.upsert(ride));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rideService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
