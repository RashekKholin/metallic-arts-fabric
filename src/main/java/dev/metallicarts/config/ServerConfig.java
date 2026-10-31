package dev.metallicarts.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.*;

/** Loaded once by each logical server's host. Invalid config fails visibly; never silently resets. */
public record ServerConfig(int schema, boolean allowPlayerHemalurgy, int snapshotCooldownTicks) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final ServerConfig DEFAULT = new ServerConfig(1, false, 20);
    public ServerConfig {
        if (schema != 1 || snapshotCooldownTicks < 1 || snapshotCooldownTicks > 1200)
            throw new IllegalArgumentException("Invalid metallicarts server config");
    }
    public static ServerConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("metallicarts-server.json");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(DEFAULT), StandardOpenOption.CREATE_NEW);
                return DEFAULT;
            }
            ServerConfig config = GSON.fromJson(Files.readString(path), ServerConfig.class);
            if (config == null) throw new IllegalArgumentException("Config cannot be null");
            return config;
        } catch (IOException | RuntimeException e) { throw new IllegalStateException("Cannot load " + path, e); }
    }
}
