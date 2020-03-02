package org.foi.nwtis.lljubici1.web;

/**
 * Klasa za format poruke RestOdgovorERR kojeg koristimo u REST servisu.
 * 
 * @author Luka Ljubičić
 */

public class RestOdgovorERR {
    /** status ERR*/
    private final String status = "ERR";
    /** poruka - odgovor u slučaju greške */
    private String poruka;

    public RestOdgovorERR() {
    }
    
    
    public RestOdgovorERR(String poruka){
        this.poruka = poruka;
    }

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }
}
