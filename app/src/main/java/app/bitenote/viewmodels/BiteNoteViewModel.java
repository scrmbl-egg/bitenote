package app.bitenote.viewmodels;

import android.app.Application;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.Objects;

import app.bitenote.database.BiteNoteSQLiteHelper;
import app.bitenote.database.RecipeQuery;
import app.bitenote.instances.Recipe;

/**
 * {@link AndroidViewModel} that shares lifetime with the application. It grants access to the
 * database functions, and also allows for safe shared mutation of elements that the user will edit
 * through numerous activities.
 * @author Daniel N.
 */
public class BiteNoteViewModel extends AndroidViewModel {
    /**
     * SQLite database helper.
     */
    public final BiteNoteSQLiteHelper sqliteHelper;

    /**
     * Live data of the recipe query currently being edited.
     */
    public final LiveData<RecipeQuery> queryLiveData;

    /**
     * Live data of the recipe (along with its database ID) currently being edited.
     */
    public final LiveData<Pair<Integer, Recipe>> recipeLiveData;

    /**
     * Mutable live data for {@link #queryLiveData}.
     */
    private final MutableLiveData<RecipeQuery> mMutableQueryLiveData;

    /**
     * Mutable live data for {@link #recipeLiveData}.
     */
    private final MutableLiveData<Pair<Integer, Recipe>> mMutableRecipeLiveData;

    /**
     * BiteNoteViewModel constructor.
     * @param application {@link Application} instance. It is used as context for the database
     * helper.
     */
    public BiteNoteViewModel(@NonNull Application application) {
        super(application);

        sqliteHelper = new BiteNoteSQLiteHelper(application);

        mMutableQueryLiveData = new MutableLiveData<>(new RecipeQuery());
        mMutableRecipeLiveData = new MutableLiveData<>(Pair.create(0, new Recipe()));

        queryLiveData = mMutableQueryLiveData;
        recipeLiveData = mMutableRecipeLiveData;
    }

    @Override
    protected void onCleared() {
        sqliteHelper.close();
        super.onCleared();
    }

    /**
     * Atomically posts a new recipe ID into {@link #recipeLiveData}.
     * @param id ID of the recipe in the database.
     */
    public void postRecipeId(int id) {
        postRecipeWithId(id, Objects.requireNonNull(mMutableRecipeLiveData.getValue()).second);
    }

    /**
     * Atomically posts a new recipe instance into {@link #recipeLiveData}.
     * @param recipe {@link Recipe} instance.
     */
    public void postRecipe(@NonNull Recipe recipe) {
        postRecipeWithId(Objects.requireNonNull(mMutableRecipeLiveData.getValue()).first, recipe);
    }

    /**
     * Atomically posts a new recipe instance along with its ID into {@link #recipeLiveData}.
     * @param id ID of the recipe in the database.
     * @param recipe {@link Recipe} instance.
     */
    public void postRecipeWithId(int id, @NonNull Recipe recipe) {
        mMutableRecipeLiveData.postValue(Pair.create(id, recipe));
    }

    /**
     * Atomically posts a new query instance into {@link #queryLiveData}.
     * @param query {@link RecipeQuery} instance.
     */
    public void postQuery(@NonNull RecipeQuery query) {
        mMutableQueryLiveData.postValue(query);
    }
}
