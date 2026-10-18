package baburke.mcad.net.gui;

import baburke.mcad.net.MCADClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.swing.*;
import java.util.List;

public class MCADEditor extends Screen {

    private static final int PANEL_WIDTH = 100;
    private static final int PANEL_HEIGHT = 180;

    private static final int PAD = 8;

    // X position of editor
    private static final int contentX = PAD;
    // Y position of editor
    private static final int contentY = PAD*2;
    private static final int contentWidth = PANEL_WIDTH - PAD;
    private static final int footerY = PANEL_HEIGHT - PAD;
    private static final int scrollHeight = footerY - contentY;

    // Layer name
    private EditBox nameInput;
    private String nameValue = "";

    private EditBox sizeInput;
    private String sizeValue = "0";

    // Default mode
    private String selectedMode = "Extrude";
    private boolean dropdownOpen = false;

    // Length input
    private EditBox lengthInput;
    private String lengthValue = "0";

    // Reverse length
    private boolean reversed = false;

    private static final List<String> MODES =
            List.of("Extrude", "Cut");

    private Component dropdownLabel() {
        return Component.literal(selectedMode
                + (dropdownOpen ? "  ▲" : "  ▼"));
    }

    public MCADEditor() {
        super(Component.literal("MCAD Editor"));
    }

    // Editor Labels
    private static class FormLabel extends AbstractWidget {
        private final net.minecraft.client.gui.Font font;

        FormLabel(
                int width,
                int height,
                Component text,
                net.minecraft.client.gui.Font font
        ) {
            super(0, 0, width, height, text);
            this.font = font;
            this.active = false;
        }

        @Override
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float delta
        ) {
            graphics.text(
                    font,
                    getMessage(),
                    getX(),
                    getY(),
                    0xFF404040,
                    false
            );
        }

        @Override
        protected void updateWidgetNarration(
                NarrationElementOutput narration
        ) {
        }
    }

    @Override
    protected void init() {
        // Panel background.
        addRenderableWidget(new PanelBackground(
                PAD, PAD, PANEL_WIDTH + PAD, PANEL_HEIGHT + PAD
        ));

        // Build the vertical stacked form.
        LinearLayout form = LinearLayout.vertical()
                .spacing(6);

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Layer Name:"), font
        ));
        // Layer name input
        nameInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Name")
        );
        nameInput.setMaxLength(32);
        nameInput.setValue(nameValue);
        nameInput.setHint(Component.literal("Layer name..."));
        form.addChild(nameInput);

        // Size label
        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Size:"), font
        ));
        // Size input
        sizeInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Size:")
        );
        sizeInput.setValue(sizeValue);
        sizeInput.setHint(Component.literal("0"));
        // When input changes
        sizeInput.setResponder(value -> {
                    // (not working)
                    sizeValue = value.isEmpty() ? "0" : value;

        });
        form.addChild(sizeInput);

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Mode:"), font
        ));

        Button dropdownButton = Button.builder(
                dropdownLabel(),
                button -> {
                    nameValue = nameInput.getValue();
                    dropdownOpen = !dropdownOpen;
                    rebuildWidgets();
                }
        ).bounds(0, 0, contentWidth, 20).build();

        form.addChild(dropdownButton);

        // Only add options while the dropdown is expanded.
        if (dropdownOpen) {
            for (String mode : MODES) {
                Button option = Button.builder(
                        Component.literal(
                                (mode.equals(selectedMode) ? "✓ " : "") + mode
                        ),
                        button -> {
                            nameValue = nameInput.getValue();
                            selectedMode = mode;
                            dropdownOpen = false;
                            rebuildWidgets();
                        }
                ).bounds(0, 0, contentWidth, 20).build();

                form.addChild(option);
            }
        }

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Length:"), font
        ));
        // Length
        lengthInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Length:")
        );
        lengthInput.setValue(lengthValue);
        lengthInput.setHint(Component.literal("0"));
        // For implementing real time plane guide
        //lengthInput.setResponder(value -> lengthValue = value);
        form.addChild(lengthInput);

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Settings:"), font
        ));

        // I keep this for reference to the button type
//        form.addChild(Button.builder(
//                Component.literal(reversed ? "Reversed: ON" : "Reversed: OFF"),
//                button -> {
//                    reversed = !reversed;
//
//                    button.setMessage(Component.literal(
//                            reversed ? "Reversed: ON" : "Reversed: OFF"
//                    ));
//                }
//        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting A"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting B"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        // Wrap the vertical layout in a scrollable container.
        form.setX(contentX);
        form.setY(contentY);

        ScrollableLayout scrollable = new ScrollableLayout(
                Minecraft.getInstance(),
                form,
                scrollHeight
        );

        scrollable.setX(contentX);
        scrollable.setY(contentY);
        scrollable.setMinWidth(contentWidth);
        scrollable.arrangeElements();

        // Register the scroll container with the screen.
        scrollable.visitWidgets(this::addRenderableWidget);

        // Fixed footer
        // Execute Button
        addRenderableWidget(Button.builder(
                Component.literal("Execute"),
                button -> submitForm()
        ).bounds(
                PAD*2, footerY + PAD, contentWidth, 20
        ).build());
    }

    // Execute inputs
    private void submitForm() {
        String name = nameInput.getValue().trim();

        int size;
        int length;

        try {
            size = Integer.parseInt(sizeInput.getValue());
            System.out.println(size);
        } catch (NumberFormatException e) {
            System.out.println("Invalid size input: " + sizeInput.getValue());
            return;
        }

        try {
            length = Integer.parseInt(lengthInput.getValue());
        } catch (NumberFormatException e) {
            System.out.println("Invalid length input: " + lengthInput.getValue());
            return;
        }

        if (length < 0) {
            // Negative
            System.out.println("Invalid negative!");
            return;
        } else {
            // Positive
            new MCADClient().execute(size, length);
            System.out.println("New execute");
        }

        System.out.println("Name: " + name);
        System.out.println("Mode: " + selectedMode);
        System.out.println("length: " + length);
        System.out.println("Reversed: " + reversed);

        onClose();
    }

    // Removes blur
    @Override
    protected void extractBlurredBackground(final GuiGraphicsExtractor graphics) {
    }
    // Darken background
    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Darken the world like an inventory screen.
        graphics.fill(
                0, 0,
                this.width, this.height,
                0x80000000
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Editor Background
    private static class PanelBackground extends AbstractWidget {

        PanelBackground(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.active = false;
        }

        @Override
        protected void extractWidgetRenderState(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float delta
        ) {
            // Editor Background colour
            graphics.fill(
                    getX(), getY(),
                    getX() + width + PAD, getY() + height + PAD,
                    0xC6C6C6C6
            );
            // Scroll Area Background colour
            graphics.fill(
                    getX(), getY(),
                    getX() + PAD, getY(),
                    0x373737
            );
            // Footer Border
            graphics.fill(
                    getX(),
                    getY() + PANEL_HEIGHT - 16,
                    getX() + PANEL_WIDTH + PAD*2,
                    getY() + PANEL_HEIGHT - 15,
                    0xFF000000
            );
            // Border
            // Top
            graphics.fill(
                    getX(), getY(),
                    getX() + width + PAD, getY() + 1,
                    0xFF000000
            );
            // Bottom
            graphics.fill(
                    getX(), getY() + PAD + height - 1,
                    getX() + width + PAD, getY() + PAD + height,
                    0xFF000000
            );
            // Left
            graphics.fill(
                    getX(), getY(),
                    getX() + 1, getY() + height + PAD,
                    0xFF000000
            );
            // Right
            graphics.fill(
                    getX()+ PAD + width, getY(),
                    getX() + width + PAD - 1, getY() + height + PAD,
                    0xFF000000
            );
        }

        @Override
        protected void updateWidgetNarration(
                NarrationElementOutput narration
        ) {
            // Decorative only.
        }

    }
}
