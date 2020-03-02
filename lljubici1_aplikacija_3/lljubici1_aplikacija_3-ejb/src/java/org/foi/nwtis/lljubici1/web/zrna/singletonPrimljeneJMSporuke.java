package org.foi.nwtis.lljubici1.web.zrna;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import org.foi.nwtis.lljubici1.web.podaci.JMS;

@Startup
@Singleton
@LocalBean
public class singletonPrimljeneJMSporuke {

    private List<JMS> jmsPoruke = new ArrayList<>();

    public List<JMS> getJmsPoruke() {
        return jmsPoruke;
    }

    public void setJmsPoruke(List<JMS> jmsPoruke) {
        this.jmsPoruke = jmsPoruke;
    }
    
}
