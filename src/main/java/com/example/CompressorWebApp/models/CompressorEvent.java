package com.example.CompressorWebApp.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class CompressorEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDate date;
    private LocalTime time;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "parameter_id")
    private Parameter parameter;

    @ManyToOne
    @JoinColumn(name = "compressor_id")
    private Compressor compressor;


    public CompressorEvent() {

    }

    public CompressorEvent(User user, Parameter parameter, Compressor compressor, LocalDate date, LocalTime time) {
        this.user = user;
        this.parameter = parameter;
        this.compressor = compressor;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public void setCompressor(Compressor compressor) {
        this.compressor = compressor;
    }
}
