# Process Mining Applications
## Selection-projection Comparison
*Selection-projection Comparison* is an application that aims to provide insights into control-flow differences between two *event logs*.
To this end, we decompose the comparison and analyze different subprocesses where we identify a subprocess by a set of frequently co-occuring activities. 
Using this characterization, we extract the event data for a set of activities $A$ as follows: 

1. Maintain all process execution variants that contain all activities of $A$.
2. Project the extracted variants on $A$.

This approach establishes a specialization relation between activity sets $A_1 \subset A_2$ w.r.t. the extracted subtraces. 
Therefore, we define a **Selection-projection Structure (SPS)**. 
In an SPS, each vertex corresponds to an activity set, and the edges indicate the transitive reduction of the specialization relation.
Ultimately, we use the graph to find interesting differences exploiting the graph to find maximal differences.

### Requirements
Running the program requires JDK21.

### End-to-end Difference Extraction Entrypoint
If you have two event logs and discover the control flow differences, change into the directory `procmin-java/entrypoints/sps-cmd`. In this folder, execute

    ../../gradlew(.bat) run --args="path-to-first-log.xes path-to-second-log.xes path-where-to-create-result-directory result-directory-name"

You see the list of available parameters, execute

    ../../gradlew(.bat) run --args="--help"

### Process Comparison Evaluation Framework
Moreover, you can run the evaluation of the integrated process comparison on distinguishing logs across a concept drift and logs that originate from a stable period of the process. 
To this end, change into the folder `procmin-java/evaluation/sps-evaluation` and execute


    ../../gradlew run -PchooseMain="hfdd.evaluation.cdrift.PVAOnCDMain" --args="--cdcollections path-to-concept-drift-collections --resultdirectory path-to-resultdirectory --janus --boltDefault --boltTwoSeq --sps --plainemd"

The `path-to-concept-drift-collections` should be top-level directory that contains a subfolder for each collection. These subfolders should then contain event logs.
The last four flags indicate the algorithms to be evaluated. For Bolts approach and the declarative janus tool, you may also provide `--evalparallel` to run multiple classification tasks concurrently. These methods always run in on a single thread. 

If you want to add your own method, you will need to implement an entry point that implements the `PVAOnCDEvaluator` interface.



