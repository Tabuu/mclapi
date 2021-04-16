package nl.tabuu.mclapi.mojang.rule;

import java.util.Arrays;

public class ConformedVariable<T, C> {

    private final T _value;
    private final IRule<C>[] _rules;

    @SafeVarargs
    private ConformedVariable(T value, IRule<C>... rules) {
        _value = value;
        _rules = rules;
    }

    public boolean conforms(C variable) {
        return Arrays.stream(_rules).allMatch(rule -> rule.conforms(variable));
    }

    public T get() {
        return _value;
    }

    public class RuleWrapper implements IRule<T> {

        private String action;

        @Override
        public boolean conforms(T value) {
            return false;
        }
    }
}
