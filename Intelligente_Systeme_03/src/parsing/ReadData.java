package parsing;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import geometry.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ReadData {

    public static List<List<Vector>> getVectorsFromFile(File file) {
        List<List<Vector>> vec_list = new ArrayList<>();

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.getFormat().setDelimiter(';');
        settings.setMaxColumns(10000); // this might need to be changed, depending on the use case

        CsvParser parser = new CsvParser(settings);

        List<String[]> allRows = parser.parseAll(file);

        // go through each element in each row
        for (String[] row: allRows) {
            List<Vector> vecs = new ArrayList<>();
            double[] prev = getCoordinatesFromString(row[0]);
            for (int i = 1; i < row.length; i++) {
                double[] curr = getCoordinatesFromString(row[i]);
                vecs.add(new Vector(curr[0]-prev[0], curr[1]-prev[1]));
            }
            vec_list.add(vecs);
        }

        return vec_list;
    }


    private static double[] getCoordinatesFromString(String s) {
        String[] sep = s.split(",");
        double[] values = new double[2];
        values[0] = Double.parseDouble(sep[0]);
        values[1] = Double.parseDouble(sep[1]);
        return values;
    }

}
