package uk.ac.standrews.cs.descent;

public class Update {

    public int list_to_update_index;
    public int value_to_add;
    public double distance;


    public Update(int list_to_update_index, int value_to_add, double distance) {
        this.list_to_update_index = list_to_update_index;
        this.value_to_add = value_to_add;
        this.distance = distance;
    }
}
