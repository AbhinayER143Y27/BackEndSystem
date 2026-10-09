package OOPS;

class Human
{
    private int age = 21;
    private String name = "Abhinay";

    public int getAge()
    {return age;}

    public void setAge(int newage)
    {
        age = newage;
    }

    public void setName(String newName)
    {
        name = newName;
    }

    public String getName()
    {
        return name;
    }
}
public class Encapsulation {
    public static void main(String args[])
    {
        Human obj = new Human();
        int age = obj.getAge();
        String name = obj.getName();

        System.out.println("This is the before " + name + " " + age);
        System.out.println("This is the after alright " + name + " " + age);
        obj.setAge(2222);
        obj.setName("Abhinay2");
        age = obj.getAge();
        name = obj.getName();
        System.out.println("This is the after " + name + " " + age);

    }

}
