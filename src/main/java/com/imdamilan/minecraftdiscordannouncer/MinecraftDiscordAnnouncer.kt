package com.imdamilan.minecraftdiscordannouncer

import com.imdamilan.minecraftdiscordannouncer.Colors.*
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.internal.entities.EntityBuilder
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color

class MinecraftDiscordAnnouncer : JavaPlugin() {

    private var configFile = config
    private var jda: JDA? = null
    private val embed = EmbedBuilder()

    override fun onEnable() {
        Metrics(this, 15802)
        initConfig()
        if (configFile.getBoolean("discord.bot.enabled")) initBot()
        if (configFile.getBoolean("discord.webhook.enabled")) initWebhook()
        update()
    }

    override fun onDisable() {
        if (configFile.getBoolean("discord.bot.enabled")) disableBot()
        if (configFile.getBoolean("discord.webhook.enabled")) disableWebhook()
    }

    private fun update() {
        if (!Update.isLatest(this, 103419)) {
            if (configFile.getBoolean("autoupdate.enabled")) Update.updatePlugin(this, 103419)
            else logger.warning("You are using an outdated version of the plugin. Please update it to the latest version.")
        }
    }

    private fun initConfig() {
        configFile.addDefault("discord.bot.enabled", false)
        configFile.addDefault("discord.bot.token", "")
        configFile.addDefault("discord.bot.server-id", "")
        configFile.addDefault("discord.bot.channel-id", "")
        configFile.addDefault("discord.bot.activity", "PLAYING")
        configFile.addDefault("discord.bot.status", "Minecraft")
        configFile.addDefault("discord.webhook.enabled", false)
        configFile.addDefault("discord.webhook.url", "")
        configFile.addDefault("discord.webhook.username", "")
        configFile.addDefault("discord.webhook.avatar", "")
        configFile.addDefault("messages.color-on", "GREEN")
        configFile.addDefault("messages.color-off", "RED")
        configFile.addDefault("messages.announcement-on", "SERVER ON!")
        configFile.addDefault("messages.announcement-off", "SERVER OFF!")
        configFile.addDefault("messages.mention-role.serverup.enabled", false)
        configFile.addDefault("messages.mention-role.serverup.role-id", "")
        configFile.addDefault("messages.mention-role.serverdown.enabled", false)
        configFile.addDefault("messages.mention-role.serverdown.role-id", "")
        configFile.addDefault("autoupdate.enabled", false)
        config.options().copyDefaults(true)
        saveConfig()
    }

    private fun initBot() {
        try {
            jda = JDABuilder.createDefault(configFile.getString("discord.bot.token")).build()
            jda!!.presence.activity = EntityBuilder.createActivity(configFile.getString("discord.bot.status"), null,
                configFile.getString("discord.bot.activity")?.let { Activity.ActivityType.valueOf(it) })
            jda!!.presence.setStatus(OnlineStatus.ONLINE)

            jda!!.awaitReady()
            if (configFile.getBoolean("messages.mention-role.serverup.enabled")) {
                jda!!.getGuildById(configFile.getString("discord.bot.server-id")!!)!!
                    .getTextChannelById(configFile.getString("discord.bot.channel-id")!!)!!
                    .sendMessage("<@&${configFile.getString("messages.mention-role.serverup.role-id")}>").queue()
            }

            embed.setTitle(configFile.getString("messages.announcement-on")!!)
            embed.setColor(handleColor(Colors.valueOf(configFile.getString("messages.color-on")!!)))

            jda!!.getGuildById(configFile.getString("discord.bot.server-id")!!)!!
                .getTextChannelById(configFile.getString("discord.bot.channel-id")!!)!!
                .sendMessageEmbeds(embed.build()).queue()
            embed.clear()

        } catch (e: Exception) {
            logger.severe("Error while initializing bot: $e")
            logger.severe("If this is your first time running this plugin, please make sure you have the correct credentials in the config.yml file.")
        }
    }

    private fun initWebhook() {
        val webhook = TemmieWebhook(configFile.getString("discord.webhook.url")!!)
        if (configFile.getBoolean("messages.mention-role.serverup.enabled")) {
            webhook.sendMessage(DiscordMessage.builder()
                .content("<@&${configFile.getString("messages.mention-role.serverup.role-id")}>")
                .username(configFile.getString("discord.webhook.username")!!)
                .avatarUrl(configFile.getString("discord.webhook.avatar")!!)
                .build())
        }
        val embed = DiscordEmbed.builder()
            .title(configFile.getString("messages.announcement-on")!!)
            .color(valueOf(configFile.getString("messages.color-on")!!).getColorInt())
            .build()
        val message = DiscordMessage.builder()
            .embeds(listOf(embed))
            .username(configFile.getString("discord.webhook.username")!!)
            .avatarUrl(configFile.getString("discord.webhook.avatar")!!)
            .build()
        webhook.sendMessage(message)
    }

    private fun disableBot() {
        try {
            if (configFile.getBoolean("messages.mention-role.serverdown.enabled")) {
                jda!!.getGuildById(configFile.getString("discord.bot.server-id")!!)!!
                    .getTextChannelById(configFile.getString("discord.bot.channel-id")!!)!!
                    .sendMessage("<@&${configFile.getString("messages.mention-role.serverdown.role-id")}>").queue()
            }

            embed.setTitle(configFile.getString("messages.announcement-off")!!)
            embed.setColor(handleColor(Colors.valueOf(configFile.getString("messages.color-off")!!)))
            jda?.getGuildById(configFile.getString("discord.bot.server-id")!!)
                ?.getTextChannelById(configFile.getString("discord.bot.channel-id")!!)
                ?.sendMessageEmbeds(embed.build())!!.queue()
            embed.clear()

            jda?.shutdown()
        } catch (e: Exception) {
            logger.severe("Error while initializing bot: $e")
            logger.severe("If this is your first time running this plugin, please make sure you have the correct credentials in the config.yml file.")
        }
    }

    private fun disableWebhook() {
        val webhook = TemmieWebhook(configFile.getString("discord.webhook.url")!!)
        if (configFile.getBoolean("messages.mention-role.serverdown.enabled")) {
            webhook.sendMessage(DiscordMessage.builder()
                .content("<@&${configFile.getString("messages.mention-role.serverdown.role-id")}>")
                .username(configFile.getString("discord.webhook.username")!!)
                .avatarUrl(configFile.getString("discord.webhook.avatar")!!)
                .build())
        }
        val embed = DiscordEmbed.builder()
            .title(configFile.getString("messages.announcement-off")!!)
            .color(valueOf(configFile.getString("messages.color-off")!!).getColorInt())
            .build()
        val message = DiscordMessage.builder()
            .embeds(listOf(embed))
            .username(configFile.getString("discord.webhook.username")!!)
            .avatarUrl(configFile.getString("discord.webhook.avatar")!!)
            .build()
        webhook.sendMessage(message)
    }

    private fun handleColor(color: Colors): Color {
        return when (color) {
            GREEN -> Color.GREEN
            RED -> Color.RED
            BLUE -> Color.BLUE
            YELLOW -> Color.YELLOW
            ORANGE -> Color.ORANGE
            PINK -> Color.PINK
            MAGENTA -> Color.MAGENTA
        }
    }
}

enum class Colors(private val value: Int) {
    GREEN(65280),
    RED(16711680),
    BLUE(24539),
    YELLOW(16776960),
    ORANGE(16756224),
    PINK(16761035),
    MAGENTA(16711935);

    fun getColorInt(): Int {
        return value
    }
}
