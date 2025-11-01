package edu.aygo.taller3aygo.service;


import edu.aygo.taller3aygo.model.Ride;
import edu.aygo.taller3aygo.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements CrudService<Ride>{

    private final RideRepository rideRepository;

    @Override
    public List<Ride> findAll() {
        return rideRepository.findAll();
    }

    @Override
    public Optional<Ride> findById(long id) {
        return rideRepository.findById(id);
    }

    @Override
    public Ride upsert(Ride ride) {
        return rideRepository.save(ride);
    }

    @Override
    public void delete(long id) {
        rideRepository.deleteById(id);
    }
}