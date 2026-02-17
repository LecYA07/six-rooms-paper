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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
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
    private static final int ROUND_MIN_EXTRA_SECONDS = 90;
    private static final int ROUND_EXTRA_SECONDS_PER_PLAYER = 10;
    private static final int SECOND_ROUND_CALL_SECONDS = 40;
    private static final int TNT_TRANSFER_LOCK_SECONDS = 5;
    private static final int TNT_FUSE_MIN_SECONDS = 90;
    private static final int TNT_FUSE_MAX_SECONDS = 210;
    private static final int SWORD_CHOICE_SECONDS = 90;
    private static final int SWORD_VISIT_SECONDS = 60;
    private static final int DOORS_ROUND_SECONDS = 90;
    private static final int VOTE_ROUND_SECONDS = 90;
    private static final int ROULETTE_TIMEOUT_SECONDS = 20;
    private static final int ROULETTE_CHAMBER_MULTIPLIER = 2;
    private static final int MEMORY_STUDY_SECONDS = 15;
    private static final int MEMORY_TURN_SECONDS = 20;
    private static final int MEMORY_WORDS_PER_PLAYER = 5;
    private static final int BANK_CALL_SECONDS = 60;
    private static final int BANK_TURN_SECONDS = 15;
    private static final int EXCLUDE_ROUND_SECONDS = 90;
    private static final int MAX_ROUNDS = 8;
    private static final int MAX_SCORE = 20;
    private static final String CLOWN_HEAD_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc0ZjZiNmEwYTY5MzVlNTA3M2M2MTY2NGZmODljZWVmMGFjYTY3NmM3NzgxMDE3NTFlMjMyY2Q3Yjc5MWZiMyJ9fX0=";
    private static final String PEPE_HEAD_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDkzNjMzYWEzYmQyOWRkZGZmMTlmMmQzZjQyZDFhZWRiOTczNWM5ODUwMWU5NjEwMjRmYmVhYTg3YjM3ZmU1ZSJ9fX0=";
    private static final String SMILE_HEAD_BASE64 = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQzZmM3MzVhYWFlOGY3NDRiMTIyM2YwM2ZiY2ZiZWZlYzk5YWU2YzMxYTRjN2U0NTIwODBjYzRlZWE3YjZmOSJ9fX0=";
    private static final String ADMIN_PERMISSION = "sixrooms.admin";
    private final List<UUID> participants = new ArrayList<>();
    private final Map<UUID, Integer> playerNumbers = new HashMap<>();
    private final Map<UUID, CallSession> callsByPlayer = new HashMap<>();
    private final Map<UUID, Integer> scores = new HashMap<>();
    private final Map<UUID, UUID> firstRoundChoices = new HashMap<>();
    private final Map<UUID, ItemStack> previousHelmets = new HashMap<>();
    private final Map<UUID, ItemStack> roulettePreviousItems = new HashMap<>();
    private final Map<UUID, InventorySnapshot> savedInventories = new HashMap<>();
    private final Map<UUID, Location> savedLocations = new HashMap<>();
    private final List<CuboidRegion> schematicRegions = new ArrayList<>();
    private GameState state = GameState.IDLE;
    private Location baseLocation;
    private NamespacedKey phoneItemKey;
    private NamespacedKey phoneBlockKey;
    private NamespacedKey phoneTargetKey;
    private NamespacedKey phoneResetKey;
    private NamespacedKey voteBookKey;
    private NamespacedKey maskBookKey;
    private NamespacedKey revolverItemKey;
    private NamespacedKey rouletteAmmoKey;
    private NamespacedKey memoryBookKey;
    private NamespacedKey excludeBookKey;
    private FileConfiguration translations;
    private String language = "ru";
    private SixRoomsVoiceAddon voiceAddon;
    private boolean debug = false;
    private BossBar roundPrepBar;
    private int roundPrepTaskId = -1;
    private BossBar roundBar;
    private int roundBarTaskId = -1;
    private int regenTaskId = -1;
    private int firstRoundTaskId = -1;
    private boolean firstRoundActive = false;
    private UUID clownPlayerId;
    private int currentRound = 0;
    private RoundType currentRoundType = RoundType.FIRST;
    private RoundType nextRoundType;
    private final Random random = new Random();
    private final List<RoundType> roundPool = List.of(RoundType.FIRST, RoundType.SECOND, RoundType.THIRD, RoundType.FIFTH, RoundType.SIXTH, RoundType.SEVENTH, RoundType.EIGHTH, RoundType.NINTH, RoundType.TENTH);
    private final List<RoundType> remainingRoundPool = new ArrayList<>();
    private final Map<Integer, List<RoundType>> fixedRoundSlots = new HashMap<>();
    private final Map<UUID, Location> roomCenters = new HashMap<>();
    private boolean usingSchematicRooms = false;
    private boolean secondRoundActive = false;
    private UUID secondRoundWordOwner;
    private UUID secondRoundCurrentCaller;
    private String secondRoundWord;
    private boolean secondRoundAwaitingWord = false;
    private final Set<UUID> secondRoundCalled = new HashSet<>();
    private final Map<UUID, UUID> secondRoundLastCaller = new HashMap<>();
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
    private boolean voteRoundActive = false;
    private int voteRoundTaskId = -1;
    private final Map<UUID, UUID> voteChoices = new HashMap<>();
    private boolean rouletteRoundActive = false;
    private int rouletteChambers = 0;
    private int rouletteBulletIndex = -1;
    private int rouletteCurrentIndex = 0;
    private int rouletteHolderIndex = 0;
    private int rouletteAutoTaskId = -1;
    private final List<UUID> rouletteOrder = new ArrayList<>();
    private boolean memoryRoundActive = false;
    private int memoryStudyTaskId = -1;
    private int memoryTurnTaskId = -1;
    private int memoryTurnIndex = 0;
    private UUID memoryCurrentPlayerId;
    private int memoryWordIndex = 0;
    private final List<String> memoryWordSequence = new ArrayList<>();
    private final List<String> memorySpokenWords = new ArrayList<>();
    private final List<UUID> memoryOrder = new ArrayList<>();
    private boolean bankRoundActive = false;
    private int bankPoints = 0;
    private int bankCallTaskId = -1;
    private int bankTurnTaskId = -1;
    private int bankTurnIndex = 0;
    private UUID bankCurrentPlayerId;
    private final List<UUID> bankOrder = new ArrayList<>();
    private boolean excludeRoundActive = false;
    private int excludeRoundTaskId = -1;
    private final Map<UUID, UUID> excludeChoices = new HashMap<>();
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
        translations = null;
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
        translations = null;
        language = getConfig().getString("language", "ru").toLowerCase(Locale.ROOT);
        phoneItemKey = new NamespacedKey(this, "phone_item");
        phoneBlockKey = new NamespacedKey(this, "phone_block");
        phoneTargetKey = new NamespacedKey(this, "phone_target");
        phoneResetKey = new NamespacedKey(this, "phone_reset");
        voteBookKey = new NamespacedKey(this, "vote_book");
        maskBookKey = new NamespacedKey(this, "mask_book");
        revolverItemKey = new NamespacedKey(this, "revolver_item");
        rouletteAmmoKey = new NamespacedKey(this, "roulette_ammo");
        memoryBookKey = new NamespacedKey(this, "memory_book");
        excludeBookKey = new NamespacedKey(this, "exclude_book");
        fixedRoundSlots.clear();
        fixedRoundSlots.put(4, List.of(RoundType.FOURTH));
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
            completions.add("vote");
            completions.add("exclude");
            
            String input = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String s : completions) {
                if (s.startsWith(input)) {
                    result.add(s);
                }
            }
            return result;
        }
        if (args.length == 2 && sender.hasPermission(ADMIN_PERMISSION)) {
            if ("debug".equalsIgnoreCase(args[0])) {
                List<String> options = List.of("start", "skip");
                String input = args[1].toLowerCase();
                List<String> result = new ArrayList<>();
                for (String option : options) {
                    if (option.startsWith(input)) {
                        result.add(option);
                    }
                }
                return result;
            }
        }
        if (args.length == 3 && sender.hasPermission(ADMIN_PERMISSION)) {
            if ("debug".equalsIgnoreCase(args[0]) && ("start".equalsIgnoreCase(args[1]) || "skip".equalsIgnoreCase(args[1]))) {
                List<String> options = List.of("1","2","3","4","5","6","7","8","9","10");
                String input = args[2].toLowerCase();
                List<String> result = new ArrayList<>();
                for (String option : options) {
                    if (option.startsWith(input)) {
                        result.add(option);
                    }
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        endAllCalls(null);
        stopRegenTask();
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        returnPlayersToSavedLocations();
        clearSchematicRooms();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        restoreInventoriesForOnline();
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
                handleDebug(sender, args);
                return true;
            case "vote":
                handleVote(sender, args);
                return true;
            case "mask":
                handleMask(sender, args);
                return true;
            case "exclude":
                handleExclude(sender, args);
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
        Bukkit.broadcastMessage(tr("§aРегистрация открыта! §7Используйте §e/sixrooms join §7для входа."));
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
        int minPlayers = debug ? 1 : MIN_PLAYERS;
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
        stopSixthRound();
        scores.clear();
        firstRoundChoices.clear();
        currentRound = 0;
        nextRoundType = null;
        roomCenters.clear();
        savedLocations.clear();
        schematicRegions.clear();
        remainingRoundPool.clear();
        remainingRoundPool.addAll(roundPool);
        Collections.shuffle(remainingRoundPool, random);
        World world = baseLocation.getWorld();
        if (world == null) {
            sender.sendMessage(tr("§cМир недоступен."));
            return;
        }
        File schematic = findSchematicFile();
        boolean useSchematic = schematic != null;
        usingSchematicRooms = useSchematic;
        Clipboard clipboard = null;
        List<BlockVector3> spawnOffsets = new ArrayList<>();
        int schematicPasteY = baseLocation.getBlockY();

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
                spawnOffsets = findSchematicSpawnOffsets(clipboard);
                schematicPasteY = getSchematicPasteY(world, clipboard, baseLocation.getBlockY());
                sender.sendMessage(tr("§eСхематика выбрана: §a%s", schematic.getName()));
            } catch (IOException e) {
                sender.sendMessage(tr("§cОшибка чтения схематики."));
                return;
            }
        } else {
            if (hasSchematicLikeFiles()) {
                sender.sendMessage(tr("§cФормат схематики не поддерживается WorldEdit."));
            } else {
                sender.sendMessage(tr("§eСхематика не найдена. Будет использована процедурная генерация."));
            }
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

        for (Player player : players) {
            savedLocations.put(player.getUniqueId(), player.getLocation());
        }
        for (Player player : players) {
            saveAndClearInventory(player);
        }

        playerNumbers.clear();
        int totalPlayers = players.size();
        if (useSchematic) {
            if (clipboard == null) {
                sender.sendMessage(tr("§cНе удалось прочитать схематику."));
                return;
            }
            List<BlockVector3> sortedOffsets = new ArrayList<>(spawnOffsets);
            if (sortedOffsets.isEmpty()) {
                sender.sendMessage(tr("§cВ схематике нет маркеров спавна."));
                return;
            }
            sortedOffsets.sort(Comparator.comparingInt(BlockVector3::x)
                .thenComparingInt(BlockVector3::z)
                .thenComparingInt(BlockVector3::y));
            if (sortedOffsets.size() < totalPlayers) {
                sender.sendMessage(tr("§cНедостаточно маркеров спавна (%d/%d).", sortedOffsets.size(), totalPlayers));
                return;
            }
            BlockVector3 pasteTo = BlockVector3.at(baseLocation.getBlockX(), schematicPasteY, baseLocation.getBlockZ());
            if (!pasteSchematic(clipboard, world, pasteTo)) {
                sender.sendMessage(tr("§cОшибка при вставке схематики."));
                return;
            }
            schematicRegions.add(getPastedRegion(clipboard, world, pasteTo));
            markPhoneBlocks(clipboard, world, pasteTo);
            for (int i = 0; i < totalPlayers; i++) {
                int number = i + 1;
                BlockVector3 spawnBlock = pasteTo.add(sortedOffsets.get(i));
                Location teleportTo = new Location(world, spawnBlock.x() + 0.5, spawnBlock.y() + 0.01, spawnBlock.z() + 0.5);
                Player player = players.get(i);
                player.teleport(teleportTo);
                playerNumbers.put(player.getUniqueId(), number);
                roomCenters.put(player.getUniqueId(), teleportTo);
                player.sendMessage(tr("§aТвой номер: §e%d §aиз §e%d", number, totalPlayers));
            }
        } else {
            for (int i = 0; i < totalPlayers; i++) {
                int number = i + 1;
                BlockVector3 pasteTo = BlockVector3.at(
                    baseLocation.getBlockX() + (i * ROOM_SPACING),
                    baseLocation.getBlockY(),
                    baseLocation.getBlockZ()
                );
                generateProceduralRoom(world, pasteTo);
                Location teleportTo = new Location(world, pasteTo.x() + 4.5, pasteTo.y() + 1, pasteTo.z() + 4.5);
                Player player = players.get(i);
                player.teleport(teleportTo);
                playerNumbers.put(player.getUniqueId(), number);
                roomCenters.put(player.getUniqueId(), teleportTo);
                player.sendMessage(tr("§aТвой номер: §e%d §aиз §e%d", number, totalPlayers));
            }
        }

        state = GameState.RUNNING;
        startRegenTask();

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
        stopRegenTask();
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        
        // Cleanup groups for all participants
        for (UUID playerId : playerNumbers.keySet()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.performCommand("groups leave");
                p.sendMessage(tr("§eПриватный канал связи отключен."));
            }
        }

        restoreInventoriesForOnline();
        
        participants.clear();
        playerNumbers.clear();
        roomCenters.clear();
        state = GameState.IDLE;
        baseLocation = null;
        sender.sendMessage(tr("§eРегистрация отменена."));
    }

    private void startFirstRoundPreparation(List<Player> players) {
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, FIRST_ROUND_PREP_SECONDS, tr("§eДо начала раунда: §a"), this::startNextRound, nextRound);
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
        nextRoundType = null;
    }

    private void startRoundPreparation(List<Player> players, int totalSeconds, String titlePrefix, Runnable onComplete, RoundType upcomingRound) {
        stopFirstRoundPreparation();
        nextRoundType = upcomingRound;
        roundPrepBar = Bukkit.createBossBar(titlePrefix + formatTime(totalSeconds), BarColor.YELLOW, BarStyle.SOLID);
        roundPrepBar.setProgress(1.0);
        for (Player player : players) {
            roundPrepBar.addPlayer(player);
        }
        sendRoundRules(players, upcomingRound);
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

    private void sendRoundRules(List<Player> players, RoundType roundType) {
        if (players == null || players.isEmpty() || roundType == null) {
            return;
        }
        List<String> rules = getRoundRules(roundType, players.size());
        if (rules.isEmpty()) {
            return;
        }
        int nextNumber = currentRound + 1;
        String title = tr("§6§lРаунд %d: §e%s", nextNumber, getRoundTitle(roundType));
        for (Player player : players) {
            player.sendMessage(title);
            for (String line : rules) {
                player.sendMessage(line);
            }
        }
    }

    private List<String> getRoundRules(RoundType roundType, int playerCount) {
        List<String> rules = new ArrayList<>();
        if (roundType == RoundType.FIRST) {
            rules.add(tr("§eПравила: §7Все кроме одного в одинаковых масках."));
            rules.add(tr("§7Найти игрока чья маска отлитчаеться от остальных."));
            rules.add(tr("§7Выбор через книгу или чат, можно менять."));
            rules.add(tr("§7За правильный выбор +1 балл."));
            return rules;
        }
        if (roundType == RoundType.SECOND) {
            rules.add(tr("§eПравила: §7Одному из игроков дают секретное слово."));
            rules.add(tr("§7Он звонит и передаёт слово по цепочке."));
            rules.add(tr("§7Время разговора: §a%d §7сек.", SECOND_ROUND_CALL_SECONDS));
            rules.add(tr("§7Нельзя звонить тому, кто только что звонил тебе."));
            rules.add(tr("§7Раунд заканчивается, когда каждый получил звонок."));
            rules.add(tr("§7После звонков напиши догадку в чат."));
            rules.add(tr("§7Владелец слова и угадавшие получают +1 балл."));
            return rules;
        }
        if (roundType == RoundType.THIRD) {
            rules.add(tr("§eПравила: §7Динамит взорвёться через время, которое никто не знает."));
            rules.add(tr("§7Передавай динамит через GUI телефона, но не назад отправителю."));
            rules.add(tr("§7Передача доступна через §a%d §7секунд после получения.", TNT_TRANSFER_LOCK_SECONDS));
            if (TNT_FUSE_MAX_SECONDS > TNT_FUSE_MIN_SECONDS) {
                rules.add(tr("§7Взрыв через §a%d–%d §7секунд после старта.", TNT_FUSE_MIN_SECONDS, TNT_FUSE_MAX_SECONDS));
            } else {
                rules.add(tr("§7Взрыв через §a%d §7секунд после старта.", TNT_FUSE_MIN_SECONDS));
            }
            rules.add(tr("§7Раунд закониться когда взорвётся динамит."));
            return rules;
        }
        if (roundType == RoundType.FOURTH) {
            rules.add(tr("§eПравила: §7Один из игроков получает меч. Владелец меча выбирает цель в телефоне."));
            rules.add(tr("§7На выбор цели: §a%d §7сек.", SWORD_CHOICE_SECONDS));
            rules.add(tr("§7Он телепортируется к цели на разговор (§a%d §7сек).", SWORD_VISIT_SECONDS));
            rules.add(tr("§7По итогам выбора владелец меча +1, цель -1."));
            rules.add(tr("§7Если цель не выбрана, выбирается случайно. Отказаться от выбора нельзя"));
            return rules;
        }
        if (roundType == RoundType.FIFTH) {
            rules.add(tr("§eПравила: §7Выбор — выйти или остаться."));
            rules.add(tr("§7У вас есть время на обсуждаение."));
            rules.add(tr("§7Нечётное число открывших: +1 тем, кто открыл дверь."));
            rules.add(tr("§7Чётное число открывших: -1."));
            return rules;
        }
        if (roundType == RoundType.SIXTH) {
            rules.add(tr("§eПравила: §7Тайно голосуй против игрока."));
            rules.add(tr("§7Игрок с большим количесвом голосов теряет 1 балл."));
            rules.add(tr("§7Голосовавшие за наказанного получают 1 балл."));
            rules.add(tr("§7Выбор можно менять до конца раунда."));
            return rules;
        }
        if (roundType == RoundType.SEVENTH) {
            rules.add(tr("§eПравила: §7Револьвер идёт по кругу."));
            rules.add(tr("§7На выстрел: §a%d §7сек.", ROULETTE_TIMEOUT_SECONDS));
            rules.add(tr("§7Отказаться от выстрела нельзя."));
            rules.add(tr("§7Холостой патрон даёт +1 и ход дальше."));
            rules.add(tr("§7Боевой патрон даёт -1 и завершает раунд."));
            return rules;
        }
        if (roundType == RoundType.EIGHTH) {
            rules.add(tr("§eПравила: §7Запомни слова за §a%d §7секунд.", MEMORY_STUDY_SECONDS));
            rules.add(tr("§7Дальше по очереди называйте следующее слово в чат (сохраняя изначальный порядок)."));
            rules.add(tr("§7Время на ход: §a%d §7секунд.", MEMORY_TURN_SECONDS));
            rules.add(tr("§7Ошибка или тайм-аут — выбываешь."));
            rules.add(tr("§7Последние два игрока получают 1 балл."));
            return rules;
        }
        if (roundType == RoundType.NINTH) {
            int callSeconds = getDynamicSeconds(BANK_CALL_SECONDS, playerCount);
            int turnSeconds = getDynamicSeconds(BANK_TURN_SECONDS, playerCount);
            rules.add(tr("§eПравила: §7У вас есть §a%d §7секунд на обсуждение и банк с баллами.", callSeconds));
            rules.add(tr("§7Далее каждый по очереди берёт из банка столько, сколько захочет."));
            rules.add(tr("§7Время на выбор: §a%d §7секунд.", turnSeconds));
            rules.add(tr("§7Взятые очки добавляются сразу."));
            rules.add(tr("§7Если не успеешь сделать выбор — берёшь 0."));
            return rules;
        }
        if (roundType == RoundType.TENTH) {
            rules.add(tr("§eПравила: §7Выбери игрока, которого хочешь лишить награды."));
            rules.add(tr("§7Можно никого не выбирать."));
            rules.add(tr("§7Игроки с большим количеством голосов не получают баллы."));
            rules.add(tr("§7Все остальные получают +1."));
            return rules;
        }
        return rules;
    }

    private String getRoundTitle(RoundType roundType) {
        if (roundType == RoundType.FIRST) {
            return tr("Маски");
        }
        if (roundType == RoundType.SECOND) {
            return tr("Цепочка слов");
        }
        if (roundType == RoundType.THIRD) {
            return tr("Динамит");
        }
        if (roundType == RoundType.FOURTH) {
            return tr("Меч");
        }
        if (roundType == RoundType.FIFTH) {
            return tr("Двери");
        }
        if (roundType == RoundType.SIXTH) {
            return tr("Голосование");
        }
        if (roundType == RoundType.SEVENTH) {
            return tr("Русская рулетка");
        }
        if (roundType == RoundType.EIGHTH) {
            return tr("Память");
        }
        if (roundType == RoundType.NINTH) {
            return tr("Банк");
        }
        if (roundType == RoundType.TENTH) {
            return tr("Исключение");
        }
        return tr("Раунд");
    }

    private RoundType getNextRoundType() {
        int nextIndex = currentRound + 1;
        List<RoundType> fixedOptions = fixedRoundSlots.get(nextIndex);
        if (fixedOptions != null && !fixedOptions.isEmpty()) {
            RoundType fixed = fixedOptions.get(random.nextInt(fixedOptions.size()));
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
        RoundType nextRound = nextRoundType != null ? nextRoundType : getNextRoundType();
        nextRoundType = null;
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
        } else if (nextRound == RoundType.SIXTH) {
            startSixthRound();
        } else if (nextRound == RoundType.SEVENTH) {
            startSeventhRound();
        } else if (nextRound == RoundType.EIGHTH) {
            startEighthRound();
        } else if (nextRound == RoundType.NINTH) {
            startNinthRound();
        } else if (nextRound == RoundType.TENTH) {
            startTenthRound();
        }
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private int getDynamicSeconds(int baseSeconds, int playerCount) {
        int extraPlayers = Math.max(0, playerCount - MIN_PLAYERS);
        return baseSeconds + ROUND_MIN_EXTRA_SECONDS + extraPlayers * ROUND_EXTRA_SECONDS_PER_PLAYER;
    }

    private void startFirstRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
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
            player.sendMessage(tr("§7Открой книгу и кликни по имени, или напиши ник/номер в чат."));
            player.sendMessage(tr("§7Сообщение увидишь только ты. Выбор можно менять."));
            updateMaskBook(player, false);
        }
        int roundSeconds = getDynamicSeconds(FIRST_ROUND_SECONDS, players.size());
        startRoundBar(players, roundSeconds);
        firstRoundTaskId = Bukkit.getScheduler().runTaskLater(this, this::endFirstRound, roundSeconds * 20L).getTaskId();
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
                applyScore(entry.getKey(), 1, false);
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
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
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
        for (Player player : getOnlineParticipants()) {
            removeMaskBooks(player);
        }
        firstRoundChoices.clear();
    }

    private void startSecondRound() {
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        secondRoundActive = true;
        secondRoundCalled.clear();
        secondRoundLastCaller.clear();
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
            player.sendMessage(tr("§7Время разговора %d секунд. Пиши слово в чат.", SECOND_ROUND_CALL_SECONDS));
        }
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
            applyScore(secondRoundWordOwner, 1, true);
        }
        for (UUID playerId : secondRoundCorrectGuessers) {
            applyScore(playerId, 1, true);
        }
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
    }

    private void stopSecondRound() {
        secondRoundActive = false;
        secondRoundAwaitingWord = false;
        secondRoundWord = null;
        secondRoundWordOwner = null;
        secondRoundCurrentCaller = null;
        secondRoundCalled.clear();
        secondRoundLastCaller.clear();
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
        }, TNT_TRANSFER_LOCK_SECONDS * 20L).getTaskId();
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
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        thirdRoundActive = true;
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        Player holder = players.get(random.nextInt(players.size()));
        tntHolderId = holder.getUniqueId();
        tntLastHolderId = null;
        tntTransferAllowed = false;
        tntFuseSeconds = TNT_FUSE_MIN_SECONDS + random.nextInt(TNT_FUSE_MAX_SECONDS - TNT_FUSE_MIN_SECONDS + 1);
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
            player.sendMessage(tr("§eПередавать можно через %d секунд.", TNT_TRANSFER_LOCK_SECONDS));
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
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
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
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
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
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
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
        holder.sendMessage(tr("§eУ вас %d секунд на разговор.", SWORD_VISIT_SECONDS));
        target.sendMessage(tr("§eУ вас %d секунд на разговор.", SWORD_VISIT_SECONDS));
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
        if (holder.isOnline()) {
            holder.getInventory().setItemInMainHand(swordPreviousItem);
        }
        applyScore(holder.getUniqueId(), 1, true);
        applyScore(target.getUniqueId(), -1, true);
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
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
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
        int roundSeconds = getDynamicSeconds(DOORS_ROUND_SECONDS, players.size());
        startRoundBar(players, roundSeconds);
        if (doorsRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(doorsRoundTaskId);
        }
        doorsRoundTaskId = Bukkit.getScheduler().runTaskLater(this, this::endFifthRound, roundSeconds * 20L).getTaskId();
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
                applyScore(playerId, delta, true);
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
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
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

    private void startSixthRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        voteRoundActive = true;
        voteChoices.clear();
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            return;
        }
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eГолосование против игрока."));
            player.sendMessage(tr("§eВыбери, кто мешает победить."));
            player.sendMessage(tr("§7Выбор тайный. Можно менять до конца раунда."));
            player.sendMessage(tr("§7Открой книгу и кликни по имени, или напиши ник/номер в чат."));
            giveItemToActiveSlot(player, createVoteBook(player));
        }
        int roundSeconds = getDynamicSeconds(VOTE_ROUND_SECONDS, players.size());
        startRoundBar(players, roundSeconds);
        if (voteRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(voteRoundTaskId);
        }
        voteRoundTaskId = Bukkit.getScheduler().runTaskLater(this, this::endSixthRound, roundSeconds * 20L).getTaskId();
    }

    private void endSixthRound() {
        if (!voteRoundActive) {
            return;
        }
        voteRoundActive = false;
        stopRoundBar();
        if (voteRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(voteRoundTaskId);
            voteRoundTaskId = -1;
        }
        Map<UUID, Integer> voteCounts = new HashMap<>();
        for (Map.Entry<UUID, UUID> entry : voteChoices.entrySet()) {
            UUID targetId = entry.getValue();
            if (targetId == null) {
                continue;
            }
            voteCounts.put(targetId, voteCounts.getOrDefault(targetId, 0) + 1);
        }
        int maxVotes = 0;
        for (int count : voteCounts.values()) {
            if (count > maxVotes) {
                maxVotes = count;
            }
        }
        Set<UUID> leaders = new HashSet<>();
        if (maxVotes > 0) {
            for (Map.Entry<UUID, Integer> entry : voteCounts.entrySet()) {
                if (entry.getValue() == maxVotes) {
                    leaders.add(entry.getKey());
                }
            }
        }
        for (UUID leaderId : leaders) {
            applyScore(leaderId, -1, false);
        }
        for (Map.Entry<UUID, UUID> entry : voteChoices.entrySet()) {
            if (leaders.contains(entry.getValue())) {
                UUID voterId = entry.getKey();
                applyScore(voterId, 1, false);
            }
        }
        List<Player> players = getOnlineParticipants();
        if (leaders.isEmpty()) {
            for (Player player : players) {
                player.sendMessage(tr("§eГолоса не были отданы."));
            }
        } else {
            String leadersText = formatPlayerList(leaders);
            for (Player player : players) {
                player.sendMessage(tr("§eНаказание получают: §c%s", leadersText));
            }
        }
        for (UUID voterId : voteChoices.keySet()) {
            if (!leaders.contains(voteChoices.get(voterId))) {
                continue;
            }
            Player voter = Bukkit.getPlayer(voterId);
            if (voter != null && voter.isOnline()) {
                voter.sendMessage(tr("§aВы получили +1 за голос."));
            }
        }
        for (UUID leaderId : leaders) {
            Player leader = Bukkit.getPlayer(leaderId);
            if (leader != null && leader.isOnline()) {
                leader.sendMessage(tr("§cВы получили -1 по итогам голосования."));
            }
        }
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
        stopSixthRound();
    }

    private void stopSixthRound() {
        voteRoundActive = false;
        voteChoices.clear();
        if (voteRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(voteRoundTaskId);
            voteRoundTaskId = -1;
        }
        for (Player player : getOnlineParticipants()) {
            removeVoteBooks(player);
        }
    }

    private void startSeventhRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        rouletteRoundActive = true;
        rouletteOrder.clear();
        roulettePreviousItems.clear();
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<UUID, Integer> entry : entries) {
            UUID playerId = entry.getKey();
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                rouletteOrder.add(playerId);
            }
        }
        if (rouletteOrder.isEmpty()) {
            rouletteRoundActive = false;
            return;
        }
        rouletteChambers = rouletteOrder.size() * ROULETTE_CHAMBER_MULTIPLIER;
        rouletteBulletIndex = random.nextInt(Math.max(1, rouletteChambers));
        rouletteCurrentIndex = 0;
        rouletteHolderIndex = 0;
        for (Player player : getOnlineParticipants()) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eРусская рулетка."));
            player.sendMessage(tr("§7Револьвер уходит по кругу. Натяни лук и выстрели."));
            player.sendMessage(tr("§7Холостой даёт +1, боевой −1 и конец раунда."));
            player.sendMessage(tr("§7Время на выстрел: %d сек.", ROULETTE_TIMEOUT_SECONDS));
        }
        giveRevolverToCurrentHolder(true);
        scheduleRouletteAutoTrigger();
    }

    private void stopSeventhRound() {
        rouletteRoundActive = false;
        for (Map.Entry<UUID, ItemStack> entry : roulettePreviousItems.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.getInventory().setItemInMainHand(entry.getValue());
                removeRouletteAmmo(player);
            }
        }
        rouletteOrder.clear();
        roulettePreviousItems.clear();
        rouletteChambers = 0;
        rouletteBulletIndex = -1;
        rouletteCurrentIndex = 0;
        rouletteHolderIndex = 0;
        if (rouletteAutoTaskId != -1) {
            Bukkit.getScheduler().cancelTask(rouletteAutoTaskId);
            rouletteAutoTaskId = -1;
        }
        for (Player player : getOnlineParticipants()) {
            removeRevolver(player);
        }
    }

    private void giveRevolverToCurrentHolder(boolean announceAll) {
        UUID holderId = getCurrentRouletteHolder();
        if (holderId == null) {
            return;
        }
        Player holder = Bukkit.getPlayer(holderId);
        if (holder == null || !holder.isOnline()) {
            advanceRouletteHolder();
            return;
        }
        if (!roulettePreviousItems.containsKey(holderId)) {
            roulettePreviousItems.put(holderId, holder.getInventory().getItemInMainHand());
        }
        holder.getInventory().setItemInMainHand(createRevolverItem());
        ensureRouletteAmmo(holder);
        holder.sendMessage(tr("§eУ тебя револьвер. Натяни лук и выстрели."));
        if (announceAll) {
            for (Player player : getOnlineParticipants()) {
                if (!player.getUniqueId().equals(holderId)) {
                    player.sendMessage(tr("§eРевольвер у игрока §a%s", holder.getName()));
                }
            }
        }
    }

    private void advanceRouletteHolder() {
        if (!rouletteRoundActive || rouletteOrder.isEmpty()) {
            return;
        }
        UUID previousId = getCurrentRouletteHolder();
        if (previousId != null) {
            Player previous = Bukkit.getPlayer(previousId);
            if (previous != null && previous.isOnline()) {
                restoreRevolverItem(previousId, previous);
                removeRouletteAmmo(previous);
            }
        }
        int size = rouletteOrder.size();
        for (int i = 0; i < size; i++) {
            rouletteHolderIndex = (rouletteHolderIndex + 1) % size;
            UUID nextId = rouletteOrder.get(rouletteHolderIndex);
            Player next = Bukkit.getPlayer(nextId);
            if (next != null && next.isOnline()) {
                giveRevolverToCurrentHolder(true);
                scheduleRouletteAutoTrigger();
                return;
            }
        }
        stopSeventhRound();
    }

    private void scheduleRouletteAutoTrigger() {
        if (rouletteAutoTaskId != -1) {
            Bukkit.getScheduler().cancelTask(rouletteAutoTaskId);
        }
        rouletteAutoTaskId = Bukkit.getScheduler().runTaskLater(this, () -> handleRouletteTrigger(null, true), ROULETTE_TIMEOUT_SECONDS * 20L).getTaskId();
    }

    private void handleRouletteTrigger(Player player, boolean auto) {
        if (!rouletteRoundActive || rouletteOrder.isEmpty()) {
            return;
        }
        UUID holderId = getCurrentRouletteHolder();
        if (holderId == null) {
            return;
        }
        if (!auto) {
            if (player == null || !holderId.equals(player.getUniqueId())) {
                if (player != null) {
                    player.sendMessage(tr("§cРевольвер не у тебя."));
                }
                return;
            }
        }
        if (rouletteAutoTaskId != -1) {
            Bukkit.getScheduler().cancelTask(rouletteAutoTaskId);
            rouletteAutoTaskId = -1;
        }
        boolean bullet = rouletteCurrentIndex == rouletteBulletIndex;
        rouletteCurrentIndex++;
        Player holder = Bukkit.getPlayer(holderId);
        if (holder == null || !holder.isOnline()) {
            advanceRouletteHolder();
            return;
        }
        if (bullet) {
            applyScore(holderId, -1, true);
            for (Player p : getOnlineParticipants()) {
                p.sendMessage(tr("§cВыстрел!"));
            }
            endSeventhRound();
            return;
        }
        applyScore(holderId, 1, true);
        for (Player p : getOnlineParticipants()) {
            String prefix = auto ? tr("§eАвтовыстрел.") : tr("§eВыстрел.");
            p.sendMessage(prefix);
        }
        advanceRouletteHolder();
    }

    private void endSeventhRound() {
        if (!rouletteRoundActive) {
            return;
        }
        stopSeventhRound();
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
    }

    private void startEighthRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        memoryRoundActive = true;
        memoryOrder.clear();
        memoryWordSequence.clear();
        memorySpokenWords.clear();
        memoryTurnIndex = 0;
        memoryWordIndex = 0;
        memoryCurrentPlayerId = null;
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            memoryRoundActive = false;
            return;
        }
        List<String> pool = new ArrayList<>(SECOND_ROUND_WORDS);
        Collections.shuffle(pool, random);
        int wordCount = Math.min(MEMORY_WORDS_PER_PLAYER, pool.size());
        if (wordCount <= 0) {
            memoryRoundActive = false;
            return;
        }
        memoryWordSequence.addAll(pool.subList(0, wordCount));
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eРаунд памяти."));
            player.sendMessage(tr("§eУ тебя %d секунд на запоминание слов.", MEMORY_STUDY_SECONDS));
            giveItemToActiveSlot(player, createMemoryBook(memoryWordSequence));
        }
        startRoundBar(players, MEMORY_STUDY_SECONDS);
        if (memoryStudyTaskId != -1) {
            Bukkit.getScheduler().cancelTask(memoryStudyTaskId);
        }
        memoryStudyTaskId = Bukkit.getScheduler().runTaskLater(this, this::startMemoryTurns, MEMORY_STUDY_SECONDS * 20L).getTaskId();
    }

    private void startMemoryTurns() {
        if (!memoryRoundActive) {
            return;
        }
        for (Player player : getOnlineParticipants()) {
            removeMemoryBooks(player);
        }
        memoryOrder.clear();
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        for (Map.Entry<UUID, Integer> entry : entries) {
            UUID playerId = entry.getKey();
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                memoryOrder.add(playerId);
            }
        }
        for (Player player : getOnlineParticipants()) {
            player.sendMessage(tr("§eПоказ окончен. Пишите слова по очереди."));
        }
        startMemoryTurn();
    }

    private void startMemoryTurn() {
        if (!memoryRoundActive) {
            return;
        }
        if (memoryOrder.isEmpty()) {
            endEighthRound();
            return;
        }
        if (memoryOrder.size() <= 2 || memoryWordIndex >= memoryWordSequence.size()) {
            endEighthRound();
            return;
        }
        int size = memoryOrder.size();
        for (int i = 0; i < size; i++) {
            if (memoryOrder.isEmpty()) {
                endEighthRound();
                return;
            }
            int index = memoryTurnIndex % memoryOrder.size();
            UUID playerId = memoryOrder.get(index);
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                memoryCurrentPlayerId = playerId;
                String spoken = memorySpokenWords.isEmpty() ? "—" : String.join(", ", memorySpokenWords);
                for (Player online : getOnlineParticipants()) {
                    online.sendMessage(tr("§eХод игрока §a%s", player.getName()));
                }
                player.sendMessage(tr("§eСказанные слова: §a%s", spoken));
                player.sendMessage(tr("§eНапиши следующее слово."));
                scheduleMemoryTurnTimeout();
                return;
            }
            memoryOrder.remove(index);
            if (index < memoryTurnIndex) {
                memoryTurnIndex = Math.max(0, memoryTurnIndex - 1);
            }
        }
        endEighthRound();
    }

    private void scheduleMemoryTurnTimeout() {
        if (memoryTurnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(memoryTurnTaskId);
        }
        memoryTurnTaskId = Bukkit.getScheduler().runTaskLater(this, this::handleMemoryTimeout, MEMORY_TURN_SECONDS * 20L).getTaskId();
    }

    private void handleMemoryTimeout() {
        if (!memoryRoundActive || memoryCurrentPlayerId == null) {
            return;
        }
        Player player = Bukkit.getPlayer(memoryCurrentPlayerId);
        if (player != null && player.isOnline()) {
            player.sendMessage(tr("§cВремя вышло. Ты выбываешь."));
        }
        removeMemoryPlayer(memoryCurrentPlayerId, true);
    }

    private void handleMemoryChat(Player player, String message) {
        if (!memoryRoundActive) {
            return;
        }
        UUID playerId = player.getUniqueId();
        if (memoryCurrentPlayerId == null || !playerId.equals(memoryCurrentPlayerId)) {
            player.sendMessage(tr("§cСейчас не твоя очередь."));
            return;
        }
        if (memoryTurnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(memoryTurnTaskId);
            memoryTurnTaskId = -1;
        }
        String expected = memoryWordSequence.get(memoryWordIndex);
        if (!message.trim().equalsIgnoreCase(expected)) {
            player.sendMessage(tr("§cНеверное слово. Ты выбываешь."));
            removeMemoryPlayer(playerId, true);
            return;
        }
        memorySpokenWords.add(expected);
        memoryWordIndex++;
        for (Player online : getOnlineParticipants()) {
            online.sendMessage(tr("§eИгрок §a%s §eназвал слово: §a%s", player.getName(), expected));
        }
        int currentIndex = memoryOrder.indexOf(playerId);
        memoryTurnIndex = currentIndex + 1;
        memoryCurrentPlayerId = null;
        startMemoryTurn();
    }

    private void removeMemoryPlayer(UUID playerId, boolean announce) {
        int index = memoryOrder.indexOf(playerId);
        if (index >= 0) {
            memoryOrder.remove(index);
            if (index < memoryTurnIndex) {
                memoryTurnIndex = Math.max(0, memoryTurnIndex - 1);
            }
        }
        if (announce) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                for (Player online : getOnlineParticipants()) {
                    online.sendMessage(tr("§eИгрок §a%s §eвыбыл из раунда.", player.getName()));
                }
            }
        }
        memoryCurrentPlayerId = null;
        startMemoryTurn();
    }

    private void endEighthRound() {
        if (!memoryRoundActive) {
            return;
        }
        List<UUID> survivors = new ArrayList<>(memoryOrder);
        stopEighthRound();
        if (survivors.size() > 0 && survivors.size() <= 2) {
            for (UUID playerId : survivors) {
                applyScore(playerId, 1, false);
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(tr("§aВы получили +1 за выживание."));
                }
            }
        }
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
    }

    private void stopEighthRound() {
        memoryRoundActive = false;
        if (memoryStudyTaskId != -1) {
            Bukkit.getScheduler().cancelTask(memoryStudyTaskId);
            memoryStudyTaskId = -1;
        }
        if (memoryTurnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(memoryTurnTaskId);
            memoryTurnTaskId = -1;
        }
        stopRoundBar();
        for (Player player : getOnlineParticipants()) {
            removeMemoryBooks(player);
        }
        memoryOrder.clear();
        memoryWordSequence.clear();
        memorySpokenWords.clear();
        memoryTurnIndex = 0;
        memoryWordIndex = 0;
        memoryCurrentPlayerId = null;
    }

    private void startNinthRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        bankRoundActive = true;
        bankPoints = getOnlineParticipants().size();
        bankOrder.clear();
        bankTurnIndex = 0;
        bankCurrentPlayerId = null;
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            bankRoundActive = false;
            return;
        }
        for (Player player : players) {
            bankOrder.add(player.getUniqueId());
        }
        Collections.shuffle(bankOrder, random);
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eРаунд с банком."));
            player.sendMessage(tr("§eВ банке: §a%d", bankPoints));
            int callSeconds = getDynamicSeconds(BANK_CALL_SECONDS, players.size());
            player.sendMessage(tr("§eВремя на звонки: %d секунд.", callSeconds));
        }
        sendBankOrder(players);
        int callSeconds = getDynamicSeconds(BANK_CALL_SECONDS, players.size());
        startRoundBar(players, callSeconds);
        if (bankCallTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bankCallTaskId);
        }
        bankCallTaskId = Bukkit.getScheduler().runTaskLater(this, this::startBankTurns, callSeconds * 20L).getTaskId();
    }

    private void sendBankOrder(List<Player> players) {
        if (bankOrder.isEmpty()) {
            return;
        }
        List<String> lines = new ArrayList<>();
        lines.add(tr("§eОчередь на банк:"));
        int index = 1;
        for (UUID playerId : bankOrder) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                lines.add(tr("§a№%d §7%s", index, player.getName()));
                index++;
            }
        }
        for (Player player : players) {
            for (String line : lines) {
                player.sendMessage(line);
            }
        }
    }

    private void startBankTurns() {
        if (!bankRoundActive) {
            return;
        }
        List<Player> players = getOnlineParticipants();
        for (Player player : players) {
            player.sendMessage(tr("§eВремя звонков вышло."));
            player.sendMessage(tr("§eНачинаем разбор банка."));
        }
        startBankTurn();
    }

    private void startBankTurn() {
        if (!bankRoundActive) {
            return;
        }
        if (bankPoints <= 0) {
            endNinthRound();
            return;
        }
        if (bankOrder.isEmpty()) {
            endNinthRound();
            return;
        }
        int size = bankOrder.size();
        for (int i = 0; i < size; i++) {
            if (bankOrder.isEmpty()) {
                endNinthRound();
                return;
            }
            int index = bankTurnIndex % bankOrder.size();
            UUID playerId = bankOrder.get(index);
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                bankCurrentPlayerId = playerId;
                for (Player online : getOnlineParticipants()) {
                    online.sendMessage(tr("§eХод игрока §a%s", player.getName()));
                    online.sendMessage(tr("§eБаллов в банке: §a%d", bankPoints));
                }
                player.sendMessage(tr("§eСколько баллов берёшь?"));
                scheduleBankTurnTimeout();
                return;
            }
            bankOrder.remove(index);
            if (index < bankTurnIndex) {
                bankTurnIndex = Math.max(0, bankTurnIndex - 1);
            }
        }
        endNinthRound();
    }

    private void scheduleBankTurnTimeout() {
        if (bankTurnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bankTurnTaskId);
        }
        int turnSeconds = getDynamicSeconds(BANK_TURN_SECONDS, bankOrder.size());
        bankTurnTaskId = Bukkit.getScheduler().runTaskLater(this, () -> handleBankChoice(bankCurrentPlayerId, 0, true), turnSeconds * 20L).getTaskId();
        startRoundBar(getOnlineParticipants(), turnSeconds);
    }

    private void handleBankChat(Player player, String message) {
        if (!bankRoundActive) {
            return;
        }
        if (player == null || bankCurrentPlayerId == null) {
            return;
        }
        if (!bankCurrentPlayerId.equals(player.getUniqueId())) {
            player.sendMessage(tr("§cСейчас не твоя очередь."));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(message);
        } catch (NumberFormatException ex) {
            player.sendMessage(tr("§cНужно число."));
            return;
        }
        if (amount < 0) {
            player.sendMessage(tr("§cНельзя брать отрицательное число."));
            return;
        }
        if (amount > bankPoints) {
            player.sendMessage(tr("§cВ банке недостаточно баллов."));
            return;
        }
        handleBankChoice(player.getUniqueId(), amount, false);
    }

    private void handleBankChoice(UUID playerId, int amount, boolean timeout) {
        if (!bankRoundActive || bankCurrentPlayerId == null || playerId == null) {
            return;
        }
        if (!bankCurrentPlayerId.equals(playerId)) {
            return;
        }
        if (bankTurnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bankTurnTaskId);
            bankTurnTaskId = -1;
        }
        Player player = Bukkit.getPlayer(playerId);
        if (timeout) {
            if (player != null) {
                player.sendMessage(tr("§cВремя вышло. Ты берёшь 0."));
            }
            amount = 0;
        }
        if (amount > 0) {
            applyScore(playerId, amount, true);
        }
        bankPoints = Math.max(0, bankPoints - amount);
        if (player != null) {
            for (Player online : getOnlineParticipants()) {
                online.sendMessage(tr("§eИгрок §a%s §eвзял §a%d §eиз банка.", player.getName(), amount));
                online.sendMessage(tr("§eВ банке осталось: §a%d", bankPoints));
            }
        }
        bankCurrentPlayerId = null;
        bankTurnIndex++;
        if (bankPoints <= 0) {
            endNinthRound();
            return;
        }
        startBankTurn();
    }

    private void endNinthRound() {
        if (!bankRoundActive) {
            return;
        }
        stopNinthRound();
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
    }

    private void stopNinthRound() {
        bankRoundActive = false;
        if (bankCallTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bankCallTaskId);
            bankCallTaskId = -1;
        }
        if (bankTurnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(bankTurnTaskId);
            bankTurnTaskId = -1;
        }
        stopRoundBar();
        bankPoints = 0;
        bankTurnIndex = 0;
        bankCurrentPlayerId = null;
        bankOrder.clear();
    }

    private void startTenthRound() {
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        excludeRoundActive = true;
        excludeChoices.clear();
        List<Player> players = getOnlineParticipants();
        if (players.isEmpty()) {
            excludeRoundActive = false;
            return;
        }
        for (Player player : players) {
            player.sendMessage(tr("§6§lРаунд %d", currentRound));
            player.sendMessage(tr("§eКого вычеркнем?"));
            player.sendMessage(tr("§7Выбери игрока, чтобы лишить награды."));
            player.sendMessage(tr("§7Можно не выбирать."));
            player.sendMessage(tr("§7Открой книгу и кликни по имени, или напиши ник/номер в чат."));
            updateExcludeBook(player, false);
        }
        int roundSeconds = getDynamicSeconds(EXCLUDE_ROUND_SECONDS, players.size());
        startRoundBar(players, roundSeconds);
        if (excludeRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(excludeRoundTaskId);
        }
        excludeRoundTaskId = Bukkit.getScheduler().runTaskLater(this, this::endTenthRound, roundSeconds * 20L).getTaskId();
    }

    private void endTenthRound() {
        if (!excludeRoundActive) {
            return;
        }
        excludeRoundActive = false;
        stopRoundBar();
        if (excludeRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(excludeRoundTaskId);
            excludeRoundTaskId = -1;
        }
        Map<UUID, Integer> voteCounts = new HashMap<>();
        for (UUID targetId : excludeChoices.values()) {
            if (targetId == null) {
                continue;
            }
            voteCounts.put(targetId, voteCounts.getOrDefault(targetId, 0) + 1);
        }
        int maxVotes = 0;
        for (int count : voteCounts.values()) {
            if (count > maxVotes) {
                maxVotes = count;
            }
        }
        Set<UUID> leaders = new HashSet<>();
        if (maxVotes > 0) {
            for (Map.Entry<UUID, Integer> entry : voteCounts.entrySet()) {
                if (entry.getValue() == maxVotes) {
                    leaders.add(entry.getKey());
                }
            }
        }
        for (UUID playerId : playerNumbers.keySet()) {
            if (!leaders.contains(playerId)) {
                applyScore(playerId, 1, false);
            }
        }
        for (Player player : getOnlineParticipants()) {
            removeExcludeBooks(player);
        }
        excludeChoices.clear();
        if (shouldFinishGame()) {
            finishGame(tr("§eИгра окончена."));
            return;
        }
        List<Player> players = getOnlineParticipants();
        notifyRoundEnd(players);
        RoundType nextRound = getNextRoundType();
        startRoundPreparation(players, BETWEEN_ROUND_PREP_SECONDS, tr("§eПодготовка к раунду: §a"), this::startNextRound, nextRound);
        sendDebugScores();
    }

    private void stopTenthRound() {
        excludeRoundActive = false;
        if (excludeRoundTaskId != -1) {
            Bukkit.getScheduler().cancelTask(excludeRoundTaskId);
            excludeRoundTaskId = -1;
        }
        stopRoundBar();
        for (Player player : getOnlineParticipants()) {
            removeExcludeBooks(player);
        }
        excludeChoices.clear();
    }

    private UUID getCurrentRouletteHolder() {
        if (rouletteOrder.isEmpty()) {
            return null;
        }
        int size = rouletteOrder.size();
        int index = rouletteHolderIndex % size;
        if (index < 0) {
            index += size;
        }
        return rouletteOrder.get(index);
    }

    private void restoreRevolverItem(UUID playerId, Player player) {
        ItemStack previous = roulettePreviousItems.get(playerId);
        if (previous != null) {
            player.getInventory().setItemInMainHand(previous);
            roulettePreviousItems.remove(playerId);
        } else {
            removeRevolver(player);
        }
        removeRouletteAmmo(player);
    }

    private void saveAndClearInventory(Player player) {
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        if (savedInventories.containsKey(id)) {
            return;
        }
        ItemStack[] contents = cloneItems(player.getInventory().getContents());
        ItemStack[] armor = cloneItems(player.getInventory().getArmorContents());
        ItemStack offhand = player.getInventory().getItemInOffHand();
        ItemStack offhandClone = offhand == null ? null : offhand.clone();
        savedInventories.put(id, new InventorySnapshot(contents, armor, offhandClone));
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[0]);
        player.getInventory().setItemInOffHand(null);
    }

    private void restoreInventory(Player player) {
        if (player == null) {
            return;
        }
        InventorySnapshot snapshot = savedInventories.remove(player.getUniqueId());
        if (snapshot == null) {
            return;
        }
        player.getInventory().setContents(snapshot.contents);
        player.getInventory().setArmorContents(snapshot.armor);
        player.getInventory().setItemInOffHand(snapshot.offhand);
        player.updateInventory();
    }

    private void restoreInventoriesForOnline() {
        for (UUID id : new ArrayList<>(savedInventories.keySet())) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                restoreInventory(player);
            }
        }
    }

    private void returnPlayersToSavedLocations() {
        for (UUID id : new ArrayList<>(savedLocations.keySet())) {
            Player player = Bukkit.getPlayer(id);
            Location location = savedLocations.get(id);
            if (player != null && player.isOnline() && location != null) {
                player.teleport(location);
            }
        }
        savedLocations.clear();
    }

    private void clearSchematicRooms() {
        if (!usingSchematicRooms || schematicRegions.isEmpty()) {
            schematicRegions.clear();
            usingSchematicRooms = false;
            return;
        }
        for (CuboidRegion region : new ArrayList<>(schematicRegions)) {
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
                editSession.setBlocks(region, BlockTypes.AIR.getDefaultState());
                editSession.flushSession();
            } catch (Exception ignored) {
            }
        }
        schematicRegions.clear();
        usingSchematicRooms = false;
    }

    private ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) {
            return new ItemStack[0];
        }
        ItemStack[] copy = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            copy[i] = item == null ? null : item.clone();
        }
        return copy;
    }

    private String formatPlayerList(Set<UUID> ids) {
        List<String> names = new ArrayList<>();
        for (UUID id : ids) {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline()) {
                names.add(player.getName());
            }
        }
        if (names.isEmpty()) {
            return tr("§cНеизвестно");
        }
        return String.join(", ", names);
    }

    private void applyScore(UUID playerId, int delta, boolean notifyPlayer) {
        if (delta == 0) {
            return;
        }
        scores.put(playerId, scores.getOrDefault(playerId, 0) + delta);
        if (!notifyPlayer) {
            return;
        }
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }
        if (delta > 0) {
            player.sendMessage(tr("§aВы получили +%d.", delta));
        } else {
            player.sendMessage(tr("§cВы получили %d.", delta));
        }
    }

    private void notifyRoundEnd(List<Player> players) {
        if (!debug) {
            for (Player player : players) {
                player.sendMessage(tr("§aРаунд завершён."));
            }
        }
        for (Player player : players) {
            player.sendMessage(tr("§eРаунд закончен. §7Подготовка 30 секунд."));
        }
    }

    private void sendDebugScores() {
        if (!debug) {
            return;
        }
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        for (Player viewer : getOnlineParticipants()) {
            viewer.sendMessage(tr("§8[Debug] §eИтоги раунда:"));
            for (Map.Entry<UUID, Integer> entry : entries) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    continue;
                }
                int score = scores.getOrDefault(entry.getKey(), 0);
                viewer.sendMessage(tr("§7№%d §f%s §7— §a%d", entry.getValue(), player.getName(), score));
            }
        }
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
        stopRegenTask();
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
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
        returnPlayersToSavedLocations();
        clearSchematicRooms();
        restoreInventoriesForOnline();
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

    private boolean hasOtherSecondRoundTargets(UUID callerId, UUID blockedId) {
        for (UUID playerId : playerNumbers.keySet()) {
            if (playerId.equals(callerId)) {
                continue;
            }
            if (secondRoundCalled.contains(playerId)) {
                continue;
            }
            if (blockedId != null && blockedId.equals(playerId)) {
                continue;
            }
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                return true;
            }
        }
        return false;
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
        secondRoundLastCaller.put(calleeId, callerId);
        secondRoundCurrentCaller = calleeId;
        Player callee = Bukkit.getPlayer(calleeId);
        if (callee != null && callee.isOnline()) {
            callee.sendMessage(tr("§eТеперь ты можешь звонить."));
        }
        if (secondRoundCalled.size() >= playerNumbers.size()) {
            endSecondRound();
        }
    }

    private void handleVote(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        if (!voteRoundActive || state != GameState.RUNNING) {
            sender.sendMessage(tr("§cСейчас нет раунда голосования."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(tr("§eИспользование: §7/sixrooms vote <ник|номер>"));
            return;
        }
        Player player = (Player) sender;
        if (!playerNumbers.containsKey(player.getUniqueId())) {
            player.sendMessage(tr("§cТы не участвуешь в игре."));
            return;
        }
        handleVoteChoice(player, args[1]);
    }

    private void handleVoteChoice(Player player, String message) {
        if (!voteRoundActive || state != GameState.RUNNING) {
            return;
        }
        UUID targetId = resolvePlayerChoice(message);
        if (targetId == null) {
            player.sendMessage(tr("§cНеверный выбор. Напиши ник или номер игрока."));
            return;
        }
        if (player.getUniqueId().equals(targetId)) {
            player.sendMessage(tr("§cНельзя голосовать за себя."));
            return;
        }
        voteChoices.put(player.getUniqueId(), targetId);
        player.sendMessage(tr("§7Выбор сохранён."));
    }

    private void handleMask(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        if (!firstRoundActive || state != GameState.RUNNING) {
            sender.sendMessage(tr("§cСейчас нет раунда с масками."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(tr("§eИспользование: §7/sixrooms mask <ник|номер>"));
            return;
        }
        Player player = (Player) sender;
        if (!playerNumbers.containsKey(player.getUniqueId())) {
            player.sendMessage(tr("§cТы не участвуешь в игре."));
            return;
        }
        handleRoundChoice(player, args[1]);
    }

    private void handleExclude(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(tr("§cКоманду можно выполнить только в игре."));
            return;
        }
        if (!excludeRoundActive || state != GameState.RUNNING) {
            sender.sendMessage(tr("§cСейчас нет раунда вычеркивания."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(tr("§eИспользование: §7/sixrooms exclude <ник|номер|none>"));
            return;
        }
        Player player = (Player) sender;
        if (!playerNumbers.containsKey(player.getUniqueId())) {
            player.sendMessage(tr("§cТы не участвуешь в игре."));
            return;
        }
        handleExcludeChoice(player, args[1]);
    }

    private void handleExcludeChoice(Player player, String message) {
        if (!excludeRoundActive || state != GameState.RUNNING) {
            return;
        }
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("none") || normalized.equals("никого") || normalized.equals("0")) {
            excludeChoices.remove(player.getUniqueId());
            updateExcludeBook(player, true);
            player.sendMessage(tr("§7Выбор сброшен."));
            return;
        }
        UUID targetId = resolvePlayerChoice(message);
        if (targetId == null) {
            player.sendMessage(tr("§cНеверный выбор. Напиши ник или номер игрока."));
            return;
        }
        if (player.getUniqueId().equals(targetId)) {
            player.sendMessage(tr("§cНельзя выбрать себя."));
            return;
        }
        excludeChoices.put(player.getUniqueId(), targetId);
        updateExcludeBook(player, true);
        player.sendMessage(tr("§7Выбор сохранён."));
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
            if (!firstRoundActive && !secondRoundActive && !thirdRoundActive && !fourthRoundActive && !doorsRoundActive && !voteRoundActive && !rouletteRoundActive && !memoryRoundActive && !bankRoundActive && !excludeRoundActive) {
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

    private void startRegenTask() {
        stopRegenTask();
        regenTaskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (state != GameState.RUNNING) {
                stopRegenTask();
                return;
            }
            for (Player player : getOnlineParticipants()) {
                if (player.isDead()) {
                    continue;
                }
                double maxHealth = player.getMaxHealth();
                if (player.getHealth() < maxHealth) {
                    player.setHealth(maxHealth);
                }
                if (player.getFoodLevel() < 20) {
                    player.setFoodLevel(20);
                }
                if (player.getSaturation() < 20f) {
                    player.setSaturation(20f);
                }
            }
        }, 0L, 20L).getTaskId();
    }

    private void stopRegenTask() {
        if (regenTaskId != -1) {
            Bukkit.getScheduler().cancelTask(regenTaskId);
            regenTaskId = -1;
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
        updateMaskBook(player, true);
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
        sender.sendMessage(tr("§eИспользование: §7/sixrooms open|join|leave|start|cancel|status|phone|vote|mask|exclude|debug [start|skip] <round>"));
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

    private void handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(tr("§cНет прав."));
            return;
        }
        if (args.length == 1) {
            debug = !debug;
            sender.sendMessage(tr("§eРежим отладки: %s", debug ? tr("§aВКЛ") : tr("§cВЫКЛ")));
            return;
        }
        if (!debug) {
            sender.sendMessage(tr("§cСначала включи режим отладки."));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(tr("§eИспользование: §7/sixrooms debug start|skip <round>"));
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        Integer roundNumber = parseRoundNumber(args[2]);
        if (roundNumber == null) {
            sender.sendMessage(tr("§cНеверный номер раунда."));
            return;
        }
        RoundType roundType = getRoundTypeByNumber(roundNumber);
        if (roundType == null) {
            sender.sendMessage(tr("§cНеверный номер раунда."));
            return;
        }
        if (state != GameState.RUNNING) {
            sender.sendMessage(tr("§cИгра не запущена."));
            return;
        }
        if ("start".equals(action) || "skip".equals(action)) {
            startDebugRound(roundType, roundNumber);
            sender.sendMessage(tr("§aЗапущен раунд %d.", roundNumber));
            return;
        }
        sender.sendMessage(tr("§eИспользование: §7/sixrooms debug start|skip <round>"));
    }

    private Integer parseRoundNumber(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        try {
            int number = Integer.parseInt(value);
            if (number < 1 || number > 10) {
                return null;
            }
            return number;
        } catch (NumberFormatException ignored) {
        }
        switch (value) {
            case "first":
            case "первый":
                return 1;
            case "second":
            case "второй":
                return 2;
            case "third":
            case "третий":
                return 3;
            case "fourth":
            case "четвертый":
                return 4;
            case "fifth":
            case "пятый":
                return 5;
            case "sixth":
            case "шестой":
                return 6;
            case "seventh":
            case "седьмой":
                return 7;
            case "eighth":
            case "восьмой":
                return 8;
            case "ninth":
            case "девятый":
                return 9;
            case "tenth":
            case "десятый":
                return 10;
            default:
                return null;
        }
    }

    private RoundType getRoundTypeByNumber(int number) {
        switch (number) {
            case 1:
                return RoundType.FIRST;
            case 2:
                return RoundType.SECOND;
            case 3:
                return RoundType.THIRD;
            case 4:
                return RoundType.FOURTH;
            case 5:
                return RoundType.FIFTH;
            case 6:
                return RoundType.SIXTH;
            case 7:
                return RoundType.SEVENTH;
            case 8:
                return RoundType.EIGHTH;
            case 9:
                return RoundType.NINTH;
            case 10:
                return RoundType.TENTH;
            default:
                return null;
        }
    }

    private void startDebugRound(RoundType roundType, int roundNumber) {
        stopAllRoundsForDebug();
        currentRound = roundNumber;
        currentRoundType = roundType;
        nextRoundType = null;
        if (roundType == RoundType.FIRST) {
            startFirstRound();
        } else if (roundType == RoundType.SECOND) {
            startSecondRound();
        } else if (roundType == RoundType.THIRD) {
            startThirdRound();
        } else if (roundType == RoundType.FOURTH) {
            startFourthRound();
        } else if (roundType == RoundType.FIFTH) {
            startFifthRound();
        } else if (roundType == RoundType.SIXTH) {
            startSixthRound();
        } else if (roundType == RoundType.SEVENTH) {
            startSeventhRound();
        } else if (roundType == RoundType.EIGHTH) {
            startEighthRound();
        } else if (roundType == RoundType.NINTH) {
            startNinthRound();
        } else if (roundType == RoundType.TENTH) {
            startTenthRound();
        }
    }

    private void stopAllRoundsForDebug() {
        stopFirstRoundPreparation();
        stopFirstRound();
        stopSecondRound();
        stopThirdRound();
        stopFourthRound();
        stopFifthRound();
        stopSixthRound();
        stopSeventhRound();
        stopEighthRound();
        stopNinthRound();
        stopTenthRound();
        stopRoundBar();
    }

    private ClipboardReader getClipboardReader(File schematic) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        if (format == null) {
            return null;
        }
        return format.getReader(new FileInputStream(schematic));
    }

    private File findSchematicFile() {
        File[] files = getDataFolder().listFiles();
        if (files == null) {
            return null;
        }
        File best = null;
        long bestTime = -1;
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            if (ClipboardFormats.findByFile(file) == null) {
                continue;
            }
            long modified = file.lastModified();
            if (modified > bestTime) {
                bestTime = modified;
                best = file;
            }
        }
        return best;
    }

    private boolean hasSchematicLikeFiles() {
        File[] files = getDataFolder().listFiles();
        if (files == null) {
            return false;
        }
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".schem") || name.endsWith(".schematic") || name.endsWith(".litematic") || name.endsWith(".nbt")) {
                return true;
            }
        }
        return false;
    }

    private int getSchematicPasteY(World world, Clipboard clipboard, int baseY) {
        int maxHeight = world.getMaxHeight();
        int minHeight = world.getMinHeight();
        int originY = clipboard.getOrigin().y();
        int regionMinY = clipboard.getRegion().getMinimumPoint().y();
        int regionMaxY = clipboard.getRegion().getMaximumPoint().y();
        int pasteY = baseY;
        int minWorldY = pasteY + (regionMinY - originY);
        int maxWorldY = pasteY + (regionMaxY - originY);
        if (minWorldY < minHeight) {
            pasteY += (minHeight - minWorldY);
        }
        if (maxWorldY > maxHeight - 1) {
            pasteY -= (maxWorldY - (maxHeight - 1));
        }
        return pasteY;
    }

    private List<BlockVector3> findSchematicSpawnOffsets(Clipboard clipboard) {
        BlockVector3 origin = clipboard.getOrigin();
        List<BlockVector3> offsets = new ArrayList<>();
        for (BlockVector3 pos : clipboard.getRegion()) {
            BlockType type = clipboard.getBlock(pos).getBlockType();
            if (type == BlockTypes.STRUCTURE_VOID) {
                offsets.add(pos.subtract(origin));
            }
        }
        if (offsets.isEmpty()) {
            offsets.add(clipboard.getRegion().getCenter().toBlockPoint().subtract(origin));
        }
        return offsets;
    }

    private CuboidRegion getPastedRegion(Clipboard clipboard, World world, BlockVector3 pasteTo) {
        BlockVector3 offset = pasteTo.subtract(clipboard.getOrigin());
        BlockVector3 min = clipboard.getRegion().getMinimumPoint().add(offset);
        BlockVector3 max = clipboard.getRegion().getMaximumPoint().add(offset);
        return new CuboidRegion(BukkitAdapter.adapt(world), min, max);
    }

    private void markPhoneBlocks(Clipboard clipboard, World world, BlockVector3 pasteTo) {
        BlockVector3 offset = pasteTo.subtract(clipboard.getOrigin());
        for (BlockVector3 pos : clipboard.getRegion()) {
            BlockType type = clipboard.getBlock(pos).getBlockType();
            if (type != BlockTypes.BARREL) {
                continue;
            }
            BlockVector3 worldPos = pos.add(offset);
            Block block = world.getBlockAt(worldPos.x(), worldPos.y(), worldPos.z());
            if (!(block.getState() instanceof TileState)) {
                continue;
            }
            TileState state = (TileState) block.getState();
            state.getPersistentDataContainer().set(phoneBlockKey, PersistentDataType.BYTE, (byte) 1);
            state.update();
        }
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
    public void onRevolverShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        ItemStack bow = event.getBow();
        if (!isRevolverItem(bow)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (!rouletteRoundActive || state != GameState.RUNNING) {
            player.sendMessage(tr("§cСейчас нет раунда русской рулетки."));
            return;
        }
        event.setCancelled(true);
        handleRouletteTrigger(player, false);
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
        if (secondRoundActive) {
            UUID lastCaller = secondRoundLastCaller.get(caller.getUniqueId());
            if (lastCaller != null && lastCaller.equals(targetUuid) && hasOtherSecondRoundTargets(caller.getUniqueId(), lastCaller)) {
                caller.sendMessage(tr("§cНельзя звонить тому, кто только что звонил тебе."));
                return;
            }
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (state == GameState.RUNNING) {
            return;
        }
        UUID playerId = event.getPlayer().getUniqueId();
        if (savedInventories.containsKey(playerId)) {
            restoreInventory(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CallSession call = callsByPlayer.get(event.getPlayer().getUniqueId());
        if (call != null) {
            endCall(call, tr("§eЗвонок завершен."));
        }
        if (rouletteRoundActive) {
            UUID holderId = getCurrentRouletteHolder();
            if (holderId != null && holderId.equals(event.getPlayer().getUniqueId())) {
                advanceRouletteHolder();
            }
        }
        if (memoryRoundActive) {
            UUID playerId = event.getPlayer().getUniqueId();
            if (memoryOrder.contains(playerId)) {
                removeMemoryPlayer(playerId, true);
            }
        }
        if (bankRoundActive) {
            UUID playerId = event.getPlayer().getUniqueId();
            int index = bankOrder.indexOf(playerId);
            if (index >= 0) {
                bankOrder.remove(index);
                if (index < bankTurnIndex) {
                    bankTurnIndex = Math.max(0, bankTurnIndex - 1);
                }
                if (playerId.equals(bankCurrentPlayerId)) {
                    bankCurrentPlayerId = null;
                    startBankTurn();
                }
            }
        }
        if (excludeRoundActive) {
            excludeChoices.remove(event.getPlayer().getUniqueId());
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
        if (memoryRoundActive) {
            String message = event.getMessage().trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> handleMemoryChat(event.getPlayer(), message));
            return;
        }
        if (bankRoundActive) {
            String message = event.getMessage().trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> handleBankChat(event.getPlayer(), message));
            return;
        }
        if (excludeRoundActive) {
            String message = event.getMessage().trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> handleExcludeChoice(event.getPlayer(), message));
            return;
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
        if (voteRoundActive) {
            String message = event.getMessage().trim();
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(this, () -> handleVoteChoice(event.getPlayer(), message));
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

    private ItemStack createRevolverItem() {
        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(tr("§cРевольвер"));
            meta.getPersistentDataContainer().set(revolverItemKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isRevolverItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(revolverItemKey, PersistentDataType.BYTE);
    }

    private void removeRevolver(Player player) {
        if (player == null) {
            return;
        }
        ItemStack current = player.getInventory().getItemInMainHand();
        if (isRevolverItem(current)) {
            player.getInventory().setItemInMainHand(null);
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) {
                continue;
            }
            if (isRevolverItem(item)) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
    }

    private ItemStack createRouletteAmmo() {
        ItemStack item = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(tr("§7Патрон"));
            meta.getPersistentDataContainer().set(rouletteAmmoKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isRouletteAmmo(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(rouletteAmmoKey, PersistentDataType.BYTE);
    }

    private void ensureRouletteAmmo(Player player) {
        if (player == null) {
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (isRouletteAmmo(item)) {
                return;
            }
        }
        player.getInventory().addItem(createRouletteAmmo());
    }

    private void removeRouletteAmmo(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (isRouletteAmmo(item)) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
    }

    private ItemStack createVoteBook(Player player) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setTitle(tr("Голосование"));
        meta.setAuthor("SixRooms");
        meta.getPersistentDataContainer().set(voteBookKey, PersistentDataType.BYTE, (byte) 1);
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        TextComponent page = new TextComponent("");
        TextComponent header = new TextComponent(tr("Выберите игрока:"));
        header.setColor(ChatColor.GOLD);
        page.addExtra(header);
        page.addExtra("\n");
        for (Map.Entry<UUID, Integer> entry : entries) {
            UUID targetId = entry.getKey();
            if (targetId.equals(player.getUniqueId())) {
                continue;
            }
            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !target.isOnline()) {
                continue;
            }
            String lineText = String.format("%d. %s", entry.getValue(), target.getName());
            TextComponent line = new TextComponent(lineText);
            line.setColor(ChatColor.AQUA);
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sixrooms vote " + entry.getValue()));
            page.addExtra(line);
            page.addExtra("\n");
        }
        List<BaseComponent[]> pages = new ArrayList<>();
        pages.add(new BaseComponent[] {page});
        meta.spigot().setPages(pages);
        item.setItemMeta(meta);
        return item;
    }

    private void giveItemToActiveSlot(Player player, ItemStack item) {
        if (player == null || item == null) {
            return;
        }
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack previous = player.getInventory().getItem(slot);
        if (previous != null && previous.getType() != Material.AIR) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(previous);
            if (!leftover.isEmpty()) {
                for (ItemStack stack : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), stack);
                }
            }
        }
        player.getInventory().setItem(slot, item);
        player.updateInventory();
    }

    private void updateMaskBook(Player player, boolean open) {
        if (player == null) {
            return;
        }
        removeMaskBooks(player);
        UUID selected = firstRoundChoices.get(player.getUniqueId());
        ItemStack book = createMaskBook(player, selected);
        giveItemToActiveSlot(player, book);
        if (open) {
            player.openBook(book);
        }
    }

    private ItemStack createMaskBook(Player player, UUID selected) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setTitle(tr("Маски"));
        meta.setAuthor("SixRooms");
        meta.getPersistentDataContainer().set(maskBookKey, PersistentDataType.BYTE, (byte) 1);
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        TextComponent page = new TextComponent("");
        TextComponent header = new TextComponent(tr("Выберите игрока:"));
        header.setColor(ChatColor.GOLD);
        page.addExtra(header);
        page.addExtra("\n");
        for (Map.Entry<UUID, Integer> entry : entries) {
            UUID targetId = entry.getKey();
            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !target.isOnline()) {
                continue;
            }
            String lineText = String.format("%d. %s", entry.getValue(), target.getName());
            TextComponent line = new TextComponent(lineText);
            boolean isSelected = selected != null && selected.equals(targetId);
            line.setColor(isSelected ? ChatColor.GRAY : ChatColor.AQUA);
            line.setStrikethrough(isSelected);
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sixrooms mask " + entry.getValue()));
            page.addExtra(line);
            page.addExtra("\n");
        }
        List<BaseComponent[]> pages = new ArrayList<>();
        pages.add(new BaseComponent[] {page});
        meta.spigot().setPages(pages);
        item.setItemMeta(meta);
        return item;
    }

    private void updateExcludeBook(Player player, boolean open) {
        if (player == null) {
            return;
        }
        removeExcludeBooks(player);
        UUID selected = excludeChoices.get(player.getUniqueId());
        ItemStack book = createExcludeBook(player, selected);
        giveItemToActiveSlot(player, book);
        if (open) {
            player.openBook(book);
        }
    }

    private ItemStack createExcludeBook(Player player, UUID selected) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setTitle(tr("Вычёркивание"));
        meta.setAuthor("SixRooms");
        meta.getPersistentDataContainer().set(excludeBookKey, PersistentDataType.BYTE, (byte) 1);
        List<Map.Entry<UUID, Integer>> entries = new ArrayList<>(playerNumbers.entrySet());
        entries.sort(Comparator.comparingInt(Map.Entry::getValue));
        TextComponent page = new TextComponent("");
        TextComponent header = new TextComponent(tr("Выберите игрока:"));
        header.setColor(ChatColor.GOLD);
        page.addExtra(header);
        page.addExtra("\n");
        TextComponent noneLine = new TextComponent(tr("0. Никого"));
        boolean noneSelected = selected == null;
        noneLine.setColor(noneSelected ? ChatColor.GRAY : ChatColor.AQUA);
        noneLine.setStrikethrough(noneSelected);
        noneLine.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sixrooms exclude none"));
        page.addExtra(noneLine);
        page.addExtra("\n");
        for (Map.Entry<UUID, Integer> entry : entries) {
            UUID targetId = entry.getKey();
            if (targetId.equals(player.getUniqueId())) {
                continue;
            }
            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !target.isOnline()) {
                continue;
            }
            String lineText = String.format("%d. %s", entry.getValue(), target.getName());
            TextComponent line = new TextComponent(lineText);
            boolean isSelected = selected != null && selected.equals(targetId);
            line.setColor(isSelected ? ChatColor.GRAY : ChatColor.AQUA);
            line.setStrikethrough(isSelected);
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sixrooms exclude " + entry.getValue()));
            page.addExtra(line);
            page.addExtra("\n");
        }
        List<BaseComponent[]> pages = new ArrayList<>();
        pages.add(new BaseComponent[] {page});
        meta.spigot().setPages(pages);
        item.setItemMeta(meta);
        return item;
    }

    private void removeVoteBooks(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            if (meta.getPersistentDataContainer().has(voteBookKey, PersistentDataType.BYTE)) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
    }

    private void removeExcludeBooks(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            if (meta.getPersistentDataContainer().has(excludeBookKey, PersistentDataType.BYTE)) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
    }

    private void removeMaskBooks(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            if (meta.getPersistentDataContainer().has(maskBookKey, PersistentDataType.BYTE)) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
    }

    private ItemStack createMemoryBook(List<String> words) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setTitle(tr("Слова"));
        meta.setAuthor("SixRooms");
        meta.getPersistentDataContainer().set(memoryBookKey, PersistentDataType.BYTE, (byte) 1);
        StringBuilder page = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            page.append(i + 1).append(". ").append(words.get(i));
            if (i < words.size() - 1) {
                page.append("\n");
            }
        }
        meta.setPages(page.toString());
        item.setItemMeta(meta);
        return item;
    }

    private void removeMemoryBooks(Player player) {
        if (player == null) {
            return;
        }
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            if (meta.getPersistentDataContainer().has(memoryBookKey, PersistentDataType.BYTE)) {
                contents[i] = null;
            }
        }
        player.getInventory().setContents(contents);
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
                        lore.add(tntTransferAllowed ? tr("§eПередача доступна") : tr("§7Передача через %d секунд", TNT_TRANSFER_LOCK_SECONDS));
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
            } else if (voteRoundActive) {
                lore.add(tr("§7Раунд с голосованием"));
            } else if (rouletteRoundActive) {
                lore.add(tr("§7Раунд с рулеткой"));
            } else if (memoryRoundActive) {
                lore.add(tr("§7Раунд на память"));
            } else if (bankRoundActive) {
                lore.add(tr("§7Раунд с банком"));
            } else if (excludeRoundActive) {
                lore.add(tr("§7Раунд с вычеркиванием"));
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
            }, SECOND_ROUND_CALL_SECONDS * 20L).getTaskId();
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
        FIFTH,
        SIXTH,
        SEVENTH,
        EIGHTH,
        NINTH,
        TENTH
    }

    private static final class MaskDefinition {
        private final String name;
        private final String base64;

        private MaskDefinition(String name, String base64) {
            this.name = name;
            this.base64 = base64;
        }
    }

    private static final class InventorySnapshot {
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final ItemStack offhand;

        private InventorySnapshot(ItemStack[] contents, ItemStack[] armor, ItemStack offhand) {
            this.contents = contents;
            this.armor = armor;
            this.offhand = offhand;
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
