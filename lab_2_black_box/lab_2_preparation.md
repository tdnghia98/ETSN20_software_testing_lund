# Lab 2 preparation

# Assignment 1: Reading

# Assignment 2: Triangle Test

## Specification
Triangle. The main function takes 3 positive whole-number lengths to be typed in as command line arguments. The program responds with a description of the triangle, as follows:
- <b>equilateral</b> - if all three sides have equal length
- <b>isosceles</b> - if two sides have equal length
- <b>right-angled</b> - if one angle is a right angle
- <b>scalene</b> - all sides different lengths, no right angles
- <b>impossible</b> - if the given side lengths do not form a triangle

Area and perimeter of the triangle are calculated, too.

## Output

### One set of equivalence classes covering valid and invalid cases

Equivalence Class Partioning:
 - Split input/output into classes which the software handles equivalently.
 - Select test cases to represent each class.

### Side length set
**Input** : 3 positive whole-number lengths
 
**Output**: description of the triangle
- <b>equilateral</b> - if all three sides have equal length
- <b>isosceles</b> - if two sides have equal length
- <b>right-angled</b> - if one angle is a right angle
- <b>scalene</b> - all sides different lengths, no right angles
- <b>impossible</b> - if the given side lengths do not form a triangle

#### Classes
| Class | Type | Description | Example Value |
| ----- | ---- | ----------- | -------------- |
| C1 | Input | 3 sides of equal length |
| C2 | Input | 2 sides of equal length |
| C3 | Input | All sides of different lengths| 
| C4 | Input | Lengths does not form a triangle |
| C5 | Input | Lengths forms a right angle |
| C6 | Input | Triangle with negative sides |
| C7 | Input | String as side length |
| C8 | Output | -1 |
| C9 | Output | Area of the triangle [number] |
| C10 | Output | Perimeter of the triangle [number] |
| C11 | Output | Triangle |
| C12 | Output | 3 sides' length [string] |
| C13 | Output | true |
| C14 | Output | false |

# Test Result

#### Test Cases EP

| EP Test Case Equilateral | Input | Output | Done | Invalid? |
| ----- | ---- | ----------- | --- | --- |
| TC1 | C1 | true | x | |
| TC2 | C2 | false | x | |


| EP Test Case Isosceles | Input | Output | Done | Invalid? |
| ----- | ---- | ----------- | --- | --- |
| TC3 | C2 | true | x | |
| TC4 | C3 | false | x | |


| EP Test Case Scalene | Input | Output |
| ----- | ---- | ----------- | --- |
| TC5 | C3 | true | x |
| TC6 | C1 | false | x |


| EP Test Case Impossible | Input | Output |
| ----- | ---- | ----------- |
| TC1 | C4 | true | x |
| TC2 | C1 | false | x |


| EP Test Case Right-Angled | Input | Output |
| ----- | ---- | ----------- | --- |
| TC1 | C5 | true | x |
| TC2 | C1 | false | x |


#### INSERT BVA TEST CASES FOR PARAMETER AND AREA

| EP Test Case Area | Input | Output |
| ----- | ---- | ----------- |
| TC1 | C5 | [0-inf]|
| TC2 | C4 | -1 |


| EP Test Case Parameter | Input | Output |
| ----- | ---- | ----------- |
| TC1 | C5 | [0-inf]|
| TC2 | C4 | -1 |

 

