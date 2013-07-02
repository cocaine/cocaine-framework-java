package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
interface ReduceFunction<V, R> {

    V apply(V accumulator, R value);

}
