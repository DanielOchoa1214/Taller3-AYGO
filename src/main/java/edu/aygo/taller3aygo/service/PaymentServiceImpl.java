package edu.aygo.taller3aygo.service;

import edu.aygo.taller3aygo.model.Payment;
import edu.aygo.taller3aygo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements CrudService<Payment>{

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> findById(long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Payment upsert(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public void delete(long id) {
        paymentRepository.deleteById(id);
    }
}
