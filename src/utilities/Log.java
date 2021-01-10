    package utilities;

    import java.io.File;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.io.PrintWriter;
    import java.text.SimpleDateFormat;
    import java.util.Date;

    public class Log {

        private String path = "logMonopoli.txt";

        public Log(){

            try {
                File file = new File(path);

                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(path, true);
                writer.close();
                this.append("Server avviato!");
            }
            catch(IOException e){
                System.err.println("Errore nella scrittura su file di log!");
                e.printStackTrace();
            }
        }

        public synchronized boolean append(String text){
            try{
                String data = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                FileWriter writer = new FileWriter(path,true);
                PrintWriter printWriter = new PrintWriter(writer);
                printWriter.println("Ora: "+data+" : "+text);
                printWriter.close();
                writer.close();
            }
            catch(IOException e){
                return false;
            }
            return true;
        }


    }
