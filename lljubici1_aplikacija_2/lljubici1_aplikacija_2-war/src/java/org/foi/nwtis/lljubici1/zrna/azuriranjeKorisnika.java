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
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import com.google.gson.JsonObject;
import org.foi.nwtis.lljubici1.RestOdgovorOK;
import java.util.List;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.lljubici1.ws.Korisnik;

/**
 *
 * @author root
 */
@Named(value = "azuriranjeKorisnika")
@SessionScoped
@ManagedBean
public class azuriranjeKorisnika implements Serializable {

    private REST_Korisnici_Klijent rest;

    private List<Korisnik> korisnici = new ArrayList<>();
    private Korisnik korisnikZaAzuriranje;
    private String korisnickoIme;
    private String lozinka;
    private String ime;
    private String prezime;
    private String poruka = null;
    private Gson gson = new Gson();

    public azuriranjeKorisnika() {
        rest = new REST_Korisnici_Klijent();
        preuzmiKorisnike();
    }

    public void azuriraj() {
        dohvatiKorisnikaZaAzuriranje();
        if (korisnikZaAzuriranje != null) {
            if (korisnikZaAzuriranje.getKorisnickoIme().equals(this.korisnickoIme)) {
                HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
                String _lozinka = (String) sesija.getAttribute("lozinka");
                Korisnik k = new Korisnik();
                k.setId(korisnikZaAzuriranje.getId());
                k.setKorisnickoIme(korisnickoIme);
                k.setLozinka(lozinka);
                k.setIme(ime);
                k.setPrezime(prezime);
                String jsonSalji = gson.toJson(k, Korisnik.class);
                System.out.println(jsonSalji);
                String odgovor = rest.azurirajKorisnikaPut(jsonSalji, korisnikZaAzuriranje.getKorisnickoIme(), _lozinka);
                JsonParser parser = new JsonParser();
                JsonObject json = (JsonObject) parser.parse(odgovor);
                String status = json.get("status").getAsString();
                if (status.equals("OK")) {
                    this.poruka = "Uspjesno azuriran!";
                    info();
                    sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
                    sesija.setAttribute("korisnickoIme", this.korisnickoIme);
                    sesija.setAttribute("lozinka", this.lozinka);
                    preuzmiKorisnike();
                } else {
                    this.poruka = "Pogreska pri azuriranju korisnika!";
                    error();
                }
                return;
            }
        }
        this.poruka = "Mozete azurirati samo svoj racun!";
        error();
    }

    private void dohvatiKorisnikaZaAzuriranje() {
        //ono u sesiji == upisanom
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        String _korisnickoIme = (String) sesija.getAttribute("korisnickoIme");
        for (Korisnik k : korisnici) {
            if (k.getKorisnickoIme().equals(_korisnickoIme)) {
                this.korisnikZaAzuriranje = k;
            }
        }
    }

    private void preuzmiKorisnike() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        String odgovor = rest.preuzimanjeKorisnikaGet((String) sesija.getAttribute("korisnickoIme"), (String) sesija.getAttribute("lozinka"));
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

    public String getPoruka() {
        return poruka;
    }

    public List<Korisnik> getKorisnici() {
        return korisnici;
    }

    public void info() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", this.poruka));
    }

    public void error() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Greska!", this.poruka));
    }

    public void setKorisnici(List<Korisnik> korisnici) {
        this.korisnici = korisnici;
    }

    static class REST_Korisnici_Klijent {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/lljubici1_aplikacija_3-war/webresources";

        public REST_Korisnici_Klijent() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("korisnici");
        }

        public String azurirajKorisnikaPut(Object requestEntity, String korisnickoIme, String lozinka) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).header("korisnickoIme", korisnickoIme).header("lozinka", lozinka).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
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
