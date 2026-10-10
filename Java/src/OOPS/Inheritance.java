package OOPS;

public class Inheritance {

    public static void main()
    {
        AdvCal objCal = new AdvCal();
        int add = objCal.add(7,10);
        int sub = objCal.sub(1,5);
        int mul = objCal.mul(9,3);
        int divide = objCal.divide(9,3);
        System.out.println("add " + add + " is sub " + sub + " mul " + mul + " div " + divide);
    }
}
