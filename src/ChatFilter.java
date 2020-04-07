import java.util.ArrayList;
import java.io.*;

/**
 * @author Apoorva Gupta, gupta481@purdue.edu
 */

public class ChatFilter {
    public ArrayList<String> lines = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        File f = new File(badWordsFileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {

            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }


    }

    public String filter(String msg) {

        String output = msg ;
        String star = "" ;
        for (int i = 0; i < lines.size(); i++) {

            for (int j = 0; j < lines.get(i).length(); j++) {

                star = star.concat("*");
            }

            output = output.replaceAll(lines.get(i), star);
            star = "";
        }

        return output;
    }
}
