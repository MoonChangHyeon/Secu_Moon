
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
    public static void main(String[] args) {
        String text = "Scan time: 00:12\n" +
                "SCA Engine version: 25.3.0.0014\n" +
                "Machine Name: munchanghyeon-ui-MacBookAir.local\n" +
                "Username running scan: munchanghyeon";

        testRegex(text, "Scan time:");
        testRegex(text, "SCA Engine version:");
        testRegex(text, "Machine Name:");
    }

    private static void testRegex(String text, String key) {
        // Original Regex
        Pattern pattern = Pattern.compile(key + "\\s*(.*)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            System.out.println("Match for '" + key + "': '" + matcher.group(1).trim() + "'");
        } else {
            System.out.println("No match for '" + key + "'");
        }
    }
}
