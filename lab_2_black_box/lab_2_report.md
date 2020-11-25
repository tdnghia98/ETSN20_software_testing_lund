# Lab 1 + 2 report

## Introduction
This report is done to deepen the understanding of different testing methods and the different technics to generate tests for them, including equivalence class partitioning and boundary value analysis. The labs are made to demonstrate the difference between black box and white box testing.    

First step of "Lab 1: White Box Testing" was to first draw out the control flow graph of the program we were testing and found the McCabe chromatic complexity measure. After that we started to implement the tests for the program originally from out control flow graph but after we had made the tests we first had thought of when looking att the control flow graph we started to new tests baced on the code coverage we had got as response from Eclima. So then we added more and more tests to be able to get 100% code coverage. 

In "Lab 2: Black Box Testing" we started with reading the program documentation and studied the different output from the different methods. We thought of trying out all the methods to get as good code coverage as possible. We could not look at the code so we did not know our code coverage or in other words what we did not test with our unit tests. We wanted to cover all output classes and therefor all boolean methods were set up with two test one that was asserted true and the other one false. We tried to use boundary values according to boundary value analysis. We used invalid test cases in accordance with equal class partitioning, so we used a triangle with a negative side         

#### Classes
| Class | Type | Description | 
| ----- | ---- | ----------- | 
| C1 | Input | 3 sides of equal length (2,2,2) |
| C2 | Input | 2 sides of equal length (2,2,3) |
| C3 | Input | All sides of different lengths (2,3,4) | 
| C4 | Input | Lengths does not form a triangle (1,2,5) |
| C5 | Input | Lengths forms a right angle (3,4,5) |
| C6 | Input | Triangle with negative sides (-4,2,3) |
| C7 | Input | String as side length (2,2,2) |
| C8 | Input | Integer.MAX_VALUE sides |
| C9 | Output | -1 |
| C10 | Output | Area of the triangle [number] |
| C11 | Output | Perimeter of the triangle [number] |
| C12 | Output | Triangle |
| C13 | Output | 3 sides' length [string] |
| C14 | Output | true |
| C15 | Output | false |

## Result

Faults were found inside three methods:
    
    public boolean isImpossible()
    public double getArea()
    public int getParameter()

#### Test Cases 

| Tests for method isEquilateral() | Input | Expected Output | Passes? |
| ----- | ---- | ----------- | --- | 
| TC1 | C1 | true | x | 
| TC2 | C2 | false | x | 


| Tests for method isIsosceles() | Input | Expected Output  | Passes? |
| ----- | ---- | ----------- | --- | 
| TC3 | C2 | true | x | 
| TC4 | C3 | false | x | 


| Tests for method isScalene() | Input | Expected Output  | Passes? |
| ----- | ---- | ----------- | --- |
| TC5 | C3 | true | x |
| TC6 | C1 | false | x |


| Tests for method isImpossible() | Input | Expected Output  | Passes? |
| ----- | ---- | ----------- | --- |
| TC7 | C4 | true | **NO** |
| TC8 | C1 | false | x |
| TC9 | C6 | true | x |


| Tests for method isRight-Angled() | Input | Expected Output  | Passes? |
| ----- | ---- | ----------- | --- |
| TC10 | C5 | true | x |
| TC11 | C1 | false | x |


| Tests for method getArea() | Input | Expected Output  | Passes? |
| ----- | ---- | ----------- | --- |
| TC12 | C5 | [0-inf]| x|
| TC13 | C4 | -1 | **NO** |
| TC14 | C8 | Integer.MAX_VALUE | x |
| TC15 | C6 | -1 | x |


| Tests for method getParameter() | Input | Expected Output  | Passes? |
| ----- | ---- | ----------- | --- |
| TC15 | C5 | [0-inf]| x |
| TC16 | C4 | -1 | **NO** |
| TC17 | C8 | Integer.MAX_VALUE | x |
| TC15 | C6 | -1 | **NO** |

## Discussion and conclusion

### White box techniques

#### Coverage Criterion

##### Statement

Using the statement coverage only cover a small portion of the code since most of the work is to test on condition. This technique only is not sufficient for our program but can be suitable for more straight forward classes that do simple calculations for example (without branching).

##### Branch

Branch coverage takes up most of the test cases written. This is expected as the program mainly test conditions.

##### Predicate

The predicate coverage is partly achieved when we have written the branch coverage but we needed to further identify the predicates and test cases for them.

##### Path

The path coverage is achieved when both branch and predicate coverage is achieved. This helps us to ensure every possible executions at runtime is tested.

#### Coverage Tool

##### Strength

The coverage tool was very useful when developing test cases since it highlights the fully covered, partly covered and uncovered statements. It helped us to spot the cases that we forgot to include, especially on the predicate coverage and on nested function calls (when function A calls function B and use the result to feed function C).

##### Weakness

If the tool can help us achieve full coverage, it is also a distraction. Sometimes the coverage can become a pressure, we only try to reach higher coverage but forget that the testing is also about writing meaningful and delicate test cases. For that we should rely on the control flow chart, the coverage should only be an indicator.

Sometimes the tool can be buggy, we got two different coverages on two different machines.

### Blackbox Techniques

#### EP vs BVA

The EP and BVA are complementary. By combining both techniques, we can be sure to cover the ordinary and and also edge cases. With that said, I feel like the EP test can have more weight since the inputs are logically more likely to be used in real life scenario while BVA test more "impossible" and edge cases where we would expect error.

In our case,
Using BVA, we can spot out that the program does not handle correctly the invalid cases. This is also covered with EP but BVA makes it more explicit.

#### Other blackbox testing techniques

- Combinatorial Testing: should be used when we have interactions with other parameters, for example when the class is used in conjunction with other classes or in different environments such as different OS.
- State transition testing: when the class includes events, we should include a set of test cases that triggers each event at least once.

### Blackbox vs Whitebox Techniques
