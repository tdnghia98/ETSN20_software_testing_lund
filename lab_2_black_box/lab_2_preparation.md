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
| Class | Type | Description |
| ----- | ---- | ----------- |
| C1 | Input | 3 sides of equal length |
| C2 | Input | 2 sides of equal length |
| C3 | Input | All sides of different lengths| 
| C4 | Input | Lengths does not form a triangle |
| C5 | Output | "Equilateral" |
| C6 | Output | "Isosceles" |
| C7 | Output | "Scalene" |
| C8 | Output | "Impossible" |
| C9 | Output | "Right-Angled" |
| C10 | Output | -1 |
| C11 | Output | Area of the triangle [number] |
| C12 | Output | Perimeter of the triangle [number] |
| C13 | Output | Triangle |
| C14 | Output | 3 sides' length [string] |
| C15 | Output | true |
| C16 | Output | false |

#### Test Cases
