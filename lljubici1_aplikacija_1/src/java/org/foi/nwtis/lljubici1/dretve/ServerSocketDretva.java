package org.foi.nwtis.lljubici1.dretve;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.foi.nwtis.lljubici1.web.slusaci.SlusacAplikacije;

/**
 * Klasa je 
 *
 * @author Luka Ljubicic
 */
public class ServerSocketDretva extends Thread {
    private ServerSocket serverSocket;
    private final ServletContext servletContext;
    private PreuzimanjeAviona preuzimanjeAvionaDretva;
    private final int port;
    private boolean kraj = false;
    
    private boolean preuzmiKomandu;
    
    
    public ServerSocketDretva(ServletContext servletContext, PreuzimanjeAviona preuzimanjeAvionaDretva) {
        this.servletContext = servletContext;
        this.preuzimanjeAvionaDretva = preuzimanjeAvionaDretva;
        this.port = Integer.valueOf(SlusacAplikacije.konf.dajPostavku("port"));
    }
    
    @Override
    public void interrupt() {
        System.out.println("Zaustavljanje server socketa...");
        kraj = true;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Pogreska kod zaustavljanja server socketa!");
        }
    }

    @Override
    public void run() {
         try {
            System.out.println("Dosao u socket server!");
            serverSocket = new ServerSocket(port);
            while(kraj==false){
                Socket veza = serverSocket.accept();
                System.out.println("NOVI ZAHTJEV DOSAO!");
                DretvaZahtjeva obrada = new DretvaZahtjeva(veza, this, servletContext);
                obrada.setName("Dretva Zahtjeva");
                obrada.start();
                
            }
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Dretva Socket Server završava");
    }

    @Override
    public synchronized void start() {
        System.out.println("ServerSocketDretva - pokrenuta");
        super.start();
    }

    public boolean isKraj() {
        return kraj;
    }

    public void setKraj(boolean kraj) {
        System.out.println("Zaustavljanje server socketa!");
        this.preuzimanjeAvionaDretva = null;
        this.kraj = kraj;
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketDretva.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PreuzimanjeAviona getPreuzimanjeAvionaDretva() {
        return preuzimanjeAvionaDretva;
    }

    public boolean isPreuzmiKomandu() {
        return preuzmiKomandu;
    }

    public void setPreuzmiKomandu(boolean preuzmiKomandu) {
        this.preuzmiKomandu = preuzmiKomandu;
    }



    
}
