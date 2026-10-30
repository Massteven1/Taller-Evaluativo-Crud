/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.taller.service;

import com.example.taller.entity.Prestamo;
import com.example.taller.repository.PrestamoRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author alber
 */
@Service
public class PrestamoService {
    @Autowired
    PrestamoRepository prestamoRepository;
    
    //obtener todos, paginado
    public Page<Prestamo> getPrestamos(int page, int size){
        return prestamoRepository.findAll(PageRequest.of(page, size));
    }
    
    //obtener uno
    public Optional<Prestamo> getPrestamo(Long id){
        return prestamoRepository.findById(id);
    }
    
    public void saveOrUpdate(Prestamo prestamo){
        if (prestamo.getId() == null) {
            registrar(prestamo);
        } else {
            actualizar(prestamo);
        }
    }
    

    private void registrar(Prestamo prestamo){
        boolean devuelto = Boolean.TRUE.equals(prestamo.getDevuelto());
        prestamo.setDevuelto(devuelto);
        if (!devuelto && prestamoRepository.existsByLibroIdAndDevueltoFalse(prestamo.getLibroId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El libro ya esta prestado");
        }
        prestamoRepository.save(prestamo);
    }
    
    private void actualizar(Prestamo prestamo){
        Prestamo actual = prestamoRepository.findById(prestamo.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El prestamo no existe"));
        
        boolean yaDevuelto = Boolean.TRUE.equals(actual.getDevuelto());
        boolean seQuiereDevolver = Boolean.TRUE.equals(prestamo.getDevuelto());
        if (yaDevuelto && seQuiereDevolver) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El prestamo ya fue devuelto y no puede devolverse otra vez");
        }
        
        if (prestamo.getDevuelto() == null) {
            prestamo.setDevuelto(yaDevuelto);
            seQuiereDevolver = yaDevuelto;
        }
        
        if (!seQuiereDevolver && prestamoRepository.existsByLibroIdAndDevueltoFalseAndIdNot(prestamo.getLibroId(), prestamo.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El libro ya esta prestado");
        }
        
        prestamoRepository.save(prestamo);
    }
    
    public void delete(Long id){
        prestamoRepository.deleteById(id);
    }       
}
