package com.quoomy.timecapsules.utils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple multi-line text field widget with wrapping and scrolling.
 *
 * Features:
 * - Supports insertion of newline characters (via Enter key or pasted content)
 * - Supports basic left/right/up/down arrow navigation
 * - Renders the text over multiple wrapped lines and a blinking cursor when focused
 * - Automatically wraps long lines and supports vertical scrolling to keep the cursor visible
 *
 * Unsupported features:
 * - Text highlighting / copying
 * - Proper pixel-based vertical cursor movement (currently character-count-based)
 */
public class MultiLineTextFieldWidget extends ClickableWidget
{
    private final TextRenderer textRenderer;
    private String text = "";
    private final int maxLength;
    private int cursor = 0; // overall cursor position in 'text'
    private boolean focused = false;
    private int blinkCounter = 0;
    private int scrollOffset = 0; // vertical scroll offset in wrapped lines

    /**
     * @param textRenderer the font renderer to use for drawing text
     * @param x            widget x position (in pixels)
     * @param y            widget y position (in pixels)
     * @param width        widget width (in pixels)
     * @param height       widget height (in pixels)
     * @param message      a prompt message (displayed as narration)
     * @param maxLength    maximum number of characters allowed
     */
    public MultiLineTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text message, int maxLength)
    {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.maxLength = maxLength;
    }

    /**
     * Sets the text, clamping to the maximum length.
     */
    public void setText(String text)
    {
        if (text.length() > maxLength)
            text = text.substring(0, maxLength);
        this.text = text;
        this.cursor = text.length();
    }

    /**
     * Returns the full text (which may contain newline characters).
     */
    public String getText()
    {
        return this.text;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta)
    {
        blinkCounter++;

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0xFF000000);
        drawBorder(context, getX(), getY(), getWidth(), getHeight(), 0xFFFFFFFF);

        List<WrappedLine> wrappedLines = getWrappedLines();
        int lineHeight = this.textRenderer.fontHeight;
        int availableHeight = getHeight() - 4; // 2 pixels padding on top & bottom
        int visibleLines = availableHeight / lineHeight;

        int[] cursorWrappedPos = getCursorWrappedPosition(wrappedLines);
        int cursorLine = cursorWrappedPos[0];
        int cursorColumn = cursorWrappedPos[1];

        if (cursorLine < scrollOffset)
            scrollOffset = cursorLine;
        else if (cursorLine >= scrollOffset + visibleLines)
            scrollOffset = cursorLine - visibleLines + 1;

        int startY = getY() + 2;
        for (int i = scrollOffset; i < wrappedLines.size() && i < scrollOffset + visibleLines; i++)
        {
            WrappedLine wl = wrappedLines.get(i);
            int yPos = startY + (i - scrollOffset) * lineHeight;
            context.drawTextWithShadow(this.textRenderer, wl.text, getX() + 2, yPos, 0xFFFFFF);
        }

        if (focused && (blinkCounter / 20) % 2 == 0)
        {
            if (cursorLine >= scrollOffset && cursorLine < scrollOffset + visibleLines)
            {
                WrappedLine wl = wrappedLines.get(cursorLine);
                int cursorX = getX() + 2 + this.textRenderer.getWidth(wl.text.substring(0, cursorColumn));
                int cursorY = getY() + 2 + (cursorLine - scrollOffset) * lineHeight;
                context.fill(cursorX, cursorY, cursorX + 1, cursorY + lineHeight, 0xFFFFFFFF);
            }
        }
    }

    /**
     * Draws a simple 1-pixel border.
     */
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color)
    {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // Handle keys by their key codes:
        // 259 = Backspace, 257 = Enter, 262 = Right Arrow, 263 = Left Arrow,
        // 265 = Up Arrow, 264 = Down Arrow.
        return switch (keyCode)
        {
            case 259 -> {
                if (cursor > 0) {
                    text = text.substring(0, cursor - 1) + text.substring(cursor);
                    cursor--;
                }
                yield true;
            }
            case 257 -> {
                if (text.length() < maxLength) {
                    text = text.substring(0, cursor) + "\n" + text.substring(cursor);
                    cursor++;
                }
                yield true;
            }
            case 262 -> {
                if (cursor < text.length()) {
                    cursor++;
                }
                yield true;
            }
            case 263 -> {
                if (cursor > 0) {
                    cursor--;
                }
                yield true;
            }
            case 265 -> { // Up arrow
                moveCursorVerticallyWrapped(-1);
                yield true;
            }
            case 264 -> { // Down arrow
                moveCursorVerticallyWrapped(1);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (!Character.isISOControl(chr))
        {
            if (text.length() < maxLength)
            {
                text = text.substring(0, cursor) + chr + text.substring(cursor);
                cursor++;
            }
            return true;
        }
        return false;
    }

    /**
     * Moves the cursor vertically by the given delta (e.g. -1 for up, +1 for down),
     * preserving the column if possible, taking into account wrapped lines.
     */
    private void moveCursorVerticallyWrapped(int delta)
    {
        List<WrappedLine> wrappedLines = getWrappedLines();
        int[] pos = getCursorWrappedPosition(wrappedLines);
        int currentWrappedLine = pos[0];
        int col = pos[1];
        int targetWrappedLine = MathHelper.clamp(currentWrappedLine + delta, 0, wrappedLines.size() - 1);
        WrappedLine targetLine = wrappedLines.get(targetWrappedLine);
        int targetCol = Math.min(col, targetLine.text.length());
        cursor = targetLine.startIndex + targetCol;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (mouseX >= getX() && mouseX < getX() + getWidth() &&
                mouseY >= getY() && mouseY < getY() + getHeight())
        {
            this.focused = true;
            int relX = (int) mouseX - getX() - 2;
            int relY = (int) mouseY - getY() - 2;
            int lineHeight = this.textRenderer.fontHeight;
            List<WrappedLine> wrappedLines = getWrappedLines();
            int clickedWrappedLine = scrollOffset + (relY / lineHeight);
            if (clickedWrappedLine >= wrappedLines.size())
                clickedWrappedLine = wrappedLines.size() - 1;
            WrappedLine clickedLine = wrappedLines.get(clickedWrappedLine);
            int clickPos = 0;
            for (int i = 0; i < clickedLine.text.length(); i++)
            {
                if (this.textRenderer.getWidth(clickedLine.text.substring(0, i + 1)) > relX)
                    break;
                clickPos = i + 1;
            }
            cursor = clickedLine.startIndex + clickPos;
            return true;
        }
        else
        {
            this.focused = false;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused)
    {
        this.focused = focused;
        if (focused)
            blinkCounter = 0;
    }

    @Override
    public boolean isFocused()
    {
        return this.focused;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder)
    {
        builder.put(NarrationPart.TITLE, Text.literal("Multi-line Text Field"));
        builder.put(NarrationPart.USAGE, Text.literal("Type to enter text. Use arrow keys to navigate. Press Enter for newline."));
    }

    /**
     * A helper class representing a wrapped line of text.
     */
    private static class WrappedLine
    {
        final String text;
        final int startIndex; // Global text index of the first character in this wrapped line

        WrappedLine(String text, int startIndex) {
            this.text = text;
            this.startIndex = startIndex;
        }
    }

    /**
     * Computes wrapped lines for the current text, taking into account the available width.
     *
     * @return a list of WrappedLine objects representing each visual line.
     */
    private List<WrappedLine> getWrappedLines()
    {
        List<WrappedLine> linesList = new ArrayList<>();
        int availableWidth = getWidth() - 4; // accounting for 2-pixel padding on each side
        int globalIndex = 0;
        String[] origLines = text.split("\n", -1);
        for (String orig : origLines)
        {
            if (orig.isEmpty())
            {
                linesList.add(new WrappedLine("", globalIndex));
            }
            else
            {
                int start = 0;
                while (start < orig.length())
                {
                    int end = start;
                    // Extend the segment until it would exceed the available width
                    while (end < orig.length() && textRenderer.getWidth(orig.substring(start, end + 1)) <= availableWidth)
                        end++;
                    if (end == start)
                        end = start + 1;
                    String segment = orig.substring(start, end);
                    linesList.add(new WrappedLine(segment, globalIndex + start));
                    start = end;
                }
            }
            globalIndex += orig.length() + 1;
        }
        return linesList;
    }

    /**
     * Maps the global cursor position to its wrapped line and column offset.
     *
     * @param wrappedLines the list of wrapped lines computed from the text
     * @return an int array: {wrappedLineIndex, columnOffset}
     */
    private int[] getCursorWrappedPosition(List<WrappedLine> wrappedLines)
    {
        int clampedCursor = MathHelper.clamp(cursor, 0, text.length());
        for (int i = 0; i < wrappedLines.size(); i++)
        {
            WrappedLine wl = wrappedLines.get(i);
            int lineStart = wl.startIndex;
            int lineEnd = wl.startIndex + wl.text.length();
            if (clampedCursor < lineEnd)
            {
                return new int[] { i, clampedCursor - lineStart };
            }
            else if (clampedCursor == lineEnd)
            {
                // If there's a following wrapped line from the same original line, place the cursor at its start
                if (i + 1 < wrappedLines.size() && wrappedLines.get(i + 1).startIndex == lineEnd)
                    return new int[] { i + 1, 0 };
                else
                    return new int[] { i, wl.text.length() };
            }
        }
        // If no wrapped line is found, default to the last line
        if (!wrappedLines.isEmpty())
        {
            WrappedLine last = wrappedLines.get(wrappedLines.size() - 1);
            return new int[] { wrappedLines.size() - 1, last.text.length() };
        }
        return new int[] { 0, 0 };
    }
}
