package de.rwth.processmining.tb.core.util.streaming;

@FunctionalInterface
public interface CheckedFunction<T,R> {
  R apply (T t) throws Exception;
}
