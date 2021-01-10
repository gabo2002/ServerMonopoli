package main;

import utilities.ErrorCode;
import utilities.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    private ServerSocket socket;
    private final static int port = 50000;
    private final static int maxReq = 8;
    private InetAddress address;
    private Log log;
    private MainThread gioco;

    public Main(){

        log = new Log();

        try {
            address = InetAddress.getLocalHost();
            System.out.println(address.getHostAddress());
            this.socket = new ServerSocket(port,maxReq,address);
            log.append("Server in ascolto sulla porta "+port);

        } catch (IOException e) {
            System.err.println("Errore! Porta gia' in uso\nStackTrace dell'errore:");
            e.printStackTrace();
            log.append("Errore nell'aprire la socket!");
        }
        gioco = new MainThread();
        gioco.start();
    }

    public void run(){
        while(true){
            try {
                Socket newUser = this.socket.accept();
                System.out.println("Nuova connessione da: "+newUser.getInetAddress().getHostAddress());
                log.append("Nuova connessione da: "+newUser.getInetAddress().getHostAddress());

                if(gioco.getGiocatori() == 8 || gioco.isRunning()){
                    PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(newUser.getOutputStream())),true);
                    writer.println(ErrorCode.ERROR_MAX_CLIENT);
                    writer.close();
                    newUser.close();
                }
                else
                    gioco.addGiocatore(new ThreadClient(newUser,gioco));
            } catch (IOException e) {
                System.err.println("Errore! Qualcosa e' andato storto nell'accettare un nuovo utente");
                e.printStackTrace();
                log.append("Errore nell' accettare un nuovo client");
            }
        }
    }

    public static void main(String[] args) {

        System.out.println("12ciao".substring(0,2));

        Main main = new Main();
        main.run();
    }

}
