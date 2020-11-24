package trianglepackage;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.*;

import jdk.jfr.Timestamp;

/**
 * TriangleTest for testing the Triangle class.
 * This template is used in the exercise phase 1.
 * Students should add relevant unit test cases related to the Triangle 
 * class to this class.
 */
public class TriangleTest {

	// EP
	private Triangle rightAngledTriangle;
	private Triangle impossibleTriangle;
	private Triangle negativeTriangle;
	private Triangle isoscelesTriangle;
	private Triangle scaleneTriangle;
	private Triangle equilateralTriangle;

	// BVA
	private Triangle boudaryNegativeTriangle;
	private Triangle zeroSideTriangle;
	private Triangle oneSideTriangle;
	private Triangle infinitySideTriangle;


	
	@BeforeClass
	/*
	 * The method run once before any of the test methods in the class.
	 */
	public static void setUpBeforeClass() throws Exception {
	
	}

	@AfterClass
	/* 
	 * The method will be run after all the tests in the class have been run
	 */
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	/*
	 * Initializes common objects. The method will be run before the Test method.
	 */
	public void setUp() throws Exception {
		// EP
		this.rightAngledTriangle = new Triangle(3, 4, 5);
		this.impossibleTriangle = new Triangle(1, 2, 5);
		this.negativeTriangle = new Triangle(-5, 3, 4);
		this.isoscelesTriangle = new Triangle(2, 2, 3);
		this.scaleneTriangle = new Triangle(4, 2, 3);
		this.equilateralTriangle = new Triangle(2, 2, 2); 

		// Boundary Value Analysis for Side length (Input: positive while-number length)
		// Lenght [0 +Inf]
		// Length: 0, -1, +Inf
		this.zeroSideTriangle = new Triangle(0, 2, 2);
		this.oneSideTriangle = new Triangle(1, 3, 2);
		this.infinitySideTriangle = new Triangle(Integer.MAX_VALUE, Integer.MAX_VALUE + 1, Integer.MAX_VALUE - 1);
		this.boudaryNegativeTriangle = new Triangle(999, 997, 998);

	}

	@After
	/*
	 *   Cleanup method. This method will be run after the Test method is completed
	 */
	public void tearDown() {}

	@Test
	/* 
	 * Tests whether the triangle specified in the fixture (setUp) 
	 * is right-angled. 	
	 *
	 * A public void method that is attached to be run as a test case. 
	 * To run the method, JUnit first constructs a fresh instance of the class then 
	 * invokes the annotated method. Any exceptions thrown by the test will be reported
	 * by JUnit as a failure. If no exceptions are thrown, the test is assumed to have 
	 * succeeded. 
	*/
	public void testRightAngled() {
		assertTrue("Should return true for a right-angled triangle",
							rightAngledTriangle.isRightAngled());
		assertFalse("Should return false for a right-angled triangle",
				impossibleTriangle.isRightAngled());
		
	}


	// EP
	@Test
	public void testEquilateral() {
		assertTrue("Should return true for an equilateral triangle",
							equilateralTriangle.isEquilateral());
		assertFalse("Should return false for an equilateral triangle",
		scaleneTriangle.isEquilateral());
	

	}
	
	@Test
	public void negativeSideTriangleTest_isImpossible() {
		assertTrue("Should return true for an negative triange returned " + negativeTriangle.isImpossible(),
							negativeTriangle.isImpossible());
	}
	
	
	
	@Test 
	public void throwTest() {
		assertThrows(Exception.class, () -> {
			negativeTriangle.isRightAngled();
		});
		assertThrows(Exception.class, () -> {
			zeroSideTriangle.isRightAngled();
		});
		assertThrows(Exception.class, () -> {
			oneSideTriangle.isRightAngled();
		});
		assertThrows(Exception.class, () -> {
			infinitySideTriangle.isRightAngled();
		});
		assertThrows(Exception.class, () -> {
			boudaryNegativeTriangle.isRightAngled();
		});
	}
	
	@Test
	public void testIsosceles(){
		assertTrue("Should return true for a Isosceles triangle", isoscelesTriangle.isIsosceles());
		assertFalse("Should return false for a Isosceles triangle", isoscelesTriangle.isImpossible());
	}

	@Test
	public void testImpossible(){
		assertFalse("Should return false for a valid triangle, returned "+isoscelesTriangle.isImpossible(), isoscelesTriangle.isImpossible());
		assertTrue("Should return true for a Impossible triangle, returned "+impossibleTriangle.isImpossible(), impossibleTriangle.isImpossible());
	}

	@Test
	public void testScalene(){
		assertTrue("Should return true for a right-angled triangle",
							scaleneTriangle.isScalene());
		assertFalse("Should return false for a scalene triangle",
		scaleneTriangle.isImpossible());
	
	}

	@Test
	public void testArea(){
		assertTrue("Should return 6 for a 3,4,5 returned "+rightAngledTriangle.getArea(), rightAngledTriangle.getArea() == 6);
	}
	
	@Test 
	public void testAreaImpossibleTriangle() {
		assertTrue("Should return -1 for a impossible triangle, returned "+impossibleTriangle.getArea(), impossibleTriangle.getArea() == -1);
	}
	
	@Test
	public void testParameter(){
		assertTrue("Should return 12 for a 3,4,5 triangle, returned "+rightAngledTriangle.getPerimeter(), rightAngledTriangle.getPerimeter() == 12);
		assertTrue("Should return -1 for a impossible triangle, returned "+impossibleTriangle.getPerimeter(), impossibleTriangle.getPerimeter() == -1);
	
	}
	
	@Test 
	public void areaOfNegativSideTriangle() {
		assertTrue("Should return -1 for a impossible triangle, returned "+negativeTriangle.getArea(), negativeTriangle.getArea() == -1);
	}
	
	@Test 
	public void parameterOfNegativSideTriangle() {
		assertTrue("Should return -1 for a impossible triangle, returned "+negativeTriangle.getPerimeter(), negativeTriangle.getPerimeter() == -1);
	}

	//BVA tests

	@Test 
	public void areaOfZeroSide(){
		assertTrue("Should return -1 for a zero sided triangle, returned "+zeroSideTriangle.getArea(), zeroSideTriangle.getArea() == -1);
	}

	@Test 
	public void areaOfInfinitSide(){
		assertTrue("Should return -1 for a inf sided triangle, returned "+infinitySideTriangle.getArea(), infinitySideTriangle.getArea() == Integer.MAX_VALUE);
	}

	@Test 
	public void parameterOfZeroSide(){
		assertTrue("Should return -1 for a zero sided triangle, returned "+zeroSideTriangle.getPerimeter(), zeroSideTriangle.getPerimeter() == -1);
	}

	@Test 
	public void parameterOfInfinitSide(){
		assertTrue("Should return Integer max for a inf sided triangle, returned "+infinitySideTriangle.getPerimeter(), infinitySideTriangle.getPerimeter() == Integer.MAX_VALUE);
	}
}
