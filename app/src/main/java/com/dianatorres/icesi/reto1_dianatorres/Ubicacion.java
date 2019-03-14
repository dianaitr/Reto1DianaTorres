package com.dianatorres.icesi.reto1_dianatorres;

public class Ubicacion {

    public double latitud;
    public double longitud;

    private float distancia;


    private String nombre;

    public Ubicacion(String nombre,double latitud, double longitud){
        this.nombre=nombre;
        this.latitud=latitud;
        this.longitud=longitud;
        distancia=0;

    }

    public void setDistancia(float distancia){

        this.distancia=distancia;
    }

    public float getDistancia(){
        return distancia;
    }


    public String getNombre(){
        return nombre;
    }



    public double getLatitud(){
        return latitud;
    }

    public double getLongitud(){
        return longitud;
    }


}
