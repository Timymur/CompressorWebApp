package com.example.CompressorWebApp.models;

import jakarta.persistence.*;

@Entity
public class Parameter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String parameterName;

    private String unitOfMeasurement;


    public Parameter() {

    }

    public Parameter(String parameterName, String unitOfMeasurement) {
        this.parameterName = parameterName;
        this.unitOfMeasurement = unitOfMeasurement;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getUnitOfMeasurement() {
        return unitOfMeasurement;
    }

    public void setUnitOfMeasurement(String unitOfMeasurement) {
        this.unitOfMeasurement = unitOfMeasurement;
    }
}
