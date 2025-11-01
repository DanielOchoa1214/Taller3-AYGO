package edu.aygo.taller3aygo.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T> {
    List<T> findAll();
    Optional<T> findById(long id);
    T upsert(T entity);
    void delete(long id);
}
