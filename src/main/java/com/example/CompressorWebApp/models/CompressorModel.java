package com.example.CompressorWebApp.models;

import jakarta.persistence.*;

@Entity
public class CompressorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String modelName;


    public CompressorModel() {

    }

    public CompressorModel(String modelName) {
        this.modelName = modelName;
    }

    public void setId(Long id){
        this.id = id;
    }
    
    public Long getId(){
        return id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
