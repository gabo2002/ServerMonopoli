package letturaXml;

import monopoli.Evento;
import monopoli.Terreno;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utilities.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class LeggiXml {

    private Log log;

    public LeggiXml(){
        log = new Log();
    }

    public Evento[] getEventi(String nomeFile,String nomeElemento){
        Evento[] eventi = null;
        ArrayList<Evento> eventiDinamico = new ArrayList<Evento>();

        try{
            //Inizializzazione documento
            File file = new File(nomeFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            System.out.println("Elemento radice: "+doc.getDocumentElement().getNodeName());

            //lista di eventi
            NodeList nList = doc.getElementsByTagName(nomeElemento);

            for(int i=0;i<nList.getLength();i++){
                //singolo evento
                Node nodo = nList.item(i);

                if(nodo.getNodeType() == Node.ELEMENT_NODE){
                    Element elemento = (Element) nodo;

                    int id = Integer.parseInt(elemento.getElementsByTagName("id").item(0).getTextContent());
                    String testo = elemento.getElementsByTagName("testo").item(0).getTextContent();
                    String valore = null;

                    if(elemento.getElementsByTagName("posizione").getLength() != 0){
                        Node nodoPosizione = elemento.getElementsByTagName("posizione").item(0);
                        Element elementoPosizione = (Element) nodoPosizione;
                        valore = nodoPosizione.getNodeName()+nodoPosizione.getTextContent()+" " +elementoPosizione.getAttribute("via");
                    }
                    else if(elemento.getElementsByTagName("pausa").getLength() != 0){
                        Node nodoPosizione = elemento.getElementsByTagName("pausa").item(0);
                        valore = nodoPosizione.getNodeName()+ nodoPosizione.getTextContent();
                    }
                    else if(elemento.getElementsByTagName("denaro").getLength() != 0) {
                        Node nodoPosizione = elemento.getElementsByTagName("denaro").item(0);
                        valore = nodoPosizione.getNodeName()+nodoPosizione.getTextContent();
                    }
                    eventiDinamico.add(new Evento(testo,id,valore));
                }
            }
        }
        catch(SAXException e){
            System.err.println("Errore nel parsing del documento");
            log.append("Errore nel parsing del documento xml "+nomeFile);
            e.printStackTrace();
        }
        catch(IOException e){
            System.err.println("Errore nell'apertura del file xml");
            log.append("Errore nell'apertura del file xml: "+nomeFile);
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.err.println("Errore nel parsing del documento");
            log.append("Errore nel parsing del documento xml "+nomeFile);
            e.printStackTrace();
        }

        eventi = new Evento[eventiDinamico.size()];
        eventi =eventiDinamico.toArray(eventi);
        return eventi;
    }

    public Terreno[] getTerreni(String nomeFile){
        Terreno[] ritorno = null;
        ArrayList<Terreno> terreni = new ArrayList<Terreno>();

        try{
            //Inizializzazione documento
            File file = new File(nomeFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            //lista di eventi
            NodeList nList = doc.getElementsByTagName("terreno");

            for(int i=0;i<nList.getLength();i++){
                //singolo evento
                Node nodo = nList.item(i);

                if(nodo.getNodeType() == Node.ELEMENT_NODE){
                    Element elemento = (Element) nodo;

                    boolean edibile = elemento.getAttribute("edibile").equals("true");
                    boolean acquistabile = elemento.getAttribute("acquistabile").equals("true");
                    String nome = elemento.getElementsByTagName("nome").item(0).getTextContent();
                    int posizione = Integer.parseInt(elemento.getElementsByTagName("id").item(0).getTextContent());

                    if(nome.toLowerCase().equals("imprevisto") || nome.toLowerCase().equals("probabilita")){
                        terreni.add(new Terreno(nome,posizione,edibile,acquistabile));
                    }
                    else if(elemento.getElementsByTagName("colore").getLength() != 0){
                        String colore = elemento.getElementsByTagName("colore").item(0).getTextContent();
                        int valore = Integer.parseInt(elemento.getElementsByTagName("valore").item(0).getTextContent());
                        int prezzoCostruzioneCasa = Integer.parseInt(elemento.getElementsByTagName("prezzoCostruzioneCasa").item(0).getTextContent());
                        int prezzoCostruzioneAlbergo = Integer.parseInt(elemento.getElementsByTagName("prezzoCostruzioneAlbergo").item(0).getTextContent());
                        int costoACasa = Integer.parseInt(elemento.getElementsByTagName("aCasa").item(0).getTextContent());
                        int costoAlbergo = Integer.parseInt(elemento.getElementsByTagName("albergo").item(0).getTextContent());
                        int prezzoSoloTerreno = Integer.parseInt(elemento.getElementsByTagName("soloTerreno").item(0).getTextContent());
                        terreni.add(new Terreno(nome,colore,posizione,valore,prezzoSoloTerreno,costoACasa,costoAlbergo,prezzoCostruzioneCasa,prezzoCostruzioneAlbergo,edibile,acquistabile));
                    }
                    else if(elemento.getElementsByTagName("linea").getLength() != 0){
                        int valore = Integer.parseInt(elemento.getElementsByTagName("valore").item(0).getTextContent());
                        terreni.add(new Terreno(nome,posizione,valore,edibile,acquistabile));
                    }
                    else{
                        int valore = 0,prezzoTerreno = 0;
                        if(elemento.getElementsByTagName("soloTerreno").getLength() != 0){
                            prezzoTerreno = Integer.parseInt(elemento.getElementsByTagName("soloTerreno").item(0).getTextContent());
                            if(elemento.getElementsByTagName("valore").getLength() != 0)
                                valore = Integer.parseInt(elemento.getElementsByTagName("valore").item(0).getTextContent());
                        }
                        terreni.add(new Terreno(nome,posizione,valore,prezzoTerreno,edibile,acquistabile));
                    }
                }
            }
        }
        catch(SAXException e){
            System.err.println("Errore nel parsing del documento");
            log.append("Errore nel parsing del documento xml "+nomeFile);
            e.printStackTrace();
        }
        catch(IOException e){
            System.err.println("Errore nell'apertura del file xml");
            log.append("Errore nell'apertura del file xml: "+nomeFile);
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.err.println("Errore nel parsing del documento");
            log.append("Errore nel parsing del documento xml "+nomeFile);
            e.printStackTrace();
        }

        ritorno = new Terreno[terreni.size()];
        ritorno = terreni.toArray(ritorno);
        return ritorno;
    }


}
