package ninja.crinkle.mod.client.gui.states.references;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Ref<T> {
    private T value;
    private final T original;

    public Ref(T value) {
        this.value = value;
        this.original = value;
    }

    public T value() {
        return value;
    }

    public T original() {
        return original;
    }

    public void value(T value) {
        this.value = value;
    }

    public void mutate(@NotNull Function<T, T> mutator) {
        T result = mutator.apply(value);
        value(result);
    }

    public String toString() {
        return value().toString();
    }
}
