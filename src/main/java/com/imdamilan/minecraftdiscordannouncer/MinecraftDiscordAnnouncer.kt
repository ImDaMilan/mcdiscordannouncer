package com.imdamilan.minecraftdiscordannouncer

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
    }

    override fun onDisable() {
        if (configFile.getBoolean("discord.bot.enabled")) disableBot()
        if (configFile.getBoolean("discord.webhook.enabled")) disableWebhook()
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
        configFile.addDefault("messages.announcement-on", "SERVER ON!")
        configFile.addDefault("messages.announcement-off", "SERVER OFF!")
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
            embed.setTitle(configFile.getString("messages.announcement-on")!!)
            embed.setColor(Color.GREEN)

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
        val embed = DiscordEmbed.builder()
            .title(configFile.getString("messages.announcement-on")!!)
            .color(65280)
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
            embed.setTitle(configFile.getString("messages.announcement-off")!!)
            embed.setColor(Color.RED)

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
        val embed = DiscordEmbed.builder()
            .title(configFile.getString("messages.announcement-off")!!)
            .color(16711680)
            .build()
        val message = DiscordMessage.builder()
            .embeds(listOf(embed))
            .username(configFile.getString("discord.webhook.username")!!)
            .avatarUrl(configFile.getString("discord.webhook.avatar")!!)
            .build()
        webhook.sendMessage(message)
    }
}
