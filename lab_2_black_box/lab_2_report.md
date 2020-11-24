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

 

