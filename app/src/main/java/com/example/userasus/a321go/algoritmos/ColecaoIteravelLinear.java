package com.example.userasus.a321go.algoritmos;

/**
 * @author Actual code:
 * Carlos Urbano<carlos.urbano@ipleiria.pt>
 * Catarina Reis<catarina.reis@ipleiria.pt>
 * Marco Ferreira<marco.ferreira@ipleiria.pt>
 * João Ramos<joao.f.ramos@ipleiria.pt>
 * Original code: José Magno<jose.magno@ipleiria.pt>
 */
public interface ColecaoIteravelLinear<T> extends ColecaoIteravel<T> {
    void inserir(T elem);

    T remover(T elem);

    T remover(int indice);

    T removerPorReferencia(T elem);

    T consultar(int indice);

    boolean contem(T elem);

    boolean contemReferencia(T elem);
}
