package parsing;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import geometry.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used for working with csv files
 */
public class csv {

    /**
     * Reads points from a csv file and returns a list containing the points
     * @param file  the csv to read the points from
     * @return  a list containing the points
     */
    public static List<Point> getPointsFromCSV(File file) {
        List<Point> returnList = new ArrayList<>();

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.setMaxColumns(10000);
        CsvParser parser = new CsvParser(settings);

        List<String[]> allRows = parser.parseAll(file);

        for (int y = 0; y < allRows.size(); y++) {
            for (int x = 0; x < allRows.get(y).length; x++) {
                returnList.add(new Point(x, y, Double.parseDouble(allRows.get(y)[x])));
            }
        }

        return returnList;
    }

    /**
     * Writes given points to a csv
     * @param file  the file to write the points to
     * @param pointList the list containing the points
     * @param withZCoordinate   {@code true} if the z-coordinate should be written into the file, {@code false} otherwise
     */
    public static void writePointstoCSV(File file, List<Point> pointList, boolean withZCoordinate) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            String outputstring = "";
            for (Point point : pointList) {
                if (withZCoordinate) {
                    outputstring += String.valueOf(point.getX()) + "," + String.valueOf(point.getY()) + "," + String.valueOf(point.getZ()) + "\n";
                } else {
                    outputstring += String.valueOf(point.getX()) + "," + String.valueOf(point.getY()) + "\n";
                }
            }
            fileWriter.write(outputstring);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ignored) {}
    }

}
