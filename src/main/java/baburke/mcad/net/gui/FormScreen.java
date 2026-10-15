package baburke.mcad.net.gui;

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

import java.util.List;

public class FormScreen extends Screen {

    private static final int PANEL_WIDTH = 100;
    private static final int PANEL_HEIGHT = 180;

    private static final int PAD = 8;

    private int contentX;
    private int contentY;
    private int contentWidth;
    private int cursorY;

    private EditBox nameInput;
    private Button dropdownButton;
    private Button[] optionButtons;

    private String nameValue = "";
    private String selectedMode = "Extrude";
    private boolean dropdownOpen = false;

    private static final List<String> MODES =
            List.of("Extrude", "Cut");

    private Component dropdownLabel() {
        return Component.literal(selectedMode
                + (dropdownOpen ? "  ▲" : "  ▼"));
    }

    public FormScreen() {
        super(Component.literal("MCAD Editor"));
    }

    @Override
    protected void init() {
        // X position of editor
        final int contentX = PAD;
        // Y position of editor
        final int contentY = PAD*2;

        final int contentWidth = PANEL_WIDTH - PAD;

        final int footerY = PANEL_HEIGHT - PAD;

        final int scrollHeight = footerY - contentY;

        // Panel background.
        addRenderableWidget(new PanelBackground(
                PAD, PAD, PANEL_WIDTH + PAD, PANEL_HEIGHT + PAD
        ));

        // Build the vertically stacked form.
        LinearLayout form = LinearLayout.vertical()
                .spacing(6);

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Layer Name:"), font
        ));

        nameInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Name")
        );
        nameInput.setMaxLength(32);
        nameInput.setValue(nameValue);
        nameInput.setHint(Component.literal("Layer name..."));
        form.addChild(nameInput);

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Mode:"), font
        ));

        dropdownButton = Button.builder(
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

        // Example extra fields to demonstrate scrolling.
        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Length:"), font
        ));

        EditBox distanceInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Length:")
        );
        distanceInput.setValue("0");
        form.addChild(distanceInput);

        form.addChild(new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal("Settings:"), font
        ));

        form.addChild(Button.builder(
                Component.literal("Reversed"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting A"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting B"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting C"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting D"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting E"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting F"),
                button -> {}
        ).bounds(0, 0, contentWidth, 20).build());

        form.addChild(Button.builder(
                Component.literal("Setting G"),
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

        // Fixed footer: these buttons don't scroll with the form.

        // Execute Button
        addRenderableWidget(Button.builder(
                Component.literal("Execute"),
                button -> submitForm()
        ).bounds(
                PAD*2, footerY + PAD, contentWidth, 20
        ).build());
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

    // Execute inputs
    private void submitForm() {
        String name = nameInput.getValue().trim();

        if (name.isEmpty()) {
            return;
        }

        // TODO: Connect this to your mod's actual functionality.
        System.out.println("Name: " + name);
        System.out.println("Mode: " + selectedMode);

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
