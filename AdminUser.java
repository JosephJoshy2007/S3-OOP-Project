// Module 6: AdminUser model
public class AdminUser extends User {
    private final String accessLevel;

    public AdminUser(String id, String username, String password, String name, String email, String accessLevel) {
        super(id, username, password, name, email, "Admin");
        this.accessLevel = accessLevel;
    }

    public AdminUser(String id, String name, String email, String accessLevel) {
        super(id, name, email, "Admin");
        this.accessLevel = accessLevel;
    }

    public String getAccessLevel() { return accessLevel; }

    @Override
    public String getDashboardGreeting() {
        return "CineBook Cinema Management Console - Logged in as " + getName() + " [" + accessLevel + "]";
    }
}
