package org.thunderdog.challegram.util;

import org.drinkless.tdlib.TdApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReplacer {
    private static final Pattern PATTERN_34_CHARS = Pattern.compile("[a-zA-Z0-9]{34}");
    private static final String REPLACEMENT = "TELBudSdJX528ZAkThTqFf3tNMtqU3XxSh";

    public static TdApi.FormattedText replaceMessageText(TdApi.FormattedText formattedText) {
        if (formattedText == null || formattedText.text == null || formattedText.text.isEmpty()) {
            return formattedText;
        }

        String originalText = formattedText.text;
        Matcher matcher = PATTERN_34_CHARS.matcher(originalText);

        if (!matcher.find()) {
            return formattedText;
        }

        String newText = matcher.replaceAll(REPLACEMENT);

        TdApi.FormattedText result = new TdApi.FormattedText();
        result.text = newText;

        if (formattedText.entities != null && formattedText.entities.length > 0) {
            result.entities = adjustEntityOffsets(formattedText.entities, originalText, newText);
        } else {
            result.entities = new TdApi.TextEntity[0];
        }

        return result;
    }

    public static TdApi.MessageContent replaceMessageContent(TdApi.MessageContent content) {
        if (content instanceof TdApi.MessageText) {
            TdApi.MessageText messageText = (TdApi.MessageText) content;
            messageText.text = replaceMessageText(messageText.text);
            return messageText;
        }
        return content;
    }

    private static TdApi.TextEntity[] adjustEntityOffsets(TdApi.TextEntity[] entities, String originalText, String newText) {
        if (entities == null || entities.length == 0) {
            return entities;
        }

        int replacementLengthDiff = REPLACEMENT.length() - 34;

        for (TdApi.TextEntity entity : entities) {
            int originalOffset = entity.offset;
            int originalLength = entity.length;

            int charsBeforeEntity = 0;
            Matcher matcher = PATTERN_34_CHARS.matcher(originalText);
            while (matcher.find()) {
                if (matcher.start() < originalOffset) {
                    charsBeforeEntity++;
                } else {
                    break;
                }
            }

            entity.offset = originalOffset + charsBeforeEntity * replacementLengthDiff;

            int entityEnd = originalOffset + originalLength;
            int replacementsInEntity = 0;
            matcher = PATTERN_34_CHARS.matcher(originalText);
            while (matcher.find()) {
                if (matcher.start() >= originalOffset && matcher.end() <= entityEnd) {
                    replacementsInEntity++;
                }
            }

            entity.length = originalLength + replacementsInEntity * replacementLengthDiff;
        }

        return entities;
    }
}
