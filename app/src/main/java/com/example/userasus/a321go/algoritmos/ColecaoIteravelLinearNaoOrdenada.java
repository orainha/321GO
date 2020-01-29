package com.example.userasus.a321go.algoritmos;


/**
 * @author Actual code:
 * Carlos Urbano<carlos.urbano@ipleiria.pt>
 * Catarina Reis<catarina.reis@ipleiria.pt>
 * Marco Ferreira<marco.ferreira@ipleiria.pt>
 * João Ramos<joao.f.ramos@ipleiria.pt>
 * Original code: José Magno<jose.magno@ipleiria.pt>
 */
public interface ColecaoIteravelLinearNaoOrdenada<T> extends ColecaoIteravelLinear<T> {
    void inserirNoInicio(T elem);

    void inserirNoFim(T elem);

    void inserir(int indice, T elem);

    @Override
    default void inserir(T elem) {
        inserirNoFim(elem);
    }

    T removerDoInicio();

    T removerDoFim();
}
