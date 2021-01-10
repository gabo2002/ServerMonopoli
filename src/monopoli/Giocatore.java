package monopoli;

import java.util.ArrayList;

public class Giocatore{

    private int badoniCoins, posizione, id;
    private String nome;
    private ArrayList<Terreno> terreni;
    private boolean prigioniero;
    private int pausa;

    public Giocatore(int id,String nome){
        badoniCoins = 2500;
        posizione = 0;
        prigioniero = false;
        pausa = 0;
        this.id = id;
        this.nome = nome;
        terreni = new ArrayList<Terreno>();
    }

    public Giocatore(int id){
        this(id,null);
    }

    public Giocatore(String nome){
        this(-1,nome);
    }

    public int getBadoniCoins(){
        return badoniCoins;
    }

    public boolean modificaBadoniCoins(int quantita){
        badoniCoins += quantita;
        return badoniCoins < 0;
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public ArrayList<Terreno> getTerreni(){
        return terreni;
    }

    public boolean rimuoviTerreno(int idTerreno){
        for(int i=0;i<terreni.size();i++)
            if(terreni.get(i).getPosizione() == idTerreno)
                return terreni.remove(i) != null;
        return false;
    }

    public void addTerreno(Terreno terreno){
        terreni.add(terreno);
    }

    public int getPosizione(){
        return posizione;
    }

    public int setPosizione(int differenza){
        posizione =  (posizione + differenza) % 40;
        return posizione;
    }

    public int setPosizioneEsatta(int posizione){
        this.posizione = posizione;
        return this.posizione;
    }

    public void setPrigioniero(boolean value){
        this.prigioniero = value;
    }

    public boolean isPrigioniero(){
        return prigioniero;
    }

    public void setPausa(int turni){
        this.pausa = turni;
    }

    public int getPausa(){
        return pausa;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    @Override
    public String toString() {
        return "Giocatore{" +
                "badoniCoins=" + badoniCoins +
                ", posizione=" + posizione +
                ", id=" + id +
                ", nome='" + nome + '\'' +
                ", terreni=" + terreni +
                ", prigioniero=" + prigioniero +
                ", pausa=" + pausa +
                '}';
    }
}
