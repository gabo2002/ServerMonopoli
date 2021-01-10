package monopoli;

public class Evento {

    private String testo;
    private int id;
    private String azione;

    public Evento(String testo,int id,String azione){
        this.testo = testo;
        this.id = id;
        this.azione = azione;
    }

    public String getTesto(){
        return testo;
    }

    public int getId(){
        return id;
    }

    public String getAzione(){
        return azione;
    }

    @Override
    public String toString(){
        return "Testo: "+testo+"\nId: "+id+"\nAzione: "+azione+"\n";
    }
}
