/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.lljubici1.KomunikacijaBaza;

/**
 *
 * @author root
 */
@Named(value = "prijavaKorisnika")
@SessionScoped
public class prijavaKorisnika implements Serializable {
    private String korisnickoIme;
    private String lozinka;
    private String poruka = null;

    public prijavaKorisnika() {
    }
    
    public String provjeriPrijavu(){
        try {
            if(KomunikacijaBaza.autentikacijaKorisnika(korisnickoIme, lozinka)){
                return "index.xhtml";
            }
        } catch (SQLException ex) {
            Logger.getLogger(prijavaKorisnika.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.poruka = "Pogresno korisnicko ime ili lozinka!";
        return "";
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getPoruka() {
        return poruka;
    }
    
}
