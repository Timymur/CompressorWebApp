package com.example.CompressorWebApp.repositories;


import com.example.CompressorWebApp.models.CompressorEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface CompressorEventRepository extends JpaRepository<CompressorEvent, Long> {


    List<CompressorEvent> findByCompressorId(Long id);

    @Query("SELECT compressorEvents " +
            "FROM CompressorEvent compressorEvents " +
            "WHERE compressorEvents.compressor.id = :compressorId " +
            "ORDER BY compressorEvents.id " +
            "DESC")
    List<CompressorEvent> findLast10ByCompressorId(@Param("compressorId") Long compressorId, Pageable pageable);

}