package trianglepackage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class Blackbox_test {

    @Test
    void area_test() {
        Triangle t = new Triangle(2,2,2);
        assertTrue(t.getArea() == 2);

        Triangle t2 = new Triangle(2,2,5);
        assertTrue(t2.getArea() == -1);
    }

    @Test
    void scalen_test() {
        Triangle t = new Triangle(2,3,4);
        assertTrue(t.isScalene());

        Triangle t2 = new Triangle(2, 2, 3);
        assertFalse(t2.isScalene());
    }

    @Test
    void parameter_test() {
        Triangle t = new Triangle(3,4,5);
        assertTrue(t.getPerimeter() == 12);

        Triangle t2 = new Triangle(3,4,12);
        assertFalse(t2.getPerimeter() == 0);
    }

    @Test
    void set_side_and_get_side_test() {
        Triangle t = new Triangle(3,4,5);
        assertTrue(t.getSideLengths() == "3,4,5");

        t.setSideLengths(1, 2, 2);
        assertTrue(t.getSideLengths() == "1,2,2");
    }

    @Test
    void right_angeled_test() {
        Triangle t = new Triangle(3,4,5);
        assertTrue(t.isRightAngled());

        Triangle t2 = new Triangle(3,4,6);
        assertFalse(t2.isRightAngled());
    }

    @Test
    void impossible_triangle_test() {
        Triangle t = new Triangle(2,2,5);
        assertTrue(t.isImpossible());


        Triangle t2 = new Triangle(2,3,2);
        assertFalse(t2.isImpossible());
    }

    @Test
    void equal_side_test() {
        Triangle t = new Triangle(2,2,2);
        assertTrue(t.isEquilateral());

        Triangle t2 = new Triangle(2,3,2);
        assertFalse(t2.isEquilateral());
    }
}