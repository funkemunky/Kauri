package cc.funkemunky.anticheat;

import cc.funkemunky.anticheat.data.SimpleDataManager;
import cc.funkemunky.anticheat.profiling.BaseProfiler;
import cc.funkemunky.anticheat.tinyprotocol.api.TinyProtocolHandler;
import cc.funkemunky.anticheat.utils.BlockUtils;
import cc.funkemunky.anticheat.utils.Color;
import cc.funkemunky.anticheat.utils.MiscUtils;
import cc.funkemunky.anticheat.utils.ReflectionsUtil;
import cc.funkemunky.anticheat.utils.blockbox.BlockBoxManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Kauri extends JavaPlugin {

    public static Kauri INSTANCE;

    //API
    public BlockBoxManager blockBoxManager;
    public TinyProtocolHandler tinyProtocolHandler;

    //Threads
    public ScheduledExecutorService service;

    //Kauri Stuff
    public SimpleDataManager dataManager;
    public BaseProfiler baseProfiler;
    public void onEnable() {
        INSTANCE = this;
        service = Executors.newScheduledThreadPool(3);

        registerAPI();
        registerKauriAPI();
    }


    private void registerAPI() {
        blockBoxManager = new BlockBoxManager();
        tinyProtocolHandler = new TinyProtocolHandler();
        new Color();
        new BlockUtils();
        new MiscUtils();
        new ReflectionsUtil();
    }

    private void registerKauriAPI() {
        dataManager = new SimpleDataManager();
        baseProfiler = new BaseProfiler();
    }
}
