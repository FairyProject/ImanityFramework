/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.imanity.framework.bukkit.packet.type;

import lombok.SneakyThrows;
import org.imanity.framework.bukkit.util.reflection.resolver.minecraft.NMSClassResolver;

public class PacketTypeClasses {
    private static final NMSClassResolver PACKET_CLASS_RESOLVER = new NMSClassResolver();
    
    public static class Client {
        private static final String c = "PacketPlayIn";
        public static Class<?> FLYING, POSITION, POSITION_LOOK, LOOK, CLIENT_COMMAND,
                TRANSACTION, BLOCK_DIG, ENTITY_ACTION, USE_ENTITY,
                WINDOW_CLICK, STEER_VEHICLE, CUSTOM_PAYLOAD, ARM_ANIMATION,
                BLOCK_PLACE, USE_ITEM, ABILITIES, HELD_ITEM_SLOT,
                CLOSE_WINDOW, TAB_COMPLETE, CHAT, SET_CREATIVE_SLOT,
                KEEP_ALIVE, SETTINGS, ENCHANT_ITEM, TELEPORT_ACCEPT,
                TILE_NBT_QUERY, DIFFICULTY_CHANGE, B_EDIT, ENTITY_NBT_QUERY,
                JIGSAW_GENERATE, DIFFICULTY_LOCK, VEHICLE_MOVE, BOAT_MOVE, PICK_ITEM,
                AUTO_RECIPE, RECIPE_DISPLAYED, ITEM_NAME, RESOURCE_PACK_STATUS,
                ADVANCEMENTS, TR_SEL, BEACON, SET_COMMAND_BLOCK,
                SET_COMMAND_MINECART, SET_JIGSAW, STRUCT, UPDATE_SIGN, SPECTATE;

        /**
         * Initiate all server-bound packet classes.
         */
        @SneakyThrows
        public static void load() {
            FLYING = PACKET_CLASS_RESOLVER.resolve(c + "Flying");
            try {
                POSITION = PACKET_CLASS_RESOLVER.resolve(c + "Position");
                POSITION_LOOK = PACKET_CLASS_RESOLVER.resolve(c + "PositionLook");
                LOOK = PACKET_CLASS_RESOLVER.resolve(c + "Look");
            } catch (ClassNotFoundException e) {
                POSITION = PACKET_CLASS_RESOLVER.resolve(c + "Flying$" + c + "Position") ;//SubclassUtil.getSubClass(FLYING, c + "Position");
                POSITION_LOOK = PACKET_CLASS_RESOLVER.resolve(c + "Flying$" + c + "PositionLook"); //SubclassUtil.getSubClass(FLYING, c + "PositionLook");
                LOOK = PACKET_CLASS_RESOLVER.resolve(c + "Flying$" + c + "Look"); // SubclassUtil.getSubClass(FLYING, c + "Look");
            }

            try {
                SETTINGS = PACKET_CLASS_RESOLVER.resolve(c + "Settings");
                ENCHANT_ITEM = PACKET_CLASS_RESOLVER.resolve(c + "EnchantItem");

                CLIENT_COMMAND = PACKET_CLASS_RESOLVER.resolve(c + "ClientCommand");
                TRANSACTION = PACKET_CLASS_RESOLVER.resolve(c + "Transaction");
                BLOCK_DIG = PACKET_CLASS_RESOLVER.resolve(c + "BlockDig");
                ENTITY_ACTION = PACKET_CLASS_RESOLVER.resolve(c + "EntityAction");
                USE_ENTITY = PACKET_CLASS_RESOLVER.resolve(c + "UseEntity");
                WINDOW_CLICK = PACKET_CLASS_RESOLVER.resolve(c + "WindowClick");
                STEER_VEHICLE = PACKET_CLASS_RESOLVER.resolve(c + "SteerVehicle");
                CUSTOM_PAYLOAD = PACKET_CLASS_RESOLVER.resolve(c + "CustomPayload");
                ARM_ANIMATION = PACKET_CLASS_RESOLVER.resolve(c + "ArmAnimation");
                ABILITIES = PACKET_CLASS_RESOLVER.resolve(c + "Abilities");
                HELD_ITEM_SLOT = PACKET_CLASS_RESOLVER.resolve(c + "HeldItemSlot");
                CLOSE_WINDOW = PACKET_CLASS_RESOLVER.resolve(c + "CloseWindow");
                TAB_COMPLETE = PACKET_CLASS_RESOLVER.resolve(c + "TabComplete");
                CHAT = PACKET_CLASS_RESOLVER.resolve(c + "Chat");
                SET_CREATIVE_SLOT = PACKET_CLASS_RESOLVER.resolve(c + "SetCreativeSlot");
                KEEP_ALIVE = PACKET_CLASS_RESOLVER.resolve(c + "KeepAlive");
                UPDATE_SIGN = PACKET_CLASS_RESOLVER.resolve(c + "UpdateSign");

                TELEPORT_ACCEPT = PACKET_CLASS_RESOLVER.resolve(c + "TeleportAccept");
                TILE_NBT_QUERY = PACKET_CLASS_RESOLVER.resolve(c + "TileNBTQuery");
                DIFFICULTY_CHANGE = PACKET_CLASS_RESOLVER.resolve(c + "DifficultyChange");
                B_EDIT = PACKET_CLASS_RESOLVER.resolve(c + "BEdit");
                ENTITY_NBT_QUERY = PACKET_CLASS_RESOLVER.resolve(c + "EntityNBTQuery");
                JIGSAW_GENERATE = PACKET_CLASS_RESOLVER.resolve(c + "JigsawGenerate");
                DIFFICULTY_LOCK = PACKET_CLASS_RESOLVER.resolve(c + "DifficultyLock");
                VEHICLE_MOVE = PACKET_CLASS_RESOLVER.resolve(c + "VehicleMove");
                BOAT_MOVE = PACKET_CLASS_RESOLVER.resolve(c + "BoatMove");
                PICK_ITEM = PACKET_CLASS_RESOLVER.resolve(c + "PickItem");
                AUTO_RECIPE = PACKET_CLASS_RESOLVER.resolve(c + "AutoRecipe");
                RECIPE_DISPLAYED = PACKET_CLASS_RESOLVER.resolve(c + "RecipeDisplayed");
                ITEM_NAME = PACKET_CLASS_RESOLVER.resolve(c + "ItemName");
                //1.8+
                RESOURCE_PACK_STATUS = PACKET_CLASS_RESOLVER.resolve(c + "ResourcePackStatus");

                ADVANCEMENTS = PACKET_CLASS_RESOLVER.resolve(c + "Advancements");
                TR_SEL = PACKET_CLASS_RESOLVER.resolve(c + "TrSel");
                BEACON = PACKET_CLASS_RESOLVER.resolve(c + "Beacon");
                SET_COMMAND_BLOCK = PACKET_CLASS_RESOLVER.resolve(c + "SetCommandBlock");
                SET_COMMAND_MINECART = PACKET_CLASS_RESOLVER.resolve(c + "SetCommandMinecart");
                SET_JIGSAW = PACKET_CLASS_RESOLVER.resolve(c + "SetJigsaw");
                STRUCT = PACKET_CLASS_RESOLVER.resolve(c + "Struct");
                SPECTATE = PACKET_CLASS_RESOLVER.resolve(c + "Spectate");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            //Block place
            try {
                BLOCK_PLACE = PACKET_CLASS_RESOLVER.resolve(c + "BlockPlace");
            } catch (ClassNotFoundException e) {
                //They are just on a newer version
                try {
                    USE_ITEM = PACKET_CLASS_RESOLVER.resolve(c + "UseItem");
                } catch (ClassNotFoundException e2) {
                    e.printStackTrace();
                    e2.printStackTrace();
                }
            }

            PacketType.Client.init();
        }
    }

    public static class Server {
        private static final String s = "PacketPlayOut";
        public static Class<?> SPAWN_ENTITY, SPAWN_ENTITY_EXPERIENCE_ORB, SPAWN_ENTITY_WEATHER, SPAWN_ENTITY_LIVING,
                SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_SPAWN, ANIMATION, STATISTIC,
                BLOCK_BREAK, BLOCK_BREAK_ANIMATION, TILE_ENTITY_DATA, BLOCK_ACTION,
                BLOCK_CHANGE, BOSS, SERVER_DIFFICULTY, CHAT, MULTI_BLOCK_CHANGE,
                TAB_COMPLETE, COMMANDS, TRANSACTION, CLOSE_WINDOW,
                WINDOW_ITEMS, WINDOW_DATA, SET_SLOT, SET_COOLDOWN,
                CUSTOM_PAYLOAD, CUSTOM_SOUND_EFFECT, KICK_DISCONNECT, ENTITY_STATUS,
                EXPLOSION, UNLOAD_CHUNK, GAME_STATE_CHANGE, OPEN_WINDOW_HORSE,
                KEEP_ALIVE, MAP_CHUNK, WORLD_EVENT, WORLD_PARTICLES,
                LIGHT_UPDATE, LOGIN, MAP, OPEN_WINDOW_MERCHANT,
                REL_ENTITY_MOVE, REL_ENTITY_MOVE_LOOK, ENTITY_LOOK, ENTITY,
                VEHICLE_MOVE, OPEN_BOOK, OPEN_WINDOW, OPEN_SIGN_EDITOR,
                AUTO_RECIPE, ABILITIES, COMBAT_EVENT, PLAYER_INFO,
                LOOK_AT, POSITION, RECIPES, ENTITY_DESTROY,
                REMOVE_ENTITY_EFFECT, RESOURCE_PACK_SEND, RESPAWN, ENTITY_HEAD_ROTATION,
                SELECT_ADVANCEMENT_TAB, WORLD_BORDER, CAMERA, HELD_ITEM_SLOT,
                VIEW_CENTRE, VIEW_DISTANCE, SCOREBOARD_DISPLAY_OBJECTIVE, ENTITY_METADATA,
                ATTACH_ENTITY, ENTITY_VELOCITY, ENTITY_EQUIPMENT, EXPERIENCE,
                UPDATE_HEALTH, SCOREBOARD_OBJECTIVE, MOUNT, SCOREBOARD_TEAM,
                SCOREBOARD_SCORE, SPAWN_POSITION, UPDATE_TIME, TITLE,
                ENTITY_SOUND, NAMED_SOUND_EFFECT, STOP_SOUND, PLAYER_LIST_HEADER_FOOTER,
                NBT_QUERY, COLLECT, ENTITY_TELEPORT, ADVANCEMENTS, UPDATE_ATTRIBUTES,
                ENTITY_EFFECT, RECIPE_UPDATE, TAGS, MAP_CHUNK_BULK;

        /**
         * Initiate all client-bound packet classes.
         */
        @SneakyThrows
        public static void load() {
            SPAWN_ENTITY = PACKET_CLASS_RESOLVER.resolve(s + "SpawnEntity");
            SPAWN_ENTITY_EXPERIENCE_ORB = PACKET_CLASS_RESOLVER.resolve(s + "SpawnEntityExperienceOrb");
            SPAWN_ENTITY_WEATHER = PACKET_CLASS_RESOLVER.resolve(s + "SpawnEntityWeather");
            SPAWN_ENTITY_LIVING = PACKET_CLASS_RESOLVER.resolve(s + "SpawnEntityLiving");
            SPAWN_ENTITY_PAINTING = PACKET_CLASS_RESOLVER.resolve(s + "SpawnEntityPainting");
            SPAWN_ENTITY_SPAWN = PACKET_CLASS_RESOLVER.resolve(s + "SpawnEntitySpawn");
            ANIMATION = PACKET_CLASS_RESOLVER.resolve(s + "Animation");
            STATISTIC = PACKET_CLASS_RESOLVER.resolve(s + "Statistic");
            BLOCK_BREAK = PACKET_CLASS_RESOLVER.resolve(s + "BlockBreak");
            BLOCK_BREAK_ANIMATION = PACKET_CLASS_RESOLVER.resolve(s + "BlockBreakAnimation");
            TILE_ENTITY_DATA = PACKET_CLASS_RESOLVER.resolve(s + "TileEntityData");
            BLOCK_ACTION = PACKET_CLASS_RESOLVER.resolve(s + "BlockAction");
            BLOCK_CHANGE = PACKET_CLASS_RESOLVER.resolve(s + "BlockChange");
            BOSS = PACKET_CLASS_RESOLVER.resolve(s + "Boss");
            SERVER_DIFFICULTY = PACKET_CLASS_RESOLVER.resolve(s + "ServerDifficulty");
            CHAT = PACKET_CLASS_RESOLVER.resolve(s + "Chat");
            MULTI_BLOCK_CHANGE = PACKET_CLASS_RESOLVER.resolve(s + "MultiBlockChange");
            TAB_COMPLETE = PACKET_CLASS_RESOLVER.resolve(s + "TabComplete");
            COMMANDS = PACKET_CLASS_RESOLVER.resolve(s + "Commands");
            TRANSACTION = PACKET_CLASS_RESOLVER.resolve(s + "Transaction");
            CLOSE_WINDOW = PACKET_CLASS_RESOLVER.resolve(s + "CloseWindow");
            WINDOW_ITEMS = PACKET_CLASS_RESOLVER.resolve(s + "WindowItems");
            WINDOW_DATA = PACKET_CLASS_RESOLVER.resolve(s + "WindowData");
            SET_SLOT = PACKET_CLASS_RESOLVER.resolve(s + "SetSlot");
            SET_COOLDOWN = PACKET_CLASS_RESOLVER.resolve(s + "SetCooldown");
            CUSTOM_PAYLOAD = PACKET_CLASS_RESOLVER.resolve(s + "CustomPayload");
            CUSTOM_SOUND_EFFECT = PACKET_CLASS_RESOLVER.resolve(s + "CustomSoundEffect");
            KICK_DISCONNECT = PACKET_CLASS_RESOLVER.resolve(s + "KickDisconnect");
            ENTITY_STATUS = PACKET_CLASS_RESOLVER.resolve(s + "EntityStatus");
            EXPLOSION = PACKET_CLASS_RESOLVER.resolve(s + "Explosion");
            UNLOAD_CHUNK = PACKET_CLASS_RESOLVER.resolve(s + "UnloadChunk");
            GAME_STATE_CHANGE = PACKET_CLASS_RESOLVER.resolve(s + "GameStateChange");
            OPEN_WINDOW_HORSE = PACKET_CLASS_RESOLVER.resolve(s + "OpenWindowHorse");
            KEEP_ALIVE = PACKET_CLASS_RESOLVER.resolve(s + "KeepAlive");
            MAP_CHUNK = PACKET_CLASS_RESOLVER.resolve(s + "MapChunk");
            WORLD_EVENT = PACKET_CLASS_RESOLVER.resolve(s + "WorldEvent");
            WORLD_PARTICLES = PACKET_CLASS_RESOLVER.resolve(s + "WorldParticles");
            LIGHT_UPDATE = PACKET_CLASS_RESOLVER.resolve(s + "LightUpdate");
            LOGIN = PACKET_CLASS_RESOLVER.resolve(s + "Login");
            MAP = PACKET_CLASS_RESOLVER.resolve(s + "Map");
            OPEN_WINDOW_MERCHANT = PACKET_CLASS_RESOLVER.resolve(s + "OpenWindowMerchant");
            ENTITY = PACKET_CLASS_RESOLVER.resolve(s + "Entity");
            REL_ENTITY_MOVE = PACKET_CLASS_RESOLVER.resolve(s + "Flying$" + s + "RelEntityMove"); //SubclassUtil.getSubClass(ENTITY, s + "RelEntityMove");
            REL_ENTITY_MOVE_LOOK = PACKET_CLASS_RESOLVER.resolve(s + "Flying$" + s + "RelEntityMoveLook"); //SubclassUtil.getSubClass(ENTITY, s + "RelEntityMoveLook");
            ENTITY_LOOK = PACKET_CLASS_RESOLVER.resolve(s + "Entity$" + s + "EntityLook"); //SubclassUtil.getSubClass(ENTITY, s + "EntityLook");
            if (REL_ENTITY_MOVE == null) {
                //is not a subclass and should be accessed normally
                REL_ENTITY_MOVE = PACKET_CLASS_RESOLVER.resolve(s + "RelEntityMove");
                REL_ENTITY_MOVE_LOOK = PACKET_CLASS_RESOLVER.resolve(s + "RelEntityMoveLook");
                ENTITY_LOOK = PACKET_CLASS_RESOLVER.resolve(s + "RelEntityLook");
            }
            VEHICLE_MOVE = PACKET_CLASS_RESOLVER.resolve(s + "VehicleMove");
            OPEN_BOOK = PACKET_CLASS_RESOLVER.resolve(s + "OpenBook");
            OPEN_WINDOW = PACKET_CLASS_RESOLVER.resolve(s + "OpenWindow");
            OPEN_SIGN_EDITOR = PACKET_CLASS_RESOLVER.resolve(s + "OpenSignEditor");
            AUTO_RECIPE = PACKET_CLASS_RESOLVER.resolve(s + "AutoRecipe");
            ABILITIES = PACKET_CLASS_RESOLVER.resolve(s + "Abilities");
            COMBAT_EVENT = PACKET_CLASS_RESOLVER.resolve(s + "CombatEvent");
            PLAYER_INFO = PACKET_CLASS_RESOLVER.resolve(s + "PlayerInfo");
            LOOK_AT = PACKET_CLASS_RESOLVER.resolve(s + "LookAt");
            POSITION = PACKET_CLASS_RESOLVER.resolve(s + "Position");
            RECIPES = PACKET_CLASS_RESOLVER.resolve(s + "Recipes");
            ENTITY_DESTROY = PACKET_CLASS_RESOLVER.resolve(s + "EntityDestroy");
            REMOVE_ENTITY_EFFECT = PACKET_CLASS_RESOLVER.resolve(s + "RemoveEntityEffect");
            RESOURCE_PACK_SEND = PACKET_CLASS_RESOLVER.resolve(s + "ResourcePackSend");
            RESPAWN = PACKET_CLASS_RESOLVER.resolve(s + "Respawn");
            ENTITY_HEAD_ROTATION = PACKET_CLASS_RESOLVER.resolve(s + "EntityHeadRotation");
            SELECT_ADVANCEMENT_TAB = PACKET_CLASS_RESOLVER.resolve(s + "SelectAdvancementTab");
            WORLD_BORDER = PACKET_CLASS_RESOLVER.resolve(s + "WorldBorder");
            CAMERA = PACKET_CLASS_RESOLVER.resolve(s + "Camera");
            HELD_ITEM_SLOT = PACKET_CLASS_RESOLVER.resolve(s + "HeldItemSlot");
            VIEW_CENTRE = PACKET_CLASS_RESOLVER.resolve(s + "ViewCentre");
            VIEW_DISTANCE = PACKET_CLASS_RESOLVER.resolve(s + "ViewDistance");
            SCOREBOARD_DISPLAY_OBJECTIVE = PACKET_CLASS_RESOLVER.resolve(s + "ScoreboardDisplayObjective");
            ENTITY_METADATA = PACKET_CLASS_RESOLVER.resolve(s + "EntityMetadata");
            ATTACH_ENTITY = PACKET_CLASS_RESOLVER.resolve(s + "AttachEntity");
            ENTITY_VELOCITY = PACKET_CLASS_RESOLVER.resolve(s + "EntityVelocity");
            ENTITY_EQUIPMENT = PACKET_CLASS_RESOLVER.resolve(s + "EntityEquipment");
            EXPERIENCE = PACKET_CLASS_RESOLVER.resolve(s + "Experience");
            UPDATE_HEALTH = PACKET_CLASS_RESOLVER.resolve(s + "UpdateHealth");
            SCOREBOARD_OBJECTIVE = PACKET_CLASS_RESOLVER.resolve(s + "ScoreboardObjective");
            MOUNT = PACKET_CLASS_RESOLVER.resolve(s + "Mount");
            SCOREBOARD_TEAM = PACKET_CLASS_RESOLVER.resolve(s + "ScoreboardTeam");
            SCOREBOARD_SCORE = PACKET_CLASS_RESOLVER.resolve(s + "ScoreboardScore");
            SPAWN_POSITION = PACKET_CLASS_RESOLVER.resolve(s + "SpawnPosition");
            UPDATE_TIME = PACKET_CLASS_RESOLVER.resolve(s + "UpdateTime");
            TITLE = PACKET_CLASS_RESOLVER.resolve(s + "Title");
            ENTITY_SOUND = PACKET_CLASS_RESOLVER.resolve(s + "EntitySound");
            NAMED_SOUND_EFFECT = PACKET_CLASS_RESOLVER.resolve(s + "NamedSoundEffect");
            STOP_SOUND = PACKET_CLASS_RESOLVER.resolve(s + "StopSound");
            PLAYER_LIST_HEADER_FOOTER = PACKET_CLASS_RESOLVER.resolve(s + "PlayerListHeaderFooter");
            NBT_QUERY = PACKET_CLASS_RESOLVER.resolve(s + "NBTQuery");
            COLLECT = PACKET_CLASS_RESOLVER.resolve(s + "Collect");
            ENTITY_TELEPORT = PACKET_CLASS_RESOLVER.resolve(s + "EntityTeleport");
            ADVANCEMENTS = PACKET_CLASS_RESOLVER.resolve(s + "Advancements");
            UPDATE_ATTRIBUTES = PACKET_CLASS_RESOLVER.resolve(s + "UpdateAttributes");
            ENTITY_EFFECT = PACKET_CLASS_RESOLVER.resolve(s + "EntityEffect");
            RECIPE_UPDATE = PACKET_CLASS_RESOLVER.resolve(s + "RecipeUpdate");
            TAGS = PACKET_CLASS_RESOLVER.resolve(s + "Tags");
            MAP_CHUNK_BULK = PACKET_CLASS_RESOLVER.resolve(s + "MapChunkBulk");
            PacketType.Server.init();
        }
    }

}
