package org.foi.nwtis.lljubici1.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.lljubici1.KomunikacijaBaza;
import org.foi.nwtis.lljubici1.web.podaci.Dnevnik;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;

@Named(value = "pregledDnevnikaRada")
@SessionScoped
public class pregledDnevnikaRada implements Serializable {

    private int brojLinijaPoStranici;
    private List<Dnevnik> listaDnevnika;
    private Dnevnik odabraniDnevnik;

    public pregledDnevnikaRada() {
        dohvatiListuDnevnika();
        brojLinijaPoStranici = Integer.parseInt(dohvatiVrijednostKonfiguracije("broj.linija.dnevnik"));
    }
    
    private String dohvatiListuDnevnika(){
        try {
            this.listaDnevnika = KomunikacijaBaza.dohvatiSveDnevnike();
        } catch (SQLException ex) {
            Logger.getLogger(pregledKorisnika.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private ServletContext getServletContext() {
        return SlusacAplikacije.getSc();
    }

    /**
     * Dohvaća vrijednosti konfiguracije.
     */
    private String dohvatiVrijednostKonfiguracije(String kljuc) {
        Properties konfiguracijaApp = (Properties) getServletContext().getAttribute("konfiguracijaApp");
        return konfiguracijaApp.getProperty(kljuc);
    }
    
    

    public List<Dnevnik> getListaDnevnika() {
        return listaDnevnika;
    }

    public Dnevnik getOdabraniDnevnik() {
        return odabraniDnevnik;
    }

    public int getBrojLinijaPoStranici() {
        return brojLinijaPoStranici;
    }

}
