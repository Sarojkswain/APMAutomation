package com.ca.apm.es;
import java.util.*;

/**
 * Created by venpr05 on 5/21/2017.
 */
public class FakeDataGenerator {

    private static String[] PREFIX1 = new String[]{"Frontends|Apps", "Business Segment|Business Transaction", "Random prefix"};
    private static String[] PREFIX2 = new String[]{"App1", "App300", "App1000", "Login", "Add Account", "Add to my account", "Steal Money", "Drain Account", "Randomg Prefix", "Gawd Pwned"};
    private static String[] URL = new String[]{
            "http://alice.name",
            "http://amie.net",
            "https://dorris.name",
            "https://forest.org",
            "https://kaitlin.info",
            "https://constance.name",
            "https://cameron.info",
            "https://emerson.net",
            "https://ophelia.biz",
            "http://melyna.info",
            "http://william.biz",
            "https://erich.org",
            "http://fausto.name",
            "https://julianne.info",
            "http://orpha.name",
            "http://name.info",
            "http://lelah.name",
            "https://kiara.biz",
            "https://lukas.org",
            "http://ole.com",
            "https://ari.name",
            "http://camden.name",
            "https://margarett.info",
            "http://michel.biz",
            "https://hayley.biz",
            "https://anahi.biz",
            "http://thelma.com",
            "https://kara.info",
            "http://helga.com",
            "http://geoffrey.biz",
            "https://candelario.biz",
            "http://erica.org",
            "http://bryana.biz",
            "https://joan.com",
            "https://lurline.biz",
            "http://cristobal.name",
            "https://gus.com",
            "http://heath.net",
            "http://torrance.net",
            "https://maximillia.org",
            "http://adolf.info",
            "https://gail.org",
            "http://horacio.biz",
            "https://sonia.info",
            "https://melyssa.info",
            "https://jerome.org",
            "http://german.biz",
            "http://branson.com",
            "http://joseph.name",
            "http://emie.biz",
            "http://shaun.com",
            "http://milan.info",
            "https://solon.org",
            "http://kali.info",
            "http://gussie.org",
            "http://toby.com",
            "https://ernest.net",
            "https://minnie.org",
            "https://lisandro.org",
            "https://ellie.org",
            "https://hollis.name",
            "http://dolly.info",
            "http://antoinette.org",
            "http://tomasa.org",
            "http://dewayne.biz",
            "https://gladyce.biz",
            "http://wendy.info",
            "https://buddy.biz",
            "https://jacinthe.org",
            "http://andre.info",
            "http://hadley.org",
            "https://lisa.info",
            "https://johnny.name",
            "https://kasey.org",
            "http://fidel.biz",
            "http://mireille.info",
            "http://santiago.org",
            "http://alexzander.com",
            "https://monty.net",
            "https://geraldine.name",
            "https://mozelle.name",
            "http://paul.info",
            "http://gene.org",
            "https://diamond.net",
            "https://mylene.net",
            "http://tommie.com",
            "https://rose.com",
            "http://rose.info",
            "https://aliza.org",
            "https://linda.info",
            "http://myriam.info",
            "https://arnulfo.net",
            "http://gillian.info",
            "https://lew.name",
            "https://colby.net",
            "https://krystel.name",
            "https://alysha.name",
            "https://palma.org",
            "http://zelma.biz",
            "http://sienna.info",
            "https://lucienne.name",
            "http://eda.info",
            "http://emelia.net",
            "https://natalia.org",
            "https://josianne.org",
            "https://leone.com",
            "https://bethel.biz",
            "https://shanna.info",
            "http://amanda.org",
            "https://norval.com",
            "http://lincoln.com",
            "https://johann.biz",
            "http://laurence.net",
            "https://velma.org",
            "http://maymie.biz",
            "http://sebastian.com",
            "https://nannie.net",
            "http://faustino.net",
            "https://annamae.net",
            "http://ciara.net",
            "https://nakia.org",
            "http://jovan.net",
            "https://nash.biz",
            "https://augusta.name",
            "http://joey.info",
            "http://will.org",
            "http://elda.org",
            "https://hugh.info",
            "https://carlotta.biz",
            "https://marion.com",
            "https://tianna.com",
            "https://lilliana.com",
            "https://cameron.biz",
            "http://gerardo.biz",
            "https://margaret.name",
            "https://alfreda.net",
            "https://gina.name",
            "https://theodore.biz",
            "https://ella.org",
            "http://sonny.com",
            "https://lesley.biz",
            "https://eldred.net",
            "https://bridgette.biz",
            "https://ubaldo.com",
            "http://bella.name",
            "http://helena.name",
            "https://reta.biz",
            "https://ellie.net",
            "https://greta.org",
            "http://rebeka.net",
            "http://maiya.org",
            "https://linnie.info",
            "http://anthony.info"
    };
    private static String[] AGENT = new String[]{"Tomcat Agent", "JBoss Agent", "eCommerceApp", "WebSphere Agent", "Weblogic Agent", "Unnamed", "Unknown", "Node Agent", "Java Application"};
    private static String[] PROCESS = new String[]{"Tomcat", "JBoss", ".Net Process", "NodeJS", "WebSphere", "Weblogic", "CEM", "EPAgent", "DxC", "Unnamed", "Unknown"};
    private static String[] HOST = new String[]{"11.162.76.110",
            "130.198.181.47",
            "108.248.150.120",
            "226.204.96.135",
            "183.252.114.43",
            "155.209.228.90",
            "48.15.199.31",
            "195.135.179.205",
            "43.254.61.172",
            "198.241.219.52",
            "234.115.171.206",
            "65.12.87.247",
            "10.200.36.164",
            "155.9.254.203",
            "24.198.233.218",
            "78.157.169.121",
            "157.229.38.101",
            "216.2.152.196",
            "117.69.148.181",
            "80.234.173.89",
            "180.209.253.80",
            "217.252.28.240",
            "195.6.204.19",
            "163.118.139.65",
            "92.176.166.54",
            "178.63.249.40",
            "131.142.221.211",
            "122.139.46.47",
            "94.44.126.56",
            "42.153.42.236"
    };
    private static String[] TYPE = new String[]{"Normal", "Sampled", "ErrorSnapshot"};
    private static String[] USER = new String[]{
            "Vasco",
            "Sergio",
            "Otello",
            "Lapo",
            "Graziano",
            "Massimiliano",
            "Ubaldo",
            "Ettore",
            "Marcello",
            "Duccio",
            "Luciano",
            "Enrico",
            "Alvaro",
            "Stefano",
            "David",
            "Neri",
            "Mauro",
            "Manuel",
            "Mario",
            "Alessio",
            "Enzo",
            "Iacopo",
            "Giancarlo",
            "Antonino",
            "Nicola",
            "Sandro",
            "Matteo",
            "Lorenzo",
            "Fabrizio",
            "Dino",
            "Alessio",
            "Diego",
            "Angiolo",
            "Samuele",
            "Giorgio",
            "Bernardo",
            "Claudio",
            "Cosimo",
            "Silvano",
            "Aldo",
            "Rolando",
            "Giacomo",
            "Salvatore",
            "Maurizio",
            "Giuliano",
            "Luciano",
            "Matteo",
            "Pietro",
            "Paolo",
            "Antonio",
            "Mario",
            "Antonio",
            "Fernando",
            "Domenico",
            "Samuele",
            "Michele",
            "Mario",
            "Giovanni",
            "Carlo",
            "Mirko",
            "Nello",
            "Cristiano",
            "Daniele",
            "Vasco",
            "Gino",
            "Gabriele",
            "Manuel",
            "Antonino",
            "Bernardo",
            "Samuele",
            "Marino",
            "Giampaolo",
            "Cosimo",
            "Tommaso",
            "Giulio",
            "Cosimo",
            "Pier Luigi",
            "Luciano",
            "Mattia",
            "Bernardo",
            "Andrea",
            "Emilio",
            "Elio",
            "Otello",
            "Fabrizio",
            "Jacopo",
            "Nello",
            "Maurizio",
            "Alfredo",
            "Nello",
            "Duccio",
            "Giuseppe",
            "Mauro",
            "Luciano",
            "Claudio",
            "Pier Luigi",
            "Giampiero",
            "Graziano",
            "Ugo",
            "Niccolo",
            "Alessandro",
            "Luca",
            "Alessandro",
            "Alessandro",
            "Vincenzo",
            "Alessandro",
            "Niccolo",
            "Valter",
            "Enrico",
            "Giovanni",
            "Vasco",
            "Iacopo",
            "Mario",
            "Simone",
            "Marino",
            "Corrado",
            "Duccio",
            "Giampiero",
            "Elia",
            "Enzo",
            "Sergio",
            "Graziano",
            "Gianni",
            "Simone",
            "Renato",
            "Alberto",
            "Gregorio",
            "Christian",
            "Roberto",
            "Massimiliano",
            "Jacopo",
            "Giuliano",
            "Giovanni",
            "Silvano",
            "Vittorio",
            "Daniele",
            "Cristiano",
            "Bernardo",
            "Renzo",
            "Alfredo",
            "Elia",
            "Luca",
            "Daniele",
            "Fabio",
            "Giuliano",
            "Ugo",
            "Fabrizio",
            "Alvaro",
            "Luciano",
            "Benito",
            "Emilio",
            "Neri",
            "Vasco",
            "Romano",
            "Vincenzo",
            "Franco",
            "Riccardo",
            "Massimiliano",
            "Vittorio",
            "Maurizio",
            "Andrea",
            "Carlo",
            "Raffaele",
            "Ugo",
            "Davide",
            "Gabriele",
            "Enzo",
            "Mattia",
            "Emiliano"
    };
    Random random = new Random();
    TraceFlakeIdGenerator idGen = new TraceFlakeIdGenerator();

    public String getTraceId() {

        try {
            return idGen.getNextTraceId();
        } catch (Exception e) {
        }
        return UUID.randomUUID().toString();
    }

    public String getType() {

        return TYPE[random.nextInt(TYPE.length)];
    }

    public String getUser() {

        return USER[random.nextInt(USER.length)];
    }

    public String getAgent() {

        return HOST[random.nextInt(HOST.length)] + "|" + PROCESS[random.nextInt(PROCESS.length)] + "|" + AGENT[random.nextInt(AGENT.length)];
    }

    public String getUrl() {

        return URL[random.nextInt(URL.length)] + "/" + USER[random.nextInt(USER.length)];
    }

    public int getFlags() {

        return random.nextInt(7);
    }

    public long getTime() {

        return System.currentTimeMillis();
    }

    public int getDuration() {

        return random.nextInt(3000);
    }

    public String getResource() {

        return PREFIX1[random.nextInt(PREFIX1.length)] + "|" + PREFIX2[random.nextInt(PREFIX2.length)] + "|URLs|Default";
    }

    public String getCallerId() {

        return UUID.randomUUID().toString();
    }

    public String getApp() {

        return PREFIX2[random.nextInt(PREFIX2.length)];
    }

    public int getCompCount() {

        return random.nextInt(100);
    }

    public int getPosId() {

        return random.nextInt();
    }

    public Set<String> getCorKeys() {

        Set<String> keys = new HashSet<String>();
        for (int i = 0; i <= random.nextInt(3); i++) {
            keys.add("CorCrossProcessData-" + UUID.randomUUID().toString());
        }

        return keys;
    }

    public Map<String, String> getParameters() {

        Map<String, String> map = new HashMap<String, String>();

        map.put("Resource Name", getResource());
        map.put("Remote Server Host", HOST[random.nextInt(HOST.length)]);
        map.put("Remote Server Port", "" + random.nextInt(65535));
        map.put("Class", this.getClass().toString());
        map.put("Method", "getParameters");
        map.put("is dynamic", "false");
        map.put("Component ID", "" + random.nextInt());
        map.put("HTTP Method", "POST");
        map.put("Called URL", getUrl());
        return map;
    }
}
