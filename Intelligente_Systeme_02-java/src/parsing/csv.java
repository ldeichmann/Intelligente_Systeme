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
     * @param file the csv to read the points from
     * @return an array containing the points
     */
    public static Point[][] getPointsFromCSV(File file) {
        Point[][] returnArray;

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.setMaxColumns(10000);
        CsvParser parser = new CsvParser(settings);

        List<String[]> allRows = parser.parseAll(file);

        returnArray = new Point[allRows.get(0).length][allRows.size()];

        Point.setBounds(allRows.get(0).length, allRows.size());

        for (int y = 0; y < allRows.size(); y++) {
            for (int x = 0; x < allRows.get(y).length; x++) {
                returnArray[x][y] = (new Point(x, y, Double.parseDouble(allRows.get(y)[x])));
            }
        }

        return returnArray;
    }

    /**
     * Reads labels from a csv file and returns a list containing the points
     * @param file  the csv to read the points from
     * @param points the points for which the labels are
     * @return  a list containing the points
     */
    public static List<Point> getLabelsFromCSV(File file, Point[][] points) {
        List<Point> returnList = new ArrayList<>();

        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        settings.setMaxColumns(10000);
        CsvParser parser = new CsvParser(settings);

        List<String[]> allRows = parser.parseAll(file);

        for (int i = 0; i < allRows.size(); i++) {
            Point dummyPoint = new Point(Double.parseDouble(allRows.get(i)[0]), Double.parseDouble(allRows.get(i)[1]), 0.0);
            dummyPoint.setZ(points[(int)dummyPoint.getX()][(int)dummyPoint.getY()].getZ());
            returnList.add(dummyPoint);
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
