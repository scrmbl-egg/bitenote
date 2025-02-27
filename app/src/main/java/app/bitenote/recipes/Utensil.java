package app.bitenote.recipes;

import java.util.EnumSet;

public enum Utensil {
    AIR_FRYER,
    BLENDER,
    DEEP_FRYER,
    GRILL,
    MICROWAVE,
    OVEN,
    PAN,
    POT,
    ROLLING_PIN,
    TOASTER;

    public final static EnumSet<Utensil> ALL = EnumSet.allOf(Utensil.class);
}
