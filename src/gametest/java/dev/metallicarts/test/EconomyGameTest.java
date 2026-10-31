package dev.metallicarts.test;

import dev.metallicarts.domain.*;
import dev.metallicarts.economy.*;
import dev.metallicarts.metallurgy.*;
import dev.metallicarts.registry.ModRegistries;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class EconomyGameTest {
    @GameTest(maxTicks=400) public void unemployedVillagerClaimsWorkstation(GameTestHelper c) {
        for(int x=0;x<6;x++)for(int z=0;z<6;z++)c.setBlock(x,0,z,Blocks.STONE);
        var server=c.getLevel().getServer();server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"time set 2000");
        c.setBlock(new BlockPos(1,1,1),EconomyContent.TABLE);
        var villager=c.spawn(net.minecraft.world.entity.EntityType.VILLAGER,new BlockPos(2,1,2));
        c.succeedWhen(()->c.assertTrue(villager.getVillagerData().profession().is(EconomyWorld.PROFESSIONS.getFirst()),"Unemployed villager claims Metallurgy Table"));
    }
    @GameTest public void changingSpecialtyKeepsInventory(GameTestHelper c) {
        BlockPos pos=new BlockPos(1,1,1);c.setBlock(pos,EconomyContent.TABLE);
        var original=c.getBlockEntity(pos,MetallurgyBlockEntity.class);original.setItem(0,new ItemStack(Items.GOLD_INGOT,3));
        c.getLevel().setBlock(c.absolutePos(pos),c.getBlockState(pos).setValue(MetallurgyBlock.SPECIALTY,2),3);
        var changed=c.getBlockEntity(pos,MetallurgyBlockEntity.class);
        c.assertTrue(changed==original&&changed.getItem(0).getCount()==3,"Specialty switch must preserve machine data");
        c.succeed();
    }
    @GameTest public void independentServerPlayersNeverShareReserves(GameTestHelper c) {
        var server=c.getLevel().getServer();
        var a=new net.minecraft.server.level.ServerPlayer(server,c.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"IsolationA"),net.minecraft.server.level.ClientInformation.createDefault());
        var b=new net.minecraft.server.level.ServerPlayer(server,c.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"IsolationB"),net.minecraft.server.level.ClientInformation.createDefault());
        var first=a.getAttachedOrCreate(ModRegistries.SPIRITWEB);var second=b.getAttachedOrCreate(ModRegistries.SPIRITWEB);
        var payload=new MetalItemData(1,UUID.randomUUID(),Metal.IRON,10000,1,0,Optional.empty());
        a.setAttached(ModRegistries.SPIRITWEB,Ingestion.accept(first,payload));
        c.assertTrue(!first.identity().equals(second.identity()),"Distinct Identity tokens");
        c.assertTrue(b.getAttached(ModRegistries.SPIRITWEB).reserves().isEmpty(),"Second player unaffected");
        c.succeed();
    }
    private static List<ItemStack> inputs() { return new ArrayList<>(List.of(ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY)); }
    private static ItemStack water() { ItemStack s=new ItemStack(Items.POTION); s.set(DataComponents.POTION_CONTENTS,new PotionContents(Potions.WATER));return s; }
    private static long units(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var form=EconomyContent.form(stack.getItem());
        if (form!=null) return (long)form.units()*stack.getCount();
        String path=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        if (path.startsWith("raw_")||path.endsWith("_concentrate")||stack.is(Items.COAL)||stack.is(Items.CHARCOAL)||stack.is(Items.SAND)) return 900L*stack.getCount();
        if (stack.is(EconomyContent.SLAG))return stack.getCount();
        throw new AssertionError("Unaccounted material "+path);
    }
    @GameTest public void allRecipesConserveMass(GameTestHelper c) {
        c.assertTrue(MetallurgyRecipes.all().size()==485,"Full recipe catalog including earrings loaded");
        for(var recipe:MetallurgyRecipes.all()) {
            if(recipe.slice())continue; // The invested path is checked separately with residual mass and charge.
            var in=inputs();int slot=0;
            if(recipe.alloy()!=null) {
                for(var e:recipe.ratio().entrySet())in.set(slot++,new ItemStack(EconomyContent.item(e.getKey(),"portion"),e.getValue()));
            }else{
                for(var ingredient:recipe.inputs()) {
                    int count=ingredient.count();Item item=ingredient.item()!=null?
                        BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(ingredient.item())):
                        BuiltInRegistries.ITEM.stream().filter(candidate->ingredient.matches(new ItemStack(candidate))).findFirst().orElseThrow();
                    while(count>0){int n=Math.min(99,count);in.set(slot++,new ItemStack(item,n));count-=n;}
                }
            }
            var plan=MetallurgyRecipes.plan(recipe,in);c.assertTrue(plan!=null,"Recipe executable: "+recipe.id());
            long before=in.stream().mapToLong(EconomyGameTest::units).sum(),after=plan.outputs().stream().mapToLong(EconomyGameTest::units).sum();
            c.assertTrue(before==after,"Mass conserved: "+recipe.id()+" "+before+" -> "+after);
        }
        c.succeed();
    }
    @GameTest public void brewingUsesOneFlakePerBottle(GameTestHelper c) {
        for(int count=1;count<=3;count++) {
            BlockPos pos=new BlockPos(count,1,1);c.setBlock(pos,Blocks.BREWING_STAND);
            var stand=c.getBlockEntity(pos,BrewingStandBlockEntity.class);stand.setItem(3,new ItemStack(EconomyContent.item("iron","flakes"),5));stand.setItem(4,new ItemStack(Items.BLAZE_POWDER));
            for(int i=0;i<count;i++)stand.setItem(i,water());
            for(int tick=0;tick<=400;tick++)BrewingStandBlockEntity.serverTick(c.getLevel(),c.absolutePos(pos),c.getBlockState(pos),stand);
            c.assertTrue(stand.getItem(3).getCount()==5-count,"Unused flakes preserved");
            for(int i=0;i<count;i++)c.assertTrue(stand.getItem(i).is(EconomyContent.item("iron","vial")),"Water converted to vial");
            var saved=stand.saveWithoutMetadata(c.getLevel().registryAccess());c.assertTrue(saved.getByteOr("Fuel",(byte)0)==19,"One normal blaze fuel use");
        }
        c.succeed();
    }
    @GameTest public void scarceFlakesFillOnlyAvailablePortions(GameTestHelper c) {
        var items=NonNullList.withSize(5,ItemStack.EMPTY);
        for(int i=0;i<3;i++)items.set(i,water());items.set(3,new ItemStack(EconomyContent.item("steel","flakes"),1));
        MetalBrewing.brew(items);
        c.assertTrue(items.get(0).is(EconomyContent.item("steel","vial"))&&MetalBrewing.water(items.get(1))&&MetalBrewing.water(items.get(2)),"One flake cannot fill three vials");
        c.assertTrue(items.get(3).isEmpty(),"Exactly one flake consumed");
        MetalBrewing.brew(items);c.assertTrue(MetalBrewing.water(items.get(1)),"Empty ingredient cannot duplicate metal");
        c.succeed();
    }
    @GameTest public void allSixteenVialsAndInvestedPayload(GameTestHelper c) {
        for(Metal m:Metal.values()){
            var items=NonNullList.withSize(5,ItemStack.EMPTY);items.set(0,water());items.set(3,new ItemStack(EconomyContent.item(m.id(),"flakes")));
            MetalBrewing.brew(items);c.assertTrue(items.get(0).get(ModRegistries.METAL_DATA).metal()==m,"Typed vial "+m);
        }
        var data=new MetalItemData(1,UUID.randomUUID(),Metal.GOLD,Materials.PORTION_MASS,1,39,Optional.of(UUID.randomUUID()));
        var items=NonNullList.withSize(5,ItemStack.EMPTY);items.set(0,water());ItemStack flakes=new ItemStack(EconomyContent.item("gold","flakes"));flakes.set(ModRegistries.METAL_DATA,data);items.set(3,flakes);
        MetalBrewing.brew(items);c.assertValueEqual(data,items.get(0).get(ModRegistries.METAL_DATA),"Identity, charge and instance survive brewing");
        var player=c.makeMockServerPlayerInLevel();
        ItemStack bottle=items.get(0).finishUsingItem(c.getLevel(),player);
        c.assertTrue(bottle.is(Items.GLASS_BOTTLE),"Drinking returns bottle");
        c.assertTrue(player.getAttachedOrCreate(ModRegistries.SPIRITWEB).investedReserves().contains(data),"Ingested charge preserved");
        c.succeed();
    }
    @GameTest public void unsafeAndNonwaterInputsRefused(GameTestHelper c) {
        var items=NonNullList.withSize(5,ItemStack.EMPTY);items.set(0,water());
        ItemStack flakes=new ItemStack(EconomyContent.item("steel","flakes"));flakes.set(EconomyContent.COMPOSITION,new BatchComposition(Map.of("iron",50,"carbon",50),0.52));items.set(3,flakes);
        c.assertFalse(MetalBrewing.canBrew(items),"Invalid alloy cannot be brewed");
        items.set(3,new ItemStack(EconomyContent.item("silver","flakes")));c.assertFalse(MetalBrewing.canBrew(items),"Non-Allomantic flakes refused");
        items.set(3,new ItemStack(EconomyContent.item("iron","flakes")));items.set(0,new ItemStack(Items.GLASS_BOTTLE));c.assertFalse(MetalBrewing.canBrew(items),"Empty bottle is not water");
        c.succeed();
    }
    @GameTest public void vanillaBrewingStillWorks(GameTestHelper c) {
        BlockPos pos=new BlockPos(1,1,1);c.setBlock(pos,Blocks.BREWING_STAND);var stand=c.getBlockEntity(pos,BrewingStandBlockEntity.class);
        for(int i=0;i<3;i++)stand.setItem(i,water());stand.setItem(3,new ItemStack(Items.NETHER_WART));stand.setItem(4,new ItemStack(Items.BLAZE_POWDER));
        for(int tick=0;tick<=400;tick++)BrewingStandBlockEntity.serverTick(c.getLevel(),c.absolutePos(pos),c.getBlockState(pos),stand);
        c.assertTrue(stand.getItem(0).get(DataComponents.POTION_CONTENTS).is(Potions.AWKWARD),"Vanilla recipe unchanged");c.succeed();
    }
    @GameTest public void blockedOutputsNeverConsumeInputs(GameTestHelper c) {
        BlockPos pos=new BlockPos(1,1,1);c.setBlock(pos,EconomyContent.TABLE);var table=c.getBlockEntity(pos,MetallurgyBlockEntity.class);
        table.tab=2;table.setItem(0,new ItemStack(Items.IRON_NUGGET));
        var candidates=MetallurgyRecipes.candidates(2,table.inputs());for(int i=0;i<candidates.size();i++)if(candidates.get(i).id().equals("iron_nugget_to_portions"))table.selection=i;
        table.setItem(4,new ItemStack(Items.COAL));for(int i=5;i<9;i++)table.setItem(i,new ItemStack(Items.COBBLESTONE,64));
        table.running=true;for(int t=0;t<100;t++)MetallurgyBlockEntity.tick(c.getLevel(),c.absolutePos(pos),c.getBlockState(pos),table);
        c.assertTrue(table.getItem(0).getCount()==1&&table.getItem(4).getCount()==1,"Full output consumes neither input nor fuel");
        table.setItem(5,ItemStack.EMPTY);table.setItem(6,ItemStack.EMPTY);
        for(int t=0;t<40;t++)MetallurgyBlockEntity.tick(c.getLevel(),c.absolutePos(pos),c.getBlockState(pos),table);
        c.assertTrue(table.getItem(0).isEmpty(),"Input consumed once");c.assertTrue(table.getItem(5).getCount()+table.getItem(6).getCount()==100,"Exactly 100 portions");
        c.succeed();
    }
    @GameTest public void investedShapeCannotMultiplyCharge(GameTestHelper c) {
        var recipe=MetallurgyRecipes.all().stream().filter(r->r.id().equals("gold_portion_to_flakes")).findFirst().orElseThrow();
        var in=inputs();var data=new MetalItemData(1,UUID.randomUUID(),Metal.GOLD,Materials.PORTION_MASS,1,7,Optional.of(UUID.randomUUID()));
        ItemStack portion=new ItemStack(EconomyContent.item("gold","portion"));portion.set(ModRegistries.METAL_DATA,data);in.set(0,portion);
        var plan=MetallurgyRecipes.plan(recipe,in);c.assertValueEqual(data,plan.outputs().getFirst().get(ModRegistries.METAL_DATA),"Charge survives 1:1 flaking");
        c.assertTrue(plan.outputs().getFirst().getMaxStackSize()==1,"Invested result nonstackable");
        var split=MetallurgyRecipes.all().stream().filter(r->r.id().equals("gold_ingot_to_nuggets")).findFirst().orElseThrow();
        in.set(0,new ItemStack(Items.GOLD_INGOT));in.get(0).set(ModRegistries.METAL_DATA,new MetalItemData(1,UUID.randomUUID(),Metal.GOLD,900*Materials.PORTION_MASS,1,7,data.identity()));
        c.assertTrue(MetallurgyRecipes.plan(split,in)==null,"Invested input cannot split into duplicate charges");c.succeed();
    }
    @GameTest public void investedFlakingConservesRemainderAndCharge(GameTestHelper c) {
        var recipe=MetallurgyRecipes.all().stream().filter(r->r.id().equals("gold_ring_invested_flaking")).findFirst().orElseThrow();
        var in=inputs();ItemStack ring=new ItemStack(EconomyContent.item("gold","ring"));UUID key=UUID.randomUUID();
        ring.set(ModRegistries.METAL_DATA,new MetalItemData(1,UUID.randomUUID(),Metal.GOLD,3*Materials.PORTION_MASS,1,10,Optional.of(key)));in.set(0,ring);
        long charge=0,mass=0;
        for(int n=0;n<3;n++){
            var plan=MetallurgyRecipes.plan(recipe,in);var flake=plan.outputs().getFirst().get(ModRegistries.METAL_DATA);
            charge+=flake.charge();mass+=flake.massMicrograms();c.assertTrue(flake.identity().orElseThrow().equals(key),"Flake Identity retained");
            in.get(0).shrink(plan.consumed()[0]);plan.replacements().forEach(in::set);
        }
        c.assertTrue(in.get(0).isEmpty()&&charge==10&&mass==3*Materials.PORTION_MASS,"No duplicated or lost charge/mass");c.succeed();
    }
    @GameTest public void villagersOfferEveryDeclaredCategory(GameTestHelper c) {
        for(var key:EconomyWorld.PROFESSIONS) {
            var poi=BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(net.minecraft.resources.ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,key.identifier()));
            c.assertTrue(poi.is(net.minecraft.tags.PoiTypeTags.ACQUIRABLE_JOB_SITE),"New villagers can claim "+key);
            var profession=BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(key);
            for(int level=1;level<=5;level++){
                var set=c.getLevel().registryAccess().lookupOrThrow(Registries.TRADE_SET).getOrThrow(profession.value().getTrades(level));
                c.assertTrue(set.value().getTrades().size()>0,"Trades exist "+key+" level "+level);
            }
        }
        var protectedItem=new ItemStack(Items.GOLD_INGOT,8);protectedItem.set(ModRegistries.METAL_DATA,new MetalItemData(1,UUID.randomUUID(),Metal.GOLD,900*Materials.PORTION_MASS,1,1,Optional.empty()));
        var offer=new MerchantOffer(new ItemCost(Items.GOLD_INGOT,8),new ItemStack(Items.EMERALD),12,5,0.05F);
        c.assertFalse(offer.satisfiedBy(protectedItem,ItemStack.EMPTY),"Charged trade input refused");c.succeed();
    }
    @GameTest public void oresHaveCorrectMiningAndRawDrops(GameTestHelper c) {
        for(var entry:EconomyContent.ORES.entrySet()){
            ItemStack pickaxe=new ItemStack(Items.IRON_PICKAXE);
            c.assertTrue(pickaxe.isCorrectToolForDrops(entry.getValue().defaultBlockState()),"Ore has pickaxe/tool tags "+entry.getKey());
            var drops=Block.getDrops(entry.getValue().defaultBlockState(),c.getLevel(),c.absolutePos(new BlockPos(1,1,1)),null,null,pickaxe);
            String raw="raw_"+entry.getKey().replace("deepslate_","").replace("_ore","");
            c.assertTrue(drops.size()==1&&drops.getFirst().is(EconomyContent.ITEMS.get(raw))&&drops.getFirst().getCount()==1,"Raw ore loot "+entry.getKey());
        }
        c.succeed();
    }
}
