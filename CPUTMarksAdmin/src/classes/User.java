package classes;

// Base class for everyone who logs in (Lecturer and HeadOfDepartment extend this)
public abstract class User {

    private final String name;

    protected User(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    // role shown in the menus / GUI
    public abstract String getRole();

    // can this user change marks?
    public abstract boolean canEditMarks();

    @Override
    public String toString() {
        return getRole() + ": " + name;
    }
}
