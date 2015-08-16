package com.moez.QKSMS.common.emoji;

import com.vdurmont.emoji.EmojiParser;

import junit.framework.TestCase;

public class EmojiRegistryTest extends TestCase {
    public void testWhitespaceBothSides() {
        String src = " :) ";
        String expected = EmojiParser.parseToUnicode(" :" + Emojis.SMILEY + ": ");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testJustEmoji() {
        String src = ":)";
        String expected = EmojiParser.parseToUnicode(":" + Emojis.SMILEY + ":");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testPunctuationNothing() {
        String src = ".:)";
        String expected = EmojiParser.parseToUnicode(".:" + Emojis.SMILEY + ":");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testNothingPunctuation() {
        String src = ":)!";
        String expected = EmojiParser.parseToUnicode(":" + Emojis.SMILEY + ":!");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testPunctuationPunctuation() {
        String src = ".:)!!!";
        String expected = EmojiParser.parseToUnicode(".:" + Emojis.SMILEY + ":!!!");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testUrlsWork() {
        String src = "http://github.com/srcreigh";
        assertEquals(src, EmojiRegistry.parseEmojis(src));
    }

    public void testMultipleEmojisOnlyReplaceFirst() {
        String src = ":):):)";
        String expected = EmojiParser.parseToUnicode(":" + Emojis.SMILEY + "::):)");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testEmojiWithWordDoesntReplace() {
        String src = "hey:)";
        assertEquals(src, EmojiRegistry.parseEmojis(src));
    }

    public void testPoop() {
        String src = ":poop:";
        assertEquals(EmojiParser.parseToUnicode(src), EmojiRegistry.parseEmojis(src));
    }

    public void testNewlineAfter() {
        String src = ":)\n";
        String expected = EmojiParser.parseToUnicode(":" + Emojis.SMILEY + ":\n");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }

    public void testNewlineNewline() {
        String src = "\n:)\n";
        String expected = EmojiParser.parseToUnicode("\n:" + Emojis.SMILEY + ":\n");
        assertEquals(EmojiParser.parseToUnicode(expected), EmojiRegistry.parseEmojis(src));
    }
}
