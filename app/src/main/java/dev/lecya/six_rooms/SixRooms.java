// Copyright (c) 2023 Joseph Hale
// 
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

package dev.lecya.six_rooms;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.addon.InjectPlasmoVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.slib.spigot.entity.SpigotServerPlayer;
import su.plo.voice.groups.GroupsAddon;
import su.plo.voice.groups.GroupsManager;
import su.plo.voice.groups.group.Group;

public class SixRooms extends JavaPlugin implements Listener {

    private static final int MIN_PLAYERS = 5;
    private static final int MAX_PLAYERS = 12;
    private static final int ROOM_SPACING = 50;
    private static final int FIRST_ROUND_PREP_SECONDS = 60;
    private static final int FIRST_ROUND_SECONDS = 150;
    private static final int BETWEEN_ROUND_PREP_SECONDS = 30;
    private static final int SWORD_CHOICE_SECONDS = 90;
    private static final int SWORD_VISIT_SECONDS = 60;
    private static final int DOORS_ROUND_SECONDS = 90;
    private static final int MAX_ROUNDS = 8;
    private static final int MAX_SCORE = 20;
    private static final String CLOWN_HEAD_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc0ZjZiNmEwYTY5MzVlNTA3M2M2MTY2NGZmODljZWVmMGFjYTY3NmM3NzgxMDE3NTFlMjMyY2Q3Yjc5MWZiMyJ9fX0=";
    private static final String PEPE_HEAD_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDkzNjMzYWEzYmQyOWRkZGZmMTlmMmQzZjQyZDFhZWRiOTczNWM5ODUwMWU5NjEwMjRmYmVhYTg3YjM3ZmU1ZSJ9fX0=";
    private static final String SMILE_HEAD_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQzZmM3MzVhYWFlOGY3NDRiMTIyM2YwM2ZiY2ZiZWZlYzk5YWU2YzMxYTRjN2U0NTIwODBjYzRlZWE3YjZmOSJ9fX0=";
    private static final String ADMIN_PERMISSION = "sixrooms.admin";
    private static final String SCHEMATIC_NAME = "room.schem";
    private final List<UUID> participants = new ArrayList<>();
    private final Map<UUID, Integer> playerNumbers = new HashMap<>();
    private final Map<UUID, CallSession> callsByPlayer = new HashMap<>();
    private final Map<UUID, Integer> scores = new HashMap<>();
    private final Map<UUID, UUID> firstRoundChoices = new HashMap<>();
    private final Map<UUID, ItemStack> previousHelmets = new HashMap<>();
    private GameState state = GameState.IDLE;
    private Location baseLocation;
    private NamespacedKey phoneItemKey;
    private NamespacedKey phoneBlockKey;
    private NamespacedKey phoneTargetKey;
    private NamespacedKey phoneResetKey;
    private FileConfiguration translations;
    private String language = "ru";
    private SixRoomsVoiceAddon voiceAddon;
    private boolean debug = false;
    private BossBar roundPrepBar;
    private int roundPrepTaskId = -1;
    private BossBar roundBar;
    private int roundBarTaskId = -1;
    private int firstRoundTaskId = -1;
    private boolean firstRoundActive = false;
    private UUID clownPlayerId;
    private int currentRound = 0;
    private RoundType currentRoundType = RoundType.FIRST;
    private final Random random = new Random();
    private final List<RoundType> roundPool = List.of(RoundType.FIRST, RoundType.SECOND, RoundType.THIRD, RoundType.FIFTH);
    private final List<RoundType> remainingRoundPool = new ArrayList<>();
    private final Map<Integer, RoundType> fixedRoundSlots = new HashMap<>();
    private final Map<UUID, Location> roomCenters = new HashMap<>();
    private boolean usingSchematicRooms = false;
    private boolean secondRoundActive = false;
    private UUID secondRoundWordOwner;
    private UUID secondRoundCurrentCaller;
    private String secondRoundWord;
    private boolean secondRoundAwaitingWord = false;
    private final Set<UUID> secondRoundCalled = new HashSet<>();
    private final Set<UUID> secondRoundCorrectGuessers = new HashSet<>();
    private int secondRoundCallTaskId = -1;
    private boolean thirdRoundActive = false;
    private UUID tntEntityId;
    private UUID tntHolderId;
    private UUID tntLastHolderId;
    private boolean tntTransferAllowed = false;
    private int tntTransferTaskId = -1;
    private int tntFuseSeconds = 0;
    private boolean fourthRoundActive = false;
    private UUID swordHolderId;
    private boolean swordUsed = false;
    private ItemStack swordPreviousItem;
    private boolean swordVisitActive = false;
    private UUID swordVisitTargetId;
    private Location swordReturnLocation;
    private int swordChoiceTaskId = -1;
    private int swordVisitTaskId = -1;
    private int swordReturnTaskId = -1;
    private boolean doorsRoundActive = false;
    private final Map<UUID, Boolean> doorsRoundChoices = new HashMap<>();
    private final Map<UUID, Location> doorOpenPlateLocations = new HashMap<>();
    private final Map<UUID, Location> doorStayPlateLocations = new HashMap<>();
    private final Map<Location, Material> doorRoundReplacedBlocks = new HashMap<>();
    private final List<UUID> doorRoundHolograms = new ArrayList<>();
    private int doorsRoundTaskId = -1;
    private final List<MaskDefinition> maskDefinitions = List.of(
        new MaskDefinition("Клоун", CLOWN_HEAD_BASE64),
        new MaskDefinition("Пепе", PEPE_HEAD_BASE64),
        new MaskDefinition("Смайлик", SMILE_HEAD_BASE64),
        new MaskDefinition("Маска 1", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODRiYjhiNWRkZTRlMmVhMDdjNjQ3ZmVkYzAyNmUxNjQ4YzUwNTEyOWY1NjQxNTBlMzk3ZDRiMzRkYTEyNmVjIn19fQ=="),
        new MaskDefinition("Маска 2", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzE0Mjk1YWQ3NTUxZGQ4NjE5YWNiYmE2MjFmMGM3YzZmYjZkZjBiNGZkMGZhYTdlMTdiNmU5NjczYzc3NGUyOCJ9fX0="),
        new MaskDefinition("Маска 3", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFhZjUzMTNjODcyNzJkM2MzNDM2NmU0NDMzNjhhNjc2ZTg2NTUxNDA1YmViNDM2Mjg3ZmY2MjNjYzc4ZWFhYSJ9fX0="),
        new MaskDefinition("Маска 4", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWEzNTFhODA4ODBhZmNiYzkzODgwOTRmYjZhYWE0MjllZDI3Y2I4OWJhNzBlYThkOWE5YTg1MTNkOTc5YWU5ZSJ9fX0="),
        new MaskDefinition("Волтер", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVjNTE4NzAyZWI5NzUwYWJlMDFhNGEyNWIwYmQ3ZDUzYzNhNDJhOTc0YzU4YWNiZGNiYzIxM2IyNjBlYjU5YSJ9fX0="),
        new MaskDefinition("Джени", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzJhYmE5MWRiMDY4ODZiNTNiZWIwNjFjMWRkMjRjMzJkZTg1MDg4YTQxOTcyNDY3NjY4ZjFlNDc0ZjAwYmYzZCJ9fX0=")
    );
    private static final List<String> SECOND_ROUND_WORDS = List.of(
        "минон", "шляпа", "ватафа", "шлемофон", "ведьма", "женщина", "потэйто", "лежит", "слово", "дуб", "семя", "маслёнок", "дима маслёнок", "фембой", "давакер", "капучинатор", "петлички", "петушок", "арбузоохладитель", "z", "сэкссэвэн", "авиасэйлс",
        "кикимора", "гойда"
    );

    @Override
    public void onLoad() {
        voiceAddon = new SixRoomsVoiceAddon();
        voiceAddon.setPlugin(this);
        PlasmoVoiceServer.getAddonsLoader().load(voiceAddon);
    }

    public UUID getCallPartner(UUID playerId) {
        CallSession session = callsByPlayer.get(playerId);
        if (session != null && session.state == CallState.ACTIVE) {
            return session.caller.equals(playerId) ? session.callee : session.caller;
        }
        return null;
    }

    public boolean isPlayerInGame(UUID playerId) {
        return state == GameState.RUNNING && playerNumbers.containsKey(playerId);
    }

    private void loadTranslations() {
        File file = new File(getDataFolder(), "lang.yml");
        if (!file.exists()) {
            saveResource("lang.yml", false);
        }
        translations = YamlConfiguration.loadConfiguration(file);
        if (getResource("lang.yml") != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new InputStreamReader(Objects.requireNonNull(getResource("lang.yml")), StandardCharsets.UTF_8)
            );
            translations.setDefaults(defaults);
            translations.options().copyDefaults(true);
        }
    }

    private String tr(String text, Object... args) {
        String resolved = text;
        if (!"ru".equalsIgnoreCase(language) && translations != null) {
            String candidate = translations.getString(language + "." + text);
            if (candidate != null) {
                resolved = candidate;
            }
        }
        if (args.length == 0) {
            return resolved;
        }
        return String.format(Locale.ROOT, resolved, args);
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        saveDefaultConfig();
        loadTranslations();
        language = getConfig().getString("language", "ru").toLowerCase(Locale.ROOT);
        phoneItemKey = new NamespacedKey(this, "phone_item");
        phoneBlockKey = new NamespacedKey(this, "phone_block");
        phoneTargetKey = new NamespacedKey(this, "phone_target");
        phoneResetKey = new NamespacedKey(this, "phone_reset");
        fixedRoundSlots.clear();
        fixedRoundSlots.put(4, RoundType.FOURTH);
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("sixrooms") != null) {
            getCommand("sixrooms").setTabCompleter(this);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("sixrooms")) {
            return null;
        }
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission(ADMIN_PERMISSION)) {
                completions.add("open");
                completions.add("start");
                completions.add("cancel");
                completions.add("status");
                completions.add("phone");
                completions.add("debug");
            }
            completions.add("join");
            completions.add("leave");
            
            String input = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String s : completions) {
                if (s.startsWith(input)) {
                    result.add(s);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public void onDisable() {
        endAllCalls(null);
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        participants.clear();
        playerNumbers.clear();
        scores.clear();
        state = GameState.IDLE;
        baseLocation = null;
        roomCenters.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("sixrooms")) {
            return false;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "open":
                handleOpen(sender);
                return true;
            case "join":
                handleJoin(sender);
                return true;
            case "leave":
                handleLeave(sender);
                return true;
            case "start":
                handleStart(sender);
                return true;
            case "cancel":
                handleCancel(sender);
                return true;
            case "status":
                handleStatus(sender);
                return true;
            case "phone":
                handlePhone(sender);
                return true;
            case "debug":
                handleDebug(sender);
                return true;
            default:
                sendUsage(sender);
                return true;
        }
    }

    private void handleOpen(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(tr("§cНет прав."));
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        if (state != GameState.IDLE) {
            sender.sendMessage(tr("§cРегистрация уже запущена или игра в процессе."));
            return;
        }
        participants.clear();
        playerNumbers.clear();
        baseLocation = ((Player) sender).getLocation();
        state = GameState.REGISTRATION;
        sender.sendMessage(tr("§aРегистрация открыта! §7Используйте §e/sixrooms join §7для входа."));
    }

    private void handleJoin(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        if (state != GameState.REGISTRATION) {
            sender.sendMessage(tr("§cРегистрация не активна."));
            return;
        }
        Player player = (Player) sender;
        UUID id = player.getUniqueId();
        if (participants.contains(id)) {
            player.sendMessage(tr("§cВы уже зарегистрированы."));
            return;
        }
        if (participants.size() >= MAX_PLAYERS) {
            player.sendMessage(tr("§cРегистрация заполнена."));
            return;
        }
        participants.add(id);
        player.sendMessage(tr("§aВы успешно зарегистрировались!"));
        for (UUID participantId : participants) {
            Player participant = Bukkit.getPlayer(participantId);
            if (participant != null && participant.isOnline() && !participant.getUniqueId().equals(id)) {
                participant.sendMessage(tr("§eИгрок §a%s §eприсоединился к игре. §7(%d/%d)", player.getName(), participants.size(), MAX_PLAYERS));
            }
        }
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        if (state != GameState.REGISTRATION) {
            sender.sendMessage(tr("§cРегистрация не активна."));
            return;
        }
        Player player = (Player) sender;
        if (participants.remove(player.getUniqueId())) {
            player.sendMessage(tr("§eВы покинули регистрацию."));
        } else {
            player.sendMessage(tr("§cВы не были зарегистрированы."));
        }
    }

    private void handleStart(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(tr("§cНет прав."));
            return;
        }
        if (state != GameState.REGISTRATION) {
            sender.sendMessage(tr("§cРегистрация не активна."));
            return;
        }
        int minPlayers = debug ? 2 : MIN_PLAYERS;
        if (participants.size() < minPlayers) {
            sender.sendMessage(tr("§cНедостаточно игроков для старта (минимум %d).", minPlayers));
            return;
        }
        if (baseLocation == null) {
            sender.sendMessage(tr("§cНе задана точка старта."));
            return;
        }
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        scores.clear();
        firstRoundChoices.clear();
        currentRound = 0;
        roomCenters.clear();
        remainingRoundPool.clear();
        remainingRoundPool.addAll(roundPool);
        Collections.shuffle(remainingRoundPool, random);
        File schematic = new File(getDataFolder(), SCHEMATIC_NAME);
        boolean useSchematic = schematic.exists();
        usingSchematicRooms = useSchematic;
        Clipboard clipboard = null;

        if (useSchematic) {
            if (!isWorldEditAvailable()) {
                sender.sendMessage(tr("§cWorldEdit не найден."));
                return;
            }
            try (ClipboardReader reader = getClipboardReader(schematic)) {
                if (reader == null) {
                    sender.sendMessage(tr("§cНе удалось прочитать схематику."));
                    return;
                }
                clipboard = reader.read();
            } catch (IOException e) {
                sender.sendMessage(tr("§cОшибка чтения схематики."));
                return;
            }
        } else {
            sender.sendMessage(tr("§eСхематика не найдена. Будет использована процедурная генерация."));
        }

        World world = baseLocation.getWorld();
        if (world == null) {
            sender.sendMessage(tr("§cМир недоступен."));
            return;
        }

        List<Player> players = new ArrayList<>();
        for (UUID id : participants) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        if (players.size() < minPlayers) {
            sender.sendMessage(tr("§cНедостаточно игроков онлайн (минимум %d).", minPlayers));
            return;
        }

        playerNumbers.clear();
        int totalPlayers = players.size();
        for (int i = 0; i < totalPlayers; i++) {
            int number = i + 1;
            BlockVector3 pasteTo = BlockVector3.at(
                baseLocation.getBlockX() + (i * ROOM_SPACING),
                baseLocation.getBlockY(),
                baseLocation.getBlockZ()
            );
            
            if (useSchematic) {
                if (!pasteSchematic(clipboard, world, pasteTo)) {
                    sender.sendMessage(tr("§cОшибка при вставке схематики."));
                    return;
                }
            } else {
                generateProceduralRoom(world, pasteTo);
            }

            Location teleportTo = new Location(
                world,
                pasteTo.x() + 0.5 + (useSchematic ? 0 : 4), // Center in 8x8 room
                pasteTo.y() + 1,
                pasteTo.z() + 0.5 + (useSchematic ? 0 : 4)
            );
            Player player = players.get(i);
            player.teleport(teleportTo);
            playerNumbers.put(player.getUniqueId(), number);
            roomCenters.put(player.getUniqueId(), teleportTo);
            player.sendMessage(tr("§aТвой номер: §e%d §aиз §e%d", number, totalPlayers));
        }

        state = GameState.RUNNING;

        // Create private groups for each player
        for (UUID playerId : playerNumbers.keySet()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                // Ensure fresh state
                p.performCommand("groups leave");
                
                String groupName = "SixRooms-" + p.getName();
                String password = "sr-" + p.getUniqueId().toString().substring(0, 8);
                // Create group (not persistent so it auto-deletes when empty)
                p.performCommand("groups create name:" + groupName + " password:" + password);

                p.sendMessage(tr("§aВы перешли в приватный канал связи."));
                p.sendMessage(tr("§7Используйте клавишу Группового Чата (по умолчанию 'V') для разговора."));
            }
        }
        
        getServer().broadcastMessage("");
        getServer().broadcastMessage(tr("§6§l---------------------------------------------"));
        getServer().broadcastMessage(tr("§e                SIX ROOMS"));
        getServer().broadcastMessage(tr("§a              Игра началась!"));
        getServer().broadcastMessage(tr("§7   Участникам выданы телефоны. Удачи!"));
        getServer().broadcastMessage(tr("§6§l---------------------------------------------"));
        getServer().broadcastMessage("");
        
        startFirstRoundPreparation(players);
    }

    private void generateProceduralRoom(World world, BlockVector3 origin) {
        int x = origin.x();
        int y = origin.y();
        int z = origin.z();
        int size = 8;
        int height = 5;

        // Generate box
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < height; dy++) {
                for (int dz = 0; dz < size; dz++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    if (dy == 0) {
                        block.setType(Material.OAK_PLANKS);
                    } else if (dy == height - 1) {
                        block.setType(Material.SMOOTH_STONE);
                    } else if (dx == 0 || dx == size - 1 || dz == 0 || dz == size - 1) {
                        block.setType(Material.STONE_BRICKS);
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        // Interior
        // Bed
        world.getBlockAt(x + 1, y + 1, z + 2).setType(Material.BLACK_BED); 
        // Torch
        world.getBlockAt(x + 4, y + 2, z + 4).setType(Material.TORCH);
        // Chest
        world.getBlockAt(x + 6, y + 1, z + 6).setType(Material.CHEST);

        // Phone block (Barrel)
        Block phoneBlock = world.getBlockAt(x + 4, y + 1, z + 1);
        phoneBlock.setType(Material.BARREL);
        if (phoneBlock.getState() instanceof TileState) {
            TileState state = (TileState) phoneBlock.getState();
            state.getPersistentDataContainer().set(phoneBlockKey, PersistentDataType.BYTE, (byte) 1);
            state.update();
        }
    }

    private void handleCancel(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(tr("§cНет прав."));
            return;
        }
        
        endAllCalls(tr("§eИгра отменена."));
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        
        // Cleanup groups for all participants
        for (UUID playerId : playerNumbers.keySet()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.performCommand("groups leave");
                p.sendMessage(tr("§eПриватный канал связи отключен."));
            }
        }
        
        participants.clear();
        playerNumbers.clear();
        roomCenters.clear();
        state = GameState.IDLE;
        baseLocation = null;
        sender.sendMessage(tr("§eРегистрация отменена."));
    }

    private void startFirstRoundPreparation(List<Player> players) {
        startRoundPreparation(players, FIRST_ROUND_PREP_SECONDS, tr("§eДо начала первого раунда: §a"), this::startNextRound);
    }

    private void stopFirstRoundPreparation() {
        if (roundPrepTaskId != -1) {
            Bukkit.getScheduler().cancelTask(roundPrepTaskId);
            roundPrepTaskId = -1;
        }
        if (roundPrepBar != null) {
            roundPrepBar.removeAll();
            roundPrepBar = null;
        }
    }

    private void startRoundPreparation(List<Player> players, int totalSeconds, String titlePrefix, Runnable onComplete) {
        stopFirstRoundPreparation();
        roundPrepBar = Bukkit.createBossBar(titlePrefix + formatTime(totalSeconds), BarColor.YELLOW, BarStyle.SOLID);
        roundPrepBar.setProgress(1.0);
        for (Player player : players) {
            roundPrepBar.addPlayer(player);
        }
        final int[] remaining = {totalSeconds};
        roundPrepTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            int secondsLeft = remaining[0]--;
            if (secondsLeft <= 0) {
                stopFirstRoundPreparation();
                onComplete.run();
                return;
            }
            if (roundPrepBar != null) {
                roundPrepBar.setTitle(titlePrefix + formatTime(secondsLeft));
                roundPrepBar.setProgress(Math.max(0.0, Math.min(1.0, secondsLeft / (double) totalSeconds)));
            }
        }, 0L, 20L).getTaskId();
    }

    private RoundType getNextRoundType() {
        int nextIndex = currentRound + 1;
        RoundType fixed = fixedRoundSlots.get(nextIndex);
        if (fixed != null) {
            remainingRoundPool.remove(fixed);
            return fixed;
        }
        if (remainingRoundPool.isEmpty()) {
            remainingRoundPool.addAll(roundPool);
            Collections.shuffle(remainingRoundPool, random);
        }
        if (remainingRoundPool.isEmpty()) {
            return RoundType.FIRST;
        }
        return remainingRoundPool.remove(0);
    }

    private void startNextRound() {
        if (currentRound >= MAX_ROUNDS) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        RoundType nextRound = getNextRoundType();
        currentRound++;
        currentRoundType = nextRound;
        if (nextRound == RoundType.FIRST) {
            startFirstRound();
        } else if (nextRound == RoundType.SECOND) {
            startSecondRound();
        } else if (nextRound == RoundType.THIRD) {
            startThirdRound();
        } else if (nextRound == RoundType.FOURTH) {
            startFourthRound();
        } else if (nextRound == RoundType.FIFTH) {
            startFifthRound();
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private void startFirstRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        firstRoundActive = true;
        firstRoundChoices.clear();
        previousHelmets.clear();
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        clownPlayerId = players.get(random.nextInt(players.size())).getUniqueId();
        List<MaskDefinition> selectedMasks = pickTwoMasks();
        MaskDefinition soloMask = selectedMasks.get(0);
        MaskDefinition groupMask = selectedMasks.get(1);
        ItemStack soloHead = createCustomHead(soloMask.base64);
        ItemStack groupHead = createCustomHead(groupMask.base64);
        for (Player player : players) {
            previousHelmets.put(player.getUniqueId(), player.getInventory().getHelmet());
            if (player.getUniqueId().equals(clownPlayerId)) {
                player.getInventory().setHelmet(soloHead);
            } else {
                player.getInventory().setHelmet(groupHead);
            }
        }
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eОпредели, у кого маска отличается от остальных."));
            player.sendMessage(tr("§eНапиши в чат ник или номер игрока."));
            player.sendMessage(tr("§7Сообщение увидишь только ты. Выбор можно менять."));
        }
        startRoundBar(players, FIRST_ROUND_SECONDS);
        firstRoundTaskId = Bukkit.getScheduler().runTaskLater(this, this::endFirstRound, FIRST_ROUND_SECONDS * 20L).getTaskId();
    }

    private void endFirstRound() {
        if (!firstRoundActive) {
            return;
        }
        firstRoundActive = false;
        stopRoundBar();
        if (firstRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(firstRoundTaskId);
            firstRoundTaskId = -1;
        }
        for (Map.Entry<UUID, UUID> entry : firstRoundChoices.entrySet()) {
            if (clownPlayerId != null && clownPlayerId.equals(entry.getValue())) {
                scores.put(entry.getKey(), scores.getOrDefault(entry.getKey(), 0) + 1);
            }
        }
        for (UUID playerId : previousHelmets.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().setHelmet(previousHelmets.get(playerId));
            }
        }
        previousHelmets.clear();
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eРаунд закончен. §7Подготовка 30 секунд."));
        }
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound);
        if (debug) {
            for (UUID playerId : playerNumbers.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("§eБаллы: §a%d", scores.getOrDefault(playerId, 0)));
                }
            }
        }
        stopSecondRound();
    }

    private void stopFirstRound() {
        firstRoundActive = false;
        stopRoundBar();
        if (firstRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(firstRoundTaskId);
            firstRoundTaskId = -1;
        }
        for (UUID playerId : previousHelmets.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.getInventory().setHelmet(previousHelmets.get(playerId));
            }
        }
        previousHelmets.clear();
        clownPlayerId = null;
        firstRoundChoices.clear();
    }

    private void startSecondRound() {
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        secondRoundActive = true;
        secondRoundCalled.clear();
        secondRoundCorrectGuessers.clear();
        secondRoundWord = null;
        secondRoundAwaitingWord = false;
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        Player wordOwner = players.get(random.nextInt(players.size()));
        secondRoundWordOwner = wordOwner.getUniqueId();
        secondRoundCurrentCaller = wordOwner.getUniqueId();
        secondRoundCalled.add(wordOwner.getUniqueId());
        boolean systemWord = random.nextBoolean();
        if (systemWord) {
            secondRoundWord = SECOND_ROUND_WORDS.get(random.nextInt(SECOND_ROUND_WORDS.size()));
            wordOwner.sendMessage(tr("§eТебе выдано слово: §a%s", secondRoundWord));
            wordOwner.sendMessage(tr("§eТеперь ты можешь звонить."));
        } else {
            secondRoundAwaitingWord = true;
            wordOwner.sendMessage(tr("§eНапиши слово в чат. Его никто не увидит."));
        }
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eУгадай слово по цепочке звонков."));
            player.sendMessage(tr("§eЗвонить может только тот, у кого сейчас слово."));
            player.sendMessage(tr("§7Время разговора 40 секунд. Пиши слово в чат."));
        }
        int totalSeconds = Math.max(1, (players.size() - 1) * 40);
        startRoundBar(players, totalSeconds);
    }

    private void endSecondRound() {
        if (!secondRoundActive) {
            return;
        }
        secondRoundActive = false;
        stopRoundBar();
        if (secondRoundCallTaskId != -1) {
            Bukkit.getScheduler().cancelTask(secondRoundCallTaskId);
            secondRoundCallTaskId = -1;
        }
        if (secondRoundWordOwner != null) {
            scores.put(secondRoundWordOwner, scores.getOrDefault(secondRoundWordOwner, 0) + 1);
        }
        for (UUID playerId : secondRoundCorrectGuessers) {
            scores.put(playerId, scores.getOrDefault(playerId, 0) + 1);
        }
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eРаунд закончен. §7Подготовка 30 секунд."));
        }
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound);
        if (debug) {
            for (UUID playerId : playerNumbers.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("§eБаллы: §a%d", scores.getOrDefault(playerId, 0)));
                }
            }
        }
    }

    private void stopSecondRound() {
        secondRoundActive = false;
        secondRoundAwaitingWord = false;
        secondRoundWord = null;
        secondRoundWordOwner = null;
        secondRoundCurrentCaller = null;
        secondRoundCalled.clear();
        secondRoundCorrectGuessers.clear();
        if (secondRoundCallTaskId != -1) {
            Bukkit.getScheduler().cancelTask(secondRoundCallTaskId);
            secondRoundCallTaskId = -1;
        }
    }

    private void scheduleTntTransferUnlock() {
        if (tntTransferTaskId != -1) {
            Bukkit.getScheduler().cancelTask(tntTransferTaskId);
            tntTransferTaskId = -1;
        }
        tntTransferAllowed = false;
        tntTransferTaskId = Bukkit.getScheduler().runTaskLater(this, () -> {
            tntTransferAllowed = true;
            if (tntHolderId != null) {
                Player holder = Bukkit.getPlayer(tntHolderId);
                if (holder != null && holder.isOnline()) {
                    holder.sendMessage(tr("§eТеперь можно передавать динамит."));
                }
            }
        }, 5L * 20L).getTaskId();
    }

    private Location getRoomCenter(Player player) {
        if (player == null) {
            return null;
        }
        return roomCenters.get(player.getUniqueId());
    }

    private void removeTntEntity() {
        if (tntEntityId == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(tntEntityId);
        if (entity != null) {
            entity.remove();
        }
        tntEntityId = null;
    }

    private boolean transferTntTo(Player sender, Player target) {
        if (tntEntityId == null || target == null || sender == null) {
            return false;
        }
        Entity entity = Bukkit.getEntity(tntEntityId);
        if (!(entity instanceof TNTPrimed)) {
            return false;
        }
        Location targetLocation = getRoomCenter(target);
        if (targetLocation == null) {
            targetLocation = target.getLocation();
        }
        entity.teleport(targetLocation);
        tntLastHolderId = sender.getUniqueId();
        tntHolderId = target.getUniqueId();
        scheduleTntTransferUnlock();
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eДинамит передан от §a%s §eк §a%s", sender.getName(), target.getName()));
        }
        target.sendMessage(tr("§cДинамит в твоей комнате."));
        return true;
    }

    private void startThirdRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        thirdRoundActive = true;
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        Player holder = players.get(random.nextInt(players.size()));
        tntHolderId = holder.getUniqueId();
        tntLastHolderId = null;
        tntTransferAllowed = false;
        tntFuseSeconds = 90 + random.nextInt(121);
        Location spawnLocation = getRoomCenter(holder);
        if (spawnLocation == null) {
            spawnLocation = holder.getLocation();
        }
        TNTPrimed tnt = spawnLocation.getWorld().spawn(spawnLocation, TNTPrimed.class);
        tnt.setFuseTicks(tntFuseSeconds * 20);
        tnt.setSilent(true);
        tnt.setYield(0);
        tnt.setIsIncendiary(false);
        tntEntityId = tnt.getUniqueId();
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eДинамит уже подожжён."));
            player.sendMessage(tr("§eПередавать можно через 5 секунд."));
        }
        holder.sendMessage(tr("§cДинамит в твоей комнате."));
        scheduleTntTransferUnlock();
    }

    private void endThirdRound(UUID explodedFor) {
        if (!thirdRoundActive) {
            return;
        }
        thirdRoundActive = false;
        stopRoundBar();
        if (tntTransferTaskId != -1) {
            Bukkit.getScheduler().cancelTask(tntTransferTaskId);
            tntTransferTaskId = -1;
        }
        removeTntEntity();
        if (explodedFor != null) {
            Player player = Bukkit.getPlayer(explodedFor);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            }
        }
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eРаунд закончен. §7Подготовка 30 секунд."));
        }
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound);
        if (debug) {
            for (UUID playerId : playerNumbers.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("§eБаллы: §a%d", scores.getOrDefault(playerId, 0)));
                }
            }
        }
        stopThirdRound();
    }

    private void stopThirdRound() {
        thirdRoundActive = false;
        tntTransferAllowed = false;
        tntHolderId = null;
        tntLastHolderId = null;
        tntFuseSeconds = 0;
        if (tntTransferTaskId != -1) {
            Bukkit.getScheduler().cancelTask(tntTransferTaskId);
            tntTransferTaskId = -1;
        }
        removeTntEntity();
    }

    private void startFourthRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        fourthRoundActive = true;
        swordUsed = false;
        swordVisitActive = false;
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        Player holder = players.get(random.nextInt(players.size()));
        swordHolderId = holder.getUniqueId();
        swordPreviousItem = holder.getInventory().getItemInMainHand();
        ItemStack swordItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta swordMeta = swordItem.getItemMeta();
        if (swordMeta != null) {
            swordMeta.setDisplayName(tr("§cМеч"));
            swordItem.setItemMeta(swordMeta);
        }
        holder.getInventory().setItemInMainHand(swordItem);
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eВыбери в телефоне, к кому хочешь прийти."));
        }
        holder.sendMessage(tr("§cУ тебя меч. Используй телефон для выбора цели."));
        startRoundBar(players, SWORD_CHOICE_SECONDS);
        if (swordChoiceTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordChoiceTaskId);
        }
        swordChoiceTaskId = Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!fourthRoundActive || swordVisitActive || swordUsed) {
                return;
            }
            Player currentHolder = Bukkit.getPlayer(swordHolderId);
            if (currentHolder == null) {
                endFourthRound();
                return;
            }
            List<Player> available = new ArrayList<>(getOnlineParticipants());
            available.removeIf(p -> p.getUniqueId().equals(currentHolder.getUniqueId()));
            if (available.isEmpty()) {
                endFourthRound();
                return;
            }
            Player target = available.get(random.nextInt(available.size()));
            startSwordVisit(currentHolder, target);
        }, SWORD_CHOICE_SECONDS * 20L).getTaskId();
    }

    private void endFourthRound() {
        if (!fourthRoundActive) {
            return;
        }
        fourthRoundActive = false;
        stopRoundBar();
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eРаунд закончен. §7Подготовка 30 секунд."));
        }
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound);
        if (debug) {
            for (UUID playerId : playerNumbers.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("§eБаллы: §a%d", scores.getOrDefault(playerId, 0)));
                }
            }
        }
        stopFourthRound();
    }

    private void stopFourthRound() {
        fourthRoundActive = false;
        swordUsed = false;
        swordVisitActive = false;
        swordVisitTargetId = null;
        swordReturnLocation = null;
        if (swordChoiceTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordChoiceTaskId);
            swordChoiceTaskId = -1;
        }
        if (swordVisitTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordVisitTaskId);
            swordVisitTaskId = -1;
        }
        if (swordReturnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordReturnTaskId);
            swordReturnTaskId = -1;
        }
        if (swordHolderId != null) {
            Player holder = Bukkit.getPlayer(swordHolderId);
            if (holder != null && holder.isOnline()) {
                holder.getInventory().setItemInMainHand(swordPreviousItem);
            }
        }
        swordPreviousItem = null;
        swordHolderId = null;
    }

    private void startSwordVisit(Player holder, Player target) {
        if (holder == null || target == null) {
            return;
        }
        if (swordChoiceTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordChoiceTaskId);
            swordChoiceTaskId = -1;
        }
        swordVisitActive = true;
        swordVisitTargetId = target.getUniqueId();
        swordReturnLocation = getRoomCenter(holder);
        if (swordReturnLocation == null) {
            swordReturnLocation = holder.getLocation();
        }
        Location targetLocation = getRoomCenter(target);
        if (targetLocation == null) {
            targetLocation = target.getLocation();
        }
        holder.teleport(targetLocation);
        holder.sendMessage(tr("§eТы пришёл к игроку §a%s", target.getName()));
        target.sendMessage(tr("§eК тебе пришёл игрок §a%s", holder.getName()));
        holder.sendMessage(tr("§eУ вас 60 секунд на разговор."));
        target.sendMessage(tr("§eУ вас 60 секунд на разговор."));
        startRoundBar(getOnlineParticipants(), SWORD_VISIT_SECONDS);
        if (swordVisitTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordVisitTaskId);
        }
        swordVisitTaskId = Bukkit.getScheduler().runTaskLater(this, () -> finalizeSwordHit(holder, target, true), SWORD_VISIT_SECONDS * 20L).getTaskId();
    }

    private void finalizeSwordHit(Player holder, Player target, boolean auto) {
        if (!fourthRoundActive || swordUsed) {
            return;
        }
        if (holder == null || target == null) {
            endFourthRound();
            return;
        }
        swordUsed = true;
        scores.put(holder.getUniqueId(), scores.getOrDefault(holder.getUniqueId(), 0) + 1);
        scores.put(target.getUniqueId(), scores.getOrDefault(target.getUniqueId(), 0) - 1);
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eИгрок §a%s §eударил игрока §a%s", holder.getName(), target.getName()));
        }
        if (swordVisitTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordVisitTaskId);
            swordVisitTaskId = -1;
        }
        if (auto) {
            teleportSwordHolderBack(holder);
            endFourthRound();
            return;
        }
        if (swordReturnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(swordReturnTaskId);
        }
        swordReturnTaskId = Bukkit.getScheduler().runTaskLater(this, () -> {
            teleportSwordHolderBack(holder);
            endFourthRound();
        }, 5L * 20L).getTaskId();
    }

    private void teleportSwordHolderBack(Player holder) {
        if (holder == null || !holder.isOnline()) {
            return;
        }
        Location returnLocation = swordReturnLocation != null ? swordReturnLocation : getRoomCenter(holder);
        if (returnLocation == null) {
            returnLocation = holder.getLocation();
        }
        holder.teleport(returnLocation);
    }

    private void startFifthRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        doorsRoundActive = true;
        doorsRoundChoices.clear();
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eВыбери: открыть дверь или остаться."));
        }
        if (!usingSchematicRooms) {
            for (Player player : players) {
                Location center = getRoomCenter(player);
                if (center != null) {
                    spawnDoorRoundPlates(player, center);
                }
            }
        }
        startRoundBar(players, DOORS_ROUND_SECONDS);
        if (doorsRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(doorsRoundTaskId);
        }
        doorsRoundTaskId = Bukkit.getScheduler().runTaskLater(this, this::endFifthRound, DOORS_ROUND_SECONDS * 20L).getTaskId();
    }

    private void endFifthRound() {
        if (!doorsRoundActive) {
            return;
        }
        doorsRoundActive = false;
        stopRoundBar();
        int openedCount = 0;
        for (Boolean opened : doorsRoundChoices.values()) {
            if (Boolean.TRUE.equals(opened)) {
                openedCount++;
            }
        }
        boolean odd = openedCount % 2 == 1;
        for (UUID playerId : playerNumbers.keySet()) {
            if (Boolean.TRUE.equals(doorsRoundChoices.get(playerId))) {
                int delta = odd ? 1 : -1;
                scores.put(playerId, scores.getOrDefault(playerId, 0) + delta);
            }
        }
        List<Player> players = getOnlineParticipants();
        String resultMessage = odd
            ? tr("§aНечётное число открытых дверей. Открывшие получают +1.")
            : tr("§cЧётное число открытых дверей. Открывшие получают -1.");
        for (Player player : players) {
            player.sendMessage(resultMessage);
        }
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        for (Player player : players) {
            player.sendMessage(tr("§eРаунд закончен. §7Подготовка 30 секунд."));
        }
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound);
        if (debug) {
            for (UUID playerId : playerNumbers.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("§eБаллы: §a%d", scores.getOrDefault(playerId, 0)));
                }
            }
        }
        stopFifthRound();
    }

    private void stopFifthRound() {
        doorsRoundActive = false;
        doorsRoundChoices.clear();
        if (doorsRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(doorsRoundTaskId);
            doorsRoundTaskId = -1;
        }
        for (UUID id : doorRoundHolograms) {
            Entity entity = Bukkit.getEntity(id);
            if (entity != null) {
                entity.remove();
            }
        }
        doorRoundHolograms.clear();
        for (Map.Entry<Location, Material> entry : doorRoundReplacedBlocks.entrySet()) {
            Location location = entry.getKey();
            Material material = entry.getValue();
            location.getBlock().setType(material);
        }
        doorRoundReplacedBlocks.clear();
        doorOpenPlateLocations.clear();
        doorStayPlateLocations.clear();
    }

    private void spawnDoorRoundPlates(Player player, Location center) {
        if (player == null || center == null) {
            return;
        }
        World world = center.getWorld();
        if (world == null) {
            return;
        }
        int floorY = center.getBlockY() - 1;
        Location openLoc = new Location(world, center.getBlockX() + 1, floorY, center.getBlockZ());
        Location stayLoc = new Location(world, center.getBlockX() - 1, floorY, center.getBlockZ());
        placeDoorRoundPlate(openLoc);
        placeDoorRoundPlate(stayLoc);
        doorOpenPlateLocations.put(player.getUniqueId(), openLoc);
        doorStayPlateLocations.put(player.getUniqueId(), stayLoc);
        spawnDoorRoundHologram(openLoc, tr("§aОткрыть"));
        spawnDoorRoundHologram(stayLoc, tr("§eОстаться"));
    }

    private void placeDoorRoundPlate(Location location) {
        Block block = location.getBlock();
        Location key = block.getLocation();
        if (!doorRoundReplacedBlocks.containsKey(key)) {
            doorRoundReplacedBlocks.put(key, block.getType());
        }
        block.setType(Material.STONE_PRESSURE_PLATE);
    }

    private void spawnDoorRoundHologram(Location base, String text) {
        Location spawn = base.clone().add(0.5, 1.2, 0.5);
        Entity entity = base.getWorld().spawn(spawn, org.bukkit.entity.ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setCustomName(text);
            stand.setCustomNameVisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setSmall(true);
        });
        doorRoundHolograms.add(entity.getUniqueId());
    }

    private void markDoorRoundChoice(Player player, boolean opened) {
        if (player == null) {
            return;
        }
        doorsRoundChoices.put(player.getUniqueId(), opened);
        player.sendMessage(opened ? tr("§eВы выбрали: §aОткрыть") : tr("§eВы выбрали: §aОстаться"));
    }

    private boolean isSameBlock(Location first, Location second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.getWorld() == null || second.getWorld() == null) {
            return false;
        }
        return first.getWorld().equals(second.getWorld())
            && first.getBlockX() == second.getBlockX()
            && first.getBlockY() == second.getBlockY()
            && first.getBlockZ() == second.getBlockZ();
    }

    private boolean shouldFinishGame() {
        if (currentRound >= MAX_ROUNDS) {
            return true;
        }
        for (UUID playerId : playerNumbers.keySet()) {
            if (scores.getOrDefault(playerId, 0) >= MAX_SCORE) {
                return true;
            }
        }
        return false;
    }

    private void finishGame(String message) {
        endAllCalls(message);
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopRoundBar();
        stopRoundBar();
        stopRoundBar();
        List<Player> players = getOnlineParticipants();
        if (message != null) {
            for (Player player : players) {
                player.sendMessage(message);
            }
        }
        for (UUID playerId : playerNumbers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.performCommand("groups leave");
                player.sendMessage(tr("§eПриватный канал связи отключен."));
            }
        }
        participants.clear();
        playerNumbers.clear();
        roomCenters.clear();
        scores.clear();
        firstRoundChoices.clear();
        remainingRoundPool.clear();
        state = GameState.IDLE;
        baseLocation = null;
        currentRound = 0;
        currentRoundType = RoundType.FIRST;
    }

    private List<MaskDefinition> pickTwoMasks() {
        if (maskDefinitions.size() <= 1) {
            return List.of(maskDefinitions.get(0), maskDefinitions.get(0));
        }
        List<MaskDefinition> copy = new ArrayList<>(maskDefinitions);
        Collections.shuffle(copy, random);
        return List.of(copy.get(0), copy.get(1));
    }

    private boolean canSecondRoundCall(UUID playerId) {
        return secondRoundActive
            && !secondRoundAwaitingWord
            && secondRoundWord != null
            && secondRoundCurrentCaller != null
            && secondRoundCurrentCaller.equals(playerId);
    }

    private void handleSecondRoundChat(Player player, String message) {
        if (!secondRoundActive) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (secondRoundAwaitingWord && playerId.equals(secondRoundWordOwner)) {
            if (message.isEmpty()) {
                player.sendMessage(tr("§cСлово не может быть пустым."));
                return;
            }
            secondRoundWord = message;
            secondRoundAwaitingWord = false;
            player.sendMessage(tr("§7Слово установлено. Теперь ты можешь звонить."));
            return;
        }
        if (secondRoundWord == null) {
            player.sendMessage(tr("§7Слово ещё не задано."));
            return;
        }
        if (playerId.equals(secondRoundWordOwner)) {
            player.sendMessage(tr("§7Ты уже знаешь слово."));
            return;
        }
        if (message.equalsIgnoreCase(secondRoundWord)) {
            secondRoundCorrectGuessers.add(playerId);
        }
        player.sendMessage(tr("§7Ответ сохранён."));
    }

    private void handleSecondRoundCallEnded(UUID callerId, UUID calleeId) {
        if (!secondRoundActive) {
            return;
        }
        secondRoundCalled.add(calleeId);
        secondRoundCurrentCaller = calleeId;
        Player callee = Bukkit.getPlayer(calleeId);
        if (callee != null && callee.isOnline()) {
            callee.sendMessage(tr("§eТеперь ты можешь звонить."));
        }
        if (secondRoundCalled.size() >= playerNumbers.size()) {
            endSecondRound();
        }
    }

    private void startRoundBar(List<Player> players, int totalSeconds) {
        stopRoundBar();
        roundBar = Bukkit.createBossBar("§eДо конца раунда: §a" + formatTime(totalSeconds), BarColor.BLUE, BarStyle.SOLID);
        roundBar.setProgress(1.0);
        for (Player player : players) {
            roundBar.addPlayer(player);
        }
        final int[] remaining = {totalSeconds};
        roundBarTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (!firstRoundActive && !secondRoundActive && !thirdRoundActive) {
                stopRoundBar();
                return;
            }
            int secondsLeft = remaining[0]--;
            if (secondsLeft <= 0) {
                stopRoundBar();
                return;
            }
            if (roundBar != null) {
                roundBar.setTitle("§eДо конца раунда: §a" + formatTime(secondsLeft));
                roundBar.setProgress(Math.max(0.0, Math.min(1.0, secondsLeft / (double) totalSeconds)));
            }
        }, 0L, 20L).getTaskId();
    }

    private void stopRoundBar() {
        if (roundBarTaskId != -1) {
            Bukkit.getScheduler().cancelTask(roundBarTaskId);
            roundBarTaskId = -1;
        }
        if (roundBar != null) {
            roundBar.removeAll();
            roundBar = null;
        }
    }

    private List<Player> getOnlineParticipants() {
        List<Player> players = new ArrayList<>();
        for (UUID id : playerNumbers.keySet()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    private ItemStack createPlayerHead(String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createCustomHead(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }
        try {
            String url = extractTextureUrl(base64);
            if (url == null) {
                return head;
            }
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (MalformedURLException ignored) {
            return head;
        }
        head.setItemMeta(meta);
        return head;
    }

    private String extractTextureUrl(String base64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            int start = decoded.indexOf("http");
            if (start == -1) {
                return null;
            }
            int endQuote = decoded.indexOf("\"", start);
            int endBrace = decoded.indexOf("}", start);
            int end = endQuote == -1 ? endBrace : (endBrace == -1 ? endQuote : Math.min(endQuote, endBrace));
            if (end == -1) {
                return decoded.substring(start);
            }
            return decoded.substring(start, end);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handleRoundChoice(Player player, String message) {
        if (!firstRoundActive || state != GameState.RUNNING) {
            return;
        }
        UUID targetId = resolvePlayerChoice(message);
        if (targetId == null) {
            player.sendMessage(tr("§cНеверный выбор. Напиши ник или номер игрока."));
            return;
        }
        firstRoundChoices.put(player.getUniqueId(), targetId);
        player.sendMessage(tr("§7Выбор сохранён."));
    }

    private UUID resolvePlayerChoice(String message) {
        try {
            int number = Integer.parseInt(message);
            for (Map.Entry<UUID, Integer> entry : playerNumbers.entrySet()) {
                if (entry.getValue() == number) {
                    return entry.getKey();
                }
            }
        } catch (NumberFormatException ignored) {
        }
        for (UUID id : playerNumbers.keySet()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.getName().equalsIgnoreCase(message)) {
                return id;
            }
        }
        return null;
    }

    private void handleStatus(CommandSender sender) {
        sender.sendMessage(tr("§eСтатус: §a%s§e, игроков: §a%d", state.name(), participants.size()));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(tr("§eИспользование: §7/sixrooms open|join|leave|start|cancel|status|phone"));
    }

    private void handlePhone(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(tr("§cНет прав."));
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        Player player = (Player) sender;
        player.getInventory().addItem(createPhoneItem());
        player.sendMessage(tr("§aТелефон выдан."));
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(tr("§cНет прав."));
            return;
        }
        debug = !debug;
        sender.sendMessage(tr("§eРежим отладки: %s", debug ? tr("§aВКЛ") : tr("§cВЫКЛ")));
    }

    private ClipboardReader getClipboardReader(File schematic) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        if (format == null) {
            return null;
        }
        return format.getReader(new FileInputStream(schematic));
    }

    private boolean pasteSchematic(Clipboard clipboard, World world, BlockVector3 to) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(to)
                .ignoreAirBlocks(false)
                .build();
            Operations.complete(operation);
            editSession.flushSession();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWorldEditAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
        return plugin != null && plugin.isEnabled();
    }

    @EventHandler
    public void onGameBlockPlace(BlockPlaceEvent event) {
        if (!isPlayerInGame(event.getPlayer().getUniqueId())) {
            return;
        }
        if (isPhoneItem(event.getItemInHand())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onGameBlockBreak(BlockBreakEvent event) {
        if (!isPlayerInGame(event.getPlayer().getUniqueId())) {
            return;
        }
        if (isPhoneBlock(event.getBlock())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onGameDoorInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!isPlayerInGame(event.getPlayer().getUniqueId())) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || isPhoneBlock(block)) {
            return;
        }
        String name = block.getType().name();
        if (name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR") || name.endsWith("_FENCE_GATE")) {
            if (doorsRoundActive && usingSchematicRooms) {
                markDoorRoundChoice(event.getPlayer(), true);
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDoorRoundPlateInteract(PlayerInteractEvent event) {
        if (!doorsRoundActive || usingSchematicRooms) {
            return;
        }
        if (!isPlayerInGame(event.getPlayer().getUniqueId())) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.PHYSICAL && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.STONE_PRESSURE_PLATE) {
            return;
        }
        UUID playerId = event.getPlayer().getUniqueId();
        Location openLoc = doorOpenPlateLocations.get(playerId);
        Location stayLoc = doorStayPlateLocations.get(playerId);
        Location blockLoc = block.getLocation();
        if (isSameBlock(blockLoc, openLoc)) {
            markDoorRoundChoice(event.getPlayer(), true);
        } else if (isSameBlock(blockLoc, stayLoc)) {
            markDoorRoundChoice(event.getPlayer(), false);
        }
    }

    @EventHandler
    public void onTntExplode(EntityExplodeEvent event) {
        if (tntEntityId == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof TNTPrimed)) {
            return;
        }
        if (!entity.getUniqueId().equals(tntEntityId)) {
            return;
        }
        event.setCancelled(true);
        endThirdRound(tntHolderId);
    }

    @EventHandler
    public void onTntDamage(EntityDamageByEntityEvent event) {
        if (tntEntityId == null) {
            return;
        }
        Entity damager = event.getDamager();
        if (!(damager instanceof TNTPrimed)) {
            return;
        }
        if (!damager.getUniqueId().equals(tntEntityId)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onSwordHit(EntityDamageByEntityEvent event) {
        if (!fourthRoundActive || !swordVisitActive || swordUsed) {
            return;
        }
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }
        Player damager = (Player) event.getDamager();
        Player target = (Player) event.getEntity();
        if (swordHolderId == null || !swordHolderId.equals(damager.getUniqueId())) {
            return;
        }
        if (swordVisitTargetId == null || !swordVisitTargetId.equals(target.getUniqueId())) {
            return;
        }
        event.setCancelled(true);
        finalizeSwordHit(damager, target, false);
    }

    @EventHandler
    public void onPhonePlace(BlockPlaceEvent event) {
        if (!isPhoneItem(event.getItemInHand())) {
            return;
        }
        Block block = event.getBlockPlaced();
        if (!(block.getState() instanceof TileState)) {
            return;
        }
        TileState state = (TileState) block.getState();
        state.getPersistentDataContainer().set(phoneBlockKey, PersistentDataType.BYTE, (byte) 1);
        state.update();
    }

    @EventHandler
    public void onPhoneBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isPhoneBlock(block)) {
            return;
        }
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation(), createPhoneItem());
        }
    }

    @EventHandler
    public void onPhoneInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !isPhoneBlock(block)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        CallSession call = callsByPlayer.get(player.getUniqueId());
        if (call != null) {
            if (call.state == CallState.RINGING && player.getUniqueId().equals(call.callee)) {
                acceptCall(call);
                return;
            }
            openPhoneGui(player);
            return;
        }
        if (secondRoundActive && !canSecondRoundCall(player.getUniqueId())) {
            player.sendMessage(tr("§cСейчас нельзя звонить."));
            return;
        }
        openPhoneGui(player);
    }

    @EventHandler
    public void onPhoneGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof PhoneInventoryHolder)) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String resetAction = meta.getPersistentDataContainer().get(phoneResetKey, PersistentDataType.STRING);
        if (resetAction != null) {
            Player caller = (Player) event.getWhoClicked();
            CallSession call = callsByPlayer.get(caller.getUniqueId());
            if (call != null) {
                endCall(call, tr("§eЗвонок завершен."));
            } else {
                caller.sendMessage(tr("§7Нет активного звонка."));
            }
            caller.closeInventory();
            return;
        }
        String targetId = meta.getPersistentDataContainer().get(phoneTargetKey, PersistentDataType.STRING);
        if (targetId == null) {
            return;
        }
        Player caller = (Player) event.getWhoClicked();
        if (fourthRoundActive && swordHolderId != null && swordHolderId.equals(caller.getUniqueId())) {
            if (swordUsed) {
                caller.sendMessage(tr("§cМеч уже использован."));
                return;
            }
            if (swordVisitActive) {
                caller.sendMessage(tr("§cТы уже выбрал цель."));
                return;
            }
            UUID targetUuid;
            try {
                targetUuid = UUID.fromString(targetId);
            } catch (IllegalArgumentException e) {
                caller.sendMessage(tr("§cОшибка: неверный формат ID (%s)", targetId));
                return;
            }
            if (caller.getUniqueId().equals(targetUuid)) {
                caller.sendMessage(tr("§cНельзя выбрать себя."));
                return;
            }
            Player target = Bukkit.getPlayer(targetUuid);
            if (target == null || !target.isOnline()) {
                caller.sendMessage(tr("§cИгрок не в сети."));
                return;
            }
            caller.closeInventory();
            startSwordVisit(caller, target);
            return;
        }
        if (thirdRoundActive && tntHolderId != null && tntHolderId.equals(caller.getUniqueId())) {
            if (!tntTransferAllowed) {
                caller.sendMessage(tr("§cПередача ещё недоступна."));
                return;
            }
            UUID targetUuid;
            try {
                targetUuid = UUID.fromString(targetId);
            } catch (IllegalArgumentException e) {
                caller.sendMessage(tr("§cОшибка: неверный формат ID (%s)", targetId));
                return;
            }
            if (caller.getUniqueId().equals(targetUuid)) {
                caller.sendMessage(tr("§cНельзя передать себе."));
                return;
            }
            if (tntLastHolderId != null && tntLastHolderId.equals(targetUuid)) {
                caller.sendMessage(tr("§cНельзя передать тому, кто передал тебе."));
                return;
            }
            Player target = Bukkit.getPlayer(targetUuid);
            if (target == null || !target.isOnline()) {
                caller.sendMessage(tr("§cИгрок не в сети."));
                return;
            }
            if (!transferTntTo(caller, target)) {
                caller.sendMessage(tr("§cДинамит не найден."));
                return;
            }
            caller.closeInventory();
            return;
        }
        if (secondRoundActive && !canSecondRoundCall(caller.getUniqueId())) {
            caller.sendMessage(tr("§cСейчас нельзя звонить."));
            return;
        }
        UUID targetUuid;
        try {
            targetUuid = UUID.fromString(targetId);
        } catch (IllegalArgumentException e) {
            caller.sendMessage(tr("§cОшибка: неверный формат ID (%s)", targetId));
            return;
        }
        if (caller.getUniqueId().equals(targetUuid)) {
            caller.sendMessage(tr("§cНельзя звонить себе."));
            return;
        }
        if (secondRoundActive && secondRoundCalled.contains(targetUuid)) {
            caller.sendMessage(tr("§cЭтот игрок уже получил звонок."));
            return;
        }
        if (callsByPlayer.containsKey(caller.getUniqueId())) {
            caller.sendMessage(tr("§cТы уже в звонке."));
            return;
        }
        if (callsByPlayer.containsKey(targetUuid)) {
            caller.sendMessage(tr("§cАбонент занят."));
            return;
        }
        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) {
            caller.sendMessage(tr("§cАбонент не в сети."));
            return;
        }
        caller.closeInventory();
        startCall(caller, target);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CallSession call = callsByPlayer.get(event.getPlayer().getUniqueId());
        if (call != null) {
            endCall(call, tr("§eЗвонок завершен."));
        }
    }

    @EventHandler
    public void onRoundChat(AsyncPlayerChatEvent event) {
        if (state != GameState.RUNNING) {
            return;
        }
        UUID playerId = event.getPlayer().getUniqueId();
        if (!playerNumbers.containsKey(playerId)) {
            return;
        }
        if (fourthRoundActive && swordVisitActive && swordVisitTargetId != null && swordHolderId != null) {
            if (playerId.equals(swordHolderId) || playerId.equals(swordVisitTargetId)) {
                String message = event.getMessage().trim();
                event.setCancelled(true);
                Player holder = Bukkit.getPlayer(swordHolderId);
                Player target = Bukkit.getPlayer(swordVisitTargetId);
                String formatted = "§7" + event.getPlayer().getName() + ": " + message;
                if (holder != null) {
                    holder.sendMessage(formatted);
                }
                if (target != null && (holder == null || !target.getUniqueId().equals(holder.getUniqueId()))) {
                    target.sendMessage(formatted);
                }
                return;
            }
        }
        if (firstRoundActive) {
            String message = event.getMessage().trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> handleRoundChoice(event.getPlayer(), message));
            return;
        }
        if (secondRoundActive) {
            String message = event.getMessage().trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> handleSecondRoundChat(event.getPlayer(), message));
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(tr("§7Сообщение скрыто."));
        return;
    }

    private ItemStack createPhoneItem() {
        ItemStack item = new ItemStack(Material.BARREL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(tr("Телефон"));
            meta.getPersistentDataContainer().set(phoneItemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isPhoneItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(phoneItemKey, PersistentDataType.BYTE);
    }

    private boolean isPhoneBlock(Block block) {
        if (!(block.getState() instanceof TileState)) {
            return false;
        }
        TileState state = (TileState) block.getState();
        return state.getPersistentDataContainer().has(phoneBlockKey, PersistentDataType.BYTE);
    }

    private void openPhoneGui(Player player) {
        if (state != GameState.RUNNING) {
            player.sendMessage(tr("§cИгра не запущена."));
            return;
        }
        if (!playerNumbers.containsKey(player.getUniqueId())) {
            player.sendMessage(tr("§cТы не участвуешь в игре."));
            return;
        }
        Inventory inventory = Bukkit.createInventory(new PhoneInventoryHolder(), 54, tr("Телефон"));

        // Round Info
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(tr("§eСтадия раунда"));
            List<String> lore = new ArrayList<>();
            if (firstRoundActive) {
                lore.add(tr("§7Раунд с масками"));
            } else if (secondRoundActive) {
                lore.add(tr("§7Раунд с угадыванием слова"));
            } else if (thirdRoundActive) {
                lore.add(tr("§7Раунд с динамитом"));
                if (tntHolderId != null) {
                    if (tntHolderId.equals(player.getUniqueId())) {
                        lore.add(tr("§cДинамит у тебя"));
                        lore.add(tntTransferAllowed ? tr("§eПередача доступна") : tr("§7Передача через 5 секунд"));
                    } else {
                        Player holder = Bukkit.getPlayer(tntHolderId);
                        if (holder != null) {
                            Integer number = playerNumbers.get(holder.getUniqueId());
                            lore.add(tr("§7Динамит у игрока №%s", number != null ? number.toString() : "?"));
                        }
                    }
                }
            } else if (fourthRoundActive) {
                lore.add(tr("§7Раунд с мечом"));
                if (swordHolderId != null) {
                    if (swordHolderId.equals(player.getUniqueId())) {
                        lore.add(tr("§cМеч у тебя"));
                    } else {
                        Player holder = Bukkit.getPlayer(swordHolderId);
                        if (holder != null) {
                            Integer number = playerNumbers.get(holder.getUniqueId());
                            lore.add(tr("§7Меч у игрока №%s", number != null ? number.toString() : "?"));
                        }
                    }
                }
            } else if (doorsRoundActive) {
                lore.add(tr("§7Раунд с дверями"));
            } else {
                lore.add(tr("§7Подготовка к раунду"));
            }
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(4, infoItem);
        ItemStack resetItem = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = resetItem.getItemMeta();
        if (resetMeta != null) {
            resetMeta.setDisplayName(tr("§cСбросить"));
            resetMeta.getPersistentDataContainer().set(phoneResetKey, PersistentDataType.STRING, "reset");
            resetItem.setItemMeta(resetMeta);
        }
        inventory.setItem(49, resetItem);

        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        List<Integer> headSlots = new ArrayList<>();
        for (int col = 0; col < 9; col++) {
            for (int row = 0; row < 6; row++) {
                int candidate = row * 9 + col;
                if (candidate == 4 || candidate == 49) {
                    continue;
                }
                headSlots.add(candidate);
            }
        }
        int slotIndex = 0;
        for (Map.Entry<UUID, Integer> entry : entries) {
            if (slotIndex >= headSlots.size()) {
                break;
            }
            UUID targetUuid = entry.getKey();
            int number = entry.getValue();
            Player target = Bukkit.getPlayer(targetUuid);
            
            if (targetUuid.equals(player.getUniqueId())) {
                continue;
            }

            if (target == null || !target.isOnline()) {
                continue;
            }
            boolean busy = callsByPlayer.containsKey(targetUuid);
            
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(busy
                    ? tr("§c№%d §7%s (Занят)", number, target.getName())
                    : tr("§a№%d §7%s", number, target.getName()));
                meta.setOwningPlayer(target);
                meta.getPersistentDataContainer().set(phoneTargetKey, PersistentDataType.STRING, targetUuid.toString());
                item.setItemMeta(meta);
            }
            int headSlot = headSlots.get(slotIndex);
            inventory.setItem(headSlot, item);
            slotIndex++;
        }
        player.openInventory(inventory);
    }

    private void startCall(Player caller, Player callee) {
        CallSession session = new CallSession(caller.getUniqueId(), callee.getUniqueId(), CallState.RINGING);
        callsByPlayer.put(caller.getUniqueId(), session);
        callsByPlayer.put(callee.getUniqueId(), session);

        // Caller joins their own group (re-create to ensure existence)
        String groupName = "SixRooms-" + caller.getName();
        String password = "sr-" + caller.getUniqueId().toString().substring(0, 8);
        caller.performCommand("groups leave");
        caller.performCommand("groups create name:" + groupName + " password:" + password);

        int number = playerNumbers.getOrDefault(callee.getUniqueId(), -1);
        caller.sendMessage(tr("§eЗвонок на номер §a%d§e...", number));
        callee.sendMessage(tr("§eТебе звонят! §aПКМ по телефону §eдля ответа."));
        callee.playSound(callee.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    private void acceptCall(CallSession session) {
        Player caller = Bukkit.getPlayer(session.caller);
        Player callee = Bukkit.getPlayer(session.callee);
        if (caller == null || callee == null) {
            endCall(session, tr("§eЗвонок завершен."));
            return;
        }
        
        session.state = CallState.ACTIVE;
        
        // Callee joins caller's group
        String groupName = "SixRooms-" + caller.getName();
        String password = "sr-" + caller.getUniqueId().toString().substring(0, 8);
        callee.performCommand("groups leave");
        
        UUID groupUuid = null;
        GroupsManager gm = voiceAddon.getGroupsManager();
        if (gm != null) {
            for (Group g : gm.getGroups().values()) {
                if (g.getName().equals(groupName)) {
                    groupUuid = g.getId();
                    break;
                }
            }
        }
        
        if (groupUuid != null) {
            callee.performCommand("groups join " + groupUuid.toString() + " " + password);
        } else {
            callee.sendMessage(tr("§cОшибка: группа звонящего не найдена."));
        }
        
        caller.sendMessage(tr("§aЗвонок принят."));
        callee.sendMessage(tr("§aЗвонок принят."));
        if (secondRoundActive) {
            if (secondRoundCallTaskId != -1) {
                Bukkit.getScheduler().cancelTask(secondRoundCallTaskId);
            }
            secondRoundCallTaskId = Bukkit.getScheduler().runTaskLater(this, () -> {
                CallSession current = callsByPlayer.get(session.caller);
                if (current != null && current.state == CallState.ACTIVE && current.caller.equals(session.caller) && current.callee.equals(session.callee)) {
                    endCall(current, tr("§eВремя разговора вышло."));
                }
            }, 40L * 20L).getTaskId();
        }
    }

    private void endCall(CallSession session, String reason) {
        if (secondRoundCallTaskId != -1) {
            Bukkit.getScheduler().cancelTask(secondRoundCallTaskId);
            secondRoundCallTaskId = -1;
        }
        callsByPlayer.remove(session.caller);
        callsByPlayer.remove(session.callee);
        
        Player caller = Bukkit.getPlayer(session.caller);
        Player callee = Bukkit.getPlayer(session.callee);
        
        if (caller != null) {
            caller.sendMessage(reason != null ? reason : tr("§eЗвонок завершен."));
            // Caller is already in their own group (since callee joined them), no action needed
            // Unless caller joined callee? In my logic: Callee joins Caller.
            // So Caller stays in "SixRooms-Caller".
            // Callee is in "SixRooms-Caller".
        }
        
        if (callee != null) {
            callee.sendMessage(reason != null ? reason : tr("§eЗвонок завершен."));
            
            // Callee leaves Caller's group and returns to their own (re-create as it was auto-deleted)
            callee.performCommand("groups leave");
            
            String groupName = "SixRooms-" + callee.getName();
            String password = "sr-" + callee.getUniqueId().toString().substring(0, 8);
            callee.performCommand("groups create name:" + groupName + " password:" + password);
        }
        if (secondRoundActive && session.state == CallState.ACTIVE) {
            handleSecondRoundCallEnded(session.caller, session.callee);
        }
    }

    private void endAllCalls(String reason) {
        Set<CallSession> uniqueSessions = new HashSet<>(callsByPlayer.values());
        for (CallSession session : uniqueSessions) {
            endCall(session, reason);
        }
        callsByPlayer.clear();
    }

    private enum GameState {
        IDLE,
        REGISTRATION,
        RUNNING
    }

    private enum RoundType {
        FIRST,
        SECOND,
        THIRD,
        FOURTH,
        FIFTH
    }

    private static final class MaskDefinition {
        private final String name;
        private final String base64;

        private MaskDefinition(String name, String base64) {
            this.name = name;
            this.base64 = base64;
        }
    }

    private enum CallState {
        RINGING,
        ACTIVE
    }

    private static final class CallSession {
        private final UUID caller;
        private final UUID callee;
        private CallState state;

        private CallSession(UUID caller, UUID callee, CallState state) {
            this.caller = caller;
            this.callee = callee;
            this.state = state;
        }
    }

    private static final class PhoneInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}

@Addon(
    id = "pv-addon-sixrooms",
    name = "SixRooms Phone",
    version = "0.1.0",
    authors = {"lecya"}
)
final class SixRoomsVoiceAddon implements AddonInitializer {

    @InjectPlasmoVoice
    private PlasmoVoiceServer voiceServer;
    private SixRooms plugin;

    public void setPlugin(SixRooms plugin) {
        this.plugin = plugin;
    }
    
    public GroupsManager getGroupsManager() {
        Plugin groupsPlugin = Bukkit.getPluginManager().getPlugin("pv-addon-groups");
        if (groupsPlugin == null) {
            return null;
        }
        try {
            java.lang.reflect.Field field = groupsPlugin.getClass().getDeclaredField("pvAddonGroups");
            field.setAccessible(true);
            Object addon = field.get(groupsPlugin);
            if (addon instanceof GroupsAddon) {
                return ((GroupsAddon) addon).getGroupManager();
            }
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
        return null;
    }

    @Override
    public void onAddonInitialize() {
        voiceServer.getActivationManager().getActivationByName("proximity")
            .ifPresent(activation -> activation.onPlayerActivation((player, packet) -> handleActivation((VoiceServerPlayer) player, packet)));
    }
    
    public void removeSource(UUID playerId) {
        // No longer needed
    }
    
    private ServerActivation.Result handleActivation(VoiceServerPlayer player, PlayerAudioPacket packet) {
        if (plugin == null) return ServerActivation.Result.IGNORED;
        
        // Use Plasmo Voice API to get Bukkit player via UUID
        UUID playerId = null;
        if (player.getInstance() instanceof SpigotServerPlayer) {
                 playerId = ((SpigotServerPlayer) player.getInstance()).getUuid();
            } else if (player.getInstance() instanceof Player) {
             playerId = ((Player) player.getInstance()).getUniqueId();
        }

        if (playerId != null && plugin.isPlayerInGame(playerId)) {
            return ServerActivation.Result.HANDLED; // Mute proximity
        }
        
        return ServerActivation.Result.IGNORED; // Allow proximity
    }

    public boolean isReady() {
        return voiceServer != null;
    }
}
