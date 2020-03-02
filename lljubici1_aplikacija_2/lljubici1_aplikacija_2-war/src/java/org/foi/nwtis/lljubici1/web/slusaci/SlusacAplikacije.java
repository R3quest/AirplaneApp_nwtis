package org.foi.nwtis.lljubici1.web.slusaci;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.lljubici1.konfiguracije.Konfiguracija;
import org.foi.nwtis.lljubici1.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.lljubici1.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.lljubici1.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.lljubici1.konfiguracije.bp.BP_Konfiguracija;

/**
 * Klasa SlusacAplikacije inicijalizira parametre, bazu i konfiguracijske datoteke.
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    private static ServletContext sc;
    public static Konfiguracija konf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datoteka = putanja + File.separator + sc.getInitParameter("konfiguracija");
        String konfiguracijaApp = putanja + File.separator + sc.getInitParameter("konfiguracijaApp");
        try {
            inicijalizacijaKonfiguracija(datoteka, konfiguracijaApp);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        //String korisnik = (String) konfApp.get("OpenSkyNetwork.korisnik");
        

    /**
     * Metoda inicijalizira konfiguraciju i radi instancu singletone klase Baza.
     * 
     * @param datoteka putanja i naziv konfiguracijske datoteke
     * @param konfiguracijaApp putanja i naziv konfiguracijske datoteke aplikacije
     * @return objekt tipa Properties
     * @throws org.foi.nwtis.lljubici1.konfiguracije.NemaKonfiguracije u slučaju da nema konfiguracije
     * @throws org.foi.nwtis.lljubici1.konfiguracije.NeispravnaKonfiguracija u slučaju da nešto ne štima sa konfiguracijom
     */
    public void inicijalizacijaKonfiguracija(String datoteka, String konfiguracijaApp) throws NemaKonfiguracije, NeispravnaKonfiguracija {
        BP_Konfiguracija bpk = new BP_Konfiguracija(datoteka);
        sc.setAttribute("BP_Konfig", bpk);
        konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(konfiguracijaApp);
        Properties konfApp = konf.dajSvePostavke();
        sc.setAttribute("konfiguracijaApp", konfApp);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
        sc.removeAttribute("konfiguracijaApp");
    }

    public static ServletContext getSc() {
        return sc;
    }
}
