package de.crafttogether.craftbahn.localization;

/**
 * Copyright (C) 2013-2022 bergerkiller
 */

import de.crafttogether.CraftBahnPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import com.bergerkiller.bukkit.common.utils.LogicUtil;

/**
 * Interface for a LocalizationEnum. Can be implemented by
 * an actual enum to provide localization constants and defaults.
 */
public interface ILocalizationEnum extends ILocalizationDefault {
    /**
     * Sends this Localization message to the sender specified
     *
     * @param sender to send to
     * @param arguments for the node
     */
    default void message(CommandSender sender, TagResolver... arguments) {
        String text = get();

        if (!LogicUtil.nullOrEmpty(text)) {
            Component parsed = CraftBahnPlugin.plugin.getMiniMessageParser().deserialize(text, arguments);
            sender.sendMessage(parsed);
        }
    }

    /**
     * Returns the deserialized Localization message to the sender specified
     *
     * @param arguments for the node
     */
    default Component deserialize(TagResolver... arguments) {
        String text = get();

        if (LogicUtil.nullOrEmpty(text))
            return null;

        return CraftBahnPlugin.plugin.getMiniMessageParser().deserialize(text, arguments);
    }

    /**
     * Gets the locale value for this Localization node
     *
     * @return Locale value
     */
    String get();
}
