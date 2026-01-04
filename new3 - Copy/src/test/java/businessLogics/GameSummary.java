package businessLogics;

import java.util.HashSet;
import java.util.Set;

public class GameSummary {

    private String gameVariant;
    private Set<String> newlyAdded = new HashSet<>();
    private Set<String> alreadyAdded = new HashSet<>();
    private Set<String> notConfigured = new HashSet<>();

    public GameSummary(String gameVariant) {
        this.gameVariant = gameVariant;
    }

    public void addNewlyAdded(String brand) {
        newlyAdded.add(brand);
    }

    public void addAlreadyAdded(String brand) {
        alreadyAdded.add(brand);
    }

    public void addNotConfigured(String brand) {
        notConfigured.add(brand);
    }

    // Getters
    public String getGameVariant() { return gameVariant; }
    public String getNewlyAdded() { return String.join(", ", newlyAdded); }
    public String getAlreadyAdded() { return String.join(", ", alreadyAdded); }
    public String getNotConfigured() { return String.join(", ", notConfigured); }
}

