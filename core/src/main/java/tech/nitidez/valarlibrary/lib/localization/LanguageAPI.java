package tech.nitidez.valarlibrary.lib.localization;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import tech.nitidez.valarlibrary.vLib;

public class LanguageAPI {
    private static JsonParser parser = new JsonParser();
    private static Set<LanguageAPI> HANDLERS = new HashSet<>();
    private static vLib INSTANCE = vLib.getInstance();
    private static boolean L_SETUP = false;
    private static String defaultLang = INSTANCE.getConfig().getString("default-lang");

    private String code;
    private JsonObject values;
    private JsonObject defaults;
    private LanguageAPI(String code) {
        this.code = code.toLowerCase();
        this.values = new JsonObject();
        this.defaults = new JsonObject();
        HANDLERS.add(this);
    }

    public JsonObject get() {return this.values;}

    public void insertDefaults(JsonObject json) {
        this.defaults = mergeJson(this.defaults, json);
        this.values = mergeJson(this.values, this.defaults);
        this.save();
    }

    private void save() {
        if (!L_SETUP) return;
        File pluginDir = INSTANCE.getDataFolder();
        File langDir = new File(pluginDir, "lang");
        if (!langDir.exists()) langDir.mkdirs();
        File langFile = new File(langDir, this.code+".json");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(this.values);
        try (FileWriter writer = new FileWriter(langFile)) {
            writer.write(jsonString);
        } catch (Exception e) {e.printStackTrace();}
    }

    public static Set<LanguageAPI> getLanguages() {return HANDLERS;}

    public static LanguageAPI getLanguage(String code) {return HANDLERS.stream().filter(h -> h.code.equals(code)).findFirst().orElse(new LanguageAPI(code));}
    public static boolean hasLanguage(String code) {return HANDLERS.stream().anyMatch(h -> h.code.equals(code));}

    public static void setupLanguages() {
        if (L_SETUP) return;
        L_SETUP = true;
        File pluginDir = INSTANCE.getDataFolder();
        File langDir = new File(pluginDir, "lang");
        if (!langDir.exists()) langDir.mkdirs();
        try (Stream<Path> paths = Files.walk(Paths.get(langDir.getPath()))) {
            paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase().replace(".json", "");
                try {
                    JsonObject json = parser.parse(new FileReader(path.toFile())).getAsJsonObject();
                    LanguageAPI lang = getLanguage(fileName);
                    lang.values = mergeJson(json, lang.values);
                    lang.save();
                } catch (Exception e) {}
            });
        } catch (Exception e) {}
        vlib();
    }

    public static LanguageAPI getDefaultLang() {
        if (LanguageAPI.hasLanguage(defaultLang)) return getLanguage(defaultLang);
        return null;
    }

    public static LanguageAPI getClientLang(Player plr) {
        String clang = plr.spigot().getLocale().toLowerCase();
        if (LanguageAPI.hasLanguage(clang)) return getLanguage(clang);
        return null;
    }

    private static void vlib() {
        String[] langs = new String[]{"en_us", "pt_br"};
        for (String lang : langs) {
            try (InputStream inputStream = INSTANCE.getResource("lang/"+lang+".json")) {
                if (inputStream != null) {
                    getLanguage(lang).insertDefaults(new Gson().fromJson(new JsonReader(new InputStreamReader(inputStream)), JsonObject.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static JsonObject mergeJson(JsonObject j1, JsonObject j2) {
        for (Entry<String, JsonElement> entry : j2.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (!j1.has(key)) {j1.add(key, value);} else {
                JsonElement existingV = j1.get(key);
                if (existingV.isJsonObject() && value.isJsonObject()) {
                    JsonObject mergedObj = mergeJson(existingV.getAsJsonObject(), value.getAsJsonObject());
                    j1.add(key, mergedObj);
                }
            }
        }
        return j1;
    }
}
