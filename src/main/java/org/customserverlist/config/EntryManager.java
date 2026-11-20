package org.customserverlist.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.neoforged.fml.loading.FMLPaths;
import org.customserverlist.CustomServerList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EntryManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIRECTORY = FMLPaths.CONFIGDIR.get()
            .resolve("customserverlist").resolve("entries");
    private static final Map<String, CustomEntry> ENTRIES = new LinkedHashMap<>();

    private EntryManager() {}

    public static Path directory() { return DIRECTORY; }

    public static void reloadForList() {
        try {
            load();
        } catch (IOException e) {
            CustomServerList.LOGGER.warn("Could not load custom entries", e);
        }
    }

    private static void validateId(String id) {
        if (id == null || !id.matches("[a-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("ID must contain 1-64 lowercase letters, digits, _ or -");
        }
    }

    private static Path pathFor(String id) {
        validateId(id);
        return DIRECTORY.resolve(id + ".json");
    }

    private static void validate(CustomEntry entry) {
        if (entry == null) throw new IllegalArgumentException("Entry cannot be null");
        validateId(entry.id);
        if (entry.name == null || entry.name.isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        if (entry.display == null || !List.of("singleplayer", "multiplayer", "both").contains(entry.display)) {
            throw new IllegalArgumentException("Invalid display value");
        }
        if (entry.action == null || !List.of("none", "open_url").contains(entry.action)) {
            throw new IllegalArgumentException("Invalid action value");
        }
        if ("open_url".equals(entry.action)) {
            try {
                URI uri = new URI(entry.url);
                if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                        || uri.getHost() == null) throw new IllegalArgumentException("A valid HTTP(S) URL is required");
            } catch (URISyntaxException | NullPointerException e) {
                throw new IllegalArgumentException("A valid HTTP(S) URL is required", e);
            }
        }
    }

    public static synchronized void load() throws IOException {
        Files.createDirectories(DIRECTORY);
        Map<String, CustomEntry> loaded = new LinkedHashMap<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(DIRECTORY, "*.json")) {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) continue;
                String name = file.getFileName().toString();
                String id = name.substring(0, name.length() - 5);
                try {
                    String json = Files.readString(file, StandardCharsets.UTF_8);
                    CustomEntry entry = GSON.fromJson(json, CustomEntry.class);
                    validate(entry);
                    if (!id.equals(entry.id)) throw new IllegalArgumentException("Filename and JSON ID differ");
                    loaded.put(id, entry.copy());
                } catch (JsonParseException | IllegalArgumentException e) {
                    throw new IOException("Invalid entry file: " + file, e);
                }
            }
        }
        ENTRIES.clear();
        ENTRIES.putAll(loaded);
    }

    private static void write(CustomEntry entry) throws IOException {
        Files.createDirectories(DIRECTORY);
        Path target = pathFor(entry.id);
        Path temp = Files.createTempFile(DIRECTORY, ".entry-", ".tmp");
        try {
            Files.writeString(temp, GSON.toJson(entry), StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public static synchronized void add(CustomEntry entry) throws IOException {
        validate(entry);
        if (ENTRIES.containsKey(entry.id) || Files.exists(pathFor(entry.id))) {
            throw new IllegalArgumentException("ID already exists: " + entry.id);
        }
        CustomEntry copy = entry.copy();
        write(copy);
        ENTRIES.put(copy.id, copy);
    }

    public static synchronized void update(CustomEntry entry) throws IOException {
        validate(entry);
        CustomEntry old = ENTRIES.get(entry.id);
        if (old == null) throw new IllegalArgumentException("Entry not found: " + entry.id);
        if (!old.allowEdit) throw new IllegalStateException("Editing is disabled for this entry");
        CustomEntry copy = entry.copy();
        write(copy);
        ENTRIES.put(copy.id, copy);
    }

    public static synchronized boolean remove(String id) throws IOException {
        CustomEntry old = ENTRIES.get(id);
        if (old == null || !old.allowDelete) return false;
        Files.deleteIfExists(pathFor(id));
        ENTRIES.remove(id);
        return true;
    }

    public static synchronized CustomEntry get(String id) {
        CustomEntry entry = ENTRIES.get(id);
        return entry == null ? null : entry.copy();
    }

    public static synchronized List<CustomEntry> forScreen(String screen) {
        List<CustomEntry> result = new ArrayList<>();
        for (CustomEntry entry : ENTRIES.values()) {
            if (entry.enabled && (screen.equals(entry.display) || "both".equals(entry.display))) {
                result.add(entry.copy());
            }
        }
        result.sort(Comparator.comparing((CustomEntry e) -> !e.pinned)
                .thenComparingInt(e -> e.sortOrder).thenComparing(e -> e.id));
        return result;
    }

    public static synchronized List<CustomEntry> all() {
        List<CustomEntry> result = new ArrayList<>();
        ENTRIES.values().forEach(e -> result.add(e.copy()));
        result.sort(Comparator.comparing((CustomEntry e) -> !e.pinned)
                .thenComparingInt(e -> e.sortOrder).thenComparing(e -> e.id));
        return result;
    }
}
