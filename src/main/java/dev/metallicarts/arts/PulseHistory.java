package dev.metallicarts.arts;
import dev.metallicarts.allomancy.AllomancyRuntime;
import dev.metallicarts.config.AdvancedConfig;
import dev.metallicarts.domain.Metal;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.*;
/** Captures real paid short burns, including concealment at emission. No private grants or Identity transmitted. */
public final class PulseHistory {
 public record Pulse(net.minecraft.world.entity.LivingEntity owner,Object level,Vec3 position,Set<Metal> metals,double concealment,int tick){}
 private static final Map<UUID,ArrayDeque<Pulse>> HISTORY=new HashMap<>();
 public static void record(MinecraftServer server){int tick=server.getTickCount();for(var p:server.getPlayerList().getPlayers()){
  var metals=EnumSet.noneOf(Metal.class);metals.addAll(AllomancyRuntime.powers(p).keySet());metals.remove(Metal.COPPER);if(metals.isEmpty())continue;
  if(HISTORY.size()>=1024&&!HISTORY.containsKey(p.getUUID()))continue;
  var queue=HISTORY.computeIfAbsent(p.getUUID(),u->new ArrayDeque<>());var pulse=new Pulse(p,p.level(),p.position(),Set.copyOf(metals),AdvancedAllomancy.concealment(p),tick);
  var old=queue.peekLast();if(old!=null&&old.metals().equals(pulse.metals())&&old.concealment()==pulse.concealment()&&old.level()==pulse.level()&&old.position().distanceToSqr(pulse.position())<1)queue.removeLast();queue.addLast(pulse);while(queue.size()>32)queue.removeFirst();
 }
 for(var mob:NpcArts.actors()){var metals=EnumSet.noneOf(Metal.class);metals.addAll(NpcArts.powers(mob).keySet());metals.remove(Metal.COPPER);if(metals.isEmpty())continue;var q=HISTORY.computeIfAbsent(mob.getUUID(),u->new ArrayDeque<>());q.addLast(new Pulse(mob,mob.level(),mob.position(),Set.copyOf(metals),NpcArts.cloudAt(mob),tick));while(q.size()>32)q.removeFirst();}
 HISTORY.values().forEach(q->q.removeIf(p->tick-p.tick()>=AdvancedConfig.get().detection().pulseLifetime()||p.owner().isRemoved()||!p.owner().isAlive()));HISTORY.values().removeIf(Deque::isEmpty);}
 public static List<Pulse> detectable(ServerPlayer seeker,double strength){var result=new ArrayList<Pulse>();double range=Math.min(AdvancedConfig.get().detection().range(),12+3*strength);for(var q:HISTORY.values())for(var pulse:q.reversed()){
  if(pulse.owner()==seeker||pulse.level()!=seeker.level()||pulse.owner().level()!=pulse.level()||seeker.distanceToSqr(pulse.position())>range*range||Math.max(pulse.concealment(),pulse.owner() instanceof ServerPlayer player?AdvancedAllomancy.concealment(player):NpcArts.cloudAt(pulse.owner()))>=strength)continue;
  result.add(pulse);break;
 }result.sort(Comparator.comparingDouble(p->seeker.distanceToSqr(p.position())));return result.stream().limit(4).toList();}
 public static void clear(){HISTORY.clear();}
 private PulseHistory(){}
}
