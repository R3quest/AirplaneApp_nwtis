/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.dretve;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.lljubici1.KomunikacijaBaza;
import org.foi.nwtis.lljubici1.konfiguracije.Konfiguracija;
import org.foi.nwtis.lljubici1.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.lljubici1.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.lljubici1.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.lljubici1.web.podaci.Aerodrom;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 * Klasa periodički dohvača letove aviona, ovisno o datoj konfiguracijskoj datoteci.
 * 
 * @author Luka Ljubičić.
 */
public class PreuzimanjeAviona extends Thread {
    
    

    public static boolean kraj = false;
    public static boolean pauza = false;

    public static boolean isPauza() {
        return pauza;
    }

    public static void setPauza(boolean aPauza) {
        pauza = aPauza;
        //3. app FacesContext.getCurrentInstance().getExternalContext().getInitParameter("imeParametraKonfiga");
    }

    String username;
    String password;
    int inicijalniPocetakIntervala;
    int pocetakIntervala;
    int krajIntervala;
    int trajanjeIntervala;
    int ciklusDretve;
    private final String putanjaProjekta;
    private int redniBrojCiklusa;

    public PreuzimanjeAviona(String username, String password, int inicijalniPocetakIntervala, int trajanjeIntervala, int ciklusDretve, String putanjaProjekta) {
        this.username = username;
        this.password = password;
        this.inicijalniPocetakIntervala = inicijalniPocetakIntervala;
        this.trajanjeIntervala = trajanjeIntervala;
        this.ciklusDretve = ciklusDretve * 60 * 1000;
        this.pocetakIntervala = (int) (new Date().getTime() / 1000) - (inicijalniPocetakIntervala * 60 * 60);
        this.krajIntervala = this.pocetakIntervala + (trajanjeIntervala * 60 * 60);

        this.putanjaProjekta = putanjaProjekta;
    }

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        try {
            while (!kraj) {
                citajIzDatotekeRadaDretve();
                if (this.pocetakIntervala >= ((int) (new Date().getTime() / 1000))) {
                    this.pocetakIntervala = (int) (new Date().getTime() / 1000) - (inicijalniPocetakIntervala * 60 * 60);
                    this.krajIntervala = this.pocetakIntervala + (trajanjeIntervala * 60 * 60);
                    this.redniBrojCiklusa = 0;
                }
                if (!pauza) {
                    preuzmiPodatkeAerodroma();
                } else {
                    System.out.println("Dretva PreuzimanjeAviona - pauzirana");
                }
                spremiUDatotekuRadaDretve();
                obradaSpavanja();
            }
            System.out.println("Dretva PreuzimanjeAviona - zavrsava!");
        } catch (Exception ex) {
            System.out.println("Dretva PreuzimanjeAviona CATCH: " + ex.getMessage());
        }
    }

    private synchronized void preuzmiPodatkeAerodroma() throws SQLException, InterruptedException {
        List<Aerodrom> aerodromi = KomunikacijaBaza.dohvatiSveMyAirports();
        List<AvionLeti> departures = dohvatiPoletjele(aerodromi);

        Collections.sort(departures, new Comparator<AvionLeti>() {
            @Override
            public int compare(AvionLeti o1, AvionLeti o2) {
                if (o1.getFirstSeen() == o2.getFirstSeen()) {
                    return 0;
                }
                return o1.getFirstSeen() < o2.getFirstSeen() ? -1 : 1;
            }
        });

        KomunikacijaBaza.dodajAvionLetiUAirplanes(departures);
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * Metoda dohvaća avione preko OSKlijent klase (metoda getDepartures)
     */
    private List<AvionLeti> dohvatiPoletjele(List<Aerodrom> aerodromi) {
        List<AvionLeti> departures = null;
        OSKlijent oSKlijent = new OSKlijent(username, password);
        for (Aerodrom aerodrom : aerodromi) {
            departures = oSKlijent.getDepartures(aerodrom.getIcao(), pocetakIntervala, krajIntervala);
        }
        return departures;
    }

    /**
     * Metoda obrađuje spavanje dretve, korekcija spavanja.
     */
    private void obradaSpavanja() throws InterruptedException {
        pocetakIntervala = krajIntervala;
        krajIntervala = pocetakIntervala + (trajanjeIntervala * 60 * 60);
        Thread.sleep(ciklusDretve); //- trajanje
    }

    public void citajIzDatotekeRadaDretve() {
        System.out.println(this.redniBrojCiklusa);
        String putanja = putanjaProjekta + SlusacAplikacije.konf.dajPostavku("datoteka.rada.dretve");
        File file = new File(putanja);
        boolean postoji = file.exists();
        Konfiguracija json;
        if (postoji) {
            try {
                json = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanja);
                this.redniBrojCiklusa = Integer.parseInt(json.dajPostavku("redniBrojCiklusa"));
                this.pocetakIntervala = Integer.parseInt(json.dajPostavku("pocetakIntervala"));

            } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
                Logger.getLogger(PreuzimanjeAviona.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.redniBrojCiklusa = 0;
        }
    }

    public void spremiUDatotekuRadaDretve() {
        String putanja = putanjaProjekta + SlusacAplikacije.konf.dajPostavku("datoteka.rada.dretve");
        File file = new File(putanja);
        FileWriter out = null;
        try {
            out = new FileWriter(file);
            out.write(dohvatiPodatkeIntervalaDretve());
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(PreuzimanjeAviona.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(PreuzimanjeAviona.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized String dohvatiPodatkeIntervalaDretve() {
        StringBuilder formatiraneDretve = new StringBuilder();
        this.redniBrojCiklusa++;
        formatiraneDretve.append("{\"pocetakIntervala\":" + this.krajIntervala + ",\"redniBrojCiklusa\":" + this.redniBrojCiklusa + "}\n");
        return formatiraneDretve.toString();
    }

    /**
     * Metoda sluzi za dohvacanje trenutnog vremena u formatu kojem joj zadamo
     * kojeg predajemo klasi {@link java.text.SimpleDateFormat}.
     * 
     * @param formatVremena format vremena
     * @return metoda vraca trenutno vrijeme u zadanom formatu.
     */
    public String vratiTrenutnoVrijeme(String formatVremena) {
        java.util.Date datum = new java.util.Date();
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(formatVremena);
        String trenutnoVrijeme = format.format(datum);
        return trenutnoVrijeme;
    }

}
