/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.servlet.http.HttpSession;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.foi.nwtis.lljubici1.web.zrna.statefulAutenticiranjeKorisnika;

/**
 *
 * @author root
 */
@Named(value = "prijavaKorisnika")
@SessionScoped
@ManagedBean
public class prijavaKorisnika implements Serializable {
    private String korisnickoIme;
    private String lozinka;
    private String poruka = null;
    
    
    @EJB
    private statefulAutenticiranjeKorisnika statefulAutenticiranjeKorisnika;

    public prijavaKorisnika() {
    }
    
    public String provjeriPrijavu(){
       if(statefulAutenticiranjeKorisnika.provjeriPrijavu(korisnickoIme, lozinka)){
           postaviSesiju();
            return "index.xhtml";
        }
        this.poruka = "Netocno korisnicko ime ili lozinka!";
        return "";
    }
    
    private void postaviSesiju(){
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        sesija.setAttribute("korisnickoIme", this.korisnickoIme);
        sesija.setAttribute("lozinka", this.lozinka);
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
