package org.foi.nwtis.lljubici1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.lljubici1.konfiguracije.bp.BP_Konfiguracija;

/**
 * Singletone klasa za komunikaciju s bazom (izvršavanje upita).
 * 
 * @author Luka Ljubičić
 */
public class Baza {

    private static Baza instancaBaze;
    private static Connection konekcija;

    private Statement statement;
    private static BP_Konfiguracija konfiguracija;

    public Baza(BP_Konfiguracija konfiguracija) {
        this.konfiguracija = konfiguracija;
        postaviKonekciju(konfiguracija);
    }

    private Baza() {
        
    }

    public BP_Konfiguracija getKonfiguracija() {
        return konfiguracija;
    }

    /**
    * Metoda postavlja konekciju.
    */
    private void postaviKonekciju(BP_Konfiguracija konfiguracija) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
            String url = konfiguracija.getServerDatabase() + konfiguracija.getUserDatabase();
            String korisnik = konfiguracija.getUserUsername();
            String lozinka = konfiguracija.getUserPassword();

            this.konekcija = DriverManager.getConnection(url, korisnik, lozinka);

        } catch (SQLException ex) {
            Logger.getLogger(Baza.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Baza.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Baza getInstancaBaze() {
        return instancaBaze;
    }
    
    /**
     * Getter za instancu baze, postavlja novu instancu ako je null inače vraća postojeću.
     * @param konfiguracija konfiguracijska datoteka
     * @return instanca baze (referenca na singletone klasu)
    */
    public synchronized static Baza getInstancaBaze(BP_Konfiguracija konfiguracija) {
        if (instancaBaze == null) {
            instancaBaze = new Baza(konfiguracija);
        }
        return instancaBaze;
    }

    /**
     * Metoda izvršava dati SQL upit i vraća rezultate tipa ResultSet.
     * 
     * @param upit SQL upit koji se treba izvršiti
     * @return podaci (ResultSet)
     * @throws java.sql.SQLException u slučaju izvršavanja upita
    */
    public ResultSet izvrsiUpit(String upit) throws SQLException {
        statement = konekcija.createStatement();
        return statement.executeQuery(upit);
    }

    /**
     * Metoda izvršava dati SQL upit i vraća broj zahvačenih redaka.
     * @param upit SQL upit koji se treba izvršiti
     * @return broj zahvačenih redaka
     * @throws java.sql.SQLException u slučaju greške izvršavanja upita
    */
    public int izvrsiUpitVratiBrojRedaka(String upit) throws SQLException {
        statement = konekcija.createStatement();
        return statement.executeUpdate(upit);
    }

}
