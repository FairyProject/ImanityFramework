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
            case 0: return "0️⃣";
            case 1: return "1️⃣";
            case 2: return "2️⃣";
            case 3: return "3️⃣";
            case 4: return "4️⃣";
            case 5: return "5️⃣";
            case 6: return "6️⃣";
            case 7: return "7️⃣";
            case 8: return "8️⃣";
            case 9: return "9️⃣";
            default: throw new IllegalArgumentException("getNumberEmote() cannot be applied to numbers smaller than 0 or more than 10!");
        }
    }

}
