package com.leonardobishop.quests.bukkit.tasktype.type.dependent;

import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.api.IslandWorthCalculatedEvent;
import com.leonardobishop.quests.bukkit.BukkitQuestsPlugin;
import com.leonardobishop.quests.bukkit.tasktype.BukkitTaskType;
import com.leonardobishop.quests.bukkit.util.TaskUtils;
import com.leonardobishop.quests.common.config.ConfigProblem;
import com.leonardobishop.quests.common.player.QPlayer;
import com.leonardobishop.quests.common.player.questprogressfile.QuestProgress;
import com.leonardobishop.quests.common.player.questprogressfile.TaskProgress;
import com.leonardobishop.quests.common.quest.Quest;
import com.leonardobishop.quests.common.quest.Task;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//TODO update to latest ver
public final class IridiumSkyblockValueTaskType extends BukkitTaskType {

    private final BukkitQuestsPlugin plugin;

    public IridiumSkyblockValueTaskType(BukkitQuestsPlugin plugin) {
        super("iridiumskyblock_value", TaskUtils.TASK_ATTRIBUTION_STRING, "Reach a certain island value for Iridium Skyblock.");
        this.plugin = plugin;
    }

    @Override
    public @NotNull List<ConfigProblem> validateConfig(@NotNull String root, @NotNull HashMap<String, Object> config) {
        ArrayList<ConfigProblem> problems = new ArrayList<>();
        if (TaskUtils.configValidateExists(root + ".value", config.get("value"), problems, "value", super.getType()))
            TaskUtils.configValidateInt(root + ".value", config.get("value"), problems, false, false, "value");
        return problems;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandLevel(IslandWorthCalculatedEvent event) {
        Island island = event.getIsland();
        for (String member : island.members) {
            UUID uuid;
            try {
                 uuid = UUID.fromString(member);
            } catch (Exception e) {
                continue;
            }
            QPlayer qPlayer = plugin.getPlayerManager().getPlayer(uuid);
            if (qPlayer == null) {
                continue;
            }

            Player player = Bukkit.getPlayer(member);

            if (player == null) {
                continue;
            }

            for (TaskUtils.PendingTask pendingTask : TaskUtils.getApplicableTasks(player, qPlayer, this)) {
                Quest quest = pendingTask.quest();
                Task task = pendingTask.task();
                TaskProgress taskProgress = pendingTask.taskProgress();

                int islandValueNeeded = (int) task.getConfigValue("value");

                super.debug("Player island level updated to " + event.getIslandWorth(), quest.getId(), task.getId(), uuid);

                taskProgress.setProgress(event.getIslandWorth());
                super.debug("Updating task progress (now " + event.getIslandWorth() + ")", quest.getId(), task.getId(), uuid);

                if (((double) taskProgress.getProgress()) >= islandValueNeeded) {
                    super.debug("Marking task as complete", quest.getId(), task.getId(), uuid);
                    taskProgress.setProgress(islandValueNeeded);
                    taskProgress.setCompleted(true);
                }
            }
        }

    }

}
