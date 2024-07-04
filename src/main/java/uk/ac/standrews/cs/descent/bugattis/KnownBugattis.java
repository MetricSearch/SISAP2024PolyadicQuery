package uk.ac.standrews.cs.descent.bugattis;

import java.util.Arrays;
import java.util.List;

public class KnownBugattis {
    public static List<Integer> known_bugatti_ids = Arrays.asList(
            642221, // Veyron black front facing
            813063, // Veyron blue front facing
            423515,// Veyron silver front facing
            711844, // Veyron blue front facing (further back on black tiles)
            492836, // Veyron black front facing
            564471, // Veyron black front facing
            470840, // Veyron blue front facing (dark background)
            190083, // Veyron red with black wings front facing
            180540, // Veyron black front facing (outside garage)
            180310, // Veyron black 3/4 facing same car as above
            484400, // Veyron black with silver wings front facing
            28178, // Veyron black with silver wings close up
            711368, // Veyron black white wings car occluded
            41589, // Veyron black with monster stickers
            422109, // Veyron black with silver wings 3/4 view
            543049, // Veyron black with silver wings 3/4 view showroom
            813169, // Veyron black with blue wings 4/4 view showroom
            839767, // Veyron black with blue wings 3/4 blue other side from above
            845702, // Veyron white 3/4 view showroom
            106927, // Veyron black 3/4 view
            156119, // Veyron shiny black fibre bonnet
            710984, // Veyron black with white wings 3/4 view
            632741, // Veyron black 3/4 view
            180329, // 2 black Veyron 3/4 view next to each other on forecourt
            180475, // Veyron black 3/4 view with green Aston in background (near duplicate)
            181172, // Veyron black 3/4 view with green Aston in background (near duplicate)
            181360, // Veyron black 3/4 view (same car as above)
            180495, // 2 Veyron black bugatti (same as above)
            866052, // Veyron black with orange wings 3/4 view
            570717, // Veyron silver black in carpark Japanese
            676208, // Veyron orange black from side
            475278, // Veyron  black and white 'panda'
            618929, // Veyron white from front
            597306, // Veyron orange and black next to Bentley in same colours
            671500, // Veyron occluded with Bugatti sign
            619559, // Veyron white with ferarri behind in park
            185056, // Veyron orange black from front
            22302   // Veyron Black and white from the side
    );

    public static void main(String[] args) {
        System.out.println(known_bugatti_ids.size() + " in set"); // 37
    }

}
