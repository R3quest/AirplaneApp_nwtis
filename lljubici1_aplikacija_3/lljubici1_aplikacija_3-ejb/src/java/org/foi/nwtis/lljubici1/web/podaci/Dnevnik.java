package org.foi.nwtis.lljubici1.web.podaci;

import java.sql.Timestamp;

/**
 * Klasa za dnevnike.
 * 
 * @author Luka Ljubičić
 */

public class Dnevnik {
    private int id;
    private String ipAdresa;
    private String korisnickoIme;
    private String vrstaZapisa;
    private String poruka;
    private String sadrzaj;
    private Timestamp vrijemePrijema;
    private int trajanjeObrade;

    public Dnevnik(String ipAdresa, String korisnickoIme, String vrstaZapisa, String poruka, String sadrzaj, Timestamp vrijemePrijema, int trajanjeObrade) {
        this.ipAdresa = ipAdresa;
        this.korisnickoIme = korisnickoIme;
        this.vrstaZapisa = vrstaZapisa;
        this.poruka = poruka;
        this.sadrzaj = sadrzaj;
        this.vrijemePrijema = vrijemePrijema;
        this.trajanjeObrade = trajanjeObrade;
    }

    public Dnevnik(int id, String ipAdresa, String korisnickoIme, String vrstaZapisa, String poruka, String sadrzaj, Timestamp vrijemePrijema, int trajanjeObrade) {
        this.id = id;
        this.ipAdresa = ipAdresa;
        this.korisnickoIme = korisnickoIme;
        this.vrstaZapisa = vrstaZapisa;
        this.poruka = poruka;
        this.sadrzaj = sadrzaj;
        this.vrijemePrijema = vrijemePrijema;
        this.trajanjeObrade = trajanjeObrade;
    }
    
    

    public Dnevnik() {
    }

    public int getId() {
        return id;
    }

    public String getIpAdresa() {
        return ipAdresa;
    }

    public void setIpAdresa(String ipAdresa) {
        this.ipAdresa = ipAdresa;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getVrstaZapisa() {
        return vrstaZapisa;
    }

    public void setVrstaZapisa(String vrstaZapisa) {
        this.vrstaZapisa = vrstaZapisa;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

    public void setSadrzaj(String sadrzaj) {
        this.sadrzaj = sadrzaj;
    }

    public Timestamp getVrijemePrijema() {
        return vrijemePrijema;
    }

    public void setVrijemePrijema(Timestamp vrijemePrijema) {
        this.vrijemePrijema = vrijemePrijema;
    }

    public int getTrajanjeObrade() {
        return trajanjeObrade;
    }

    public void setTrajanjeObrade(int trajanjeObrade) {
        this.trajanjeObrade = trajanjeObrade;
    }
    
}
