package org.foi.nwtis.lljubici1.web.podaci;

import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa Aerodrom služi za kreiranje objekta tipa Aerodrom kao i za dohvat, unos
 * podataka u tablice Airplanes, Airports, MyAirports. Također koristi klasu Baza.
 *
 * @author Luka Ljubičić
 */
public class Aerodrom {

    private String icao;
    private String naziv;
    private String drzava;
    private Lokacija lokacija;

    /**
     * Konstruktor za kreiranje objekta tipa Aerodrom sa inicijalizacijom vrijednosti.
     *
     * @param icao    
     * @param naziv    
     * @param drzava    
     * @param lokacija    
     */
    public Aerodrom(String icao, String naziv, String drzava, Lokacija lokacija) {
        this.icao = icao;
        this.naziv = naziv;
        this.drzava = drzava;
        this.lokacija = lokacija;
    }

    /**
     * Prazan konstruktor za kreiranje objekta tipa Aerodrom.
     */
    public Aerodrom() {
    }


    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public Lokacija getLokacija() {
        return lokacija;
    }

    public void setLokacija(Lokacija lokacija) {
        this.lokacija = lokacija;
    }
}
