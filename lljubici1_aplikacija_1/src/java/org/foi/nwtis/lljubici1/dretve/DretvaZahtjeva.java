package org.foi.nwtis.lljubici1.dretve;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import org.foi.nwtis.lljubici1.KomunikacijaBaza;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.lljubici1.ws.AerodromWS;
import org.foi.nwtis.lljubici1.ws.StatusKorisnika;

/**
 * Klasa je dretva koja provodi obradu zahtjeva.
 *
 * @author Luka Ljubicic
 */
public class DretvaZahtjeva extends Thread {

    private static int id;

    private long vrijemePrijema;

    /** Kraj sluzi za prepoznavanje je li dretva obavila ono sto treba. */
    static boolean kraj = false;
    /** Socket za spajanje. */
    //private Socket socket = null;

    private List<String> komande = new ArrayList<String>(); // splitana sa ';'.trim()
    private final ServerSocketDretva dretvaServer;
    private final Socket veza;
    private final ServletContext context;
    private String komanda;
    private String korisnickoIme;
    private String lozinka;
    private String zadnja;

    private String mojeKorisnickoIme;
    private String mojaLozinka;

    DretvaZahtjeva(Socket veza, ServerSocketDretva dretvaServer, ServletContext servletContext) {
        this.veza = veza;
        this.dretvaServer = dretvaServer;
        this.context = servletContext;

        this.mojeKorisnickoIme = SlusacAplikacije.konf.dajPostavku("korisnik.ime");
        this.mojaLozinka = SlusacAplikacije.konf.dajPostavku("korisnik.lozinka");
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }
    
    
    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        try {
            while (!kraj) {
                vrijemePrijema = System.currentTimeMillis();
                InputStream inputStream = veza.getInputStream();
                OutputStream outputStream = veza.getOutputStream();
                StringBuilder sb = citaj(inputStream);
                System.out.println("PRIMLJENO: " + sb);
                this.komanda = sb.toString();
                String odgovor = obradiZahtjev(sb.toString());
                pisi(outputStream, odgovor);
                this.veza.shutdownInput();
                this.veza.shutdownOutput();
                this.veza.close();
//                if (!kraj) {
//                    synchronized (this) {
//                        wait();
//                    }
//                }

            }
        } catch (IOException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Metoda koja salje odgovor na outputStream.
     *
     * @param outputStream outputStream na koji se zeli poruka poslat
     * @param odgovor odgovor koji zeli poslat
     * @throws java.io.IOException u slucaju greske
     */
    public void pisi(OutputStream outputStream, String odgovor) throws IOException {
//        if (!odgovor.startsWith("ERR")) {
//            JMS poruka = new JMS();
//            poruka.setKomanda(komanda);
//            poruka.setVrijeme(new Date());
//            //obradaPoruka.posaljiPoruku(poruka);
//        }
        outputStream.write(odgovor.getBytes());
        outputStream.flush();
    }

    /**
     * Metoda koja cita zahtjev korisnika primljenuna inputStream.
     *
     * @param inputStream inputStream sa kojeg se cita
     * @return vraca zahtjev korisnika
     * @throws java.io.IOException 
     */
    public StringBuilder citaj(InputStream inputStream) throws IOException {
        int znak;
        StringBuilder stringBuilder = new StringBuilder();
        while ((znak = inputStream.read()) != -1) {
            stringBuilder.append((char) znak);
        }
        System.out.println(this.getName() + " primljeno: " + stringBuilder.toString()); //stringBuilder - komanda korisnika
        return stringBuilder;
    }

    private void saljiPorukuJMS(String odgovor) {
        try {
            JsonObject jo = new JsonObject();
            jo.addProperty("id", id);
            jo.addProperty("komanda", odgovor);
            jo.addProperty("vrijeme", new SimpleDateFormat("dd.MM.yyyy HH.mm.ss.SSS").format(new Timestamp(System.currentTimeMillis())));
            this.id++;
            sendJMSMessageToNWTiS_lljubici1_1(jo.toString());
        } catch (JMSException | NamingException ex) {
            Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }


    private void postaviListuKomanda(String komanda) {
        this.komande = null;
        this.komande = new ArrayList<String>();
        String[] komande = komanda.split(";");
        for (String _komanda : komande) {
            this.komande.add(_komanda.trim());
        }
    }

    private String obradiZahtjev(String komanda) {
        postaviListuKomanda(komanda);
        this.korisnickoIme = null;
        this.lozinka = null;
        this.zadnja = null;
        for (String _komanda : this.komande) {
            if (_komanda.startsWith("KORISNIK ")) {
                this.korisnickoIme = _komanda.substring(9, _komanda.length());
            } else if (_komanda.startsWith("LOZINKA ")) {
                this.lozinka = _komanda.substring(8, _komanda.length());
            } else {
                this.zadnja = _komanda;//.substring(6, komanda.length()); //hmm
            }
        }
        return provjeriKorisnika(korisnickoIme, lozinka, zadnja);
    }

    private String provjeriKorisnika(String korisnickoIme, String Lozinka, String zadnja) {
        if (zadnja == null) {
            try {
                if (KomunikacijaBaza.autentikacijaKorisnika(korisnickoIme, Lozinka)) {
                    zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 10; PODACI U REDU", this.komanda);
                    saljiPorukuJMS("OK 10; PODACI U REDU");
                    return "OK 10; PODACI U REDU";
                }
            } catch (SQLException ex) {
                Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                if (KomunikacijaBaza.autentikacijaKorisnika(korisnickoIme, Lozinka)) {
                    //zovi metodu za zadnju komandu///////////////////////////////////////////////////
                    return obradaZadnjegZahtjevaKomande(zadnja);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "ERR 12;";
    }

    private String obradaZadnjegZahtjevaKomande(String zadnja) {
        if (zadnja.startsWith("GRUPA ")) { //&& !this.posluzitelj
            return obradiKomanduGrupa(zadnja);
        } else {
            return obradiKomanduServer(zadnja);
        }
    }

    public String obradiKomanduServer(String zadnja) {
        //!pauza => nije na pauzi
        //pauza => na pauzi
        switch (zadnja) {
            case "PAUZA":
                saljiPorukuJMS(obradiServerPauza());
                return obradiServerPauza();
            case "KRENI":
                saljiPorukuJMS(obradiServerKreni());
                return obradiServerKreni();
            case "PASIVNO":
                saljiPorukuJMS(obradiServerPasivno());
                return obradiServerPasivno();
            case "AKTIVNO":
                saljiPorukuJMS(obradiServerAktivno());
                return obradiServerAktivno();
            case "STANI":
                saljiPorukuJMS(obradiServerStani());
                return obradiServerStani();
            case "STANJE":
                saljiPorukuJMS(obradiServerStanje());
                return obradiServerStanje();
            default:
                break;
        }
        return null;
    }

    public String obradiServerStanje() {
        boolean pauza;
        boolean krajServerSocket;
        pauza = PreuzimanjeAviona.isPauza();
        krajServerSocket = dretvaServer.isPreuzmiKomandu();
        if (!krajServerSocket) {
            if (!pauza) {
                zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 11; PREUZIMA SVE KOMANDE I PREUZIMA PODATKE ZA AERODROME", this.komanda);
                return "OK 11; PREUZIMA SVE KOMANDE I PREUZIMA PODATKE ZA AERODROME";
            } else {
                zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 12; PREUZIMA SVE KOMANDE I NE PREUZIMA PODATKE ZA AERODROME", this.komanda);
                return "OK 12; PREUZIMA SVE KOMANDE I NE PREUZIMA PODATKE ZA AERODROME";
            }
        } else {
            if (!pauza) {
                zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 13; PREUZIMA SAMO POSLUŽITELJSKE KOMANDE I PREUZIMA PODATKE ZA AERODROME", this.komanda);
                return "OK 13; PREUZIMA SAMO POSLUŽITELJSKE KOMANDE I PREUZIMA PODATKE ZA AERODROME;";
            } else {
                zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 14; PREUZIMA SAMO POSLUŽITELJSKE KOMANDE I NE PREUZIMA PODATKE ZA AERODROME;", this.komanda);
                return "OK 14; PREUZIMA SAMO POSLUŽITELJSKE KOMANDE I NE PREUZIMA PODATKE ZA AERODROME;";
            }
        }
    }

    public String obradiServerStani() {
        boolean krajPreuzimanjeAviona;
        boolean krajServerSocket;
        krajPreuzimanjeAviona = PreuzimanjeAviona.kraj;
        krajServerSocket = this.dretvaServer.isKraj();
        if (krajPreuzimanjeAviona || krajServerSocket) {
            zapisiUDnevnik(korisnickoIme, "SOCKET", "ERR 16; BIO JE U POSTUPKU PREKIDA", this.komanda);
            return "ERR 16; BIO JE U POSTUPKU PREKIDA";
        } else {
            PreuzimanjeAviona.kraj = true;
            if (dretvaServer.getPreuzimanjeAvionaDretva().getState().equals(Thread.State.TIMED_WAITING)) {
                dretvaServer.getPreuzimanjeAvionaDretva().interrupt();
            }
            //PROVJERI
            dretvaServer.setKraj(true);
            zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 10; NIJE BIO U POSTUPKU PREKIDA", this.komanda);
            return "OK 10; NIJE BIO U POSTUPKU PREKIDA";
        }
    }

    public String obradiServerAktivno() {
        boolean pauza;
        pauza = PreuzimanjeAviona.isPauza();
        if (pauza) {
            PreuzimanjeAviona.setPauza(false);
            zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 10; BIO JE U PASIVNOM RADU", this.komanda);
            return "OK 10; BIO JE U PASIVNOM RADU";
        } else {
            zapisiUDnevnik(korisnickoIme, "SOCKET", "ERR 15; BIO JE U AKTIVNOM RADU", this.komanda);
            return "ERR 15; BIO JE U AKTIVNOM RADU";
        }
    }

    public String obradiServerPasivno() {
        boolean pauza;
        pauza = PreuzimanjeAviona.isPauza();
        if (!pauza) {
            PreuzimanjeAviona.setPauza(true);
            zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 10; BIO JE U PAUZI", this.komanda);
            return "OK 10; BIO JE U PAUZI";
        } else {
            zapisiUDnevnik(korisnickoIme, "SOCKET", "ERR 14; BIO JE U PASIVNOM RADU", this.komanda);
            return "ERR 14; BIO JE U PASIVNOM RADU";
        }
    }

    public String obradiServerKreni() {
        boolean pauza;
        pauza = this.dretvaServer.isPreuzmiKomandu();
        if (!pauza) {

            zapisiUDnevnik(korisnickoIme, "SOCKET", "ERR 13; NIJE BIO NA PAUZI", this.komanda);
            return "ERR 13; NIJE BIO NA PAUZI";
        }
        dretvaServer.setPreuzmiKomandu(false); //PreuzimanjeAviona.setPauza(true);
        zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 10; BIO JE U PAUZI", this.komanda);
        return "OK 10; BIO JE U PAUZI";
    }

    public String obradiServerPauza() {
        boolean pauza;
        pauza = dretvaServer.isPreuzmiKomandu(); //PreuzimanjeAviona.isPauza();
        if (pauza) {
            zapisiUDnevnik(korisnickoIme, "SOCKET", "ERR 12; BIO JE NA PAUZI!", this.komanda);
            return "ERR 12; BIO JE NA PAUZI!";
        }
        dretvaServer.setPreuzmiKomandu(true);
        //PreuzimanjeAviona.setPauza(true);
        zapisiUDnevnik(korisnickoIme, "SOCKET", "OK 10; NIJE BIO U PAUZI", this.komanda);
        return "OK 10; NIJE BIO U PAUZI";
    }

    public String obradiKomanduGrupa(String zadnja) {
        if (!this.dretvaServer.isPreuzmiKomandu()) {
            String grupaKomanda = zadnja.substring(6, zadnja.length());
            switch (grupaKomanda) {
                case "DODAJ":
                    return obradiGrupuDodaj();
                case "PREKID":
                    return obradiGrupuPrekid();
                case "KRENI":
                    return obradiGrupuKreni();
                case "PAUZA":
                    return obradiGrupuPauza();
                case "STANJE":
                    return obradiGrupuStanje();
                default:
                    break;
            }
        } else {
            return "ERR Blokirano!";
        }
        return null;
    }

    public void posaljiJMSPoruku() {

    }

    public String obradiGrupuKreni() {
        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
//        if (status == StatusKorisnika.PASIVAN) {
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 21; GRUPA NE POSTOJI", this.komanda);
//            return "ERR 21; GRUPA NE POSTOJI";
//        } else if (status == StatusKorisnika.AKTIVAN) {
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 22; GRUPA JE BILA AKTIVNA", this.komanda);
//            return "ERR 22; GRUPA JE BILA AKTIVNA";
//        } else if (AerodromWS.aktivirajGrupu(mojeKorisnickoIme, mojaLozinka)) {
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 20; GRUPA NIJE BILA AKTIVNA", this.komanda);
//            return "OK 20; GRUPA NIJE BILA AKTIVNA";
//        }
//        zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 22; GRUPA JE BILA AKTIVNA", this.komanda);
//        return "ERR 22; GRUPA JE BILA AKTIVNA";

        if (status == StatusKorisnika.NEPOSTOJI || status == StatusKorisnika.DEREGISTRIRAN) {
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 21; GRUPA NE POSTOJI", this.komanda);
            return "ERR 21; GRUPA NE POSTOJI";
        } else if (status == StatusKorisnika.AKTIVAN || status == StatusKorisnika.REGISTRIRAN) {
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 22; GRUPA JE BILA AKTIVNA", this.komanda);
            return "ERR 22; GRUPA JE BILA AKTIVNA";
        } else if (status != StatusKorisnika.AKTIVAN) {
            AerodromWS.aktivirajGrupu(mojeKorisnickoIme, mojaLozinka);
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 20; GRUPA NIJE BILA AKTIVNA", this.komanda);
            return "OK 20; GRUPA NIJE BILA AKTIVNA";
        }
        zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR KRENI; STATUS:: " + status.value(), this.komanda);
        return "ERR KRENI; STATUS:: " + status.value();
    }

    public String obradiGrupuStanje() {
//        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
//        if (status == StatusKorisnika.AKTIVAN) { //status.value().equals("AKTIVAN")
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 21; Grupa aktivna", this.komanda);
//            return "OK 21; Grupa aktivna";
//        } else if (status == StatusKorisnika.BLOKIRAN) { //status.value().equals("BLOKIRAN")
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 22; Grupa blokirana", this.komanda);
//            return "OK 22; Grupa blokirana";
//        } else if (status == StatusKorisnika.NEPOSTOJI) { //status.value().equals("NEPOSTOJI")
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 21; Grupa ne postoji", this.komanda);
//            return "OK 21; Grupa ne postoji";
//        }
//        return null;
        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
        if (status == StatusKorisnika.NEPOSTOJI || status == StatusKorisnika.DEREGISTRIRAN) {
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 21; GRUPA NE POSTOJI", this.komanda);
            return "ERR 21; GRUPA NE POSTOJI";
        } else if (status == StatusKorisnika.AKTIVAN || status == StatusKorisnika.REGISTRIRAN) {
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 21; GRUPA JE AKTIVNA", this.komanda);
            return "OK 21; GRUPA JE AKTIVNA";
        } else if (status == StatusKorisnika.BLOKIRAN || status == StatusKorisnika.NEAKTIVAN) {
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 22; GRUPA JE BLOKIRANA", this.komanda);
            return "OK 22; GRUPA JE BLOKIRANA";
        }
        zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR STANJE; STATUS:: " + status.value(), this.komanda);
        return "ERR STANJE; STATUS:: " + status.value();
    }

    public String obradiGrupuPrekid() {
        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
        if (status == StatusKorisnika.REGISTRIRAN || status == StatusKorisnika.AKTIVAN) { ///ZADNJE STA SAM
            if (AerodromWS.deregistrirajGrupu(mojeKorisnickoIme, mojaLozinka)) {
                zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 20; GRUPA JE BILA REGISTRIRANA", this.komanda);
                return "OK 20; GRUPA JE BILA REGISTRIRANA";
            }
        }
        zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 21; GRUPA NIJE BILA REGISTRIRANA", this.komanda);
        return "ERR 21; GRUPA NIJE BILA REGISTRIRANA";
    }

    public String obradiGrupuDodaj() {
        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
        if (status != StatusKorisnika.REGISTRIRAN) {
            if (AerodromWS.registrirajGrupu(mojeKorisnickoIme, mojaLozinka)) {
                zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 20; GRUPA NIJE BILA REGISTRIRANA", this.komanda);
                return "OK 20; GRUPA NIJE BILA REGISTRIRANA";
            }
        }
        zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 20; BILA JE REGISTRIRANA", this.komanda);
        return "ERR 20; BILA JE REGISTRIRANA";
        //return "ERR autenticirajGrupu;";
    }

    public String obradiGrupuPauza() { //?
        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
        if (status == StatusKorisnika.AKTIVAN || status == StatusKorisnika.REGISTRIRAN) { //status.value().equals("AKTIVAN")
            if (AerodromWS.blokirajGrupu(mojeKorisnickoIme, mojaLozinka)) {
                zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 20; GRUPA JE BILA AKTIVNA", this.komanda);
                return "OK 20; GRUPA JE BILA AKTIVNA";
            }
        } else if (status == StatusKorisnika.NEPOSTOJI || status == StatusKorisnika.DEREGISTRIRAN) { //status.value().equals("NEPOSTOJI")
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 21; GRUPA NE POSTOJI", this.komanda);
            return "ERR 21; GRUPA NE POSTOJI";
        } else if (status != StatusKorisnika.AKTIVAN) { //status.value().equals("NEAKTIVAN") //!==!=!=!=!=!!?=!?!=?=!?=!?=?!=?!=?=!?=!?=?!=?!=?=!?=!?=!?=?!=?!=
            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 23; GRUPA NIJE BILA AKTIVNA", this.komanda);
            return "ERR 23; GRUPA NIJE BILA AKTIVNA";
        }
        zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR PAUZA; STATUS:: " + status.value(), this.komanda);
        return "ERR PAUZA; STATUS:: " + status.value();
//        StatusKorisnika status = AerodromWS.dajStatusGrupe(mojeKorisnickoIme, mojaLozinka);
//        if (status == StatusKorisnika.PASIVAN) { //status.value().equals("AKTIVAN")
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 21; GRUPA NE POSTOJI", this.komanda);
//            return "ERR 21; GRUPA NE POSTOJI";
//        } else if (status == StatusKorisnika.NEAKTIVAN) { //status.value().equals("NEAKTIVAN")
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "ERR 23; GRUPA NIJE BILA AKTIVNA", this.komanda);
//            return "ERR 23; GRUPA NIJE BILA AKTIVNA";
//        } else if (AerodromWS.blokirajGrupu(mojeKorisnickoIme, mojaLozinka)) {
//            zapisiUDnevnik(mojeKorisnickoIme, "SOCKET", "OK 21; GRUPA GRUPA JE BILA AKTIVNA", this.komanda);
//            return "OK 21; GRUPA GRUPA JE BILA AKTIVNA";
//        }
//        return "ERR PAUZA; STATUS: " + status.value();
    }

    private void zapisiUDnevnik(String korisnickoIme, String vrstaZapisa, String odgovor, String naredba) {
        Timestamp vrijemeUnosa = new Timestamp(System.currentTimeMillis());
        long trajanjeObrade = System.currentTimeMillis() - vrijemePrijema;

        KomunikacijaBaza.zapisiUDnevnik(this.veza.getInetAddress().toString(),
                korisnickoIme, vrstaZapisa, naredba, odgovor, vrijemeUnosa, trajanjeObrade);
    }

    private Message createJMSMessageForjmsNWTiS_lljubici1_1(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        TextMessage tm = session.createTextMessage();
        tm.setText(messageData.toString());
        return tm;
    }

    private void sendJMSMessageToNWTiS_lljubici1_1(String messageData) throws JMSException, NamingException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("java:comp/env/jms/NWTiS_lljubici1_1_factory");
        Connection conn = null;
        Session s = null;
        try {
            conn = cf.createConnection();
            s = conn.createSession(false, s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("java:comp/env/jms/NWTiS_lljubici1_1");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForjmsNWTiS_lljubici1_1(s, messageData));
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    
}
