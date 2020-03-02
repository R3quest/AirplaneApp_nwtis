/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import org.foi.nwtis.lljubici1.RestOdgovorERR;
import com.google.gson.JsonObject;
import org.foi.nwtis.lljubici1.RestOdgovorOK;
import java.util.List;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.foi.nwtis.lljubici1.ws.Korisnik;

/**
 *
 * @author root
 */
@Named(value = "pravaRegistracija")
@SessionScoped
@ManagedBean
public class pravaRegistracija implements Serializable {

    private REST_Korisnici_Klijent rest;

    private List<Korisnik> korisnici = new ArrayList<>();
    private String korisnickoIme;
    private String lozinka;
    private String ime;
    private String prezime;
    private String ponovljenaLozinka;
    private String poruka = null;
    private Gson gson = new Gson();
    
    

    public pravaRegistracija() {
        rest = new REST_Korisnici_Klijent();
        preuzmiKorisnike();
    }
    

    private boolean provjeriUnosPodataka() {
        if (ime.isEmpty() || prezime.isEmpty() || korisnickoIme.isEmpty()
                || lozinka.isEmpty() || ponovljenaLozinka.isEmpty()) {
            this.poruka = "Provjerite unesenost svih vrijednosti!";
            return false;
        }
        this.poruka = "";
        return true;
    }

    private boolean provjeriIspravnostLozinke() {
        if (lozinka.equals(ponovljenaLozinka)) {
            this.poruka = "";
            return true;
        }
        this.poruka = "Lozinka i ponovljena lozinka nisu iste!";
        return false;
    }

    private boolean provjeriPostojanjeKorisnickogImena(List<Korisnik> korisnici) {
        for (Korisnik k : korisnici) {
            if (k.getKorisnickoIme().equals(korisnickoIme)) {
                this.poruka = "Korisnicko ime je zauzeto!";
                return false;
            }
        }
        return true;
    }

    private void dodajKorisnika() {
        String strDodaj = "{\"ime\":\"" + ime + "\",\"prezime\":\"" + prezime + "\",\"korisnickoIme\":\"" + korisnickoIme + "\",\"lozinka\":\"" + lozinka + "\"}";
        String odgovor = rest.dodajKorisnikaPost(strDodaj);
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(odgovor);
        String status = json.get("status").getAsString();
        if (status.equals("OK")) {
            this.poruka = "Uspjesno dodan!";
        } else {
            this.poruka = "Pogreska pri dodavanju korisnika!";
        }
    }

    private void preuzmiKorisnike() {
        String odgovor = rest.preuzimanjeKorisnikaGet("admin", "admin");
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(odgovor);
        String status = json.get("status").getAsString();
        if (status.equals("OK")) {
            Type tip = new TypeToken<RestOdgovorOK<Korisnik>>() {
            }.getType();
            RestOdgovorOK<Korisnik> odgovorOK = new RestOdgovorOK();
            odgovorOK = gson.fromJson(odgovor, tip);
            this.korisnici = odgovorOK.getPoruka();
        }
    }

    public String registrirajKorisnika() {
        if (provjeriUnosPodataka() && provjeriIspravnostLozinke()) {
            String odgovor = rest.preuzimanjeKorisnikaGet(korisnickoIme, lozinka); //tu sam
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(odgovor);
            String status = json.get("status").getAsString();
            if (status.equals("OK")) {
                Type tip = new TypeToken<RestOdgovorOK<Korisnik>>() {
                }.getType();
                RestOdgovorOK<Korisnik> odgovorOK = new RestOdgovorOK();
                odgovorOK = gson.fromJson(odgovor, tip);
                List<Korisnik> korisnici = odgovorOK.getPoruka();
                if (!provjeriPostojanjeKorisnickogImena(korisnici)) {
                    return "";
                }
                dodajKorisnika();
                return "";
            }
            Type tip = new TypeToken<RestOdgovorERR>() {
            }.getType();
            RestOdgovorERR odgovorERR = new RestOdgovorERR();
            odgovorERR = gson.fromJson(odgovor, tip);
            this.poruka = odgovorERR.getPoruka();
        }
        return "";
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getPonovljenaLozinka() {
        return ponovljenaLozinka;
    }

    public void setPonovljenaLozinka(String ponovljenaLozinka) {
        this.ponovljenaLozinka = ponovljenaLozinka;
    }

    public String getPoruka() {
        return poruka;
    }

    public List<Korisnik> getKorisnici() {
        return korisnici;
    }

    static class REST_Korisnici_Klijent {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/lljubici1_aplikacija_3-war/webresources";

        public REST_Korisnici_Klijent() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("korisnici");
        }

        public String azurirajKorisnikaPut(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String autentikatijaKorisnikaGet(String id, String auth) throws ClientErrorException {
            WebTarget resource = webTarget;
            if (auth != null) {
                resource = resource.queryParam("auth", auth);
            }
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String preuzimanjeKorisnikaGet(String korisnickoIme, String lozinka) throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).header("korisnickoIme", korisnickoIme).header("lozinka", lozinka).get(String.class);
        }

        public String dodajKorisnikaPost(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public void close() {
            client.close();
        }
    }
}
