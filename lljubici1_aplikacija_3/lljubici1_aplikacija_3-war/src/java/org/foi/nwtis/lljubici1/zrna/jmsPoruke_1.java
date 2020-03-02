/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import org.foi.nwtis.lljubici1.web.podaci.JMS;
import org.foi.nwtis.lljubici1.web.zrna.singletonPrimljeneJMSporuke;

/**
 *
 * @author root
 */
@Named(value = "jmsPoruke_1")
@SessionScoped
@ManagedBean
public class jmsPoruke_1 implements Serializable {

    @EJB
    private singletonPrimljeneJMSporuke singletonPrimljeneJMSporuke;

    private List<JMS> listaPoruka;
    private JMS odabranaPoruka;

    public jmsPoruke_1() {

    }

    public List<JMS> getListaPoruka() {
        listaPoruka = singletonPrimljeneJMSporuke.getJmsPoruke();
        return listaPoruka;
    }

    public JMS getOdabranaPoruka() {
        return odabranaPoruka;
    }

    public void setOdabranaPoruka(JMS odabranaPoruka) {
        this.odabranaPoruka = odabranaPoruka;
    }

    public void obrisiPoruku() {
        if (odabranaPoruka != null) {
            listaPoruka.remove(odabranaPoruka);
            getListaPoruka();
        }
    }

}
