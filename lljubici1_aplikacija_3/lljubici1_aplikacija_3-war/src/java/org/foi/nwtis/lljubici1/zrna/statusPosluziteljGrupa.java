/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.zrna;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
//import org.foi.nwtis.lljubici1.rest.REST_Korisnici;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;

/**
 *
 * @author root
 */
@Named(value = "statusPosluziteljGrupa")
@SessionScoped
@ManagedBean
public class statusPosluziteljGrupa implements Serializable {

    private List<String> listaKomandi = new ArrayList<>();
    private List<String> listaKomandiGrupa = new ArrayList<>();
    private String komanda;
    private String komandaGrupa;
    private String statusPosluzitelja;
    private String statusGrupe;

    private void inicijalizirajListuKomandi() {
        HttpSession sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        String korisnickoIme = (String) sesija.getAttribute("korisnickoIme");
        String lozinka = (String) sesija.getAttribute("lozinka");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + ";");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; PAUZA;");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; KRENI;");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; PASIVNO;");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; AKTIVNO;");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; STANI;");
        listaKomandi.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; STANJE;");
        
        listaKomandiGrupa.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + ";");
        listaKomandiGrupa.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA DODAJ;");
        listaKomandiGrupa.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA PREKID;");
        listaKomandiGrupa.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA KRENI;");
        listaKomandiGrupa.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA PAUZA;");
        listaKomandiGrupa.add("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; GRUPA STANJE;");
    }

    public statusPosluziteljGrupa() {
        inicijalizirajListuKomandi();
    }

    public List<String> getListaKomandi() {
        return listaKomandi;
    }

    public String getKomanda() {
        return komanda;
    }

    public void setKomanda(String komanda) {
        this.komanda = komanda;
    }

    public String posaljiKomandu() {
        String odgovor = posaljiPoruku(this.komanda);
        this.statusPosluzitelja = odgovor;
        return "";
    }
    
    public String posaljiKomanduGrupa(){
        String odgovor = posaljiPoruku(this.komandaGrupa);
        this.statusGrupe = odgovor;
        return "";
    }

    private String posaljiPoruku(String poruka) {
        String odgovor = "";
        StringBuilder stringBuilder = null;
        int port = Integer.parseInt(SlusacAplikacije.konf.dajPostavku("port1"));
        try {
            Socket socket = new Socket(InetAddress.getByName("localhost"), port);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            pisi(outputStream, poruka);
            socket.shutdownOutput();
            stringBuilder = citaj(inputStream);
            socket.shutdownInput();
            socket.close();
            odgovor = stringBuilder.toString();
        } catch (IOException ex) {
            //Logger.getLogger(REST_Korisnici.class.getName()).log(Level.SEVERE, null, ex);
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

    public String getStatusPosluzitelja() {
        return statusPosluzitelja;
    }

    public List<String> getListaKomandiGrupa() {
        return listaKomandiGrupa;
    }

    public String getKomandaGrupa() {
        return komandaGrupa;
    }

    public void setKomandaGrupa(String komandaGrupa) {
        this.komandaGrupa = komandaGrupa;
    }

    public String getStatusGrupe() {
        return statusGrupe;
    }

}
