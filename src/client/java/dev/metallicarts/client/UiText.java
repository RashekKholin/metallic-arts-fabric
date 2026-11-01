package dev.metallicarts.client;
import com.google.gson.JsonParser;
import dev.metallicarts.MetallicArts;
import net.minecraft.network.chat.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Localizes known static server descriptions while preserving player-authored labels verbatim. */
public final class UiText {
 private static final Map<String,String> KEYS=load();
 private static Map<String,String> load(){try(var in=UiText.class.getResourceAsStream("/metallicarts/text_aliases.json")){if(in==null)return Map.of();var map=new HashMap<String,String>();try(var reader=new InputStreamReader(in,StandardCharsets.UTF_8)){JsonParser.parseReader(reader).getAsJsonObject().entrySet().forEach(e->map.put(e.getKey(),e.getValue().getAsString()));}return Map.copyOf(map);}catch(IOException|RuntimeException e){MetallicArts.LOGGER.warn("Cannot load optional UI text aliases; using reference text",e);return Map.of();}}
 public static Component of(String text){String key=KEYS.get(text);return key==null?Component.literal(text):Component.translatable(key);}
 private UiText(){}
}
