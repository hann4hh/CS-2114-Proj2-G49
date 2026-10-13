import java.util.Scanner;

/** Entry point only: builds a MelodyApp on the console and starts the menu loop. */
public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new MelodyApp(scanner).run();
        }
    }
}
