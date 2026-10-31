package dev.metallicarts.client;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
/** Local sensory options. These never alter server ownership, costs or overload penalties. */
public record TinOptions(boolean brighterVision,boolean louderSound,boolean showOverload) {
    public static final TinOptions INSTANCE=load();
    private static TinOptions load(){
        var path=FabricLoader.getInstance().getConfigDir().resolve("metallicarts-client.json");
        var gson=new GsonBuilder().setPrettyPrinting().create();
        try{
            if(!Files.exists(path)){var defaults=new TinOptions(true,true,true);Files.writeString(path,gson.toJson(defaults),StandardOpenOption.CREATE_NEW);return defaults;}
            return java.util.Objects.requireNonNull(gson.fromJson(Files.readString(path),TinOptions.class));
        }catch(Exception e){throw new IllegalStateException("Cannot load "+path,e);}
    }
}
