package wb;
import org.junit.*;

import org.junit.Assert.*;

public class TestNextDate {
    //invalid input
    @Test
    public void invalid_input() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(1, 0, 2010).equals("invalid Input Date"));
        Assert.assertTrue( q.run(0, 1, 2010).equals("invalid Input Date"));
        Assert.assertTrue( q.run(1, 5, 1710).equals("invalid Input Date"));
        Assert.assertTrue( q.run(1, 5, 2022).equals("invalid Input Date"));
        Assert.assertTrue( q.run(13, 4, 2010).equals("invalid Input Date"));

    }

    //30 days
    @Test
    public void plus_one_day_30days() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(9, 10, 2010).equals("9/11/2010"));
        Assert.assertTrue( q.run(6, 10, 2010).equals("6/11/2010"));
        Assert.assertTrue( q.run(4, 10, 2010).equals("4/11/2010"));
        Assert.assertTrue( q.run(11, 10, 2010).equals("11/11/2010"));
    }

    @Test
    public void plus_one_mouth_30days() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(11, 30, 2010).equals("12/1/2010"));
    }

    @Test
    public void invalid_input_30days() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(11, 31, 2020).equals("Invalid Input Date"));
    }



    //31 days
    @Test
    public void plus_one_mouth_31days() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(10, 4, 2010).equals("10/5/2010"));
        Assert.assertTrue( q.run(10, 31, 2010).equals("11/1/2010"));
        Assert.assertTrue( q.run(1, 31, 2010).equals("2/1/2010"));
        Assert.assertTrue( q.run(3, 31, 2010).equals("4/1/2010"));
        Assert.assertTrue( q.run(5, 31, 2010).equals("6/1/2010"));
        Assert.assertTrue( q.run(8, 31, 2010).equals("9/1/2010"));
    }

    @Test
    public void plus_one_day_31days() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(11, 10, 2010).equals("11/11/2010"));
    }

    //December
    //This one is faulty
    @Test
    public void plus_one_mouth_december() {
        NextDate q;

        q =	new NextDate(0,0,0);
        //System.out.println(q.run(12, 31, 2010));
        Assert.assertTrue( q.run(12, 31, 2010).equals("1/1/2011"));
    }

    @Test
    public void plus_one_day_december() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(12, 10, 2010).equals("12/11/2010"));
    }

    @Test
    public void plus_one_year() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(12, 32, 2010).equals("1/1/2011"));
    }

    @Test
    public void invalid_year_december() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(12, 32, 2021).equals("Invalid Next Year"));
    }

    //February

    @Test
    public void plus_one_day_february() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 10, 2010).equals("2/11/2010"));
    }

    @Test
    public void plus_one_mounth_february_leapyear() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 29, 2020).equals("3/1/2020"));
    }

    @Test
    public void plus_one_mounth_february_notleap() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 28, 2010).equals("3/1/2010"));
    }

    @Test
    public void plus_one_day_february_29_leap() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 28, 2020).equals("2/29/2020"));
    }

    @Test
    public void leap_400() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 28, 2000).equals("2/29/2000"));
    }


    @Test
    public void february_invalid_day() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 30, 2020).equals("Invalid Input Date"));
    }

    @Test
    public void february_29_not_leap() {
        NextDate q;

        q =	new NextDate(0,0,0);
        Assert.assertTrue( q.run(2, 29, 2019).equals("Invalid Input Date"));
    }

}