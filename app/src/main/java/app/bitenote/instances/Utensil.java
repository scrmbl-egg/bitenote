package app.bitenote.instances;

import androidx.annotation.NonNull;

/**
 * A container for data of one utensil in the database. The data in {@link Utensil} instances
 * can't be changed, because every possible utensil is pre-defined and immutable.
 * @see app.bitenote.database.BiteNoteSQLiteHelper#getUtensilFromId(int)
 * @author Daniel N.
 */
public class Utensil {
    /**
     * XML utensil tag in the {@code res/xml/utensils.xml} document.
     */
    public static final String XML_TAG = "utensil";

    /**
     * XML utensil tag {@code name} attribute in the {@code res/xml/utensils.xml} document.
     */
    public static final String XML_NAME_ATTRIBUTE = "name";

    /**
     * Name of the utensil.
     */
    public final String name;

    /**
     * A basic {@link Utensil} instance constructor.
     * @param name Name of the utensil.
     * @implNote Using this constructor won't add a row in the 'utensils' database table.
     * @see app.bitenote.database.BiteNoteSQLiteHelper#getUtensilFromId(int)
     */
    public Utensil(@NonNull String name) {
        this.name = name;
    }
}
