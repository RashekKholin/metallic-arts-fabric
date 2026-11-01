package dev.metallicarts.config;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.*;

/** Loaded once by each logical server's host. Invalid config fails visibly; never silently resets. */
public record ServerConfig(int schema, boolean allowPlayerHemalurgy, int snapshotCooldownTicks, boolean enableAccountPowers) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final ServerConfig DEFAULT = new ServerConfig(1, false, 20, true);
    public ServerConfig {
        if (schema != 1 || snapshotCooldownTicks < 1 || snapshotCooldownTicks > 1200)
            throw new IllegalArgumentException("Invalid metallicarts server config");
    }
    public ServerConfig(int schema,boolean allowPlayerHemalurgy,int snapshotCooldownTicks){this(schema,allowPlayerHemalurgy,snapshotCooldownTicks,true);}
    public static ServerConfig parse(String text){
        var json=JsonParser.parseString(text).getAsJsonObject();
        if(!json.has("enableAccountPowers"))json.addProperty("enableAccountPowers",true);
        if(!json.get("enableAccountPowers").isJsonPrimitive()||!json.get("enableAccountPowers").getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("enableAccountPowers must be true or false");
        return GSON.fromJson(json,ServerConfig.class);
    }
    public static ServerConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("metallicarts-server.json");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(DEFAULT), StandardOpenOption.CREATE_NEW);
                return DEFAULT;
            }
            String original=Files.readString(path);
            ServerConfig config=parse(original);
            if(!JsonParser.parseString(original).getAsJsonObject().has("enableAccountPowers")){
                var backup=path.resolveSibling("metallicarts-server.before-account-powers.json");
                if(!Files.exists(backup))Files.copy(path,backup);
                Files.writeString(path,GSON.toJson(config));
            }
            if (config == null) throw new IllegalArgumentException("Config cannot be null");
            return config;
        } catch (IOException | RuntimeException e) { throw new IllegalStateException("Cannot load " + path, e); }
    }
}
