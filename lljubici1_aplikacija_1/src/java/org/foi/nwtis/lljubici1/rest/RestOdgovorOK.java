package org.foi.nwtis.lljubici1.rest;

import java.util.ArrayList;
import java.util.List;


/**
 * Klasa za format poruke RestOdgovorOK kojeg koristimo u REST servisu.
 * 
 * @author Luka Ljubičić
 */
public class RestOdgovorOK<T> {
    /** sadržaj (lista) objekata */
    private List<T> odgovor = new ArrayList<>();
    /** poruka - odgovor status "OK" */
    private final String status = "OK";

    public RestOdgovorOK() {
    }
    
    public RestOdgovorOK(List<T> odgovor) {
        this.odgovor = odgovor;
    }
    
    /*
    * Dodaje u listu objekt.
     */
    public void dodajPoruku(T poruka){
        this.odgovor.add(poruka);
    }

    public List<T> getPoruka() {
        return odgovor;
    }

    public void setPoruka(List<T> poruka) {
        this.odgovor = poruka;
    }

    public String getStatus() {
        return status;
    }
}
