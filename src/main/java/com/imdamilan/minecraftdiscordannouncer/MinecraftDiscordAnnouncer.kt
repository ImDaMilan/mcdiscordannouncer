package com.imdamilan.minecraftdiscordannouncer

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.internal.entities.EntityBuilder
import org.bukkit.plugin.java.JavaPlugin
import java.awt.Color

class MinecraftDiscordAnnouncer : JavaPlugin() {

    private var configFile = config
    private var jda: JDA? = null
    private val embed = EmbedBuilder()

    override fun onEnable() {
        initConfig()
        initBot()
    }

    override fun onDisable() {
        try {
            embed.setTitle(configFile.getString("messages.announcement-off")!!)
            embed.setColor(Color.RED)

            jda?.getGuildById(configFile.getString("discord.server-id")!!)
                ?.getTextChannelById(configFile.getString("discord.channel-id")!!)
                ?.sendMessageEmbeds(embed.build())!!.queue()
            embed.clear()

            jda?.shutdown()
        } catch (e: Exception) {
            logger.severe("Error while initializing bot: $e")
            logger.severe("If this is your first time running this plugin, please make sure you have the correct credentials in the config.yml file.")
        }
    }

    private fun initConfig() {
        configFile.addDefault("discord.bot-token", "")
        configFile.addDefault("discord.server-id", "")
        configFile.addDefault("discord.channel-id", "")
        configFile.addDefault("discord.activity", "PLAYING")
        configFile.addDefault("discord.status", "Minecraft")
        configFile.addDefault("messages.announcement-on", "SERVER ON!")
        configFile.addDefault("messages.announcement-off", "SERVER OFF!")
        config.options().copyDefaults(true)
        saveConfig()
    }

    private fun initBot() {
        try {
            jda = JDABuilder.createDefault(configFile.getString("discord.bot-token")).build()
            jda!!.presence.activity = EntityBuilder.createActivity(configFile.getString("discord.status"), null,
                configFile.getString("discord.activity")?.let { Activity.ActivityType.valueOf(it) })
            jda!!.presence.setStatus(OnlineStatus.ONLINE)

            jda!!.awaitReady()
            embed.setTitle(configFile.getString("messages.announcement-on")!!)
            embed.setColor(Color.GREEN)

            jda!!.getGuildById(configFile.getString("discord.server-id")!!)!!
                .getTextChannelById(configFile.getString("discord.channel-id")!!)!!
                .sendMessageEmbeds(embed.build()).queue()
            embed.clear()

        } catch (e: Exception) {
            logger.severe("Error while initializing bot: $e")
            logger.severe("If this is your first time running this plugin, please make sure you have the correct credentials in the config.yml file.")
        }
    }
}
