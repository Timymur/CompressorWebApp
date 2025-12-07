package com.example.CompressorWebApp.repositories;


import com.example.CompressorWebApp.models.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ParameterRepository extends JpaRepository<Parameter, Long>{

    boolean existsByParameterName(String parameterName);

    Optional<Parameter> findByParameterName(String parameterName);

}