package dev.metallicarts.allomancy;

import com.google.gson.*;
import dev.metallicarts.domain.Metal;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;

public record AllomancyConfig(int schema,Map<String,Long> rates,int flareCost,double flareStrength,int detectionRange,
                              int maximumTargets,double force,double maximumPlayerSpeed,double maximumObjectSpeed) {
    public static final List<Metal> SUPPORTED=List.of(Metal.STEEL,Metal.IRON,Metal.PEWTER,Metal.TIN);
    public static final AllomancyConfig DEFAULT=new AllomancyConfig(1,Map.of("steel",100L,"iron",100L,"pewter",200L,"tin",100L),4,1.8,24,48,14,2.4,4);
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
            return Objects.requireNonNull(gson.fromJson(Files.readString(file),AllomancyConfig.class));
        }catch(Exception e){throw new IllegalStateException("Cannot load "+file,e);}
    }
}
