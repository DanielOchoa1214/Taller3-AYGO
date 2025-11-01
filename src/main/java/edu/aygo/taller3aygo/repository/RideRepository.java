package edu.aygo.taller3aygo.repository;

import edu.aygo.taller3aygo.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> { }
