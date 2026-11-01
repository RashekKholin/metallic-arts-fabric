package dev.metallicarts.allomancy;

import com.google.gson.*;
import dev.metallicarts.domain.Metal;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public record AllomancyConfig(int schema,Map<String,Long> rates,int flareCost,double flareStrength,int detectionRange,
                              int maximumTargets,double force,double maximumPlayerSpeed,double maximumObjectSpeed) {
    /** First four network indices remain stable for existing saves and controls. */
    public static final List<Metal> SUPPORTED=List.of(Metal.STEEL,Metal.IRON,Metal.PEWTER,Metal.TIN,Metal.ZINC,Metal.BRASS,Metal.COPPER,Metal.BRONZE,
        Metal.ALUMINUM,Metal.DURALUMIN,Metal.CHROMIUM,Metal.NICROSIL,Metal.GOLD,Metal.ELECTRUM,Metal.CADMIUM,Metal.BENDALLOY);
    private static Map<String,Long> defaultRates(){var rates=new LinkedHashMap<String,Long>();for(var m:SUPPORTED)rates.put(m.id(),m==Metal.PEWTER?200L:100L);return Map.copyOf(rates);}
    public static final AllomancyConfig DEFAULT=new AllomancyConfig(1,defaultRates(),4,1.8,24,48,14,2.4,4);
    public AllomancyConfig {
        rates=Map.copyOf(rates);
        if(schema!=1||!rates.keySet().containsAll(SUPPORTED.stream().map(Metal::id).toList())||rates.values().stream().anyMatch(v->v==null||v<1||v>1_000_000)||
            flareCost<1||flareCost>20||!Double.isFinite(flareStrength)||flareStrength<1||flareStrength>4||detectionRange<4||detectionRange>32||
            maximumTargets<1||maximumTargets>64||!Double.isFinite(force)||force<=0||force>100||
            !Double.isFinite(maximumPlayerSpeed)||maximumPlayerSpeed<=0||maximumPlayerSpeed>5||
            !Double.isFinite(maximumObjectSpeed)||maximumObjectSpeed<=0||maximumObjectSpeed>10)throw new IllegalArgumentException("Invalid Allomancy settings");
    }
    public long rate(Metal metal,BurnMode mode){
        Objects.requireNonNull(mode);
        if(!SUPPORTED.contains(metal))throw new IllegalArgumentException("Unsupported Allomancy handler");
        return mode==BurnMode.OFF?0:Math.multiplyExact(rates.get(metal.id()),mode==BurnMode.FLARE?flareCost:1);
    }
    public static AllomancyConfig load(){
        Path file=FabricLoader.getInstance().getConfigDir().resolve("metallicarts-allomancy.json");
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        try{if(!Files.exists(file)){Files.createDirectories(file.getParent());Files.writeString(file,gson.toJson(DEFAULT),StandardOpenOption.CREATE_NEW);return DEFAULT;}
            var json=JsonParser.parseString(Files.readString(file)).getAsJsonObject();boolean migrated=false;
            var rates=json.getAsJsonObject("rates");if(rates==null)throw new IllegalArgumentException("Missing burn rates");
            for(var entry:DEFAULT.rates().entrySet())if(!rates.has(entry.getKey())){rates.addProperty(entry.getKey(),entry.getValue());migrated=true;}
            var config=Objects.requireNonNull(gson.fromJson(json,AllomancyConfig.class));
            if(migrated){var backup=file.resolveSibling("metallicarts-allomancy.before-phase6.json");if(!Files.exists(backup))Files.copy(file,backup);Files.writeString(file,gson.toJson(config));}
            return config;
        }catch(Exception e){throw new IllegalStateException("Cannot load "+file,e);}
    }
}
