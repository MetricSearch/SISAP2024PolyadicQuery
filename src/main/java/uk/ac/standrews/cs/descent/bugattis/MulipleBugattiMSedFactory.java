package uk.ac.standrews.cs.descent.bugattis;

import org.apache.commons.lang.StringUtils;
import uk.ac.standrews.cs.descent.msed.MsedRep;

import java.util.ArrayList;
import java.util.List;

public class MulipleBugattiMSedFactory implements BugattiMSedFactory {

    private final List<double[]> bugattis;
    private final int number_of_items;
    private final List<List<Integer>> all_perms;

    private int index = 0;

    public MulipleBugattiMSedFactory(List<double[]> bugattis, int number_of_items) {
        this.bugattis = bugattis;
        this.number_of_items = number_of_items;
        this.all_perms = mkPerms();
    }

    private List<List<Integer>> mkPerms() {
        List<List<Integer>> all_perms = new ArrayList<>();

        int maxBitVal = (int) Math.pow(2, bugattis.size());

        for (int i = 1; i < maxBitVal; i++) {
            // Use the bits of i to select the points used
            String bits = Integer.toBinaryString(i);

            int bitLen = bits.length();
            // Prepad string
            for (int j = 0; j < bugattis.size() - bitLen; j++) {
                bits = '0' + bits;
            }

            int ones = StringUtils.countMatches(bits, "1");
            if (ones == number_of_items) {
                ArrayList<Integer> queries = new ArrayList<>();
                for (int j = 0; j < bits.length(); j++) {
                    if (bits.charAt(j) == '1') {
                        // System.out.print(j + " ");
                        queries.add(j);
                    }
                }
                // System.out.println();
                all_perms.add(queries);
            }
        }
        return all_perms;
    }

    public int getSize() {
        return all_perms.size();
    }

    public MsedRep makeRep(int i) {
        List<double[]> doubles = new ArrayList<>();
        List<Integer> perms = getPerm(i);
        for( int index : perms ) {
            doubles.add(bugattis.get(index));
        }
        return new MsedRep( doubles );
    }

    public List<Integer> getPerm(int i) {
        return all_perms.get(i);
    }
}
