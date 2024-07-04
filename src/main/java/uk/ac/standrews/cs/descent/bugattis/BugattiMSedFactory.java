package uk.ac.standrews.cs.descent.bugattis;

import uk.ac.standrews.cs.descent.msed.MsedRep;

import java.util.List;

public interface BugattiMSedFactory {
    public MsedRep makeRep(int i );
    public List<Integer> getPerm(int i);
    public int getSize();
}
