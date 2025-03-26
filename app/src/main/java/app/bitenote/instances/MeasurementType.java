package app.bitenote.instances;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * A container for data of one utensil in the database. The data in {@link MeasurementType}
 * instances can't be changed, because every possible utensil is pre-defined and immutable.
 * @see app.bitenote.database.BiteNoteSQLiteHelper#getMeasurementTypeFromId(int)
 * @author Daniel N.
 */
public class MeasurementType {
    /**
     * XML measurement type tag in the {@code res/xml/measurement_types.xml} document.
     */
    public static final String XML_TAG = "type";

    /**
     * XML measurement type {@code name} attribute in the {@code res/xml/measurement_types.xml}
     * document.
     */
    public static final String XML_NAME_ATTRIBUTE = "name";

    /**
     * Volume measurement type String.
     */
    public static final String VOLUME_TYPE_STRING = "volume";

    /**
     * Weight measurement type String.
     */
    public static final String WEIGHT_TYPE_STRING = "weight";

    /**
     * Name of the measurement type.
     */
    public final String name;

    /**
     * A basic {@link MeasurementType} instance constructor.
     * @param name Name of the measurement type.
     * @implNote Using this constructor won't add a row in the 'measurement_types' database table.
     * @see app.bitenote.database.BiteNoteSQLiteHelper#getMeasurementTypeFromId(int)
     */
    public MeasurementType(@NonNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        /*
         * Since MeasurementTypes are immutable, two references with the same data should be
         * considered equal.
         */

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementType that = (MeasurementType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
