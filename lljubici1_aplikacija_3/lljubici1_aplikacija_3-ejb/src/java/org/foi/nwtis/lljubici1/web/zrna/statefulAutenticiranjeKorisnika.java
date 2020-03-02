    package org.foi.nwtis.lljubici1.web.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.ejb.LocalBean;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.foi.nwtis.lljubici1.web.RestOdgovorERR;
import org.foi.nwtis.lljubici1.web.RestOdgovorOK;
import org.foi.nwtis.lljubici1.web.podaci.Korisnik;

@Stateful
@LocalBean
public class statefulAutenticiranjeKorisnika {

    REST_Korisnici_Klijent rest;
    private Gson gson = new Gson();

    public statefulAutenticiranjeKorisnika() {
        rest = new REST_Korisnici_Klijent();
    }

    public boolean provjeriPrijavu(String korisnickoIme, String lozinka) {
        JsonObject json = new JsonObject();
        json.addProperty("lozinka", lozinka);
        
        String odgovor = rest.autentikatijaKorisnikaGet(korisnickoIme, json.toString());
        JsonParser parser = new JsonParser();
        JsonObject json1 = (JsonObject) parser.parse(odgovor);
        String status = json1.get("status").getAsString();
        if (status.equals("OK")) {
            RestOdgovorOK<Korisnik> odgovorOK = new RestOdgovorOK();
            odgovorOK = gson.fromJson(odgovor, RestOdgovorOK.class);
            return true;
        }
        RestOdgovorERR odgovorERR = new RestOdgovorERR();
        odgovorERR = gson.fromJson(odgovor, RestOdgovorERR.class);
        return false;
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
                try {
                    resource = resource.queryParam("auth", URLEncoder.encode(auth, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(statefulAutenticiranjeKorisnika.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String preuzimanjeKorisnikaGet() throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String dodajKorisnikaPost(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public void close() {
            client.close();
        }
    }

    

    

}
