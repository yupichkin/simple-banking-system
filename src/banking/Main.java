package banking;

public class Main {

    public static String defaultPathtoDB = "Simple Banking System\\task\\src\\banking\\";
    public static String defaultDBName = "chinook.db";

    public static void main(String[] args) {
        String dbName = defaultDBName;
        String pathToDBFolder = defaultPathtoDB;

        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-fileName")) {
                if(i + 1 == args.length) {
                    throw new IllegalArgumentException("no database name in program arguments");
                }
                dbName = args[i + 1];
                pathToDBFolder = "";
            }
        }
        String url = "jdbc:sqlite:" + pathToDBFolder + dbName;
        AccountsDatabase.setupDatabaseURL(url);
        Menu.mainMenu();
    }
}
