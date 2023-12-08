package hfdd.evaluation.cdrift.taskcreation;

import java.nio.file.Path;
import java.util.stream.Stream;

public record CDCollectionStream(String collectionName, Stream<Path> logPathStream) {

}
