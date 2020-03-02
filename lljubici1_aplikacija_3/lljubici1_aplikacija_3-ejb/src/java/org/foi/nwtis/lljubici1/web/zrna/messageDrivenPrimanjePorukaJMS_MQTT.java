/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.lljubici1.web.zrna;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.foi.nwtis.lljubici1.web.podaci.JMS;

/**
 *
 * @author root
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NWTiS_lljubici1_1"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class messageDrivenPrimanjePorukaJMS_MQTT implements MessageListener {
    
    @EJB
    private singletonPrimljeneJMSporuke jmsSingleton;
    
    public messageDrivenPrimanjePorukaJMS_MQTT() {
    }
    
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = null;
        if(message instanceof TextMessage){
            try {
                textMessage = (TextMessage) message;
                JsonObject jo = new JsonParser().parse(textMessage.getText()).getAsJsonObject();
                JMS jmsPoruka = new JMS(jo.get("id").getAsInt(), jo.get("komanda").getAsString(), jo.get("vrijeme").getAsString());
                jmsSingleton.getJmsPoruke().add(jmsPoruka);
            } catch (JMSException ex) {
                Logger.getLogger(messageDrivenPrimanjePorukaJMS_MQTT.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
}
