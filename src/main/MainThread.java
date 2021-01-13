package main;

import letturaXml.LeggiXml;
import monopoli.Evento;
import monopoli.Terreno;
import utilities.ErrorCode;
import utilities.Log;
import utilities.StatusCode;
import java.util.ArrayList;
import java.util.Random;

public class MainThread extends Thread{

    private final String nomeFileTerreni = "terreni.xml";
    private final String nomeFileImprevisti = "imprevisti.xml";
    private final String nomeFileProbabilita = "probabilita.xml";
    private Terreno[] terreni;
    private Evento[] imprevisti,probabilita;
    private Log log;
    private ArrayList<ThreadClient> clients;
    private boolean start = false;
    private String resoconto;

    public MainThread(){
        LeggiXml xml = new LeggiXml();
        log = new Log();
        clients = new ArrayList<ThreadClient>();
        terreni = xml.getTerreni(nomeFileTerreni);
        imprevisti = xml.getEventi(nomeFileImprevisti,"imprevisto");
        probabilita = xml.getEventi(nomeFileProbabilita,"probabilita");
        log.append("Lettura file completata!");
    }

    @Override
    public void run(){
        //aspetta fin quando tutti i giocatori sono pronti
        while(this.getGiocatori() < 3 || !pronti()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        start = true;

        for(int i=0;i<clients.size();i++) {
            clients.get(i).setId(i);
            clients.get(i).getGiocatore().setId(i);
            clients.get(i).send("ID:"+i);
        }

        for(int i=0;i<clients.size();i++)
            this.assegnaTerritoriIniziali(clients.get(i));

        while(start){   

            //Turno di ogni giocatore
            for(int i=0;i<clients.size();i++){

                resoconto = "";
                ThreadClient clientAttuale = clients.get(i);
                for(int j=0;j<clients.size();j++)
                    if(j != i)
                        clients.get(j).send("Turno di:"+i);

                if(clientAttuale.getGiocatore().getPausa() > 0){
                    clientAttuale.getGiocatore().setPausa(clientAttuale.getGiocatore().getPausa()-1);
                    resoconto += "" + StatusCode.PAUSA + ",";
                    clientAttuale.send(resoconto);
                    continue;
                }

                int lancio1 = lanciaDado();
                int lancio2 = lanciaDado();
                resoconto += ""+StatusCode.DADI+lancio1+":"+lancio2+",";
                //se e' in carcere
                if(clientAttuale.getGiocatore().isPrigioniero()){
                    if(lancio1 != lancio2){
                        resoconto += ""+StatusCode.PRIGIONIERO+"y,";
                        clientAttuale.send(resoconto);
                        continue;
                    }
                    clientAttuale.getGiocatore().setPrigioniero(false);
                    resoconto+=""+StatusCode.PRIGIONIERO+"n,";
                }
                //se passa dal via
                if(clientAttuale.getGiocatore().getPosizione() + lancio1 + lancio2 >= 40) {
                    clientAttuale.getGiocatore().modificaBadoniCoins(200);
                    resoconto += "" + StatusCode.GUADAGNO + "200,";
                }
                //lo sposto effettivamente
                clientAttuale.getGiocatore().setPosizione(lancio1+lancio2);

                //Controlli sul terreno?
                Terreno attuale = terreni[clientAttuale.getGiocatore().getPosizione()];
                resoconto += gestisciSpostamento(clientAttuale,attuale);

                //check se e' andato in bancarotta
                if(clientAttuale.getGiocatore().getBadoniCoins() < 0){
                    resoconto += ""+StatusCode.BANCAROTTA+",";
                }
                //TODO CICLO COMPETO
                //applicare delle modifiche
                clientAttuale.send(resoconto+clientAttuale.getGiocatore().toString());
                for(int j=0;j<clients.size();j++)
                    if(j != i)
                        clients.get(j).send(resoconto);

                //L'utente puo' ora amministrare il suo territorio
                boolean check = true;
                while(check){   //fin quando tutti i comandi non sono validi
                    String azioni = clientAttuale.recv();
                    String risposta = analizzaMosseUtente(clientAttuale,azioni);
                    String rispostaAltriGiocatori = elaboraMovimentiPerAltriGiocatori(azioni,risposta);
                    if(risposta.equals("" + StatusCode.STATUS_READY))
                        check = false;
                    for(int j =0;j<clients.size();j++)
                        if(i != j)
                            clients.get(j).send(rispostaAltriGiocatori);
                    clientAttuale.send(risposta);
                }
                //TODO CHECK IF HA VINTO
                //da fare entro sabato

            }
        }
    }

    private String elaboraMovimentiPerAltriGiocatori(String azioni,String risposta){
        String ritorno = "";

        if(risposta.equals("" + StatusCode.STATUS_READY))
            return azioni;

        String[] elementi = risposta.split(",");
        String[] eventi = azioni.split(",");

        for(String elemento : elementi) {
            int numeroAzione;
            ErrorCode code = null;
            if (Character.isDigit(elemento.charAt(1)))
                numeroAzione = Integer.parseInt(elemento.substring(0, 2));
            else
                numeroAzione = Integer.parseInt(elemento.charAt(0) + "");
            eventi[numeroAzione] = null;
        }

        for(String evento : eventi)
            if(evento != null)
                ritorno += evento + ",";
        return ritorno;
    }

    private String analizzaMosseUtente(ThreadClient client,String azioni){
        String ritorno = "";
        int posizione;
        String[] elementi = azioni.split(",");
        for(int i=0;i<elementi.length;i++){
            String elemento = elementi[i];
            String[] valori = elemento.split(":");

            StatusCode code = StatusCode.DEFAULT_VALUE;

            try {
                code = StatusCode.valueOf(valori[0]);
            }
            catch(IllegalArgumentException e){
                System.err.println("Pacchetto ricevuto dal client non corretto");
                log.append("Pacchetto di risposta dal client "+client.getSocket().getLocalAddress().getHostAddress()+" non valido!");
            }
            switch (code){
                case COMPRA_TERRENO:
                    posizione = Integer.parseInt(valori[1]);
                    if(posizione != client.getGiocatore().getPosizione() || posizione < 0 || posizione > 39) {    //posizione errata
                        ritorno += i + "" + ErrorCode.ERROR_INVALID_POSITION + ",";
                        break;
                    }
                    else if(!terreni[posizione].isAcquistabile() || terreni[posizione].getIdProprietario() != -1) {  //territorio non acquistabile o occupato
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }
                    else if(client.getGiocatore().getBadoniCoins() < terreni[posizione].getValore()) {      //non ho abbastanza soldi
                        ritorno += i + "" + ErrorCode.ERROR_NOT_ENOUGH_MONEY + ",";
                        break;
                    }

                    terreni[posizione].setIdProprietario(client.getGiocatore().getId());
                    client.getGiocatore().addTerreno(terreni[posizione]);
                    client.getGiocatore().modificaBadoniCoins(-terreni[posizione].getValore());
                    break;
                case COSTRUISCI_CASA:
                    posizione = Integer.parseInt(valori[1]);
                    int quanteCasa = Integer.parseInt(valori[2]);

                    if(quanteCasa < 0 || quanteCasa > 4) {    //numero di case non valido
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }
                    else if(posizione < 0 || posizione >= 40) {       //posizione non valida
                       ritorno += i + "" + ErrorCode.ERROR_INVALID_POSITION + ",";
                        break;
                    }
                    else if(terreni[posizione].getIdProprietario() != client.getGiocatore().getId() || !terreni[posizione].isEdibile()) {   //non e' il proprietario o non posso costruirci
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }
                    else if(terreni[posizione].getNumeroCase() + quanteCasa > 4) {       //supero il numero massimo di case
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }
                    else if(client.getGiocatore().getBadoniCoins() < terreni[posizione].getCostoCostruzioneCasa()*quanteCasa) {    //non ho abbastanza soldi
                        ritorno += i + "" + ErrorCode.ERROR_NOT_ENOUGH_MONEY + ",";
                        break;
                    }
                    else if(!hasMonopolio(client,terreni[posizione].getColore())) {   //non ho il monopolio per costruire
                        ritorno += i + "" + ErrorCode.ERROR_NO_MONOPOLY + ",";
                        break;
                    }

                    terreni[posizione].setNumeroCase(terreni[posizione].getNumeroCase()+quanteCasa);
                    client.getGiocatore().modificaBadoniCoins(-quanteCasa*terreni[posizione].getCostoCostruzioneCasa());
                    break;
                case COSTRUISCI_ALBERGO:
                    posizione = Integer.parseInt(valori[1]);
                    int costruzione = Integer.parseInt(valori[2]);

                    if(costruzione != 1) {      //valore di alberghi non valido
                        ritorno += i + "" + ErrorCode.ERROR_WRONG_OPERATION + ",";
                        break;
                    }
                    else if(posizione < 0 || posizione >= 40) {  //posizione non valida
                        ritorno += i + "" + ErrorCode.ERROR_INVALID_POSITION + ",";
                        break;
                    }
                    else if(terreni[posizione].getIdProprietario() != client.getGiocatore().getId() || !terreni[posizione].isEdibile()) {    //non e' il proprietario o non posso costruirci
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }
                    else if(terreni[posizione].getAlbergo() == 1) { //ho gia' un albergo
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }
                    else if(client.getGiocatore().getBadoniCoins() < terreni[posizione].getCostoCostruzioneAlbergo()) { //non ho abbastanza soldi
                        ritorno += i + "" + ErrorCode.ERROR_NOT_ENOUGH_MONEY + ",";
                        break;
                    }
                    else if(!hasMonopolio(client,terreni[posizione].getColore())) {      //Non ho il monopolio
                        ritorno += i + "" + ErrorCode.ERROR_NO_MONOPOLY + ",";
                        break;
                    }

                    terreni[posizione].setAlbergo(1);
                    client.getGiocatore().modificaBadoniCoins(-terreni[posizione].getCostoCostruzioneAlbergo());
                    break;
                case IPOTECA_TERRITORIO:
                    posizione = Integer.parseInt(valori[1]);

                    if(posizione < 0 || posizione >= 40) {   //posizione non valida
                        ritorno += i + "" + ErrorCode.ERROR_INVALID_POSITION + ",";
                        break;
                    }
                    else if(terreni[posizione].getIdProprietario() != client.getGiocatore().getId()) {   //non e' il proprietario
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }

                    int guadagno = terreni[posizione].getValore()/2;
                    guadagno += (terreni[posizione].getNumeroCase()*terreni[posizione].getCostoCostruzioneCasa())/ 2;
                    guadagno += (terreni[posizione].getAlbergo()*terreni[posizione].getCostoCostruzioneAlbergo())/ 2;
                    terreni[posizione].setIdProprietario(-1);
                    client.getGiocatore().rimuoviTerreno(posizione);
                    client.getGiocatore().modificaBadoniCoins(guadagno);
                    break;
                case VENDI_CASA:
                    posizione = Integer.parseInt(valori[1]);
                    int numeroCase = Integer.parseInt(valori[2]);

                    if(posizione < 0 || posizione >= 40) {       //posizione non valida
                        ritorno += i + "" + ErrorCode.ERROR_INVALID_POSITION + ",";
                        break;
                    }
                    else if(numeroCase <= 0 || numeroCase > 4) {  //numero di case non valido
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }
                    else if(terreni[posizione].getIdProprietario() != client.getGiocatore().getId()) {       //NON SONO il proprietario
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }
                    else if(!terreni[posizione].isEdibile()) {    //non posso costruirci
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED +",";
                        break;
                    }
                    else if(terreni[posizione].getAlbergo() == 1) {   //ho un albergo
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }
                    else if(terreni[posizione].getNumeroCase() < numeroCase) {    //sto provando a togliere piu' case di quelle che ho
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }

                    int soldiGuadagnati = (terreni[posizione].getCostoCostruzioneCasa()*numeroCase) / 2;
                    terreni[posizione].setNumeroCase(terreni[posizione].getNumeroCase()-numeroCase);
                    client.getGiocatore().modificaBadoniCoins(soldiGuadagnati);
                    break;
                case VENDI_ALBERGO:
                    posizione = Integer.parseInt(valori[1]);
                    int albergo = Integer.parseInt(valori[2]);

                    if(posizione < 0 || posizione >= 40) {    //posizione non valida
                        ritorno += i + "" + ErrorCode.ERROR_INVALID_POSITION + ",";
                        break;
                    }
                    else if(albergo != 0) {   //non sto rimuovendo l'albergo
                        ritorno += i + "" + ErrorCode.ERROR_WRONG_OPERATION + ",";
                        break;
                    }
                    else if(terreni[posizione].getIdProprietario() != client.getGiocatore().getId()) {   //non sono il proprietario
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }
                    else if(! terreni[posizione].isEdibile()) {       //non e' edibile
                        ritorno += i + "" + ErrorCode.ERROR_PERMISSION_DENIED + ",";
                        break;
                    }
                    else if(terreni[posizione].getAlbergo() != 1) {  //non ho un albergo
                        ritorno += i + "" + ErrorCode.ERROR_OVERFLOW + ",";
                        break;
                    }
                    terreni[posizione].setAlbergo(0);
                    client.getGiocatore().modificaBadoniCoins(terreni[posizione].getCostoCostruzioneAlbergo()/2);
                    break;
                case STATUS_READY:
                    ritorno += "" + StatusCode.STATUS_READY;
                    break;
                default:
                    ritorno += i + "" + ErrorCode.ERROR_UNKNOWN_COMMAND + ",";
                    break;
            }
        }
        if("".equals(ritorno))
            ritorno = "" + StatusCode.STATUS_READY;
        return ritorno;
    }

    private boolean hasMonopolio(ThreadClient client,String colore){
        for(Terreno terreno : terreni)
            if(colore.equals(terreno.getColore()))
                if(terreno.getIdProprietario() != client.getGiocatore().getId())
                    return false;
        return true;
    }


    private int gestisciAzione(ThreadClient client, Terreno terreno){
        Evento evento = null;
        Random random = new Random(System.currentTimeMillis());

        if(terreno.getNome().equals("Imprevisto"))
            evento = imprevisti[random.nextInt(imprevisti.length)];
        else
            evento = probabilita[random.nextInt(probabilita.length)];
        String azione = evento.getAzione();

        if(azione.contains("denaro")){
            int quantita = 0;

            if(azione.contains("upgrade"))
                for(Terreno terrenoTassato : client.getGiocatore().getTerreni())
                    quantita += terrenoTassato.getNumeroCase()*30 + terrenoTassato.getAlbergo()*90;
            else
                quantita = Integer.parseInt(azione.substring(6));

            client.getGiocatore().modificaBadoniCoins(quantita);
            resoconto += "" + (quantita > 0 ? StatusCode.GUADAGNO : StatusCode.SPESA) + ""+Math.abs(quantita)+",";
        }
        else if(azione.contains("pausa")) {
            int turniPausa = Integer.parseInt(azione.substring(5));
            client.getGiocatore().setPausa(turniPausa);
            resoconto += "" + StatusCode.PAUSA + "" + turniPausa + ",";
        }
        else {
            int posizioneIniziale = client.getGiocatore().getPosizione();
            int posizioneFinale = -1;
            String valori[] = azione.substring(9).split(" ");

            if (valori[0].charAt(0) == '=') {
                posizioneFinale = client.getGiocatore().setPosizioneEsatta(Integer.parseInt(valori[0].substring(1)));
            } else
                posizioneFinale = client.getGiocatore().setPosizione(Integer.parseInt(valori[0]));

            if (valori[1].equals("true") && posizioneFinale < posizioneIniziale){
                client.getGiocatore().modificaBadoniCoins(200);
                resoconto += "" + StatusCode.GUADAGNO + "200,";
            }

            if(posizioneFinale == 10){
                client.getGiocatore().setPrigioniero(true);
                resoconto += "" + StatusCode.PRIGIONIERO + "y,";
            }

            resoconto += gestisciSpostamento(client,terreni[posizioneFinale]);
        }
        System.out.println("REsoconto dopo impre/prob = "+resoconto);
        return evento.getId();
    }

    public String gestisciSpostamento(ThreadClient clientAttuale,Terreno attuale){
        String res = "";
        if(attuale.getIdProprietario() != -1 && attuale.getIdProprietario() != clientAttuale.getGiocatore().getId()){       //il terreno e' di qualcun altro
            System.out.println("Terreno non suo!");
            int spesa = 0;
            if(attuale.isLinea())
                spesa = getPagamentoLinea(attuale.getIdProprietario());
            else
                spesa = attuale.getPagamento();
            clientAttuale.getGiocatore().modificaBadoniCoins(-spesa);
            res += ""+StatusCode.SPESA+""+spesa+",";
            clients.get(attuale.getIdProprietario()).getGiocatore().modificaBadoniCoins(spesa);
        }
        else if(attuale.isEvento()) {  //se e' un imprevisto o una probabilita'
            int idAzione = gestisciAzione(clientAttuale, attuale);
            res += ""+(attuale.getNome().equals("Imprevisto") ? StatusCode.ID_IMPREVISTO : StatusCode.ID_PROBABILITA)+""+idAzione+",";
        }
        else if(attuale.isSpeciale()){  //prigione,parcheggio,gotoPrigione,via
            if(attuale.getNome().contains("Vai in")){   //se devi andare il prigione
                clientAttuale.getGiocatore().setPrigioniero(true);
                clientAttuale.getGiocatore().setPosizioneEsatta(10);
                res += ""+StatusCode.PRIGIONIERO+"y,";
            }
        }
        else if(!attuale.isAcquistabile()){
            int spesa = attuale.getPagamento();
            clientAttuale.getGiocatore().modificaBadoniCoins(-spesa);
            res += ""+StatusCode.SPESA+""+spesa+",";
        }
        System.out.println("Resoconto interno gestisci spostamento: "+res);
        return res;
    }

    private int getPagamentoLinea(int id){
        int spesa = 0;
        for(Terreno terreno : terreni){
            if(terreno.isLinea() && terreno.getIdProprietario() == id)
                spesa += terreno.getPagamento();
        }
        return spesa;
    }

    private int lanciaDado() {
        Random random = new Random();
        return random.nextInt(6) +1;
    }

    private void assegnaTerritoriIniziali(ThreadClient client){
        Random random = new Random(System.currentTimeMillis());
        String resocontoClient = "";
        int territoriDaAssegnare = 5;
        if(getGiocatori() > 4 && getGiocatori() < 6)
            territoriDaAssegnare = 4;
        else if(getGiocatori() >= 6)
            territoriDaAssegnare = 3;

        while(territoriDaAssegnare != 0){
            int posizioneTerreno = random.nextInt(terreni.length);

            if(terreni[posizioneTerreno].isAcquistabile() && terreni[posizioneTerreno].getIdProprietario() == -1){
                client.getGiocatore().addTerreno(terreni[posizioneTerreno]);
                resocontoClient += terreni[posizioneTerreno].getPosizione() + ",";
                terreni[posizioneTerreno].setIdProprietario(client.getGiocatore().getId());
                territoriDaAssegnare--;
            }
        }

        for(int i=0;i<clients.size();i++)
            clients.get(i).send(client.getGiocatore().getId()+":"+resocontoClient);
    }

    public boolean pronti(){
        if(clients.size() == 0)
            return false;

        for(int i =0;i<clients.size();i++)
            if(!clients.get(i).isReady())
                return false;
        return true;
    }

    public boolean isRunning(){
        return start;
    }

    public synchronized int getGiocatori(){
        return clients.size();
    }

    public synchronized  boolean addGiocatore(ThreadClient client){
        if(clients.size() == 8 || start)
            return false;
        else{
            client.start();
            return clients.add(client);
        }
    }

}
