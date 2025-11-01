package edu.aygo.taller3aygo.repository;

import edu.aygo.taller3aygo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> { }
