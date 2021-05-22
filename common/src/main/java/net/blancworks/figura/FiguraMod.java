package net.blancworks.figura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.blancworks.figura.access.FiguraTextAccess;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.platform.Platform;
import net.blancworks.figura.gui.FiguraConfigScreen;
import net.blancworks.figura.lua.FiguraLuaManager;
import net.blancworks.figura.models.CustomModel;
import net.blancworks.figura.models.CustomModelPart;
import net.blancworks.figura.models.parsers.BlockbenchModelDeserializer;
import net.blancworks.figura.network.FiguraNetworkManager;
import net.blancworks.figura.network.IFiguraNetwork;
import net.blancworks.figura.network.NewFiguraNetworkManager;
import net.blancworks.figura.trust.PlayerTrustManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FiguraMod {
    public static final String MOD_ID = "figura";

    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(CustomModel.class, new BlockbenchModelDeserializer())
            .setPrettyPrinting().create();

    public static final Logger LOGGER = LogManager.getLogger();

    public static final Identifier FIGURA_FONT = new Identifier("figura", "default");

    //Loading

    //This task is what's used to manage all loading requests in the whole mod.
    //If an asset is set to load, it will attach to this if it exists, or create a new one if it doesn't.
    private static CompletableFuture globalLoadTask;

    private static PlayerDataManager dataManagerInstance;

    public static IFiguraNetwork networkManager;

    private static FiguraNetworkManager oldNetworkManager;
    private static NewFiguraNetworkManager newNetworkManager;

    //Used during rendering.
    public static AbstractClientPlayerEntity currentPlayer;
    public static PlayerData currentData;
    public static VertexConsumerProvider vertexConsumerProvider;
    public static float deltaTime;

    private static final boolean USE_DEBUG_MODEL = true;
    private static WatchKey watchKey;
    private static Path path;

    //Methods

    //Set current player.
    //If there is a model loaded for the player, it'll be assigned here to the current model.
    //Otherwise, sends the model to the request list.
    public static void setRenderingData(AbstractClientPlayerEntity player, VertexConsumerProvider vertexConsumerProvider, PlayerEntityModel mdl, float dt) {
        currentPlayer = player;
        currentData = PlayerDataManager.getDataForPlayer(player.getUuid());
        currentData.vanillaModel = mdl;
        FiguraMod.vertexConsumerProvider = vertexConsumerProvider;
        deltaTime = dt;
    }

    public static void clearRenderingData() {
        currentPlayer = null;
        currentData = null;
        deltaTime = 0;
    }

    // This is needed because the config screen must be registered before the env-specific entrypoints.
    public static void commonInit() {
        Platform.getMod(MOD_ID).registerConfigurationScreen(FiguraConfigScreen::new);
    }

    public static void init() {
        FiguraLuaManager.initialize();
        PlayerTrustManager.init();
        Config.initialize();

        try {
            SSLFixer.main();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Set up network
        oldNetworkManager = new FiguraNetworkManager();
        newNetworkManager = new NewFiguraNetworkManager();

        if ((boolean) Config.entries.get("useNewNetwork").value) {
            networkManager = newNetworkManager;
        } else {
            networkManager = oldNetworkManager;
        }

        //Register fabric events
        ClientTickEvent.CLIENT_POST.register(FiguraMod::ClientEndTick);
//        WorldRenderEvents.AFTER_ENTITIES.register(FiguraMod::renderFirstPersonWorldParts);
//        ClientLifecycleEvent.CLIENT_STOPPING.register((v) -> {
//            networkManager.onClose();
//        });

        dataManagerInstance = new PlayerDataManager();

        getModContentDirectory();
    }

    //Client-side ticks.
    public static void ClientEndTick(MinecraftClient client) {
        PlayerDataManager.tick();

        if ((boolean) Config.entries.get("useNewNetwork").value) {
            networkManager = newNetworkManager;
        } else {
            networkManager = oldNetworkManager;
        }

        if (networkManager != null)
            networkManager.tickNetwork();
    }
    
    public static Path getModContentDirectory() {
        Path p = Platform.getGameFolder().normalize().resolve("figura");
        try {
            Files.createDirectories(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    public static CompletableFuture doTask(Runnable toRun) {
        return doTask(toRun, null);
    }

    public static CompletableFuture doTask(Runnable toRun, @Nullable Runnable onFinished) {
        //If the global load task doesn't exist, create it.
        if (globalLoadTask == null || globalLoadTask.isDone()) {
            globalLoadTask = CompletableFuture.runAsync(
                    () -> {
                        runTask(toRun, onFinished);
                    }
            );
        } else {
            //Otherwise, queue up next task.
            globalLoadTask = globalLoadTask.thenRunAsync(
                    () -> {
                        runTask(toRun, onFinished);
                    }
            );
        }

        return globalLoadTask;
    }

    private static void runTask(Runnable toRun, @Nullable Runnable onFinished) {
        toRun.run();

        if (onFinished != null)
            onFinished.run();
    }


    public static void renderFirstPersonWorldParts(Camera camera, MatrixStack matrixStack, float tickDelta) {
        try {
            if (!camera.isThirdPerson()) {
                PlayerData data = PlayerDataManager.localPlayer;

                if (data != null && data.lastEntity != null) {

                    FiguraMod.currentData = data;

                    matrixStack.push();
                    matrixStack.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);
                    matrixStack.scale(-1, -1, 1);

                    try {

                        if (data.model != null) {
                            int prevCount = data.model.leftToRender;
                            data.model.leftToRender = Integer.MAX_VALUE - 100;

                            if (data != null && data.model != null) {
                                for (CustomModelPart part : data.model.worldParts) {
                                    part.renderUsingAllTextures(data, matrixStack, new MatrixStack(), FiguraMod.vertexConsumerProvider, MinecraftClient.getInstance().getEntityRenderDispatcher().getLight(data.lastEntity, tickDelta), OverlayTexture.DEFAULT_UV, 1.0f);
                                }
                            }

                            data.model.leftToRender = prevCount;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    matrixStack.pop();

                    FiguraMod.clearRenderingData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get nameplate badges
    public static Text getBadges(UUID uuid) {
        PlayerData currentData = PlayerDataManager.getDataForPlayer(uuid);

        Identifier font;
        if ((boolean) Config.entries.get("nameTagIcon").value)
            font = FiguraMod.FIGURA_FONT;
        else
            font = Style.DEFAULT_FONT_ID;

        LiteralText badges = new LiteralText(" ");
        badges.setStyle(Style.EMPTY
                .withExclusiveFormatting(Formatting.WHITE)
                .withFont(font)
        );

        if (currentData != null && currentData.model != null) {
            if (PlayerDataManager.getDataForPlayer(uuid).model.getRenderComplexity() < currentData.getTrustContainer().getFloatSetting(PlayerTrustManager.MAX_COMPLEXITY_ID)) {
                badges.append(new LiteralText("△"));
            } else {
                badges.append(new LiteralText("▲"));
            }
        }

        if (FiguraMod.special.contains(uuid))
            badges.append(new LiteralText("✭"));

        if (badges.getString().equals(" "))
            ((FiguraTextAccess) badges).figura$setText("");

        ((FiguraTextAccess) badges).figura$setFigura(true);

        return badges;
    }

    public final static List<UUID> special = Arrays.asList(
            UUID.fromString("aa0e3391-e497-4e8e-8afe-b69dfaa46afa"), //salad
            UUID.fromString("da53c608-d17c-4759-94fe-a0317ed63876"), //zandra
            UUID.fromString("66a6c5c4-963b-4b73-a0d9-162faedd8b7f"), //fran
            UUID.fromString("45361fcf-f188-46de-ae96-43d89afd6658")  //monty58
    );
}
