package edu.aygo.taller3aygo.service;

import edu.aygo.taller3aygo.model.Driver;
import edu.aygo.taller3aygo.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements CrudService<Driver> {
    private final DriverRepository driverRepository;

    @Override
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    @Override
    public Optional<Driver> findById(long id) {
        return driverRepository.findById(id);
    }

    @Override
    public Driver upsert(Driver driver) {
        return driverRepository.save(driver);
    }

    @Override
    public void delete(long driverId) {
        driverRepository.deleteById(driverId);
    }
}