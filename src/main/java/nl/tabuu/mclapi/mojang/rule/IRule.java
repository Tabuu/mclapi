package nl.tabuu.mclapi.mojang.rule;

public interface IRule<T> {
    boolean conforms(T value);
}