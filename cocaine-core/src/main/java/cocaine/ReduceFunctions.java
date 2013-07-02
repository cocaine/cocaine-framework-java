package cocaine;

import java.io.IOException;
import java.util.List;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ReduceFunctions {

    public static <T> ReduceFunction<List<T>, T> listAccumulator() {
        return new ListAccumulator<>();
    }

    private static class ListAccumulator<T> implements ReduceFunction<List<T>, T> {
        @Override
        public List<T> apply(List<T> accumulator, T value) {
            accumulator.add(value);
            return accumulator;
        }
    }

    public static ReduceFunction<Integer, Integer> adder() {
        return new Adder();
    }

    private static class Adder implements ReduceFunction<Integer, Integer> {
        @Override
        public Integer apply(Integer accumulator, Integer value) {
            return accumulator + value;
        }
    }

    public static <V extends CharSequence> ReduceFunction<String, V> stringAppender() {
        return new StringAppender<>();
    }

    private static class StringAppender<V extends CharSequence> implements ReduceFunction<String, V> {
        @Override
        public String apply(String accumulator, V value) {
            return accumulator + value;
        }
    }

    public static <T extends Appendable, V extends CharSequence> ReduceFunction<T, V> sequenceAppender() {
        return new SequenceAppender<>();
    }

    private static class SequenceAppender<T extends Appendable, V extends CharSequence> implements ReduceFunction<T, V> {
        @Override
        public T apply(T accumulator, V value) {
            try {
                accumulator.append(value);
                return accumulator;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T extends Appendable> ReduceFunction<T, Character> characterAppender() {
        return new CharacterAppender<>();
    }

    private static class CharacterAppender<T extends Appendable> implements ReduceFunction<T, Character> {
        @Override
        public T apply(T accumulator, Character value) {
            try {
                accumulator.append(value);
                return accumulator;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
