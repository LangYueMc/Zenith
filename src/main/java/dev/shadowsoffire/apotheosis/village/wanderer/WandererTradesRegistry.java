package dev.shadowsoffire.apotheosis.village.wanderer;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.village.VillageModule;
import dev.shadowsoffire.apotheosis.village.util.BasicItemListing;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WandererTradesRegistry extends DynamicRegistry<JsonTrade> {

    public static final WandererTradesRegistry INSTANCE = new WandererTradesRegistry();

    protected final Map<ResourceLocation, BasicItemListing> registry = new HashMap<>();
    protected final List<ItemListing> normTrades = new ArrayList<>();
    protected final List<ItemListing> rareTrades = new ArrayList<>();

    public WandererTradesRegistry() {
        super(VillageModule.LOGGER, "wanderer_trades", false, true);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("basic_trade"), WandererTrade.CODEC);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.normTrades.clear();
        this.rareTrades.clear();
    }


    @Override
    protected void onReload() {
        super.onReload();
        this.getValues().forEach(trade -> {
            if (trade.isRare()) this.rareTrades.add(trade);
            else this.normTrades.add(trade);
        });
    }

    public List<ItemListing> getNormalTrades() {
        return this.normTrades;
    }

    public List<ItemListing> getRareTrades() {
        return this.rareTrades;
    }

}
