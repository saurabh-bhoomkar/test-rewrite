import java.util.Arrays;
import java.util.Comparator;

public class SortByPrefixNumber {

  public static void main(String[] args) {
    String[] strings = {"ABC-1", "ABC-2", "ABC-11", "BCD-0", "BCD-111", "BCD-2", "BCD-1"};

    Comparator<String> comparator = new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        String[] parts1 = s1.split("-");
        String[] parts2 = s2.split("-");

        // Compare prefixes (ABC vs BCD)
        int prefixCompare = parts1[0].compareTo(parts2[0]);
        if (prefixCompare != 0) {
          return prefixCompare;
        }

        // Prefixes are the same, compare numbers
        return Integer.parseInt(parts1[1]) - Integer.parseInt(parts2[1]);
      }
    };

    Arrays.sort(strings, comparator);

    System.out.println(Arrays.toString(strings));
  }
}

