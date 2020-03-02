package org.foi.nwtis.lljubici1.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.lljubici1.ws.Korisnik;
import java.lang.reflect.Type;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

/**
 * REST Web Service
 *
 * @author root
 */
@Path("korisnici")
public class REST_Korisnici {

    private int port;

    @Context
    private UriInfo context;

    private void postaviParametre() {
        this.port = Integer.parseInt(SlusacAplikacije.konf.dajPostavku("port1"));
    }

    public REST_Korisnici() {
        postaviParametre();
    }
    
        private String odgovorOK() {
        Gson gson = new Gson();
        Type tip = new TypeToken<RestOdgovorOK<Korisnik>>() {
        }.getType();
        RestOdgovorOK<Korisnik> restOdgovorOK = new RestOdgovorOK<>();
        String odgovor = gson.toJson(restOdgovorOK, tip);
        return odgovor;
    }

    private String odgovorOK(Korisnik korisnik) {
        Gson gson = new Gson();
        Type tip = new TypeToken<RestOdgovorOK<Korisnik>>() {
        }.getType();
        RestOdgovorOK<Korisnik> restOdgovorOK = new RestOdgovorOK<>();
        restOdgovorOK.dodajPoruku(korisnik);
        String odgovor = gson.toJson(restOdgovorOK, tip);
        return odgovor;
    }

    private String odgovorERR(String error) {
        Gson gson = new Gson();
        Type tip = new TypeToken<RestOdgovorERR>() {
        }.getType();
        RestOdgovorERR restOdgovorERR = new RestOdgovorERR(error);
        String odgovor = gson.toJson(restOdgovorERR, tip);
        return odgovor;
    }

    private String posaljiPoruku(String poruka) {
        String odgovor = "";
        StringBuilder stringBuilder = null;
        try {
            Socket socket = new Socket("localhost", this.port);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            pisi(outputStream, poruka);
            socket.shutdownOutput();
            stringBuilder = citaj(inputStream);
            socket.shutdownInput();
            socket.close();
            odgovor = stringBuilder.toString();
        } catch (IOException ex) {
            Logger.getLogger(REST_Korisnici.class.getName()).log(Level.SEVERE, null, ex);
            odgovor = "ERROR";
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String preuzimanjeKorisnikaGet(@HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        List<Korisnik> listaKorisnika = dohvatiKorisnike(korisnickoIme, lozinka);
        for (Korisnik k : listaKorisnika) { //bezLozinke
            k.setLozinka("");
        }
        String odgovor = null;
        Gson gson = new Gson();
        Type tip = new TypeToken<RestOdgovorOK<Korisnik>>() {
        }.getType();
        RestOdgovorOK<Korisnik> restOdgovorOK = new RestOdgovorOK<>(listaKorisnika);
        odgovor = gson.toJson(restOdgovorOK, tip);
        return odgovor;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String dodajKorisnikaPost(String poruka) {
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(poruka);
        String _ime = json.get("ime").getAsString();
        String _prezime = json.get("prezime").getAsString();
        String _korisnickoIme = json.get("korisnickoIme").getAsString();
        String _lozinka = json.get("lozinka").getAsString();

        Korisnik korisnik = new Korisnik();
        korisnik.setIme(_ime);
        korisnik.setPrezime(_prezime);
        korisnik.setKorisnickoIme(_korisnickoIme);
        korisnik.setLozinka(_lozinka);

        try {
            if (dodajKorisnika(korisnik)) {
                return odgovorOK();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return odgovorERR("ERR_dodajAerodrom");
    }

    private Korisnik dohvatiKorisnikaPoKorisnickomImenu(List<Korisnik> korisnici, String korisnicko_ime) {
        for (Korisnik k : korisnici) {
            if (k.getKorisnickoIme().equals(korisnicko_ime)) {
                return k;
            }
        }
        return null;
    }

    @Path("{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String autentikatijaKorisnikaGet(
            @PathParam("id") String korisnicko_ime,
            @QueryParam("auth") String auth,
            @HeaderParam("korisnickoIme") String korisnickoIme,
            @HeaderParam("lozinka") String lozinka) {
        String odgovor = null;
        if (auth == null) {
            List<Korisnik> korisnici = dohvatiKorisnike(korisnickoIme, lozinka);
            Korisnik korisnik = dohvatiKorisnikaPoKorisnickomImenu(korisnici, korisnicko_ime);
            if (korisnik != null) {
                odgovor = odgovorOK(korisnik);
            } else {
                odgovor = odgovorERR("Pogresni podaci!");
            }
            return odgovor;
        } else {
            JsonObject jsonObject = new JsonParser().parse(auth).getAsJsonObject();
            String _lozinka = jsonObject.get("lozinka").getAsString();
            String _naredba = posaljiPoruku("KORISNIK " + korisnicko_ime + "; LOZINKA " + _lozinka + ";");
            if (_naredba.startsWith("ERR")) {
                return odgovorERR("Pogresni podaci!");
            }
            return odgovorOK();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String azurirajKorisnikaPut(String poruka,
            @HeaderParam("korisnickoIme") String korisnickoIme,
            @HeaderParam("lozinka") String lozinka) {
        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(poruka);
        int _id = json.get("id").getAsInt();
        String _ime = json.get("ime").getAsString();
        String _prezime = json.get("prezime").getAsString();
        String _korisnickoIme = json.get("korisnickoIme").getAsString();
        String _lozinka = json.get("lozinka").getAsString();

        Korisnik korisnik = new Korisnik();
        korisnik.setId(_id);
        korisnik.setIme(_ime);
        korisnik.setPrezime(_prezime);
        korisnik.setKorisnickoIme(_korisnickoIme);
        korisnik.setLozinka(_lozinka);

        if (azurirajKorisnika(korisnickoIme, lozinka, korisnik)) {
            return odgovorOK(korisnik);
            
        }
        return  odgovorERR("ERR_dodajAerodrom");
    }

    private static java.util.List<org.foi.nwtis.lljubici1.ws.Korisnik> dohvatiKorisnike(java.lang.String korisnickoIme, java.lang.String lozinka) {
        org.foi.nwtis.lljubici1.ws.WSAerodrom_Service service = new org.foi.nwtis.lljubici1.ws.WSAerodrom_Service();
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.dohvatiKorisnike(korisnickoIme, lozinka);
    }

    private static boolean dodajKorisnika(org.foi.nwtis.lljubici1.ws.Korisnik korisnik) {
        org.foi.nwtis.lljubici1.ws.WSAerodrom_Service service = new org.foi.nwtis.lljubici1.ws.WSAerodrom_Service();
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.dodajKorisnika(korisnik);
    }

    private static boolean azurirajKorisnika(java.lang.String korisnickoIme, java.lang.String lozinka, org.foi.nwtis.lljubici1.ws.Korisnik korisnik) {
        org.foi.nwtis.lljubici1.ws.WSAerodrom_Service service = new org.foi.nwtis.lljubici1.ws.WSAerodrom_Service();
        org.foi.nwtis.lljubici1.ws.WSAerodrom port = service.getWSAerodromPort();
        return port.azurirajKorisnika(korisnickoIme, lozinka, korisnik);
    }

}
