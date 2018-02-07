public class Fruit extends Food {
    @Override
    public String eaten(Animal animal) {
        return "animal eats fruit";
    }

    @Override
    public String eaten(Dog dog) {
        return "dog eats fruit";
    }

    @Override
    public String eaten(Cat cat) {
        return "cat eats fruit";
    }

}
