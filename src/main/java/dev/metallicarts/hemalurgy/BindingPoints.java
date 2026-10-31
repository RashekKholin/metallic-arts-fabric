package dev.metallicarts.hemalurgy;

import com.google.gson.Gson;
import dev.metallicarts.domain.Metal;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Stable anatomical IDs and compatibility. Exact placements are explicitly inferred game policies. */
public final class BindingPoints {
    public record Point(String id,String name,String region,List<String> materials,boolean critical,String evidence,String source){
        public boolean accepts(Metal metal){return materials.contains(metal.id());}
    }
    public static final List<Point> ALL=load();
    private static List<Point> load(){
        try(var in=BindingPoints.class.getResourceAsStream("/metallicarts/binding_points.json")){
            if(in==null)throw new IllegalStateException("Missing binding points");
            var points=List.of(new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Point[].class));
            if(points.size()!=12||points.stream().map(Point::id).distinct().count()!=12)throw new IllegalStateException("Invalid anatomical catalog");
            for(var p:points)if(p.id()==null||p.name()==null||p.region()==null||p.materials()==null||!p.evidence().equals("GAMEPLAY_INTERPRETATION")||p.source()==null)
                throw new IllegalStateException("Unlabelled anatomical evidence");
            return points;
        }catch(IOException e){throw new IllegalStateException("Cannot load anatomy",e);}
    }
    private BindingPoints(){}
}
