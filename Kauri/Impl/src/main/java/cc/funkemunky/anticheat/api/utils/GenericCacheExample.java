package cc.funkemunky.anticheat.api.utils;

import java.util.concurrent.*;

public class GenericCacheExample<K, V> {

  private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<>();

  private Future<V> createFutureIfAbsent(final K key, final Callable<V> callable) {
    Future<V> future = cache.get(key);
    if (future == null) {
      final FutureTask<V> futureTask = new FutureTask<V>(callable);
      future = cache.putIfAbsent(key, futureTask);
      if (future == null) {
        future = futureTask;
        futureTask.run();
      }
    }
    return future;
  }

  public V getValue(final K key, final Callable<V> callable) throws InterruptedException, ExecutionException {
    try {
      final Future<V> future = createFutureIfAbsent(key, callable);
      return future.get();
    } catch (final InterruptedException e) {
      cache.remove(key);
      throw e;
    } catch (final ExecutionException e) {
      cache.remove(key);
      throw e;
    } catch (final RuntimeException e) {
      cache.remove(key);
      throw e;
    }
  }

  public void setValueIfAbsent(final K key, final V value) {
    createFutureIfAbsent(key, () -> value);
  }
}