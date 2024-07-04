package uk.ac.standrews.cs.descent.bugattis;

import uk.ac.standrews.cs.descent.msed.MsedRep;

import java.util.Arrays;
import java.util.List;

public class SingleBugattiMSedFactory implements BugattiMSedFactory {
    private final List<double[]> bugattis;
    private int index = 0;

    public SingleBugattiMSedFactory(List<double[]> bugattis) {
        this.bugattis = bugattis;
    }

    public MsedRep makeRep(int i) {
        return new MsedRep(bugattis.get(i));
    }

    @Override
    public List<Integer> getPerm(int i) {
        return Arrays.asList( i );
    }

    @Override
    public int getSize() {
        return bugattis.size();
    }
}
