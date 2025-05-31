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
 * Viewmodel for handling database state and persistence.
 * @author Daniel N.
 */
public class BiteNoteViewModel extends AndroidViewModel {
    /**
     * SQLite database helper.
     */
    public final BiteNoteSQLiteHelper sqliteHelper;

    public final LiveData<RecipeQuery> queryLiveData;

    public final LiveData<Pair<Integer, Recipe>> recipeLiveData;

    private final MutableLiveData<RecipeQuery> mutableQueryLiveData;

    private final MutableLiveData<Pair<Integer, Recipe>> mutableRecipeLiveData;

    /**
     * BiteNoteViewModel constructor.
     * @param application {@link Application} instance. It is used as context for the database
     * helper.
     */
    public BiteNoteViewModel(@NonNull Application application) {
        super(application);

        sqliteHelper = new BiteNoteSQLiteHelper(application);

        mutableQueryLiveData = new MutableLiveData<>(new RecipeQuery());
        mutableRecipeLiveData = new MutableLiveData<>(Pair.create(0, new Recipe()));

        queryLiveData = mutableQueryLiveData;
        recipeLiveData = mutableRecipeLiveData;
    }

    @Override
    protected void onCleared() {
        sqliteHelper.close();
        super.onCleared();
    }

    public void postRecipeId(int id) {
        postRecipeWithId(id, Objects.requireNonNull(mutableRecipeLiveData.getValue()).second);
    }

    public void postRecipe(@NonNull Recipe recipe) {
        postRecipeWithId(Objects.requireNonNull(mutableRecipeLiveData.getValue()).first, recipe);
    }

    public void postRecipeWithId(int id, @NonNull Recipe recipe) {
        mutableRecipeLiveData.postValue(Pair.create(id, recipe));
    }

    public void postQuery(@NonNull RecipeQuery query) {
        mutableQueryLiveData.postValue(query);
    }
}
