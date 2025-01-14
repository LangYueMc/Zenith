package dev.shadowsoffire.apotheosis.spawn.modifiers;

import dev.shadowsoffire.apotheosis.spawn.SpawnerModule;
import dev.shadowsoffire.apotheosis.spawn.spawner.IBaseSpawner;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.LinkedHashMap;

public class SpawnerStats {

    public static final Map<String, SpawnerStat<?>> REGISTRY = new LinkedHashMap<>();

    public static final SpawnerStat<Short> MIN_DELAY = register(new ShortStat("min_delay", s -> s.getSpawner().minSpawnDelay, (s, v) -> s.getSpawner().minSpawnDelay = v));

    public static final SpawnerStat<Short> MAX_DELAY = register(new ShortStat("max_delay", s -> s.getSpawner().maxSpawnDelay, (s, v) -> s.getSpawner().maxSpawnDelay = v));

    public static final SpawnerStat<Short> SPAWN_COUNT = register(new ShortStat("spawn_count", s -> s.getSpawner().spawnCount, (s, v) -> s.getSpawner().spawnCount = v));

    public static final SpawnerStat<Short> MAX_NEARBY_ENTITIES = register(new ShortStat("max_nearby_entities", s -> s.getSpawner().maxNearbyEntities, (s, v) -> s.getSpawner().maxNearbyEntities = v));

    public static final SpawnerStat<Short> REQ_PLAYER_RANGE = register(new ShortStat("req_player_range", s -> s.getSpawner().requiredPlayerRange, (s, v) -> s.getSpawner().requiredPlayerRange = v));

    public static final SpawnerStat<Short> SPAWN_RANGE = register(new ShortStat("spawn_range", s -> s.getSpawner().spawnRange, (s, v) -> s.getSpawner().spawnRange = v));

    public static final SpawnerStat<Boolean> IGNORE_PLAYERS = register(new BoolStat("ignore_players", IBaseSpawner::getIgnorePlayers, IBaseSpawner::setIgnoresPlayers));

    public static final SpawnerStat<Boolean> IGNORE_CONDITIONS = register(new BoolStat("ignore_conditions", IBaseSpawner::getIgnoresConditions, IBaseSpawner::setIgnoresConditions));

    public static final SpawnerStat<Boolean> REDSTONE_CONTROL = register(new BoolStat("redstone_control", IBaseSpawner::getRedstoneControl, IBaseSpawner::setRedstoneControl));

    public static final SpawnerStat<Boolean> IGNORE_LIGHT = register(new BoolStat("ignore_light", IBaseSpawner::getIgnoreLight, IBaseSpawner::setIgnoreLight));

    public static final SpawnerStat<Boolean> NO_AI = register(new BoolStat("no_ai", IBaseSpawner::getNoAi, IBaseSpawner::setNoAi));

    public static final SpawnerStat<Boolean> SILENT = register(new BoolStat("silent", IBaseSpawner::getSilent, IBaseSpawner::setSilent));

    public static final SpawnerStat<Boolean> BABY = register(new BoolStat("baby", IBaseSpawner::getBaby, IBaseSpawner::setBaby));

    public static void generateTooltip(SpawnerBlockEntity tile, Consumer<Component> list) {
        for (SpawnerStat<?> stat : REGISTRY.values()) {
            Component comp = stat.getTooltip((IBaseSpawner) tile);
            if (!comp.getString().isEmpty()) {
                list.accept(comp);
            }
        }
    }

    private static <T extends SpawnerStat<?>> T register(T t) {
        REGISTRY.put(t.getId(), t);
        return t;
    }

    private static abstract class Base<T> implements SpawnerStat<T> {

        protected final String id;
        protected final Function<IBaseSpawner, T> getter;
        protected final BiConsumer<IBaseSpawner, T> setter;

        private Base(String id, Function<IBaseSpawner, T> getter, BiConsumer<IBaseSpawner, T> setter) {
            this.id = id;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public T getValue(IBaseSpawner spawner) {
            return this.getter.apply(spawner);
        }

    }

    private static class BoolStat extends Base<Boolean> {

        private final Codec<StatModifier<Boolean>> modifierCodec = RecordCodecBuilder.create(inst -> inst
                .group(
                        Codec.BOOL.fieldOf("value").forGetter(StatModifier::value))
                .apply(inst, (value) -> new StatModifier<>(this, value, false, true)));

        private BoolStat(String id, Function<IBaseSpawner, Boolean> getter, BiConsumer<IBaseSpawner, Boolean> setter) {
            super(id, getter, setter);
        }

        @Override
        public Codec<StatModifier<Boolean>> getModifierCodec() {
            return this.modifierCodec;
        }

        @Override
        public Component getTooltip(IBaseSpawner spawner) {
            return getValue(spawner) ? name().withStyle(ChatFormatting.DARK_GREEN) : CommonComponents.EMPTY;
        }

        @Override
        public boolean apply(Boolean value, Boolean min, Boolean max, IBaseSpawner spawner) {
            boolean old = this.getter.apply(spawner);
            this.setter.accept(spawner, value);
            return old != this.getter.apply(spawner);
        }
    }

    private static class ShortStat extends Base<Short> {

        public static final Codec<Short> BOUNDS_CODEC = Codec.intRange(-1, Short.MAX_VALUE).xmap(Integer::shortValue, Short::intValue);

        private final Codec<StatModifier<Short>> modifierCodec = RecordCodecBuilder.create(inst -> inst
                .group(
                        Codec.SHORT.fieldOf("value").forGetter(StatModifier::value),
                        BOUNDS_CODEC.fieldOf("min").forGetter(StatModifier::min),
                        BOUNDS_CODEC.fieldOf("max").forGetter(StatModifier::max))
                .apply(inst, (value, min, max) -> new StatModifier<>(this, value, min == -1 ? 0 : min, max == -1 ? Short.MAX_VALUE : max)));

        private ShortStat(String id, Function<IBaseSpawner, Integer> getter, BiConsumer<IBaseSpawner, Short> setter) {
            super(id, tile -> getter.apply(tile).shortValue(), setter);
        }

        @Override
        public Codec<StatModifier<Short>> getModifierCodec() {
            return this.modifierCodec;
        }

        @Override
        public Component getTooltip(IBaseSpawner spawner) {
            return SpawnerModule.concat(name(), getValue(spawner));
        }

        @Override
        public boolean apply(Short value, Short min, Short max, IBaseSpawner spawner) {
            int old = this.getter.apply(spawner);
            this.setter.accept(spawner, (short) Mth.clamp(old + value, min, max));
            return old != this.getter.apply(spawner);
        }

    }
}
