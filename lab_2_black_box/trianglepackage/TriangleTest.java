package trianglepackage;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
		this.impossibleTriangle = new Triangle(1, 2, 2);
		this.negativeTriangle = new Triangle(-5, 2, 2);
		this.isoscelesTriangle = new Triangle(2, 2, 3);
		this.scaleneTriangle = new Triangle(1, 2, 3);
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
		isoscelesTriangle.isImpossible());
		assertEquals("Should return 'right-angled'", "right-angled", rightAngledTriangle.classify());		
		assertThrow(Exception.class, () -> {
			negativeTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.isRightAngled();
		});
	}


	// EP
	@Test
	public void testEquilateral() {
		assertTrue("Should return true for an equilateral triangle",
							equilateralTriangle.isEquilateral());
		assertFalse("Should return false for an equilateral triangle",
		equilateralTriangle.isImpossible());
		assertEquals("Should return 'equilateral'", "equilateral", equilateralTriangle.classify());
		assertThrow(Exception.class, () -> {
			negativeTriangle.isEquilateral();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.isEquilateral();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.isEquilateral();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.isEquilateral();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.isEquilateral();
		});

	}
	
	@Test
	public void testIsosceles(){
		assertTrue("Should return true for a Isosceles triangle",
							isoscelesTriangle.isIsosceles());
		assertFalse("Should return false for a Isosceles triangle",
							isoscelesTriangle.isImpossible());
		assertEquals("Should return 'isosceles'", "isosceles", isoscelesTriangle.classify());
		assertThrow(Exception.class, () -> {
			negativeTriangle.isImpossible();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.isImpossible();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.isImpossible();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.isImpossible();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.isImpossible();
		});
	}

	@Test
	public void testImpossible(){
		assertTrue("Should return true for a Impossible triangle",
	            impossibleTriangle.isImpossible());
		assertEquals("Should return 'impossible'", "impossible", impossibleTriangle.classify());
		assertThrow(Exception.class, () -> {
			negativeTriangle.isImpossible();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.isRightAngled();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.isRightAngled();
		});
	}

	@Test
	public void testScalene(){
		assertTrue("Should return true for a right-angled triangle",
							scaleneTriangle.isScalene());
		assertFalse("Should return false for a scalene triangle",
		scaleneTriangle.isImpossible());
		assertEquals("Should return 'scalene'", "scalene", scaleneTriangle.classify());
		assertThrow(Exception.class, () -> {
			negativeTriangle.isScalene();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.isScalene();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.isScalene();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.isScalene();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.isScalene();
		});
	}

	@Test
	public void testArea(){
		assertTrue("Should return 6 for a 3,4,5 ", rightAngledTriangle.getArea() == 6);
		assertTrue("Should return -1 for a impossible triangle ", impossibleTriangle.getArea() == -1);
		assertThrow(Exception.class, () -> {
			negativeTriangle.isScalene();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.getArea();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.getArea();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.getArea();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.getArea();
		});
	}
	
	@Test
	public void testParameter(){
		assertTrue("Should return 12 for a 3,4,5 triangle", rightAngledTriangle.getPerimeter() == 6);
		assertTrue("Should return -1 for a impossible triangle ", impossibleTriangle.getPerimeter() == -1);
		assertThrow(Exception.class, () -> {
			negativeTriangle.isScalene();
		});
		assertThrow(Exception.class, () -> {
			zeroSideTriangle.getPerimeter();
		});
		assertThrow(Exception.class, () -> {
			oneSideTriangle.getPerimeter();
		});
		assertThrow(Exception.class, () -> {
			infinitySideTriangle.getPerimeter();
		});
		assertThrow(Exception.class, () -> {
			boudaryNegativeTriangle.getPerimeter();
		});
	}

}
