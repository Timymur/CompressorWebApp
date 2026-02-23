package com.example.CompressorWebApp.models;

import com.example.CompressorWebApp.enums.CompressorState;
import jakarta.persistence.*;


@Entity
public class Compressor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int serialNumber;

    @Enumerated(EnumType.STRING)
    private CompressorState state;

    private double workHours;

    @ManyToOne
    @JoinColumn(name = "compressor_model_id")
    private CompressorModel compressorModel;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;


    public Compressor(){

    }

    public Compressor(int serialNumber,  int workHours, CompressorModel compressorModel, Station station) {
        this.serialNumber = serialNumber;
        this.state = CompressorState.OFF;
        this.workHours = workHours;
        this.compressorModel = compressorModel;
        this.station = station;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public CompressorState getState() {
        return state;
    }

    public void setState(CompressorState state) {
        this.state = state;
    }

    public double getWorkHours() {
        return workHours;
    }

    public void setWorkHours(double workHours) {
        this.workHours = workHours;
    }

    public CompressorModel getCompressorModel() {
        return compressorModel;
    }

    public void setCompressorModel(CompressorModel compressorModel) {
        this.compressorModel = compressorModel;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }
}
