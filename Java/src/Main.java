class Animal
{
    public void sound()
    {
        System.out.println("The animal makes sound");
    }
}

class Dog extends Animal
{
//    public void sound()
//    {
//        super.sound();
//    }

    @Override
    public void sound()
    {
        System.out.println("Barks");
    }
}

class Main
{
    public static void main(String args[])
    {
        Dog dg = new Dog();
        dg.sound();
    }
}