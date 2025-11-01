package edu.aygo.taller3aygo.controller;

import edu.aygo.taller3aygo.model.Payment;
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
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final CrudService<Payment> paymentService;

    @GetMapping
    public ResponseEntity<List<Payment>> findAll(){
        return ResponseEntity.ok(paymentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> findById(@PathVariable Long id){
        return paymentService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Payment> save(@RequestBody Payment payment) throws URISyntaxException {
        return ResponseEntity.created(new URI("/payment/" + payment.getId())).body(paymentService.upsert(payment));
    }

    @PatchMapping
    public ResponseEntity<Payment> update(@RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.upsert(payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
