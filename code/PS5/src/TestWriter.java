import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TestWriter {

    public static void main(String[] args) throws IOException {
        printToFile("hello world");
    }

    private static void printToFile(String thingToPrint) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("textFileName.txt"));
        writer.write(thingToPrint); // print it out using the bufferedWriter
        writer.close();
    }

}
