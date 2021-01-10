package main;

import monopoli.Giocatore;
import utilities.Log;
import utilities.StatusCode;

import java.io.*;
import java.net.Socket;

public class ThreadClient extends Thread{

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Log log;
    private boolean ready = false;
    private MainThread gioco;
    private Giocatore giocatore;
    int id;

    public ThreadClient(Socket socket,MainThread gioco){
        this.socket = socket;
        this.gioco = gioco;
        log = new Log();
        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream())),true);
        } catch (IOException e) {
            log.append("Errore! impossibile ottenere le socket stream!");
            e.printStackTrace();
        }
    }

    public ThreadClient(ThreadClient client){
        socket = client.getSocket();
        writer = client.getWriter();
        reader = client.getReader();
        log = client.getLog();
        gioco = client.getGioco();
    }

    public void run(){
        try{
            send("" +StatusCode.STATUS_READY );
            String nome = reader.readLine();
            while(!ready){
                String pacchetto = reader.readLine();
                System.out.println(pacchetto);
                if(pacchetto == null)
                    return;
                if(pacchetto.equals("" + StatusCode.STATUS_READY))
                    ready = true;
                else
                    writer.println(""+gioco.getGiocatori());
            }
            giocatore = new Giocatore(nome);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public boolean isReady(){
        return ready;
    }

    public void setReady(boolean value){
        ready = value;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public MainThread getGioco(){
        return gioco;
    }

    public void setId(int id){
        this.id = id;
    }

    public Giocatore getGiocatore(){
        return giocatore;
    }

    public void terminaGame(){
        for(int i=0;i<giocatore.getTerreni().size();i++){
            giocatore.getTerreni().get(i).setIdProprietario(-1);
        }
    }

    public void send(String msg){
        writer.println(msg);
    }

    public String recv(){
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
