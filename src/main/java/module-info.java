module nl.tabuu.mclapi {
    requires transitive com.google.gson;

    exports nl.tabuu.mclapi.launcher;

    exports nl.tabuu.mclapi.mojang;
    exports nl.tabuu.mclapi.mojang.rule;

    exports nl.tabuu.mclapi.profile;

    exports nl.tabuu.mclapi.util;
    exports nl.tabuu.mclapi.util.os;

    exports nl.tabuu.mclapi.authentication;
    exports nl.tabuu.mclapi.authentication.microsoft;
    exports nl.tabuu.mclapi.authentication.yggdrasil;

    opens nl.tabuu.mclapi.mojang to com.google.gson;
    opens nl.tabuu.mclapi.mojang.rule to com.google.gson;
}