import com.carloshdzz22.zombieinfection.config.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Focused dev.5 parsing/migration checks; no Minecraft or historic tests are started. */
public class ValidateSurvivalConfig {
    public static void main(String[] args) throws Exception {
        var defaults = GameplaySettings.parse(new Properties());
        double[] chances = {.20, .30, .40, 0, .25, .10, .15};
        int[] minima = {5, 8, 10, 0, 5, 1, 2}, maxima = {10, 15, 18, 0, 10, 3, 5};
        int index = 0;
        for (var source : InfectionSource.values()) {
            var profile = defaults.infection(source);
            check(profile.chance() == chances[index] && profile.minimum() == minima[index]
                    && profile.maximum() == maxima[index], "Preserve real default for " + source);
            var configured = GameplaySettings.parse(properties(source.prefix() + "chance=4\n"
                    + source.prefix() + "min=100000000000\n" + source.prefix() + "max=-8\n"));
            check(configured.infection(source).equals(new InfectionProfile(1, 0, 100)), "Clamp/sort " + source);
            index++;
        }
        check(defaults.maximumBlockLight() == 7, "Keep block-light safety default");
        check(GameplaySettings.parse(properties("infection.common.chance=-2")).infection(InfectionSource.COMMON).chance() == 0,
                "Negative chance clamps to zero");
        for (String bad : new String[]{"infection.common.chance=NaN", "infection.common.chance=Infinity",
                "infection.common.min=3.5", "infection.runner.max=no", "infection.commmon.min=1"}) {
            try { GameplaySettings.parse(properties(bad)); throw new AssertionError("Accepted invalid: " + bad); }
            catch (IllegalArgumentException expected) { }
        }
        Path dir = Files.createTempDirectory(Path.of(args[0]), "config-");
        Path path = dir.resolve("gameplay.properties");
        Files.writeString(path, "# My existing config\nmaximumBlockLight=6\ninfection.common.chance=0.8\n");
        var file = new SettingsFile<GameplaySettings>(path, GameplaySettings::parse);
        String template = "maximumBlockLight=7\ninfection.common.chance=0.2\ninfection.runner.chance=0.3\n";
        file.initialize(template);
        file.addMissingDefaults(template);
        String migrated = Files.readString(path);
        check(migrated.startsWith("# My existing config") && migrated.contains("infection.runner.chance=0.3"), "Append missing keys");
        check(file.current().maximumBlockLight() == 6 && file.current().infection(InfectionSource.COMMON).chance() == .8,
                "Preserve custom values");
        file.addMissingDefaults(template);
        check(Files.readString(path).equals(migrated), "Migration is idempotent");
        var valid = file.current();
        Files.writeString(path, "infection.spitterZone.chance=NaN");
        try { file.reload(); throw new AssertionError("Invalid reload succeeded"); }
        catch (IllegalArgumentException expected) { }
        check(file.current() == valid, "Failed reload preserves last valid complete snapshot");
        System.out.println("PASS: 7 real defaults, all profile clamps/ranges, invalid inputs, block-light default, migration and atomic reload.");
    }
    private static Properties properties(String input) throws Exception {
        var result = new Properties(); result.load(new java.io.StringReader(input)); return result;
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
