# Lab 1 + 2 report

## Introduction

## Result

## Discusion and conclution


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
| C8 | Output | -1 |
| C9 | Output | Area of the triangle [number] |
| C10 | Output | Perimeter of the triangle [number] |
| C11 | Output | Triangle |
| C12 | Output | 3 sides' length [string] |
| C13 | Output | true |
| C14 | Output | false |

## Test Result

#### Test Cases EP

| Tests for method isEquilateral() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- | 
| TC1 | C1 | true | x | 
| TC2 | C2 | false | x | 


| Tests for method isIsosceles() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- | 
| TC3 | C2 | true | x | 
| TC4 | C3 | false | x | 


| Tests for method isScalene() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- |
| TC5 | C3 | true | x |
| TC6 | C1 | false | x |


| Tests for method isImpossible() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- |
| TC7 | C4 | true | **NO** |
| TC8 | C1 | false | x |
| TC9 | C6 | true | x |


| Tests for method isRight-Angled() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- |
| TC10 | C5 | true | x |
| TC11 | C1 | false | x |


#### INSERT BVA TEST CASES FOR PARAMETER AND AREA

| Tests for method getArea() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- |
| TC12 | C5 | [0-inf]| x|
| TC13 | C4 | -1 | **NO** |


| Tests for method getParameter() | Input | Output | Passes? |
| ----- | ---- | ----------- | --- |
| TC14 | C5 | [0-inf]| x |
| TC15 | C4 | -1 | **NO** | 
|

 

