import java.util.Random;

class CarrotGame {
    private static final int NUM_RABBITS = 5;
    private static final int NUM_BOXES = 100;
    private static final int CARROT_RATE = 300;
    private static final int CARROT_TIMEOUT = 600;
    private static final int RABBIT_SLEEP = 100;

    public static void main(String[] args) {
        CarrotGame carrotGame = new CarrotGame();
        carrotGame.startGame();
    }

    private void startGame() {
        Box[] boxes = new Box[NUM_BOXES];
        for (int i = 0; i < NUM_BOXES; i++) {
            boxes[i] = new Box(i);
        }

        Person person = new Person(boxes);
        Thread personThread = new Thread(person);
        personThread.start();

        Rabbit[] rabbits = new Rabbit[NUM_RABBITS];
        for (int i = 0; i < NUM_RABBITS; i++) {
            rabbits[i] = new Rabbit("Rabbit" + (i + 1), boxes);
            Thread rabbitThread = new Thread(rabbits[i]);
            rabbitThread.start();
        }

        for (int i = 0; i < rabbits.length; i++) {
            try {
                Thread rabbitThread = new Thread(rabbits[i]);
                rabbitThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Box {
        private int number;
        private boolean hasCarrot = false;

        public Box(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        public boolean hasCarrot() {
            return hasCarrot;
        }

        public void putCarrot() {
            hasCarrot = true;
            System.out.println("Person puts carrot to box " + number);
        }

        public void removeCarrot() {
            hasCarrot = false;
            System.out.println("Carrot in box " + number + " removed");
        }
    }

    static class Rabbit implements Runnable {
        private String name;
        private Box[] boxes;
        private int score = 0;

        public Rabbit(String name, Box[] boxes) {
            this.name = name;
            this.boxes = boxes;
        }

        @Override
        public void run() {
            while (!boxes[NUM_BOXES - 1].hasCarrot()) {
                int currentBox = jump();
                if (boxes[currentBox].hasCarrot()) {
                    eatCarrot(currentBox);
                }
                try {
                    Thread.sleep(RABBIT_SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(name + " has " + score + " points");
        }

        private int jump() {
            Random random = new Random();
            int currentBox = random.nextInt(NUM_BOXES - 1);
            int nextBox = currentBox + 1;

            synchronized (boxes[currentBox]) {
                synchronized (boxes[nextBox]) {
                    System.out.println(name + " jumps to box " + nextBox);
                }
            }

            return nextBox;
        }

        private void eatCarrot(int boxNumber) {
            synchronized (boxes[boxNumber]) {
                score++;
                System.out.println(name + " eats carrot in box " + boxNumber);
                boxes[boxNumber].removeCarrot();
            }
        }
    }

    static class Person implements Runnable {
        private Box[] boxes;

        public Person(Box[] boxes) {
            this.boxes = boxes;
        }

        @Override
        public void run() {
            while (!boxes[NUM_BOXES - 1].hasCarrot()) {
                int boxNumber = produceCarrot();
                try {
                    Thread.sleep(CARROT_TIMEOUT);
                    boxes[boxNumber].removeCarrot();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private int produceCarrot() {
            Random random = new Random();
            int boxNumber = random.nextInt(NUM_BOXES);
            synchronized (boxes[boxNumber]) {
                boxes[boxNumber].putCarrot();
            }
            return boxNumber;
        }
    }
}
