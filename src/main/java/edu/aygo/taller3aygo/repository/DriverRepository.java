package edu.aygo.taller3aygo.repository;

import edu.aygo.taller3aygo.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface DriverRepository extends JpaRepository<Driver, Long> { }
