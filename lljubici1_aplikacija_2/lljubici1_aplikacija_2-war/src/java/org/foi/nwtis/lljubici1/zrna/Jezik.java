package org.foi.nwtis.lljubici1.zrna;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Klasa za promjenu jezika.
 * 
 * @author Luka Ljubičić
 */

@Named(value = "jezik")
@SessionScoped
public class Jezik implements Serializable{
    private String jezik = "hr";

    public Jezik() {
    }

    public String getJezik() {
        return jezik;
    }

    public void setJezik(String jezik) {
        this.jezik = jezik;
    }
    
}
