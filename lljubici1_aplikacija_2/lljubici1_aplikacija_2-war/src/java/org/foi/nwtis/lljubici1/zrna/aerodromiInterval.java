/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.foi.nwtis.lljubici1.RestOdgovorERR;
import org.foi.nwtis.lljubici1.RestOdgovorOK;
import org.foi.nwtis.lljubici1.web.podaci.Aerodrom;
import org.foi.nwtis.lljubici1.ws.AvionLeti;
import org.foi.nwtis.lljubici1.ws.WSAerodrom_Service;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author root
 */
@Named(value = "aerodromiInterval")
@SessionScoped
public class aerodromiInterval implements Serializable {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8084/lljubici1_aplikacija_1/WS_Aerodrom.wsdl")
    private WSAerodrom_Service service;

    private REST_Klijent rest;
    
    /** Creates a new instance of aerodromiInterval */
    public aerodromiInterval() {
        rest = new REST_Klijent();
        dohvatiKorisnickoImeILozinku();
        preuzmiAerodrome();
    }

    private String poruka = null;
    private Gson gson = new Gson();
    private Aerodrom odabraniAerodrom;
    private List<Aerodrom> aerodromi;
    private String korisnickoIme;
    private String lozinka;
    private String odVremena;
    private String doVremena;

    private List<AvionLeti> avioniLete;
    private AvionLeti avionLeti;
    
    private List<AvionLeti> listaLetovaOdabranogAviona;
    private AvionLeti letOdabranogAviona;
    
    public void dohvatiPodatkeLetoviIzabranogAviona(){
        if(avionLeti != null){
            try {
                int odVremena = (int) new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(this.odVremena).toInstant().getEpochSecond();
                int doVremena = (int) new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(this.doVremena).toInstant().getEpochSecond();
                this.listaLetovaOdabranogAviona = podaciOLetovimaIzabranogAvionaOdDo(korisnickoIme, lozinka, avionLeti.getIcao24(), odVremena,doVremena);
            } catch (ParseException ex) {
                Logger.getLogger(aerodromiInterval.class.getName()).log(Level.SEVERE, null, ex);
                this.poruka = "Dogodila se greska!";
                error();
            }
        }
    }

    
    public double izracunajUdaljenost(String icaoOd, String icaoDo){
           return dohvatiUdaljenostIzmeduDvaAerodroma(korisnickoIme, lozinka, icaoOd, icaoDo); 
    }

    /**
     * Metoda pretvara trenutno vrijeme (u sekundama) u format datuma.
     * @param sekunde trenutno vrijem (u sekundama)
     * @return ""
     */
    public String pretvoriDatum(int sekunde) {
        Date datum = new Date(sekunde * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(datum);
    }

    public void preuzmiAerodrome() {
        String odgovor = rest.dohvatiAerodrome();
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(odgovor);
        String status = json.get("status").getAsString();
        if (status.equals("OK")) {
            Type tip = new TypeToken<RestOdgovorOK<Aerodrom>>() {
            }.getType();
            RestOdgovorOK<Aerodrom> odgovorOK = new RestOdgovorOK();
            odgovorOK = gson.fromJson(odgovor, tip);
            this.aerodromi = odgovorOK.getPoruka();
            return;
        }
        Type tip = new TypeToken<RestOdgovorERR>() {
        }.getType();
        RestOdgovorERR odgovorERR = new RestOdgovorERR();
        odgovorERR = gson.fromJson(odgovor, tip);
        this.poruka = odgovorERR.getPoruka();
        error();
    }

    public String preuzmiLetoveUIntervalu() {
        try {
            int odVremena = (int) new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(this.odVremena).toInstant().getEpochSecond();
            int doVremena = (int) new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(this.doVremena).toInstant().getEpochSecond();
            //preuzeti podatke;
            preuzmiLetove(odVremena, doVremena);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            this.poruka = "Pogresno zadan format! (dd-MM-yyyy HH:mm:ss)";
            error();
        }
        return "";
    }

    private void preuzmiLetove(int odVremena, int doVremena) {
        if(this.odabraniAerodrom != null){
            this.avioniLete = podaciOLetovimaIzabranogAerodromaOdDo(this.korisnickoIme, this.lozinka, this.odabraniAerodrom.getIcao(), odVremena, doVremena);
            return;
        }
        this.poruka = "Niste odabrali aerodrom!";
        error();
    }

    private void dohvatiKorisnickoImeILozinku() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        this.korisnickoIme = (String) sesija.getAttribute("korisnickoIme");
        this.lozinka = (String) sesija.getAttribute("lozinka");
    }

    public List<Aerodrom> getAerodromi() {
        return aerodromi;
    }


    public String getPoruka() {
        return poruka;
    }

    public Aerodrom getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(Aerodrom odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public String getOdVremena() {
        return odVremena;
    }

    public void setOdVremena(String odVremena) {
        this.odVremena = odVremena;
    }

    public String getDoVremena() {
        return doVremena;
    }

    public void setDoVremena(String doVremena) {
        this.doVremena = doVremena;
    }

    public List<AvionLeti> getAvioniLete() {
        return avioniLete;
    }

    public AvionLeti getAvionLeti() {
        return avionLeti;
    }

    public List<AvionLeti> getListaLetovaOdabranogAviona() {
        return listaLetovaOdabranogAviona;
    }

    public AvionLeti getLetOdabranogAviona() {
        return letOdabranogAviona;
    }

    public void setAvioniLete(List<AvionLeti> avioniLete) {
        this.avioniLete = avioniLete;
    }

    public void setAvionLeti(AvionLeti avionLeti) {
        this.avionLeti = avionLeti;
    }

    public void setLetOdabranogAviona(AvionLeti letOdabranogAviona) {
        this.letOdabranogAviona = letOdabranogAviona;
    }
    
    
    static class REST_Klijent {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/lljubici1_aplikacija_1/webresources";

        public REST_Klijent() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("lljubici1_aplikacija_1");
        }

        public String dohvatiAerodrome() throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String obrisiAvioneAerodroma(String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}/avion", new Object[]{id})).request().delete(String.class);
        }

        public String birsiAvionAerodroma(String id, String aid) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}/avion/{1}", new Object[]{id, aid})).request().delete(String.class);
        }

        public String dodajAerodrom(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String dohvatiPodatkeAerodromaGetId(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String dohvatiAvioneAerodromaGetIdAvion(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}/avion", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String obrisiAerodromDeleteId(String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request().delete(String.class);
        }

        public String azurirajPodatkeAerodromaPutId(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String dodajAvione(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}/avion", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public void close() {
            client.close();
        }
    }

    
    public void info() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", this.poruka));
    }

    public void error() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Greska!", this.poruka));
    }

    

    private java.util.List<org.foi.nwtis.lljubici1.ws.AvionLeti> podaciOLetovimaIzabranogAerodromaOdDo(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icao, int odVremena, int doVremena) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.podaciOLetovimaIzabranogAerodromaOdDo(korisnik, lozinka, icao, odVremena, doVremena);
    }

    private double dohvatiUdaljenostIzmeduDvaAerodroma(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icaoAerodromaOd, java.lang.String icaoAerodromaDo) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.dohvatiUdaljenostIzmeduDvaAerodroma(korisnik, lozinka, icaoAerodromaOd, icaoAerodromaDo);
    }

    private java.util.List<org.foi.nwtis.lljubici1.ws.AvionLeti> podaciOLetovimaIzabranogAvionaOdDo(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icao, int odVremena, int doVremena) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.podaciOLetovimaIzabranogAvionaOdDo(korisnik, lozinka, icao, odVremena, doVremena);
    }

        

    
    
}
