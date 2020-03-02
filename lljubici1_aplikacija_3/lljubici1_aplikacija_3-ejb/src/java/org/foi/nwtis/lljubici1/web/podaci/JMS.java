package org.foi.nwtis.lljubici1.web.podaci;

/**
 * Klasa za 
 * 
 * @author Luka Ljubičić
 */

public class JMS {
    private int id;
    private String komanda;
    private String vrijeme;

    public JMS() {
    }

    public JMS(int id, String komanda, String vrijeme) {
        this.id = id;
        this.komanda = komanda;
        this.vrijeme = vrijeme;
    }
    
    public String getKomanda() {
        return komanda;
    }

    public void setKomanda(String komanda) {
        this.komanda = komanda;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }
    
}
