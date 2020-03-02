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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import javax.faces.bean.ManagedBean;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.xml.ws.WebServiceRef;
import org.foi.nwtis.lljubici1.RestOdgovorERR;
import org.foi.nwtis.lljubici1.RestOdgovorOK;
import org.foi.nwtis.lljubici1.web.podaci.Aerodrom;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.lljubici1.ws.WSAerodrom_Service;
import org.foi.nwtis.lljubici1.ws.MeteoPodaci;

/**
 *
 * @author root
 */
@Named(value = "aerodromiMeteo")
@SessionScoped
@ManagedBean
public class aerodromiMeteo implements Serializable {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8084/lljubici1_aplikacija_1/WS_Aerodrom.wsdl")
    private WSAerodrom_Service service;

    private REST_Klijent rest;

    private String poruka = null;
    private Gson gson = new Gson();
    private Aerodrom odabraniAerodrom;
    private List<Aerodrom> aerodromi;
    private String icaoZaDodat;
    private MeteoPodaci meteoPodaci;
    private String korisnickoIme;
    private String lozinka;

    public aerodromiMeteo() {
        rest = new REST_Klijent();
        preuzmiAerodrome();
        dohvatiKorisnickoImeILozinku();
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

    public void dodajAerodrom() {
        if (!icaoZaDodat.isEmpty()) {
            String odgovor = rest.dodajAerodrom("{\"icao\":\"" + icaoZaDodat + "\"}");
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(odgovor);
            String status = json.get("status").getAsString();
            if (status.equals("OK")) {
                this.poruka = "Uspjesno dodan!";
                preuzmiAerodrome();
                info();
                return;
            }
            this.poruka = "Greska!";
            error();
            return;
        }
        this.poruka = "Niste unesli icao";
        error();
    }

    private void dohvatiKorisnickoImeILozinku() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        this.korisnickoIme = (String) sesija.getAttribute("korisnickoIme");
        this.lozinka = (String) sesija.getAttribute("lozinka");
    }

    public void preuzmiMeteoPodatke() {
        if (odabraniAerodrom != null) {
            meteoPodaci = dohvatiMeteoPodatkeIzabraniAerodrom(korisnickoIme, lozinka, odabraniAerodrom.getIcao());
            return;
        }
        this.poruka = "Niste odabrali aerodrom!";
        error();
    }

    public void obrisiAerodrom() {
        if (odabraniAerodrom != null) {
            String odgovor = rest.obrisiAerodromDeleteId(odabraniAerodrom.getIcao());
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(odgovor);
            String status = json.get("status").getAsString();
            if (status.equals("OK")) {
                this.poruka = "Uspjesno obrisan!";
                preuzmiAerodrome();
                return;
            }
            this.poruka = "Greska!";
            error();
            return;
        }
        this.poruka = "Niste odabrali aerodrom!";
        error();
    }

    public void aktiviratiGrupu() {
        String komanda = "KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA KRENI;";
        posaljiPoruku(komanda);
    }

    public void blokiratiGrupu() {
        String komanda = "KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA PAUZA;";
        posaljiPoruku(komanda);
    }

    public void pregledStatusaGrupe() {
        String komanda = "KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA STANJE;";
        posaljiPoruku(komanda);
    }

    private String posaljiPoruku(String poruka) {
        String odgovor = "";
        StringBuilder stringBuilder = null;
        int port = Integer.parseInt(SlusacAplikacije.konf.dajPostavku("port1"));
        try {
            Socket socket = new Socket(InetAddress.getByName("localhost"), port);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            pisi(outputStream, poruka);
            socket.shutdownOutput();
            stringBuilder = citaj(inputStream);
            socket.shutdownInput();
            socket.close();
            odgovor = stringBuilder.toString();
            this.poruka = odgovor;
            info();
        } catch (IOException ex) {
            this.poruka = "ERROR";
            error();
        }
        return odgovor;
    }

    private void pisi(OutputStream outputStream, String poruka) throws IOException {
        outputStream.write(poruka.getBytes());
        outputStream.flush();
    }

    private StringBuilder citaj(InputStream inputStream) throws IOException {
        int znak;
        StringBuilder stringBuilder = new StringBuilder();
        while ((znak = inputStream.read()) != -1) {
            stringBuilder.append((char) znak);
        }
        return stringBuilder;
    }

    public List<Aerodrom> getAerodromi() {
        return aerodromi;
    }

    public String getIcaoZaDodat() {
        return icaoZaDodat;
    }

    public void setIcaoZaDodat(String icaoZaDodat) {
        this.icaoZaDodat = icaoZaDodat;
    }

    public MeteoPodaci getMeteoPodaci() {
        return meteoPodaci;
    }

    public void setMeteoPodaci(MeteoPodaci meteoPodaci) {
        this.meteoPodaci = meteoPodaci;
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

    private org.foi.nwtis.lljubici1.ws.MeteoPodaci dohvatiMeteoPodatkeIzabraniAerodrom(java.lang.String korisnik, java.lang.String lozinka, java.lang.String icao) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.dohvatiMeteoPodatkeIzabraniAerodrom(korisnik, lozinka, icao);
    }

    public void info() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", this.poruka));
    }

    public void error() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Greska!", this.poruka));
    }

}
