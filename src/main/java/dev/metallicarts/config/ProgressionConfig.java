package dev.metallicarts.config;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
/** Public world-host policy. Probabilities and costs are gameplay interpretations, not cosmere genetics. */
public record ProgressionConfig(int schema,boolean enabled,double allomancerChance,double feruchemistChance,double npcChance,int discoveryLevels,long npcFuel,boolean lerasiumRecipe,boolean mistwraithRitual) {
 public static final ProgressionConfig DEFAULT=new ProgressionConfig(1,true,.5,.5,.02,5,100000,false,true);
 private static ProgressionConfig active=DEFAULT;
 public ProgressionConfig{if(schema!=1||!Double.isFinite(allomancerChance)||!Double.isFinite(feruchemistChance)||!Double.isFinite(npcChance)||allomancerChance<0||allomancerChance>1||feruchemistChance<0||feruchemistChance>1||npcChance<0||npcChance>1||discoveryLevels<0||discoveryLevels>100||npcFuel<1||npcFuel>1000000000)throw new IllegalArgumentException("Invalid progression policy");}
 public static ProgressionConfig get(){return active;}
 public static ProgressionConfig parse(String text){var gson=new Gson();var json=JsonParser.parseString(text).getAsJsonObject();for(var entry:gson.toJsonTree(DEFAULT).getAsJsonObject().entrySet())if(!json.has(entry.getKey()))json.add(entry.getKey(),entry.getValue());PolicyJson.validate(json,ProgressionConfig.class);return gson.fromJson(json,ProgressionConfig.class);}
 public static void load(){var path=FabricLoader.getInstance().getConfigDir().resolve("metallicarts-progression.json");var gson=new GsonBuilder().setPrettyPrinting().create();try{if(!Files.exists(path))Files.writeString(path,gson.toJson(DEFAULT));var json=JsonParser.parseString(Files.readString(path)).getAsJsonObject();for(var entry:gson.toJsonTree(DEFAULT).getAsJsonObject().entrySet())if(!json.has(entry.getKey()))json.add(entry.getKey(),entry.getValue());for(var entry:json.entrySet()){var expected=gson.toJsonTree(DEFAULT).getAsJsonObject().get(entry.getKey());if(expected!=null&&(!entry.getValue().isJsonPrimitive()||expected.getAsJsonPrimitive().isBoolean()&&!entry.getValue().getAsJsonPrimitive().isBoolean()||expected.getAsJsonPrimitive().isNumber()&&!entry.getValue().getAsJsonPrimitive().isNumber()))throw new IllegalArgumentException("Wrong type: "+entry.getKey());}active=parse(gson.toJson(json));}catch(Exception e){throw new IllegalStateException("Cannot load "+path,e);}}
}

