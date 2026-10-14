package ac.darkness.darkness.checks;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum CheckCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    WORLD("World"),
    OTHER("Other");

    private final String displayName;

    CheckCategory(String displayName) {
        this.displayName = displayName;
    }
}
