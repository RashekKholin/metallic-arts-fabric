package dev.metallicarts.allomancy;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import java.util.*;
/** Face-connected loaded metal is one source. Scans never generate chunks; giant networks are bounded. */
public final class ConnectedMetals {
 public record Group(BlockPos root,Set<BlockPos> members){}
 private static final class Cache{long tick;final Map<BlockPos,Group> groups=new HashMap<>();}
 private static final Map<Level,Cache> CACHES=new WeakHashMap<>();
 public static Group group(Level level,BlockPos start){var cache=CACHES.computeIfAbsent(level,l->new Cache());if(level.getGameTime()-cache.tick>=20||cache.groups.size()>65536){cache.groups.clear();cache.tick=level.getGameTime();}var old=cache.groups.get(start);if(old!=null)return old;var found=new HashSet<BlockPos>();var queued=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();queue.add(start.immutable());queued.add(start.immutable());while(!queue.isEmpty()&&found.size()<8192){var pos=queue.removeFirst();if(!level.hasChunkAt(pos)||!MetalMaterials.block(level.getBlockState(pos)))continue;found.add(pos);for(var direction:Direction.values()){var next=pos.relative(direction);if(queued.add(next))queue.addLast(next);}}var root=found.stream().min(Comparator.comparingLong(BlockPos::asLong)).orElse(start);var result=new Group(root,Set.copyOf(found));found.forEach(p->cache.groups.put(p,result));return result;}
 public static void clear(){CACHES.clear();}
 private ConnectedMetals(){}
}
