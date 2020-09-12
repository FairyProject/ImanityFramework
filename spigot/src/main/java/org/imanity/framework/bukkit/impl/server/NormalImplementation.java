package org.imanity.framework.bukkit.impl.server;

import com.google.common.collect.HashMultimap;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.imanity.framework.ImanityCommon;
import org.imanity.framework.bukkit.metadata.Metadata;
import org.imanity.framework.bukkit.metadata.MetadataKey;
import org.imanity.framework.bukkit.util.*;
import org.imanity.framework.bukkit.util.BlockPosition;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.resolver.ConstructorResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;
import org.imanity.framework.bukkit.util.reflection.resolver.wrapper.*;
import org.imanity.framework.util.CommonUtility;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class NormalImplementation implements ServerImplementation {

    public static final MetadataKey<ConcurrentMap> FAKE_BLOCK_MAP = MetadataKey.create(ImanityCommon.METADATA_PREFIX + "FakeBlockMap", ConcurrentMap.class);

    private static final ObjectWrapper MINECRAFT_SERVER;

    private static final Class<?> CHUNK_COORD_PAIR_TYPE;
    private static final Class<?> BLOCK_INFO_TYPE;

    private static final FieldWrapper<Float> BLOCK_SLIPPERINESS_FIELD;

    private static final FieldWrapper<?> PLAYER_CONNECTION_FIELD;

    private static final MethodWrapper<?> BLOCK_GET_BY_ID_METHOD;
    private static final MethodWrapper<?> FROM_LEGACY_DATA_METHOD;

    private static final ConstructorWrapper<?> BLOCK_INFO_CONSTRUCTOR;
    private static final ConstructorWrapper<?> SPAWN_NAMED_ENTITY_CONSTRUCTOR;
    private static final ConstructorWrapper<?> CHUNK_COORD_PAIR_CONSTRUCTOR;
    private static final ConstructorWrapper<?> DESTROY_ENTITY_CONSTRUCTOR;

    static {

        NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();
        try {

            Class<?> minecraftServerType = CLASS_RESOLVER.resolve("MinecraftServer");
            Object minecraftServer = minecraftServerType.getMethod("getServer").invoke(null);
            MINECRAFT_SERVER = new ObjectWrapper(minecraftServer);

            Class<?> entityPlayerType = CLASS_RESOLVER.resolve("EntityPlayer");
            PLAYER_CONNECTION_FIELD = new FieldWrapper<>(entityPlayerType.getField("playerConnection"));

            Class<?> BLOCK_INFO_PACKET_TYPE = CLASS_RESOLVER.resolve("PacketPlayOutMultiBlockChange");
            BLOCK_INFO_TYPE = CLASS_RESOLVER.resolve("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
            Class<?> blockData = CLASS_RESOLVER.resolve("IBlockData");

            ConstructorResolver constructorResolver = new ConstructorResolver(BLOCK_INFO_TYPE);
            BLOCK_INFO_CONSTRUCTOR = new ConstructorWrapper<>(constructorResolver.resolve(
                    new Class[] {short.class, blockData},
                    new Class[] {BLOCK_INFO_PACKET_TYPE, short.class, blockData}
                    ));

            Class<?> blockType = CLASS_RESOLVER.resolve("Block");
            BLOCK_GET_BY_ID_METHOD = new MethodWrapper<>(blockType.getMethod("getById", int.class));
            FROM_LEGACY_DATA_METHOD = new MethodWrapper<>(blockType.getMethod("fromLegacyData", int.class));

            BLOCK_SLIPPERINESS_FIELD = new FieldWrapper<>(blockType.getField("frictionFactor"));

            CHUNK_COORD_PAIR_TYPE = CLASS_RESOLVER.resolve("ChunkCoordIntPair");
            CHUNK_COORD_PAIR_CONSTRUCTOR = new ConstructorWrapper<>(CHUNK_COORD_PAIR_TYPE.getConstructor(int.class, int.class));

            Class<?> entityHumanType = CLASS_RESOLVER.resolve("EntityHuman");
            Class<?> spawnNamedEntityType = CLASS_RESOLVER.resolve("PacketPlayOutNamedEntitySpawn");
            SPAWN_NAMED_ENTITY_CONSTRUCTOR = new ConstructorWrapper<>(spawnNamedEntityType.getConstructor(entityHumanType));

            Class<?> destoryEntityType = CLASS_RESOLVER.resolve("PacketPlayOutEntityDestroy");
            DESTROY_ENTITY_CONSTRUCTOR = new ConstructorWrapper<>(destoryEntityType.getConstructor(int[].class));

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    public Entity getEntity(UUID uuid) {
        try {
            return MinecraftReflection.getBukkitEntity(MINECRAFT_SERVER.invoke("a", uuid));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object toBlockNMS(MaterialData materialData) {
        Object block = BLOCK_GET_BY_ID_METHOD.invoke(null, materialData.getItemTypeId());
        return FROM_LEGACY_DATA_METHOD.invoke(block, materialData.getData());
    }

    @Override
    public void showDyingNPC(Player player) {
        Location location = player.getLocation();
        final List<Player> players = this.getPlayerRadius(location, 32);

        Object entityPlayer = MinecraftReflection.getHandleSilent(player);

        PacketWrapper packet = new PacketWrapper(SPAWN_NAMED_ENTITY_CONSTRUCTOR.newInstance(entityPlayer));
        int i = MinecraftReflection.getNewEntityId();
        packet.setPacketValue("a", i);

        PacketWrapper statusPacket = PacketWrapper.createByPacketName("PacketPlayOutEntityStatus");
        statusPacket.setPacketValue("a", i);
        statusPacket.setPacketValue("b", (byte) 3);
        PacketWrapper destroyPacket = new PacketWrapper(DESTROY_ENTITY_CONSTRUCTOR.newInstance(new int[] {i}));

        for (Player other : players) {
//            ((CraftPlayer) other).getHandle().playerConnection.fakeEntities.add(i); // TODO
            MinecraftReflection.sendPacket(player, packet);
            MinecraftReflection.sendPacket(player, statusPacket);
        }

        TaskUtil.runScheduled(() -> players.forEach(other -> {
//            ((CraftPlayer) other).getHandle().playerConnection.fakeEntities.remove(i); // TODO
            MinecraftReflection.sendPacket(player, destroyPacket);
        }), 20L);
    }

    @Override
    public List<Player> getPlayerRadius(Location location, double radius) {
        return location.getWorld().getNearbyEntities(location, radius / 2, radius / 2, radius / 2)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

    @Override
    public void setFakeBlocks(Player player, Map<BlockPosition, MaterialData> blockMap, List<BlockPosition> replace, boolean send) {
        ConcurrentMap<BlockPosition, MaterialData> fakeBlockMap = Metadata.provideForPlayer(player).getOrNull(FAKE_BLOCK_MAP);
        if (fakeBlockMap == null) {
            fakeBlockMap = new ConcurrentHashMap<>();
            Metadata.provideForPlayer(player).put(FAKE_BLOCK_MAP, fakeBlockMap);
        }

        HashMultimap<CoordXZ, BlockPositionData> map = HashMultimap.create();
        for (final Map.Entry<BlockPosition, MaterialData> entry : blockMap.entrySet()) {
            final BlockPosition blockPosition = entry.getKey();
            MaterialData materialData = entry.getValue();
            if (materialData == null) {
                materialData = new MaterialData(0);
            }
            final MaterialData previous = fakeBlockMap.put(blockPosition, materialData);
            if (send && previous != materialData) {
                final int x = blockPosition.getX();
                final int y = blockPosition.getY();
                final int z = blockPosition.getZ();
                final int chunkX = x >> 4;
                final int chunkZ = z >> 4;
                final int posX = x - (chunkX << 4);
                final int posZ = z - (chunkZ << 4);
                map.put(new CoordXZ(chunkX, chunkZ), new BlockPositionData(new BlockPosition(posX, y, posZ, player.getWorld().getName()), materialData));
            }
        }
        for (final BlockPosition blockPosition : replace) {
            if (fakeBlockMap.remove(blockPosition) != null) {
                final int x2 = blockPosition.getX();
                final int y2 = blockPosition.getY();
                final int z2 = blockPosition.getZ();
                final org.bukkit.block.Block blockData = player.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                final int type = blockData.getType().getId();
                final int data = blockData.getData();
                final int chunkX2 = x2 >> 4;
                final int chunkZ2 = z2 >> 4;
                final int posX2 = x2 - (chunkX2 << 4);
                final int posZ2 = z2 - (chunkZ2 << 4);
                map.put(new CoordXZ(chunkX2, chunkZ2), new BlockPositionData(new BlockPosition(posX2, y2, posZ2, player.getWorld().getName()), new MaterialData(type, (byte) data)));
            }
        }
        if (send) {
            for (final Map.Entry<CoordXZ, Collection<BlockPositionData>> entry2 : map.asMap().entrySet()) {
                final CoordXZ chunkPosition = entry2.getKey();
                final Collection<BlockPositionData> blocks = entry2.getValue();

                PacketWrapper packet = PacketWrapper.createByPacketName("PacketPlayOutMultiBlockChange");
                Object info = Array.newInstance(BLOCK_INFO_TYPE, blocks.size());

                int i = 0;
                for (BlockPositionData positionData : blocks) {
                    BlockPosition b = positionData.getBlockPosition();
                    MaterialData materialData = positionData.getMaterialData();

                    short s = (short) ((b.getX() & 15) << 12 | (b.getZ() & 15) << 8 | b.getY());
                    Object blockNMS = this.toBlockNMS(materialData);

                    Array.set(info, i, BLOCK_INFO_CONSTRUCTOR.resolveBunch(
                            new Object[] {s, blockNMS},
                            new Object[] {packet.getPacket(), s, blockNMS}
                    ));
                    i++;
                }

                packet.setPacketValueByType(CHUNK_COORD_PAIR_TYPE, CHUNK_COORD_PAIR_CONSTRUCTOR.newInstance(chunkPosition.x, chunkPosition.z));

                packet.setPacketValueByType(info.getClass(), info);

                MinecraftReflection.sendPacket(player, packet);
            }
        }
    }

    @Override
    public void clearFakeBlocks(Player player, boolean send) {
        ConcurrentMap<BlockPosition, MaterialData> fakeBlockMap = Metadata.provideForPlayer(player).getOrNull(FAKE_BLOCK_MAP);
        if (fakeBlockMap == null) {
            return;
        }

        if (send) {
            this.setFakeBlocks(player, Collections.emptyMap(), new ArrayList<>(fakeBlockMap.keySet()), true);
        } else {
            fakeBlockMap.clear();
        }
    }

    private Class<?> CHAT_BASE_COMPONENT_TYPE;
    private ConstructorWrapper<?> PACKET_CHAT_CONSTRUCTOR;
    private MethodWrapper<?> CHAT_SERIALIZER_A;

    @Override
    public void sendActionBar(Player player, String message) {
        if (CHAT_BASE_COMPONENT_TYPE == null) {
            NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();

            try {
                CHAT_BASE_COMPONENT_TYPE = CLASS_RESOLVER.resolve("IChatBaseComponent");

                Class<?> CHAT_SERIALIZER_TYPE = CLASS_RESOLVER.resolve("IChatBaseComponent$ChatSerializer");

                CHAT_SERIALIZER_A = new MethodWrapper<>(CHAT_SERIALIZER_TYPE.getMethod("a", String.class));
                Class<?> PACKET_PLAY_OUT_CHAT_TYPE = CLASS_RESOLVER.resolve("PacketPlayOutChat");

                PACKET_CHAT_CONSTRUCTOR = new ConstructorWrapper<>(PACKET_PLAY_OUT_CHAT_TYPE.getConstructor(CHAT_BASE_COMPONENT_TYPE, byte.class));

            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        Object chatComponent = CHAT_SERIALIZER_A.invoke(null, "{\"text\": \"" +
                ChatColor.translateAlternateColorCodes('&', message) + "\"}");

        Object packet = PACKET_CHAT_CONSTRUCTOR.newInstance(chatComponent, (byte) 2);

        MinecraftReflection.sendPacket(player, packet);
    }

    @Override
    public float getBlockSlipperiness(Material material) {
        Object block = BLOCK_GET_BY_ID_METHOD.invoke(null);
        return BLOCK_SLIPPERINESS_FIELD.get(block);
    }

    @Override
    public void sendTeam(Player player, String name, String prefix, String suffix, Collection<String> nameSet, int type) {
        // TODO
    }

    @Override
    public void sendMember(Player player, String name, Collection<String> players, int type) {
        // TODO
    }
}
