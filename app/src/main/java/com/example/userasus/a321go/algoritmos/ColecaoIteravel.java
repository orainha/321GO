package com.example.userasus.a321go.algoritmos;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Iterator;

/**
 * @author Actual code:
 * Carlos Urbano<carlos.urbano@ipleiria.pt>
 * Catarina Reis<catarina.reis@ipleiria.pt>
 * Marco Ferreira<marco.ferreira@ipleiria.pt>
 * João Ramos<joao.f.ramos@ipleiria.pt>
 * Original code: José Magno<jose.magno@ipleiria.pt>
 */
public interface ColecaoIteravel<T> extends Colecao<T>, Iterable<T> {
    IteradorIteravel<T> iterador();

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    default Iterator<T> iterator() {
        return iterador();
    }

    int getNumeroElementos();

    default boolean isVazia() {
        return getNumeroElementos() == 0;
    }
}
