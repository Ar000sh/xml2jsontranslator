
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test {


        @ParameterizedTest
        @CsvSource({"src/main/resources/test.json",
                    "src/main/resources/test2.json"})
        public void testXML2JSON(String filename) throws IOException {
            String json2XMl = JSON2XML_ST.run(filename);
            String xml2Json = XML2JSON.run2(json2XMl);
            String contents = Files.readString(Paths.get(filename));
            assertEquals(contents.trim(),xml2Json);
        }


}
