package dev.metallicarts.client;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
/** Client presentation only; no effect on server ownership, payments or penalties. */
public final class ClientPreferences {
 public static final String[] NAMES={"brighterVision","louderSound","showOverload","bronzeSounds","effectParticles","showCues","highContrast"};
 private static final boolean[] VALUES={true,true,true,true,true,true,false};
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("metallicarts-client.json");
 static{try{if(Files.exists(FILE)){var json=JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();for(int i=0;i<NAMES.length;i++)if(json.has(NAMES[i])){var value=json.get(NAMES[i]);if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("Expected boolean "+NAMES[i]);VALUES[i]=value.getAsBoolean();}}else save();}catch(Exception e){throw new IllegalStateException("Cannot load "+FILE,e);}}
 public static boolean value(int i){return VALUES[i];}
 public static void toggle(int i){boolean before=VALUES[i];VALUES[i]=!before;try{save();}catch(RuntimeException e){VALUES[i]=before;throw e;}}
 private static void save(){try{Files.createDirectories(FILE.getParent());var json=Files.exists(FILE)?JsonParser.parseString(Files.readString(FILE)).getAsJsonObject():new JsonObject();for(int i=0;i<NAMES.length;i++)json.addProperty(NAMES[i],VALUES[i]);var temp=FILE.resolveSibling(FILE.getFileName()+".tmp");Files.writeString(temp,new GsonBuilder().setPrettyPrinting().create().toJson(json));try{Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}}catch(Exception e){throw new IllegalStateException("Cannot save client preferences",e);}}
 private ClientPreferences(){}
}
