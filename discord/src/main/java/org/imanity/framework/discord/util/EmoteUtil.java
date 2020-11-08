package org.imanity.framework.discord.util;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.entities.Emote;
import org.imanity.framework.discord.DiscordService;

@UtilityClass
public class EmoteUtil {

    public static String getNumberEmote(int number) {
        switch (number) {
            case 0: return ":zero:";
            case 1: return ":one:";
            case 2: return ":two:";
            case 3: return ":three:";
            case 4: return ":four:";
            case 5: return ":five:";
            case 6: return ":six:";
            case 7: return ":seven:";
            case 8: return ":eight:";
            case 9: return ":nine:";
            default: throw new IllegalArgumentException("getNumberEmote() cannot be applied to numbers smaller than 0 or more than 10!");
        }
    }

    public static String getNumberEmoteId(int number) {
        switch (number) {
            case 0: return "U+0030";
            case 1: return "U+0031";
            case 2: return "U+0032";
            case 3: return "U+0033";
            case 4: return "U+0034";
            case 5: return "U+0035";
            case 6: return "U+0036";
            case 7: return "U+0037";
            case 8: return "U+0038";
            case 9: return "U+0039";
            default: throw new IllegalArgumentException("getNumberEmote() cannot be applied to numbers smaller than 0 or more than 10!");
        }
    }

    public static Emote getNumberEmoteEntity(int number) {
        return DiscordService.INSTANCE.getJda().getEmoteById(EmoteUtil.getNumberEmoteId(number));
    }

}
