# Parallel K-Core decomposition with PKC and ParK

Java implementation of the PKC and ParK algorithms for parallel K-Core decomposition of graphs on Multi-core Platforms.

## Building

To build with [openjdk8](https://openjdk.java.net/install/) you can run:

```bash
javac *.java
```

## Running

You can run it with (<optional_parameters>):
```bash
java Main pkc <Input_file> <Number_of_Threads> <Output_file>
java Main park <Input_file> <Number_of_Threads> <Output_file>
```
### Parameters
- **Input_file**
   - Default value: **sample.txt** (included)
   - It must be a tab-separated ordered list of edges.

- **Number_of_Threads** 
   - Default value: **1**

- **Output_file**
   - Default value: **core-list.txt**
   - It's a tab-separated list of nodes with their core number.

- **Additional parameter for PKC only: Clear_percentage**
   - Default value: **1.1** (no clearance)
   - It's the percentage of processed nodes, after which a new graph with the remaining unprocessed nodes will be created.

## File converter
The repo includes a converter script that takes an unordered list of edges and outputs an ordered list of edges without duplicates.
#### Usage
```bash
./converter.sh <input> <output>
```

## References
- **PKC paper**: H. Kabir and K. Madduri, "Parallel k-core Decomposition on Multicore Platforms," in The 2nd IEEE Workshop on
Parallel and Distributed Processing for Computational Social Systems (ParSocial 2017), June 2017, to appear.
