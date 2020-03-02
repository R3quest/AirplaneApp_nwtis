/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.WebServiceContext;
import org.foi.nwtis.lljubici1.KomunikacijaBaza;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.lljubici1.ws.Aerodrom;
import org.foi.nwtis.lljubici1.ws.AerodromWS;
import org.foi.nwtis.lljubici1.ws.Avion;



/**
 * REST Web Service
 *
 * @author root
 */
@Path("lljubici1_aplikacija_1")
public class REST {

    public REST() {
    }

    @Resource
    private WebServiceContext context;

    private ServletContext getServletContext() {
        return SlusacAplikacije.getSc();
    }

    /**
     * Dohvaca vrijednosti konfiguracije.
     */
    private String dohvatiVrijednostKonfiguracije(String kljuc) {
        Properties konfiguracijaApp = (Properties) getServletContext().getAttribute("konfiguracijaApp");
        return konfiguracijaApp.getProperty(kljuc);
    }

//    private String autenticirajKorisnika(String korisnickoIme, String lozinka, Gson gson) {
//        String odgovor = null;
//        boolean postoji = false;
//        try {
//            postoji = KomunikacijaBaza.autentikacijaKorisnika(korisnickoIme, lozinka);
//        } catch (SQLException ex) {
//            Logger.getLogger(REST_AIRP2.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        if (!postoji) {
//            RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_KORISNICKO_IME_LOZINKA");
//            odgovor = gson.toJson(restOdgovorERR);
//            return odgovor;
//        }
//        return null;
//    }
    private org.foi.nwtis.lljubici1.ws.Aerodrom vratiAerodromUWSObliku(org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodrom) {
        org.foi.nwtis.lljubici1.ws.Aerodrom aerodromWS = new org.foi.nwtis.lljubici1.ws.Aerodrom();
        aerodromWS.setIcao(aerodrom.getIcao());
        aerodromWS.setNaziv(aerodrom.getNaziv());
        aerodromWS.setDrzava(aerodrom.getDrzava());
        if (aerodrom.getLokacija() != null) {
            org.foi.nwtis.lljubici1.ws.Lokacija lokacija = new org.foi.nwtis.lljubici1.ws.Lokacija();
            lokacija.setLatitude(aerodrom.getLokacija().getLatitude());
            lokacija.setLongitude(aerodrom.getLokacija().getLongitude());
            aerodromWS.setLokacija(lokacija);
        } else {
            aerodromWS.setLokacija(null);
        }
        return aerodromWS;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dohvatiAerodrome() {
        String odgovor = null;
        Gson gson = new Gson();
        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");
        if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            List<Aerodrom> listMyAirports = AerodromWS.dajSveAerodromeGrupe(korisnickoIme, lozinka);
            RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>(listMyAirports);
            odgovor = gson.toJson(restOdgovorOK);
            return odgovor;
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_dohvatiAerodrome");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dodajAerodrom(String poruka) {
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(poruka);
        String icao = json.get("icao").getAsString();
        String odgovor = null;
        Gson gson = new Gson();
        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");
        try {
            if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
                org.foi.nwtis.lljubici1.web.podaci.Aerodrom aerodrom = KomunikacijaBaza.dohvatiAirportsAerodrom(icao, dohvatiVrijednostKonfiguracije("LocationIQ.token"));
                Aerodrom aerodromWS = vratiAerodromUWSObliku(aerodrom);
                boolean uspjesno = AerodromWS.dodajAerodromGrupi(korisnickoIme, lozinka, aerodromWS);
                if (uspjesno) {
                    KomunikacijaBaza.dodajAerodromUMyAirports(aerodrom);
                    RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>();
                    odgovor = gson.toJson(restOdgovorOK);
                    return odgovor;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ex.getMessage());
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_dodajAerodrom");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dohvatiPodatkeAerodromaGetId(@PathParam("id") String icao) {
        String odgovor = null;
        Gson gson = new Gson();
        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");
        if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            Aerodrom aerodrom = null;
            List<Aerodrom> aerodromi = AerodromWS.dajSveAerodromeGrupe(korisnickoIme, lozinka);
            for (org.foi.nwtis.lljubici1.ws.Aerodrom a : aerodromi) {
                if (a.getIcao().equals(icao)) {
                    aerodrom = a;
                }
            }
            if (aerodrom != null) {
                RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>();
                restOdgovorOK.dodajPoruku(aerodrom);
                odgovor = gson.toJson(restOdgovorOK);
                return odgovor;
            }
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_dohvatiPodatkeAerodromaGetId");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    //NE RADI NE ZNAM
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String azurirajPodatkeAerodromaPutId(@PathParam("id") String icao, String poruka) {
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(poruka);
        String naziv = json.get("naziv").getAsString();
        String adresa = json.get("adresa").getAsString();
        String odgovor = null;
        Gson gson = new Gson();
        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");
        try {
            if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
                boolean uspjesno1 = KomunikacijaBaza.azurirajAerodromMyAirports(icao,
                        dohvatiVrijednostKonfiguracije("LocationIQ.token"),
                        naziv, adresa);
                if (uspjesno1) {
                    RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>();
                    odgovor = gson.toJson(restOdgovorOK);
                    return odgovor;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(REST.class.getName()).log(Level.SEVERE, null, ex);
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_azurirajPodatkeAerodromaPutId");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String obrisiAerodromDeleteId(@PathParam("id") String icao) {
        String odgovor = null;
        Gson gson = new Gson();

        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");

        if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            boolean uspjesno = AerodromWS.obrisiAerodromGrupe(korisnickoIme, lozinka, icao);

            if (uspjesno) {
                RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>();
                odgovor = gson.toJson(restOdgovorOK);
                return odgovor;
            }
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_obrisiAerodromDeleteId");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    //6. 
    @Path("{id}/avion")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String dohvatiAvioneAerodromaGetIdAvion(@PathParam("id") String icao) {
        String odgovor = null;
        Gson gson = new Gson();

        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");

        if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            List<Avion> avioni = AerodromWS.dajSveAvioneAerodromaGrupe(korisnickoIme, lozinka, icao);
            RestOdgovorOK<Avion> restOdgovorOK = new RestOdgovorOK<>();
            restOdgovorOK.setPoruka(avioni);
            odgovor = gson.toJson(restOdgovorOK);
            return odgovor;
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_obrisiAerodromDeleteId");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    //7.
    @Path("{id}/avion")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String dodajAvione(@PathParam("id") String icao24, String poruka) {
        return null;

    }

    //8
    @Path("{id}/avion")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String obrisiAvioneAerodroma(@PathParam("id") String icao) {
        String odgovor = null;
        Gson gson = new Gson();

        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");
        if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            List<Avion> avioni = AerodromWS.dajSveAvioneAerodromaGrupe(korisnickoIme, lozinka, icao);
            List<String> naziviAviona = new ArrayList<>();
            for (Avion a : avioni) {
                if (icao.equals(a.getEstdepartureairport())) {
                    naziviAviona.add(a.getIcao24());
                }
            }
            boolean obrisano = AerodromWS.obrisiOdabraneAerodromeGrupe(korisnickoIme, lozinka, naziviAviona);
            if (obrisano) {
                RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>();
                odgovor = gson.toJson(restOdgovorOK);
                return odgovor;
            }
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_obrisiAvioneAerodroma");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }

    //9
    @Path("{id}/avion/{aid}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public String birsiAvionAerodroma(@PathParam("id") String icao, @PathParam("aid") String idAviona) {
        String odgovor = null;
        Gson gson = new Gson();
        String korisnickoIme = dohvatiVrijednostKonfiguracije("korisnik.ime");
        String lozinka = dohvatiVrijednostKonfiguracije("korisnik.lozinka");
        if (AerodromWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            Avion zaBrisanje = null;
            List<Avion> listaAvionaAerodroma = AerodromWS.dajSveAvioneAerodromaGrupe(korisnickoIme, lozinka, icao);
            for(Avion a: listaAvionaAerodroma){
                if(a.getIcao24().equals(idAviona)){
                    zaBrisanje = a;
                }
            }
            List<String> brisi = new ArrayList<>();
            brisi.add(zaBrisanje.getIcao24());
            //METODA TREBA BRISATI AVIONE A NE OVO
            boolean obrisano = AerodromWS.obrisiOdabraneAerodromeGrupe(korisnickoIme, lozinka,brisi);
            if(obrisano){
                RestOdgovorOK<Aerodrom> restOdgovorOK = new RestOdgovorOK<>();
                odgovor = gson.toJson(restOdgovorOK);
                return odgovor;
            }
        }
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR("ERR_obrisiAvioneAerodroma");
        odgovor = gson.toJson(restOdgovorERR);
        return odgovor;
    }
}
