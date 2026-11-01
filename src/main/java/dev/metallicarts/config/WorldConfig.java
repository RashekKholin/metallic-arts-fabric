package dev.metallicarts.config;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
public record WorldConfig(int schema,boolean researchCamps,boolean harmoniumReactions,double constructChance,int maximumNpcSessions,int scanBudget,int godmetalBurnTicks){
 public static final WorldConfig DEFAULT=new WorldConfig(1,true,true,.002,256,2048,100);
 private static WorldConfig active=DEFAULT;
 public WorldConfig{if(schema!=1||!Double.isFinite(constructChance)||constructChance<0||constructChance>1||maximumNpcSessions<16||maximumNpcSessions>1024||scanBudget<64||scanBudget>8192||godmetalBurnTicks<20||godmetalBurnTicks>1200)throw new IllegalArgumentException("Invalid world policy");}
 public static WorldConfig get(){return active;}
 public static WorldConfig parse(String text){var gson=new Gson();var json=JsonParser.parseString(text).getAsJsonObject();for(var e:gson.toJsonTree(DEFAULT).getAsJsonObject().entrySet())if(!json.has(e.getKey()))json.add(e.getKey(),e.getValue());PolicyJson.validate(json,WorldConfig.class);return gson.fromJson(json,WorldConfig.class);}
 public static void load(){var path=FabricLoader.getInstance().getConfigDir().resolve("metallicarts-world.json");try{if(!Files.exists(path))Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(DEFAULT));active=parse(Files.readString(path));}catch(Exception e){throw new IllegalStateException("Cannot load "+path,e);}}
}
