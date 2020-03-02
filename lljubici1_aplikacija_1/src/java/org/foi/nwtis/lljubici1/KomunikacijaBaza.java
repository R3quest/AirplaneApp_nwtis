package org.foi.nwtis.lljubici1;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.lljubici1.web.podaci.Aerodrom;
import org.foi.nwtis.lljubici1.web.podaci.Dnevnik;
import org.foi.nwtis.lljubici1.web.podaci.Korisnik;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa za 
 * 
 * @author Luka Ljubičić
 */
public class KomunikacijaBaza {

    /**
     * Metoda dohvaća aerodrom za dati icao iz tablice Airports bez lokacije.
     *
     * @param icao za kojeg želimo dohvatiti vrijednosti
     * @return vraća objekt tipa Aerodrom
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static Aerodrom dohvatiAirportsAerodrom(String icao) throws SQLException {
        Aerodrom aerodrom = null;
        String upit = "SELECT * FROM AIRPORTS WHERE ident='" + icao + "'";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            aerodrom = new Aerodrom(resultSet.getString("ident"), resultSet.getString("name"), resultSet.getString("iso_country"), null);
        }
        return aerodrom;
    }
    
    
    public static List<Aerodrom> dohvatisveAirportsAerodrom() throws SQLException {
        List<Aerodrom> aerodromi = new ArrayList<>();
        String upit = "SELECT * FROM AIRPORTS";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            String[] koordinate = resultSet.getString("coordinates").split(",");
            Aerodrom aerodrom = new Aerodrom(resultSet.getString("ident"), resultSet.getString("name"), resultSet.getString("iso_country"), 
                     new Lokacija(koordinate[0].trim(), koordinate[1].trim()));
            if(aerodrom.getLokacija() != null){
                aerodromi.add(aerodrom);
            }
        }
        return aerodromi;
    }
    

    /**
     * Metoda dohvaća aerodrom za dati icao iz tablice Airports sa lokacijom klase LIQKlijent.
     *
     * @param icao za koji želimo dohvatiti vrijednosti
     * @param token LocationIQ.token da bi mogli raditi sa LIQKlijent klasom
     * @return objekt tipa Aerodrom za dati icao
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static Aerodrom dohvatiAirportsAerodrom(String icao, String token) throws SQLException {
        Aerodrom aerodrom = null;
        String upit = "SELECT * FROM AIRPORTS WHERE ident='" + icao + "'";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            aerodrom = new Aerodrom(resultSet.getString("ident"), resultSet.getString("name"), resultSet.getString("iso_country"), null);
        }
        if (aerodrom != null) {
            Lokacija lokacija = new LIQKlijent(token).getGeoLocation(aerodrom.getNaziv());
            aerodrom.setLokacija(lokacija);
        }
        return aerodrom;
    }

    /**
     *  Metoda dodaje vrijednosti u tablicu MyAirports za dati Aerodrom objekt (INSERT).
     *
     * @param aerodrom objekt tipa Aerodrom
     * @return false ako je objekt tipa Aerodrom null ili je bio neuspješan unos
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static boolean dodajAerodromUMyAirports(Aerodrom aerodrom) throws SQLException {
        if (aerodrom == null) {
            return false;
        }
        String upit = "INSERT INTO MYAIRPORTS (IDENT,NAME,ISO_COUNTRY,COORDINATES,STORED) VALUES ('" + aerodrom.getIcao() + "','" + aerodrom.getNaziv() + "','" + aerodrom.getDrzava() + "','" + aerodrom.getLokacija().getLatitude() + ", " + aerodrom.getLokacija().getLongitude() + "','" + new Timestamp(System.currentTimeMillis()) + "')";
        int brojRedaka = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
        return brojRedaka > 0;
    }

    /**
     * Metoda provjera postoji li dati avion u tablici Airplanes, ako postoji
     * vraća true, inače vraća false.
     */
    private static boolean avionPostojiAirplanes(String icao24, int odVremena) { //odVremena, FirstSeen
        //FIRSTSEEN JE BILO_INICIJALNO FIRSTSEEN
        try {
            String upit = "SELECT ID FROM AIRPLANES WHERE icao24='" + icao24 + "' AND LASTSEEN=" + odVremena;
            ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
            return resultSet.next();
        } catch (SQLException ex) {
            Logger.getLogger(Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Metoda za dohvat letova aerodroma za dati icao, od datog vremena (odVremena integer),
     * do datog vremena (doVremena integer). Vremena su zadana u sekundama.
     *
     * @param icao za koji želimo dohvatiti letove
     * @param odVremena vrijeme u sekundama
     * @param doVremena vrijeme u sekundama
     * @return vraća listu tipa AvionLeti
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static List<AvionLeti> dohvatiPoletjeleAvione(String icao, int odVremena, int doVremena) throws SQLException { //
        List<AvionLeti> poletjeliAvioni = new ArrayList<>();
        String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT='" + icao + "' AND (LASTSEEN>=" + odVremena + " AND LASTSEEN<=" + doVremena + ") ORDER BY FIRSTSEEN ASC";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            poletjeliAvioni.add(dohvatiPoletjeliAvion(resultSet));
        }
        return poletjeliAvioni;
    }

    /**
     * Upit aerodroma - tablica Airports, vraca aerodrom objekt bez lokacije
     */
    public static Aerodrom aerodromAirportsIcao(String icao) throws SQLException {
        Aerodrom a = null;

        ResultSet rs;

        String sqlUpit = String.format("SELECT * FROM AIRPORTS WHERE ident='%s'", icao);
        rs = Baza.getInstancaBaze().izvrsiUpit(sqlUpit);
        while (rs.next()) {
            a = new Aerodrom(
                    rs.getString("ident"),
                    rs.getString("name"),
                    rs.getString("iso_country"),
                    null
            );

        }
        return a;
    }
    
    
        public static Aerodrom aerodromAirportsIcaoLokacija(String icao) throws SQLException {
        Aerodrom a = null;

        ResultSet rs;


        String sqlUpit = String.format("SELECT * FROM AIRPORTS WHERE ident='%s'", icao.replaceAll("\'", ""));
        rs = Baza.getInstancaBaze().izvrsiUpit(sqlUpit);
        while (rs.next()) {
            String[] koordinate = rs.getString("coordinates").split(",");
                    
            a = new Aerodrom(
                    rs.getString("ident"),
                    rs.getString("name"),
                    rs.getString("iso_country"),
                    new Lokacija(koordinate[0].trim(), koordinate[1].trim())
            );

        }
        return a;
    }
    

    /**
     * Metoda za dohvat letova aerodroma za dati icao, od datog vremena (odVremena integer),
     * do datog vremena (doVremena integer). Vremena su zadana u sekundama.
     *
     * @param icao za koji želimo dohvatiti letove
     * @param odVremena vrijeme u sekundama
     * @param doVremena vrijeme u sekundama
     * @return vraća listu tipa AvionLeti
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static List<String> dohvatiPoletjeleAvioneNazivi(String icao24, int odVremena, int doVremena) throws SQLException {
        List<String> poletjeliAvioni = new ArrayList<>();
        String upit = "SELECT * FROM AIRPLANES WHERE icao24='" + icao24 + "' AND (LASTSEEN>=" + odVremena + " AND LASTSEEN<=" + doVremena + ") ORDER BY FIRSTSEEN";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            AvionLeti avioniLeti = dohvatiPoletjeliAvion(resultSet);
            Aerodrom aerodrom = aerodromAirportsIcao(avioniLeti.getEstDepartureAirport());
            poletjeliAvioni.add(aerodrom.getNaziv());
        }
        return poletjeliAvioni;
    }

    /**
     * Metoda za dohvat letova aerodroma za dati icao, od datog vremena (odVremena integer),
     * do datog vremena (doVremena integer). Vremena su zadana u sekundama.
     *
     * @param icao za koji želimo dohvatiti letove
     * @param odVremena vrijeme u sekundama
     * @param doVremena vrijeme u sekundama
     * @return vraća listu tipa AvionLeti
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static List<AvionLeti> dohvatiPoletjeleAvione(String icao, int limit) throws SQLException {
        List<AvionLeti> poletjeliAvioni = new ArrayList<>();
        String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT='" + icao + "' ORDER BY STORED DESC LIMIT " + limit;
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            poletjeliAvioni.add(dohvatiPoletjeliAvion(resultSet));
        }
        return poletjeliAvioni;
    }

    /**
     * Metoda koja dodaje avion tipa AvionLeti, provjerava njegove vrijednosti, i ako ne postoji zapis
     * i vrijednosti su mu različite od null zapisuje ga u tablicu Airplanes.
     *
     * @param avionLeti avion kojeg želimo upisati
     * @return vraća true u slučaju uspješnog unosa (INSERT), inače vraća false.
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static boolean dodajAvionLetiUAirplanes(AvionLeti avionLeti) throws SQLException {
        if (provjeriNull(avionLeti) && !avionPostojiAirplanes(avionLeti.getIcao24(), avionLeti.getLastSeen())) {
            String upit = "INSERT INTO AIRPLANES (icao24, firstSeen, estDepartureAirport, lastSeen, " + "estArrivalAirport, callsign, estDepartureAirportHorizDistance, estDepartureAirportVertDistance, " + "estArrivalAirportHorizDistance, estArrivalAirportVertDistance, departureAirportCandidatesCount, " + "arrivalAirportCandidatesCount, stored) VALUES " + "('" + avionLeti.getIcao24() + "','" + avionLeti.getFirstSeen() + "','" + avionLeti.getEstDepartureAirport() + "','" + avionLeti.getLastSeen() + "','" + avionLeti.getEstArrivalAirport() + "','" + avionLeti.getCallsign() + "','" + avionLeti.getEstDepartureAirportHorizDistance() + "','" + avionLeti.getEstDepartureAirportVertDistance() + "','" + avionLeti.getEstArrivalAirportHorizDistance() + "','" + avionLeti.getEstArrivalAirportVertDistance() + "','" + avionLeti.getDepartureAirportCandidatesCount() + "','" + avionLeti.getArrivalAirportCandidatesCount() + "','" + new Timestamp(System.currentTimeMillis()) + "')";
            int brojRedova = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
            return brojRedova > 0;
        }
        return false;
    }

    /**
     * Metoda koja dodaje avione tipa AvionLeti. Za svaki avion provjerava njegove vrijednosti,
     * i ako ne postoji zapisi i vrijednosti su mu različite od null zapisuje ga u tablicu Airplanes.
     *
     * @param avionLeti lista tipa AvionLeti - avioni koji se žele upisati u tablicu Airplanes
     * @throws java.sql.SQLException
     */
    public static void dodajAvionLetiUAirplanes(List<AvionLeti> avionLeti) throws SQLException {
        for (AvionLeti _avionLeti : avionLeti) {
            if (provjeriNull(_avionLeti) && !avionPostojiAirplanes(_avionLeti.getIcao24(), _avionLeti.getLastSeen())) {
                String upit = "INSERT INTO AIRPLANES (icao24, firstSeen, estDepartureAirport, lastSeen, "
                        + "estArrivalAirport, callsign, estDepartureAirportHorizDistance, estDepartureAirportVertDistance, "
                        + "estArrivalAirportHorizDistance, estArrivalAirportVertDistance, departureAirportCandidatesCount, "
                        + "arrivalAirportCandidatesCount, stored) VALUES " + "('" + _avionLeti.getIcao24() + "'," + _avionLeti.getFirstSeen()
                        + ",'" + _avionLeti.getEstDepartureAirport() + "'," + _avionLeti.getLastSeen() + ",'" + _avionLeti.getEstArrivalAirport()
                        + "','" + _avionLeti.getCallsign() + "'," + _avionLeti.getEstDepartureAirportHorizDistance() + ","
                        + _avionLeti.getEstDepartureAirportVertDistance() + "," + _avionLeti.getEstArrivalAirportHorizDistance()
                        + "," + _avionLeti.getEstArrivalAirportVertDistance() + "," + _avionLeti.getDepartureAirportCandidatesCount()
                        + "," + _avionLeti.getArrivalAirportCandidatesCount() + ",'" + new Timestamp(System.currentTimeMillis()) + "')";
                int uspjesno = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
                String icao = _avionLeti.getIcao24();
                if (uspjesno > 0) {
                    //System.out.println("Unio: " + icao);
                } else {
                    //System.out.println("Nije unio: " + icao);
                }
            }
        }
    }

    /**
     * Ažurira podatke za aerodrom u tablici MyAirports, dohvača koordinate i
     * upisuje ih u bazu.
     * Vraća false ako je neuspješno, inače true.
     *
     * @param icao aerodrom icao za kojeg želimo ažurirati vrijednosti
     * @param lIQToken token za LocationIQ.token kako bi mogli koristit klasu LIQKlijent
     * @param naziv zajedno sa parametrom adresom kako bi mogli dobit GPS lokaciju
     * @param adresa zajedno sa parametrom adresa kako bi mogli dobit GPS lokaciju
     * @return vraća true ako je uspješno, false u slučaju greške
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static boolean azurirajAerodromMyAirports(String icao, String lIQToken, String naziv, String adresa) throws SQLException {
        LIQKlijent lQKlijent = new LIQKlijent(lIQToken);
        Lokacija lokacija = lQKlijent.getGeoLocation(naziv + " " + adresa);
        if (lokacija == null) {
            return false;
        }
        String upit = "UPDATE MYAIRPORTS SET coordinates='" + lokacija.getLongitude() + ", " + lokacija.getLatitude() + "' WHERE ident='" + icao + "'";
        int brojRedova = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
        return brojRedova > 0;
    }

    /**
     * Metoda dohvaca Aerodrom iz MyAirports
     *
     * @return vraća Aerodrom
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static Aerodrom dohvatiMyAirportsAerodrom(String icao) throws SQLException {
        Aerodrom aerodrom = null;
        String upit = "SELECT * FROM MYAIRPORTS WHERE ident='" + icao + "'";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            String[] koordinate = resultSet.getString("coordinates").split(",");
            aerodrom = new Aerodrom(resultSet.getString("ident"),
                    resultSet.getString("name"),
                    resultSet.getString("iso_country"),
                    new Lokacija(koordinate[0].trim(), koordinate[1].trim()));
        }
        return aerodrom;
    }

    /**
     * Metoda dohvaća sve zapise u tablici MyAirports.
     *
     * @return vraća listu tipa Aerodrom
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static List<Aerodrom> dohvatiSveMyAirports() throws SQLException {
        List<Aerodrom> aerodromi = new ArrayList<>();
        String upit = "SELECT * FROM MYAIRPORTS";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            String[] koordinate = resultSet.getString("coordinates").split(",");
            Aerodrom aerodrom = new Aerodrom(resultSet.getString("ident"), resultSet.getString("name"), resultSet.getString("iso_country"), new Lokacija(koordinate[0].trim(), koordinate[1].trim()));
            aerodromi.add(aerodrom);
        }
        return aerodromi;
    }

    /**
     * Metoda za dati avion tipa AvionLeti provjerava ako su svi stupci ispunjeni
     * odnosno ako su null, u slučaju da je bilo koji null vraća false, inače true.
     */
    private static boolean provjeriNull(AvionLeti avionLeti) {
        for (Field f : avionLeti.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (f.get(avionLeti) == null) {
                    return false;
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Aerodrom.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }

    /**
     * Metoda briše aerodrom iz tablice MyAirports prema datom icao.
     *
     * @param icao aerodrom icao kojeg želimo izbrisati
     * @return true ako je uspješno obrisan ili false ako nije.
     * @throws java.sql.SQLException u slučaju pogreške sa bazom
     */
    public static boolean obrisiAerodromIzMyAirports(String icao) throws SQLException {
        String upit = "DELETE FROM MYAIRPORTS WHERE ident='" + icao + "'";
        int brojRedaka = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
        return brojRedaka > 0;
    }

    /**
     * Metoda provjerava ako je dati avion letio u datom vremenu iz tablice Airplanes.
     *
     * @param icao24 avion icao za koji se želi provjeriti let
     * @param icao avion24 icao za koji se želi provjeriti let
     * @param odVremena vrijeme u integeru
     * @param doVremena vrijeme u integeru
     * @return true ako je letio, false ako nije letio
     */
    public static boolean avionPoletio(String icao24, String icao, int odVremena, int doVremena) {
        try {
            String upit = "SELECT * FROM AIRPLANES WHERE icao24='" + icao24 + "' AND icao='" + icao + "' AND lastSeen>=" + odVremena + " AND lastSeen<=" + doVremena;
            ResultSet rs = Baza.getInstancaBaze().izvrsiUpit(upit);
            return !(!rs.isBeforeFirst() && rs.getRow() == 0);
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Metoda vraća objekt tipa AvionLeti i dodjeljuje mu vrijednosti iz resultSet-a.
     */
    private static AvionLeti dohvatiPoletjeliAvion(ResultSet resultSet) throws SQLException {
        AvionLeti avionLeti = new AvionLeti();
        avionLeti.setIcao24(resultSet.getString("icao24"));
        avionLeti.setFirstSeen(Integer.parseInt(resultSet.getString("firstSeen")));
        avionLeti.setEstDepartureAirport(resultSet.getString("estDepartureAirport"));
        avionLeti.setLastSeen(Integer.parseInt(resultSet.getString("lastSeen")));
        avionLeti.setEstArrivalAirport(resultSet.getString("estArrivalAirport"));
        avionLeti.setCallsign(resultSet.getString("callsign"));
        avionLeti.setEstDepartureAirportHorizDistance(Integer.parseInt(resultSet.getString("estDepartureAirportHorizDistance")));
        avionLeti.setEstDepartureAirportVertDistance(Integer.parseInt(resultSet.getString("estDepartureAirportVertDistance")));
        avionLeti.setEstArrivalAirportHorizDistance(Integer.parseInt(resultSet.getString("estArrivalAirportHorizDistance")));
        avionLeti.setEstArrivalAirportVertDistance(Integer.parseInt(resultSet.getString("estArrivalAirportVertDistance")));
        avionLeti.setDepartureAirportCandidatesCount(Integer.parseInt(resultSet.getString("departureAirportCandidatesCount")));
        avionLeti.setArrivalAirportCandidatesCount(Integer.parseInt(resultSet.getString("arrivalAirportCandidatesCount")));
        return avionLeti;
    }

    /**
     * Metoda vraca true ako je u redu, inace false.
     *
     */
    public static boolean autentikacijaKorisnika(String korisnickoIme, String lozinka) throws SQLException {
        String upit = "SELECT * FROM KORISNICI WHERE username='" + korisnickoIme + "' AND password='" + lozinka + "'";
        ResultSet rs = Baza.getInstancaBaze().izvrsiUpit(upit);
        return !(!rs.isBeforeFirst() && rs.getRow() == 0);
    }

    /**
     * Metoda dohvaca listu svih korisnika.
     */
    public static List<Korisnik> dohvatiListuSvihKorisnika() throws SQLException {
        List<Korisnik> korisnici = new ArrayList<>();
        String upit = "SELECT * FROM KORISNICI";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            Korisnik korisnik = new Korisnik(
                    resultSet.getInt("id"),
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("firstName"),
                    resultSet.getString("lastName"));
            korisnici.add(korisnik);
        }
        return korisnici;
    }

    /**
     * Metoda registrira korisnika, vraća true ako je uspjesno, ako postoji korisnicko ime vraća false;
     *
     */
    public static boolean registrirajKorisnika(String korisnickoIme, String lozinka, String ime, String prezime) {
        //ne treba provjera
        String upit = "INSERT INTO KORISNICI(username, password, firstName, lastName) VALUES('"
                + korisnickoIme + "', '" + lozinka + "', '" + ime + "', '"
                + prezime + "')";
        int brojRedaka = 0;
        try {
            brojRedaka = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
        } catch (SQLException ex) {
            Logger.getLogger(KomunikacijaBaza.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("METODA registrirajKorisnika: " + ex.getMessage());
        }
        if (brojRedaka == 0) {
            return false;
        }
        return true;
    }

    /**
     * Metoda azurira korisnika vraca true ako je azurirala, inace false.
     *
     */
    public static boolean azurirajKorisnika(int id, String korisnickoIme, String lozinka, String ime, String prezime) {
        String upit = "UPDATE KORISNICI SET username='"
                + korisnickoIme + "', password='"
                + lozinka + "', firstName='" + ime + "', lastName='"
                + prezime + "' WHERE id=" + id;
        int brojRedaka = 0;
        try {
            brojRedaka = Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
        } catch (SQLException ex) {
            Logger.getLogger(KomunikacijaBaza.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("METODA azurirajKorisnika: " + ex.getMessage());
        }
        if (brojRedaka == 0) {
            return false;
        }
        return true;
    }

    public static void zapisiUDnevnik(String ipAdresa, String korisnickoIme,
            String vrstaZapisa, String poruka, String sadrzaj,
            Timestamp vrijemePrijema, long trajanjeObrade) {
        try {
            String upit = "INSERT INTO dnevnik(ipAdresa,korisnickoIme,vrstaZapisa,"
                    + "poruka,sadrzaj,vrijemePrijema,trajanjeObrade) VALUES ('"
                    + ipAdresa + "','" + korisnickoIme + "','" + vrstaZapisa + "','"
                    + poruka + "','" + sadrzaj + "','" + vrijemePrijema + "','"
                    + trajanjeObrade + "')";
            Baza.getInstancaBaze().izvrsiUpitVratiBrojRedaka(upit);
        } catch (SQLException ex) {
            Logger.getLogger(KomunikacijaBaza.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ZAPIS DNEVNIK " + ex.getMessage());
        }
    }

    public static AvionLeti dohvatiAvionLeti(String icao) throws SQLException {
        AvionLeti avionLeti = null;
        String upit = "SELECT * FROM AIRPLANES WHERE ESTDEPARTUREAIRPORT='" + icao + "' ORDER BY STORED DESC LIMIT 1";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            avionLeti = dohvatiPoletjeliAvion(resultSet);
        }
        return avionLeti;
    }

    public static List<AvionLeti> dohvatiLetoveAviona(String icao24, int odVremena, int doVremena) throws SQLException {
        List<AvionLeti> letoviAviona = new ArrayList<>();
        String upit = "SELECT * FROM AIRPLANES WHERE icao24='" + icao24 + "' AND lastSeen>=" + odVremena + " AND lastSeen<=" + doVremena + " ORDER BY firstseen ASC";

        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            letoviAviona.add(dohvatiPoletjeliAvion(resultSet));
        }

        return letoviAviona;
    }

    public static List<Dnevnik> dohvatiSveDnevnike() throws SQLException {
        List<Dnevnik> dnevnici = new ArrayList<>();
        String upit = "SELECT * FROM DNEVNIK";
        ResultSet resultSet = Baza.getInstancaBaze().izvrsiUpit(upit);
        while (resultSet.next()) {
            Dnevnik dnevnik = new Dnevnik(resultSet.getInt("id"),
                    resultSet.getString("ipAdresa"),
                    resultSet.getString("korisnickoIme"),
                    resultSet.getString("vrstaZapisa"),
                    resultSet.getString("poruka"),
                    resultSet.getString("sadrzaj"),
                    resultSet.getTimestamp("vrijemePrijema"), resultSet.getInt("trajanjeObrade"));
            dnevnici.add(dnevnik);
        }
        return dnevnici;
    }
}
