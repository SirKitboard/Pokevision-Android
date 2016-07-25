package com.sirkitboard.pokevision.util;

import org.json.JSONArray;

/**
 * Created by abalwani on 25/07/2016.
 */
public interface AsyncCallback {
    public void preExecute();

    public void asyncSuccess(JSONArray pokemons);

    public void asyncFailure();

    public void asyncCompleted();
}
