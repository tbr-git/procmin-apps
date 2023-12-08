package de.rwth.processmining.tb.core.util.streaming;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

public class Either<L, R> {

  private final L left;
  private final R right;

  private Either(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L,R> Either<L,R> Left( L value) {
    return new Either<>(value, null);
  }

  public static <L,R> Either<L,R> Right( R value) {
    return new Either<>(null, value);
  }

  public Optional<L> getLeft() {
    return Optional.ofNullable(left);
  }

  public Optional<R> getRight() {
    return Optional.ofNullable(right);
  }

  public boolean isLeft() {
    return left != null;
  }

  public boolean isRight() {
    return right != null;
  }

  public <T> Optional<T> mapLeft(Function<? super L, T> mapper) {
    if (isLeft()) {
      return Optional.of(mapper.apply(left));
    }
    return Optional.empty();
  }

  public <T> Either<T, R> mapLeftOrPass(Function<? super L, T> mapper) {
    if (isLeft()) {
      return Either.Left(mapper.apply(left));
    }
    return Either.Right(right);
  }

  public <T> Optional<T> mapRight(Function<? super R, T> mapper) {
    if (isRight()) {
      return Optional.of(mapper.apply(right));
    }
    return Optional.empty();
  }

  public <T> Either<L, T> mapRightOrPass(Function<? super R, T> mapper) {
    if (isRight()) {
      return Either.Right(mapper.apply(right));
    }
    return Either.Left(left);
  }
  
  public String toString() {
    if (isLeft()) {
      return "Left(" + left +")";
    }
    return "Right(" + right +")";
  }
  
  public static<B, L extends Exception, R> Function<Either<L, R>, Either<? extends Exception, B>> eitherMapRightOrPass(CheckedFunction<R,B> function) {
    return e -> {
      if (e.isRight()) {
        try {
          return Either.Right(function.apply(e.right));
        } catch (Exception ex) {
          return Either.Left(ex);
        }
      }
      else {
        return Either.Left(e.left);
      }
    };
  }
  
  public static <T,R> Function<T, Either<Exception, R>> lift(CheckedFunction<T,R> function) {
    return t -> {
      try {
        return Either.Right(function.apply(t));
      } catch (Exception ex) {
        return Either.Left(ex);
      }
    };
  }
  
  public static <T,R> Function<T, Either<Pair<Exception, T>, R>> liftWithValue(CheckedFunction<T,R> function) {
    return t -> {
      try {
        return Either.Right(function.apply(t));
      } catch (Exception ex) {
        return Either.Left(Pair.of(ex,t));
      }
    };
  }
}
