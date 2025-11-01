package edu.aygo.taller3aygo.controller;

import edu.aygo.taller3aygo.model.Driver;
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
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    private final CrudService<Driver> driverService;

    @GetMapping
    public ResponseEntity<List<Driver>> findAll(){
        return ResponseEntity.ok(driverService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> findById(@PathVariable Long id){
        return driverService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Driver> save(@RequestBody Driver driver) throws URISyntaxException {
        return ResponseEntity.created(new URI("/driver/" + driver.getId())).body(driverService.upsert(driver));
    }

    @PatchMapping
    public ResponseEntity<Driver> update(@RequestBody Driver driver) {
        return ResponseEntity.ok(driverService.upsert(driver));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
