package banking;

import org.sqlite.SQLiteDataSource;
import java.sql.*;

public class AccountsDatabase {
    private static int accountsAmount = 0;
    private static int cardNumberDefaultSize = 16;
    private static SQLiteDataSource dataSource = new SQLiteDataSource();
    private static String tableName = "card";

    public static int getAccountsAmount() {
        return accountsAmount;
    }

    public static int getCardNumberDefaultSize() {
        return cardNumberDefaultSize;
    }

    public static void setupDatabaseURL(String databaseURL) {
        dataSource.setUrl(databaseURL);
        initiateDataSource();
    }

    private static void initiateDataSource() {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            //if there is no exist database, connection checking will create the new one
            //no need to use prepared statement for CREATE query
            // Statement creation
            try (Statement statement = con.createStatement()) {
                // Statement execution
                String createTableStatement = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                        "id INT NOT NULL," +
                        "number VARCHAR(16) NOT NULL," +
                        "pin VARCHAR(4) NOT NULL," +
                        "balance INT DEFAULT 0);";
                statement.executeUpdate(createTableStatement);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }

    }

    public static void transferMoney(String cardFrom, String cardTo, int moneyToTransfer) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            // Disable auto-commit mode
            con.setAutoCommit(false);
            String updateCardFromSQL = "UPDATE " + tableName + " SET balance = balance - ? WHERE number = ?";
            String updateCardToSQL   = "UPDATE " + tableName + " SET balance = balance + ? WHERE number = ?";
            Savepoint savepoint = con.setSavepoint();

            try (PreparedStatement updateBalanceFrom = con.prepareStatement(updateCardFromSQL)) {
                updateBalanceFrom.setInt(1, moneyToTransfer);
                updateBalanceFrom.setString(2, cardFrom);
                updateBalanceFrom.executeUpdate();

                PreparedStatement updateBalanceTo = con.prepareStatement(updateCardToSQL);
                updateBalanceTo.setInt(1, moneyToTransfer);
                updateBalanceTo.setString(2, cardTo);
                updateBalanceTo.executeUpdate();

                con.commit();
            }
            catch (SQLException e) {
                con.rollback(savepoint);
                System.out.println("sql exception during update query");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
    }

    public static void deleteAccount(String cardNumber) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            // Disable auto-commit mode
            con.setAutoCommit(false);
            String deleteCardSQL = "DELETE FROM " + tableName + " WHERE number = ?";
            Savepoint savepoint = con.setSavepoint();

            try (PreparedStatement deleteBalanceFrom = con.prepareStatement(deleteCardSQL)) {
                // Create a savepoint
                deleteBalanceFrom.setString(1, cardNumber);
                deleteBalanceFrom.executeUpdate();
                con.commit();
            }
            catch (SQLException e) {
                con.rollback(savepoint);
                System.out.println("sql exception during update query");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
    }

    public static boolean isMoneyEnough(String cardFrom, int moneyToTransfer) {
        Connection con = null;
        boolean answer = false;
        try {
            con = dataSource.getConnection();
            // Disable auto-commit mode
            String selectAddressSQL = "SELECT balance FROM " + tableName + " WHERE number = ?";
            try (PreparedStatement selectAddress = con.prepareStatement(selectAddressSQL)) {
                selectAddress.setString(1, cardFrom);
                ResultSet resultSet = selectAddress.executeQuery();
                int balance = Integer.parseInt(resultSet.getString("balance"));
                if(balance >= moneyToTransfer) {
                    answer = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
        return answer;
    }

    public static boolean isCardExistInDB(String cardNumber) {
        Connection con = null;
        boolean isExist = false;
        try {
            con = dataSource.getConnection();
            String select = "SELECT * FROM " + tableName + " WHERE number = ?";;
            try (PreparedStatement preparedStatement = con.prepareStatement(select)) {
                preparedStatement.setObject(1, cardNumber);
                try (ResultSet accountData = preparedStatement.executeQuery()) {
                    if (accountData.isBeforeFirst() ) {
                        isExist = true;
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
        return isExist;
    }

    public static void addIncomeToAccount(String cardNumber, int income) {
        //no need to check existence cardNumber account in DB, because it's checked already in logging
        String updateInvoiceSQL = "UPDATE " + tableName +
                " SET balance = balance + ? WHERE number = ?";

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement insertInvoice = con.prepareStatement(updateInvoiceSQL)) {
                insertInvoice.setInt(1, income);
                insertInvoice.setString(2, cardNumber);
                insertInvoice.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addToDatabase(String cardNumber, String PIN) {
        accountsAmount++;
        Connection con = null;
        try {
            con = dataSource.getConnection();
            String insert = "INSERT INTO " + tableName + " (id, number, pin) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = con.prepareStatement(insert)) {
                preparedStatement.setInt(1, accountsAmount);
                preparedStatement.setString(2, cardNumber);
                preparedStatement.setString(3, PIN);

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
    }

    public static boolean checkPINFromDatabase(String number, String possiblePIN) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            String select = "SELECT pin FROM  " + tableName + " WHERE number = ?";;
            try (PreparedStatement preparedStatement = con.prepareStatement(select)) {
                preparedStatement.setObject(1, number);
                try (ResultSet account = preparedStatement.executeQuery()) {
                    if (account.isBeforeFirst() ) {
                        String PIN = account.getString("pin");
                        return PIN.equals(possiblePIN);
                    }
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
        return false;
    }

    public static String getBalanceFromDatabase(String number) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            String insert = "SELECT balance FROM " + tableName + " WHERE number = ?";
            try (PreparedStatement preparedStatement = con.prepareStatement(insert)) {
                preparedStatement.setObject(1, number);
                try (ResultSet account = preparedStatement.executeQuery()) {
                    if (account.isBeforeFirst()) {
                        String balance = account.getString("balance");
                        return balance;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* Ignored */}
            }
        }
        return null;
    }
}
