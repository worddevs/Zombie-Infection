import com.carloshdzz22.zombieinfection.config.GameplaySettings;
import com.carloshdzz22.zombieinfection.config.SettingsFile;
import com.carloshdzz22.zombieinfection.client.config.HealthBarSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Small standalone regression check: no game, graphics or network required. */
public class ValidateConfiguration {
    public static void main(String[] args) throws Exception {
        var defaults = GameplaySettings.parse(new Properties());
        check(defaults.naturalSpawning() && defaults.densityMultiplier() == 1
                && defaults.dayCommonMultiplier() == 0.55 && defaults.totalCap(12) == 12,
                "Development defaults must preserve existing balance");
        for (String invalid : new String[]{"densityMultiplier=NaN", "densityMultiplier=Infinity",
                "densityMultiplier=-1", "densityMultiplier=3", "naturalSpawning=maybe",
                "minimumPlayerDistance=23", "maximumBlockLight=7.5", "dayComonMultiplier=0.5"}) {
            rejects(() -> GameplaySettings.parse(properties(invalid)));
        }
        var reduced = GameplaySettings.parse(properties("totalCapMultiplier=0.25\nspecialCapMultiplier=2"));
        check(reduced.totalCap(12) == 3 && reduced.specialCap(7, 3) == 3, "Special cap cannot exceed total");
        var disabled = GameplaySettings.parse(properties("specialCapMultiplier=0"));
        check(disabled.specialCap(1, 12) == 0, "Special cap can be disabled");
        var highest = GameplaySettings.parse(properties("totalCapMultiplier=2\nspecialCapMultiplier=2"));
        check(highest.totalCap(40) == 80 && highest.specialCap(7, 80) == 14, "Bounded largest population");
        check(HealthBarSettings.parse(new Properties()).range() == 16, "Client default range");
        rejects(() -> HealthBarSettings.parse(properties("range=50")));
        rejects(() -> HealthBarSettings.parse(properties("scale=NaN")));
        rejects(() -> HealthBarSettings.parse(properties("fadeSeconds=0")));
        rejects(() -> HealthBarSettings.parse(properties("enabled=yes")));

        Path directory = Files.createTempDirectory(Path.of(args[0]), "settings-check-");
        Path path = directory.resolve("gameplay.properties");
        var file = new SettingsFile<GameplaySettings>(path, GameplaySettings::parse);
        file.initialize("densityMultiplier=0.5\n");
        check(file.current().densityMultiplier() == 0.5, "Initial creation loads saved settings");
        file.initialize("densityMultiplier=2\n");
        check(file.current().densityMultiplier() == 0.5, "Initialization preserves existing files");
        var before = file.current();
        Files.writeString(path, "densityMultiplier=NaN\n");
        rejects(file::reload);
        check(file.current() == before && Files.readString(path).contains("NaN"),
                "Invalid reload preserves active snapshot and original file");
        Files.writeString(path, "densityMultiplier=1.25\nnaturalSpawning=false\n");
        file.reload();
        check(file.current().densityMultiplier() == 1.25 && !file.current().naturalSpawning(),
                "Valid reload replaces all settings together");
        before = file.current();
        Files.move(path, directory.resolve("saved.properties"));
        try {
            file.reload();
            throw new AssertionError("Missing file reload must fail");
        } catch (IOException expected) {
            check(file.current() == before, "I/O failures preserve active settings");
        }
        System.out.println("PASS: strict validation, bounded caps, client settings, safe creation and atomic reloads");
    }

    private static Properties properties(String text) throws IOException {
        var result = new Properties();
        result.load(new java.io.StringReader(text));
        return result;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void rejects(Checked action) throws Exception {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Invalid input was accepted");
    }

    private interface Checked { void run() throws Exception; }
}
