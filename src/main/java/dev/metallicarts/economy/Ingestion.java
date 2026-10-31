package dev.metallicarts.economy;

import dev.metallicarts.domain.*;
import java.util.*;

public final class Ingestion {
    public static boolean canAccept(Spiritweb state, MetalItemData data) {
        return dev.metallicarts.allomancy.ReserveLedger.valid(state) && data.purity() >= 0.999999 && data.massMicrograms() <= Spiritweb.MAX_RESERVE - state.reserves().getOrDefault(data.metal(), 0L)
            && (!invested(data) || state.investedReserves().size() < 128 && state.investedReserves().stream().noneMatch(existing->existing.instance().equals(data.instance())));
    }
    public static boolean invested(MetalItemData data) { return data.charge() > 0 || data.identity().isPresent(); }
    public static Spiritweb accept(Spiritweb state, MetalItemData data) {
        if (!canAccept(state, data)) throw new IllegalArgumentException("Unsafe metal or full reserves");
        var reserves = new EnumMap<Metal, Long>(Metal.class); reserves.putAll(state.reserves());
        reserves.merge(data.metal(), data.massMicrograms(), Math::addExact);
        var invested = new ArrayList<>(state.investedReserves());
        if (invested(data)) invested.add(data);
        return new Spiritweb(state.schema(), state.identity(), state.grants(), reserves, invested);
    }
    private Ingestion() {}
}
