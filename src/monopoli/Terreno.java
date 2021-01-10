package monopoli;

public class Terreno {

    private String nome, colore;
    private int posizione, valore;
    private int prezzoTerreno, prezzoACasa, prezzoAlbergo;
    private int costoCostruzioneCasa, costoCostruzioneAlbergo;
    private boolean edibile, acquistabile;
    private boolean linea, evento, speciale;
    private int numeroCase = 0,albergo = 0, idProprietario = -1;

    public Terreno(String nome, String colore, int posizione,int valore,int prezzoTerreno,int prezzoACasa,int prezzoAlbergo,int costoCostruzioneCasa,int costoCostruzioneAlbergo, boolean edibile, boolean acquistabile){
        this.nome = nome;
        this.colore = colore;
        this.posizione = posizione;
        this.valore = valore;
        this.prezzoTerreno = prezzoTerreno;
        this.prezzoACasa = prezzoACasa;
        this.prezzoAlbergo = prezzoAlbergo;
        this.costoCostruzioneCasa = costoCostruzioneCasa;
        this.costoCostruzioneAlbergo = costoCostruzioneAlbergo;
        this.edibile = edibile;
        this.acquistabile = acquistabile;
        linea = false;
        evento = false;
        speciale = false;
    }

    public Terreno(String nome, int posizione, int valore, boolean edibile, boolean acquistabile) {
        this.nome = nome;
        this.posizione = posizione;
        this.valore = valore;
        this.edibile = edibile;
        this.acquistabile = acquistabile;
        linea = true;
        evento = false;
        speciale = false;
    }

    public Terreno(String nome, int posizione, boolean edibile, boolean acquistabile) {
        this.nome = nome;
        this.posizione = posizione;
        this.edibile = edibile;
        this.acquistabile = acquistabile;
        linea = false;
        evento = true;
        speciale = false;
    }

    public Terreno(String nome, int posizione, int valore, int prezzoTerreno, boolean edibile, boolean acquistabile) {
        this.nome = nome;
        this.posizione = posizione;
        this.valore = valore;
        this.prezzoTerreno = prezzoTerreno;
        this.edibile = edibile;
        this.acquistabile = acquistabile;
        speciale = !nome.contains("Tassa");
        linea = false;
        evento = false;
    }

    public int getPagamento(){
            if( numeroCase == 0 && albergo == 0)
                return prezzoTerreno;
            else if(numeroCase > 0)
                return numeroCase*prezzoACasa;
            else
                return prezzoAlbergo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getColore(){
        return colore;
    }

    public void setColore(String colore){
        this.colore = colore;
    }

    public int getPosizione() {
        return posizione;
    }

    public void setPosizione(int posizione) {
        this.posizione = posizione;
    }

    public int getValore() {
        return valore;
    }

    public void setValore(int valore) {
        this.valore = valore;
    }

    public int getPrezzoTerreno() {
        return prezzoTerreno;
    }

    public void setPrezzoTerreno(int prezzoTerreno) {
        this.prezzoTerreno = prezzoTerreno;
    }

    public int getPrezzoACasa() {
        return prezzoACasa;
    }

    public void setPrezzoACasa(int prezzoACasa) {
        this.prezzoACasa = prezzoACasa;
    }

    public int getPrezzoAlbergo() {
        return prezzoAlbergo;
    }

    public void setPrezzoAlbergo(int prezzoAlbergo) {
        this.prezzoAlbergo = prezzoAlbergo;
    }

    public int getCostoCostruzioneCasa() {
        return costoCostruzioneCasa;
    }

    public void setCostoCostruzioneCasa(int costoCostruzioneCasa) {
        this.costoCostruzioneCasa = costoCostruzioneCasa;
    }

    public int getCostoCostruzioneAlbergo() {
        return costoCostruzioneAlbergo;
    }

    public void setCostoCostruzioneAlbergo(int costoCostruzioneAlbergo) {
        this.costoCostruzioneAlbergo = costoCostruzioneAlbergo;
    }

    public boolean isEdibile() {
        return edibile;
    }

    public void setEdibile(boolean edibile) {
        this.edibile = edibile;
    }

    public boolean isAcquistabile() {
        return acquistabile;
    }

    public void setAcquistabile(boolean acquistabile) {
        this.acquistabile = acquistabile;
    }

    public boolean isLinea() {
        return linea;
    }

    public void setLinea(boolean linea) {
        this.linea = linea;
    }

    public boolean isEvento() {
        return evento;
    }

    public void setEvento(boolean evento) {
        this.evento = evento;
    }

    public boolean isSpeciale() {
        return speciale;
    }

    public void setSpeciale(boolean speciale) {
        this.speciale = speciale;
    }

    public int getNumeroCase() {
        return numeroCase;
    }

    public void setNumeroCase(int numeroCase) {
        this.numeroCase = numeroCase;
    }

    public int getAlbergo() {
        return albergo;
    }

    public void setAlbergo(int albergo) {
        this.albergo = albergo;
    }

    public int getIdProprietario() {
        return idProprietario;
    }

    public void setIdProprietario(int idProprietario) {
        this.idProprietario = idProprietario;
    }

    @Override
    public String toString() {
        return "Terreno{" +
                "nome='" + nome + '\'' +
                ", colore='" + colore + '\'' +
                ", posizione=" + posizione +
                ", valore=" + valore +
                ", prezzoTerreno=" + prezzoTerreno +
                ", prezzoACasa=" + prezzoACasa +
                ", prezzoAlbergo=" + prezzoAlbergo +
                ", costoCostruzioneCasa=" + costoCostruzioneCasa +
                ", costoCostruzioneAlbergo=" + costoCostruzioneAlbergo +
                ", edibile=" + edibile +
                ", acquistabile=" + acquistabile +
                ", linea=" + linea +
                ", evento=" + evento +
                ", speciale=" + speciale +
                ", numeroCase=" + numeroCase +
                ", albergo=" + albergo +
                ", idProprietario=" + idProprietario +
                "}\n";
    }
}
