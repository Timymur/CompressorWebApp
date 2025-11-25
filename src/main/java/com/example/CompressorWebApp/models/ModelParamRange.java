package com.example.CompressorWebApp.models;

import jakarta.persistence.*;

@Entity
public class ModelParamRange {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compressor_model_id")
    private CompressorModel compressorModel;

    @ManyToOne
    @JoinColumn(name = "parameter_id")
    private Parameter parameter;

    private double maxValue;
    private double minValue;


    public ModelParamRange() {

    }
    public ModelParamRange(CompressorModel compressorModel, Parameter parameter, double maxValue, double minValue) {
        this.compressorModel = compressorModel;
        this.parameter = parameter;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CompressorModel getCompressorModel() {
        return compressorModel;
    }

    public void setCompressorModel(CompressorModel compressorModel) {
        this.compressorModel = compressorModel;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }
}
