package net.blancworks.figura;

import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;

public class FiguraCommon {
    public static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> FiguraMod::commonInit);
    }
}
