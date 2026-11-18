
package org.customserverlist;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.customserverlist.client.EntryListScreen;
import org.slf4j.Logger;

@Mod(CustomServerList.MODID)
public final class CustomServerList {

    public static final String MODID = "customserverlist";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomServerList(ModContainer modContainer) {

        LOGGER.info("CustomServerList initialized");

        // 注册模组配置界面
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parent) -> new EntryListScreen(parent)
        );
    }
}
