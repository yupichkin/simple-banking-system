package banking;

import java.util.Random;
import java.util.Scanner;

public class Menu {
    private static String input;
    public static Scanner scanner = new Scanner(System.in);

    public static void mainMenu() {
        input = null;
        while(!"0".equals(input)) {
            System.out.println("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit");
            input = scanner.nextLine();
            switch (input) {
                case "1":
                    String cardNumber = generateCardNumber();
                    String password = generatePIN();
                    while(AccountsDatabase.isCardExistInDB(cardNumber)) { //for generating unique card numbers
                        cardNumber = generateCardNumber();
                    }
                    AccountsDatabase.addToDatabase(cardNumber, password);
                    System.out.println("Your card has been created\n" +
                            "Your card number:\n" +
                            cardNumber + "\n" +
                            "Your card PIN:\n" +
                            password);
                    break;
                case "2":
                    System.out.println("Enter your card number:");
                    String inputCardNumber = scanner.nextLine().trim();
                    System.out.println("Enter your PIN:");
                    String possiblePIN = scanner.nextLine().trim();
                    if(AccountsDatabase.checkPINFromDatabase(inputCardNumber, possiblePIN)) {
                        if(loggedInMenu(inputCardNumber)) {
                            break;
                        }
                    } else {
                        System.out.println("Wrong card number or PIN!");
                    }
                    break;
                default:
                    break;
            }
        }
        System.out.println("Bye!");
    }

    private static boolean loggedInMenu(String cardNumber) {
        System.out.println("You have successfully logged in!");
        input = null;
        while(!"0".equals(input)) {
            System.out.println("1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Close account\n" +
                    "5. Log out\n" +
                    "0. Exit");
            input = scanner.nextLine();
            switch (input) {
                case "1":
                    String Balance = AccountsDatabase.getBalanceFromDatabase(cardNumber);
                    System.out.println("Balance: " + Balance);
                    break;
                case "2":
                    System.out.println("Enter income:");
                    int incomeInput = Integer.parseInt(scanner.nextLine());
                    AccountsDatabase.addIncomeToAccount(cardNumber, incomeInput);
                    System.out.println("Income was added!");
                    break;
                case "3":
                    System.out.println("Transfer\n" +
                            "Enter card number:");
                    String transferCardNumber = scanner.nextLine();
                    if(transferCardNumber.length() != AccountsDatabase.getCardNumberDefaultSize() || !checkCardForLuhn(transferCardNumber)) {
                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                        break;
                    }
                    if(!AccountsDatabase.isCardExistInDB(transferCardNumber)) {
                        System.out.println("Such a card does not exist.");
                        break;
                    }
                    System.out.println("Enter how much money you want to transfer:");
                    int moneyToTransfer = Integer.parseInt(scanner.nextLine());
                    if(!AccountsDatabase.isMoneyEnough(cardNumber, moneyToTransfer)) {
                        System.out.println("Not enough money!");
                        break;
                    }
                    AccountsDatabase.transferMoney(cardNumber, transferCardNumber, moneyToTransfer);
                    System.out.println("Success!");
                    break;
                case "4":
                    AccountsDatabase.deleteAccount(cardNumber);
                    return false; //logged out after deleting
                case "5":
                    System.out.println("You have successfully logged out!");
                    return false; //logged out
                default:
                    break;
            }
        }
        return true; //exit
    }

    private static String generatePIN() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    private static String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        cardNumber.append("400000"); //BIN part of card Number
        for (int i = 0; i < 9; i++) {
            cardNumber.append(random.nextInt(10));
        }
        String first15Digits = cardNumber.toString();
        int lastDigit = luhnAlgorithm(stringToIntArray(first15Digits));
        return first15Digits + String.format("%d", lastDigit);
    }

    private static int[] stringToIntArray(String str) {
        String[] splited = str.split("");
        int[] numbers = new int[splited.length];
        for(int i = 0; i < splited.length; i++) {
            numbers[i] = Integer.parseInt(splited[i]);
        }
        return numbers;
    }

    private static int luhnAlgorithm(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i ++) {
            if (i % 2 == 0) {
                array[i] *= 2;
                if (array[i] > 9) {
                    array[i] -= 9;
                }
            }
            sum += array[i];
        }
        return sum % 10 == 0 ? 0 : 10 - (sum % 10);
    }

    private static boolean checkCardForLuhn(String cardNumber) {
        int[] first15Digits = stringToIntArray(cardNumber.substring(0, cardNumber.length() - 1));
        int lastDigit = cardNumber.charAt(cardNumber.length() - 1) - '0';
        return lastDigit == luhnAlgorithm(first15Digits);
    }
}
