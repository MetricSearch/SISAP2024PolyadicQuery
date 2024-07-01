package uk.ac.standrews.cs.descent.msed;

import java.io.Serializable;
import java.util.List;

public class MsedItem implements Serializable {
    private int id;
    private MsedRep msed;

    MsedItem(List<double[]> vector, int id){
        this.id = id;
        this.msed = new MsedRep(vector);
    }
    MsedItem(double[] vector, int id){
        this.id = id;
        this.msed = new MsedRep(vector);
    }
    MsedItem(){

    }
    public Integer id() {
        return this.id;
    }

    public MsedRep vector() {
        return this.msed;
    }

    public int dimensions() {
        return 0;
    }
}
