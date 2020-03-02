package org.foi.nwtis.lljubici1.web.slusaci;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.lljubici1.Baza;
import org.foi.nwtis.lljubici1.konfiguracije.Konfiguracija;
import org.foi.nwtis.lljubici1.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.lljubici1.dretve.PreuzimanjeAviona;
import org.foi.nwtis.lljubici1.dretve.ServerSocketDretva;
import org.foi.nwtis.lljubici1.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.lljubici1.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.lljubici1.konfiguracije.bp.BP_Konfiguracija;

/**
 * Klasa SlusacAplikacije inicijalizira parametre, bazu i konfiguracijske datoteke.
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc;
    PreuzimanjeAviona preuzimanjeAviona;
    public static Konfiguracija konf;
    private ServerSocketDretva serverSocketDretva;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datoteka = putanja + File.separator + sc.getInitParameter("konfiguracija");
        String konfiguracijaApp = putanja + File.separator + sc.getInitParameter("konfiguracijaApp");
        try {
            Properties konfApp = inicijalizacijaKonfiguracijaIBaze(datoteka, konfiguracijaApp);
            preuzmiAvionePeriodicki(konfApp);
            pokreniServerSocketDretvu();
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void pokreniServerSocketDretvu() {
        this.serverSocketDretva = new ServerSocketDretva(sc, preuzimanjeAviona);
        serverSocketDretva.setName("Socket Server Dretva");
        serverSocketDretva.start();
    }

    /**
     * Metoda preuzmiAvionePeriodicki pokreće dretvu za preuzimanje aerodroma (Klasa PreuzimanjeAerodroma).
     * 
     * @param konfApp ključ, vrijednost iz konfiguracijske datoteke
     */
    public void preuzmiAvionePeriodicki(Properties konfApp) {
        String korisnik = (String) konfApp.get("OpenSkyNetwork.korisnik");
        String lozinka = (String) konfApp.get("OpenSkyNetwork.lozinka");
        int inicijalniPocetak = Integer.parseInt((String) konfApp.get("preuzimanje.pocetak"));
        int preuzimanjeCiklus = Integer.parseInt((String) konfApp.get("preuzimanje.ciklus"));
        int preuzimanjeTrajanje = Integer.parseInt((String) konfApp.get("preuzimanje.trajanje"));
        String putanja = sc.getRealPath("/WEB-INF").substring(0, sc.getRealPath("/WEB-INF").length() - 17) + "web" + File.separator + "WEB-INF" + File.separator;
        preuzimanjeAviona = new PreuzimanjeAviona(korisnik, lozinka, inicijalniPocetak, preuzimanjeTrajanje, preuzimanjeCiklus, putanja);
        preuzimanjeAviona.start();
    }

    /**
     * Metoda inicijalizira konfiguraciju i radi instancu singletone klase Baza.
     * 
     * @param datoteka putanja i naziv konfiguracijske datoteke
     * @param konfiguracijaApp putanja i naziv konfiguracijske datoteke aplikacije
     * @return objekt tipa Properties
     * @throws org.foi.nwtis.lljubici1.konfiguracije.NemaKonfiguracije u slučaju da nema konfiguracije
     * @throws org.foi.nwtis.lljubici1.konfiguracije.NeispravnaKonfiguracija u slučaju da nešto ne štima sa konfiguracijom
     */
    public Properties inicijalizacijaKonfiguracijaIBaze(String datoteka, String konfiguracijaApp) throws NemaKonfiguracije, NeispravnaKonfiguracija {
        BP_Konfiguracija bpk = new BP_Konfiguracija(datoteka);
        sc.setAttribute("BP_Konfig", bpk);
        konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(konfiguracijaApp);
        Properties konfApp = konf.dajSvePostavke();
        sc.setAttribute("konfiguracijaApp", konfApp);
        Baza baza = Baza.getInstancaBaze(bpk);
        return konfApp;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (preuzimanjeAviona != null) {
            preuzimanjeAviona.interrupt();
        }
//        if(serverSocketDretva != null){
//            serverSocketDretva.interrupt();
//        }
        sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
        sc.removeAttribute("konfiguracijaApp");
    }

    public static ServletContext getSc() {
        return sc;
    }
}
