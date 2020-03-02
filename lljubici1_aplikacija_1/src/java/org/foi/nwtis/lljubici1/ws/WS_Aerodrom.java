/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.ws;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.foi.nwtis.lljubici1.KomunikacijaBaza;
import org.foi.nwtis.lljubici1.web.podaci.Korisnik;
import org.foi.nwtis.rest.klijenti.OWMKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.MeteoPodaci;

/**
 *
 * @author root
 */
@WebService(serviceName = "WS_Aerodrom")
public class WS_Aerodrom {

    @Resource
    private WebServiceContext kontekst;

    private ServletContext getKontekst() {
        return (ServletContext) kontekst.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
    }

    private String dohvatiVrijednostKonfiguracije(String kljuc) {
        Properties konfiguracijaApp = (Properties) getKontekst().getAttribute("konfiguracijaApp");
        return konfiguracijaApp.getProperty(kljuc);
    }

    @WebMethod(operationName = "zadnjiPodaci")
    public AvionLeti zadnjiPodaci(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icaoAerodroma") String icaoAerodroma) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                return KomunikacijaBaza.dohvatiAvionLeti(icaoAerodroma);
            }
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "zadnjihNPodataka")
    public List<AvionLeti> zadnjihNPodataka(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icao") String icao,
            @WebParam(name = "n") int n) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                List<AvionLeti> avioniLete = KomunikacijaBaza.dohvatiPoletjeleAvione(icao, n);
                KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", "1", "REQ", new Timestamp(System.currentTimeMillis()), 0);
                return avioniLete;
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "podaciOLetovimaIzabranogAerodromaOdDo")
    public List<AvionLeti> podaciOLetovimaIzabranogAerodromaOdDo(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icao") String icao,
            @WebParam(name = "odVremena") int odVremena,
            @WebParam(name = "doVremena") int doVremena) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                List<AvionLeti> avioniLete = KomunikacijaBaza.dohvatiPoletjeleAvione(icao, odVremena, doVremena);
                //PROVJERI
                return avioniLete;
            }

            return null;
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "podaciOLetovimaIzabranogAvionaOdDo")
    public List<AvionLeti> podaciOLetovimaIzabranogAvionaOdDo(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icao") String icao,
            @WebParam(name = "odVremena") int odVremena,
            @WebParam(name = "doVremena") int doVremena) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                return KomunikacijaBaza.dohvatiLetoveAviona(icao, odVremena, doVremena);
                //provjeri
            }
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "podaciOLetovimaIzabranogAvionaOdDoNazivi")
    public List<String> podaciOLetovimaIzabranogAvionaOdDoNazivi(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icao") String icao,
            @WebParam(name = "odVremena") int odVremena,
            @WebParam(name = "doVremena") int doVremena) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                List<String> naziviAvionLeti = KomunikacijaBaza.dohvatiPoletjeleAvioneNazivi(icao, odVremena, doVremena);
                //provjeri
                return naziviAvionLeti;
            }

            return null;
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "dohvatiMeteoPodatkeIzabraniAerodrom")
    public MeteoPodaci dohvatiMeteoPodatkeIzabraniAerodrom(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icao") String icao) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                //LocationIQ.token
                String token = dohvatiVrijednostKonfiguracije("LocationIQ.token");
                org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodrom = KomunikacijaBaza.dohvatiAirportsAerodrom(icao, token);
                String apiKey = dohvatiVrijednostKonfiguracije("OpenWeatherMap.apikey");
                OWMKlijent oWMKlijent = new OWMKlijent(apiKey);
                MeteoPodaci meteoPodaci = oWMKlijent.getRealTimeWeather(
                        aerodrom.getLokacija().getLatitude(),
                        aerodrom.getLokacija().getLongitude());

                return meteoPodaci;
            }
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "dodajKorisnika")
    public boolean dodajKorisnika(
            @WebParam(name = "korisnik") Korisnik korisnik) {

        System.out.println("USO U DODAJ KORISNIKA SOAP! KORISNIK: " + korisnik.getKorisnickoIme() + " " + korisnik.getLozinka());

        KomunikacijaBaza.zapisiUDnevnik("", "n/a", "SOAP", new Object() {
        }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);

        System.out.println("AUTENTIKACIJA PROSLA");
        return KomunikacijaBaza.registrirajKorisnika(korisnik.getKorisnickoIme(),
                korisnik.getLozinka(), korisnik.getIme(),
                korisnik.getPrezime());

    }

    @WebMethod(operationName = "azurirajKorisnika")
    public boolean azurirajKorisnika(
            @WebParam(name = "korisnickoIme") String korisnickoIme,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "korisnik") Korisnik korisnik) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnickoIme, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnickoIme, lozinka)) {
                return KomunikacijaBaza.azurirajKorisnika(korisnik.getId(),
                        korisnik.getKorisnickoIme(), korisnik.getLozinka(),
                        korisnik.getIme(), korisnik.getPrezime());
            }
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @WebMethod(operationName = "dohvatiKorisnike")
    public List<Korisnik> dohvatiKorisnike(
            @WebParam(name = "korisnickoIme") String korisnickoIme,
            @WebParam(name = "lozinka") String lozinka) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnickoIme, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnickoIme, lozinka)) {
                return KomunikacijaBaza.dohvatiListuSvihKorisnika();
            }
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @WebMethod(operationName = "dohvatiUdaljenostIzmeduDvaAerodroma")
    public double dohvatiUdaljenostIzmeduDvaAerodroma(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icaoAerodromaOd") String icaoAerodromaOd,
            @WebParam(name = "icaoAerodromaDo") String icaoAerodromaDo) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodromOd = KomunikacijaBaza.aerodromAirportsIcaoLokacija(icaoAerodromaOd); //KomunikacijaBaza.dohvatiMyAirportsAerodrom(icaoAerodromaOd);
                org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodromDo = KomunikacijaBaza.aerodromAirportsIcaoLokacija(icaoAerodromaDo); //KomunikacijaBaza.dohvatiMyAirportsAerodrom(icaoAerodromaDo);
                if(aerodromOd == null ||aerodromDo == null){
                    return 0;
                }
                return izracunajUdaljenostIzmeduDvaAerodroma(aerodromOd.getLokacija(), aerodromDo.getLokacija());
            }
        } catch (SQLException ex) {
            Logger.getLogger(WS_Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0d;
    }

    ///fali 1
    private double izracunajUdaljenostIzmeduDvaAerodroma(org.foi.nwtis.rest.podaci.Lokacija lokacijaA1, org.foi.nwtis.rest.podaci.Lokacija lokacijaA2) {
        //https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude

        double lonA1 = Double.parseDouble(lokacijaA1.getLongitude());
        double lonA2 = Double.parseDouble(lokacijaA2.getLongitude());
        double theta = lonA1 - lonA2;

        double latA1 = Double.parseDouble(lokacijaA1.getLatitude());
        double latA2 = Double.parseDouble(lokacijaA2.getLatitude());

        double dist = Math.sin(deg2rad(latA1)) * Math.sin(deg2rad(latA2)) + Math.cos(deg2rad(latA1)) * Math.cos(deg2rad(latA2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    @WebMethod(operationName = "udaljenostIzmeduDvaAerodromaUGranicama")
    public List<org.foi.nwtis.lljubici1.web.podaci.Aerodrom> udaljenostIzmeduDvaAerodromaUGranicama(
            @WebParam(name = "korisnik") String korisnik,
            @WebParam(name = "lozinka") String lozinka,
            @WebParam(name = "icaoAerodroma") String icaoAerodroma,
            @WebParam(name = "odUdaljenost") double odUdaljenost,
            @WebParam(name = "doUdaljenost") double doUdaljenost) {
        try {
            KomunikacijaBaza.zapisiUDnevnik("", korisnik, "SOAP", new Object() {
            }.getClass().getEnclosingMethod().getName(), "REQ", new Timestamp(System.currentTimeMillis()), 0);
            if (KomunikacijaBaza.autentikacijaKorisnika(korisnik, lozinka)) {
                List<org.foi.nwtis.lljubici1.web.podaci.Aerodrom> aerodromiInterval = new ArrayList<>();

                org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodromOd = KomunikacijaBaza.aerodromAirportsIcaoLokacija(icaoAerodroma); //dohvatiMyAirportsAerodrom(icaoAerodroma);
                List<org.foi.nwtis.lljubici1.web.podaci.Aerodrom> sviAirports = KomunikacijaBaza.dohvatisveAirportsAerodrom(); // dohvatiSveMyAirports();
                double tmpUdaljenost = 0;
                for (org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodrom : sviAirports) {
                    tmpUdaljenost = izracunajUdaljenostIzmeduDvaAerodroma(aerodromOd.getLokacija(), aerodrom.getLokacija());
                    if (tmpUdaljenost >= odUdaljenost && tmpUdaljenost <= doUdaljenost) {
                        aerodromiInterval.add(aerodrom);
                    }
                }
                return aerodromiInterval;
            }
        } catch (SQLException ex) {
            Logger.getLogger(AerodromWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private double deg2rad(double deg) {
        //https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        //https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
        return (rad * 180.0 / Math.PI);
    }
}
