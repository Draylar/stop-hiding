package draylar.stophiding.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "stophiding")
public class StopHidingConfig implements ConfigData {

    @Comment(value = "\nRange to search for inventories within.\nOnly valid on the server.\nNote that larger ranges may result in server lag.")
    public int searchRange = 20;

    @Comment(value = "Whether a message should be displayed above the hotbar on the client with search results.")
    public boolean displayResults = true;

    @Comment(value = "The time, in ticks, that a highlighted block takes to disappear.")
    public int highlightTime = 500;
}
