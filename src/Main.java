import models.account.User;

public class Main {
    public static void main(String[] args) {

        // Buat objek menggunakan constructor
        User user = new User(1L, "John Doe", "john.doe@example.com", "123456789", "password123");

        // Show informasi user
        System.out.println("ID: " + user.getId());
        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println("NIP: " + user.getNip());
        System.out.println("Password: " + user.getPassword());
    }
}