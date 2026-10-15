package baburke.mcad.net.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FormScreen2 extends Screen {
    private static final int PANEL_WIDTH = 120;
    private static final int PANEL_HEIGHT = 200;

    private static final List<String> MODES =
            List.of("Extrude", "Cut");

    // Outer padding
    private final static int MARGIN = 8;
    // Inner padding
    private static final int PAD = MARGIN + 4;

    // inputs
    private static final int bottom_inset = PANEL_HEIGHT - 10;
    private static final int field_width = PANEL_WIDTH - 20;

    private static final int BTN_HEIGHT = 20;

    private EditBox nameInput;
    private Button dropdownButton;
    private Button[] optionButtons;

    private String nameValue = "";
    private String selectedMode = "Extrude";
    private boolean dropdownOpen = false;

    public FormScreen2() {
        super(Component.literal("MCAD Editor"));
    }

    @Override
    protected void init() {

        // Register the background first so it renders behind the controls.
        addRenderableWidget(new PanelBackground(
                MARGIN, MARGIN, PANEL_WIDTH, PANEL_HEIGHT
        ));

        // Text input.
        nameInput = new EditBox(
                font,
                PAD, PAD + 24,
                field_width, BTN_HEIGHT,
                Component.literal("Name")
        );
        nameInput.setMaxLength(32);
        nameInput.setValue(nameValue);
        nameInput.setHint(Component.literal("Layer name..."));
        addRenderableWidget(nameInput);

        // Expandable dropdown header.
        dropdownButton = Button.builder(
                dropdownLabel(),
                button -> {
                    nameValue = nameInput.getValue();
                    dropdownOpen = !dropdownOpen;
                    rebuildWidgets();
                }
        ).bounds(
                PAD, PAD + 50,
                field_width, BTN_HEIGHT
        ).build();

        addRenderableWidget(dropdownButton);

        // MODE -- Dropdown options.
        optionButtons = new Button[MODES.size()];

        for (int i = 0; i < MODES.size(); i++) {
            final String mode = MODES.get(i);
            final int optionIndex = i;

            optionButtons[i] = Button.builder(
                    Component.literal(
                            (mode.equals(selectedMode) ? "✓ " : "")
                                    + mode
                    ),
                    button -> {
                        nameValue = nameInput.getValue();
                        selectedMode = mode;
                        dropdownOpen = false;
                        rebuildWidgets();
                    }
            ).bounds(
                    PAD,
                    PAD + 60 + optionIndex * 20,
                    field_width, BTN_HEIGHT
            ).build();

            optionButtons[i].visible = dropdownOpen;
            addRenderableWidget(optionButtons[i]);
        }

        // Execute Button
        addRenderableWidget(Button.builder(
                Component.literal("Execute"),
                button -> submitForm()
        ).bounds(
                PAD, bottom_inset - 40,
                field_width, BTN_HEIGHT
        ).build());

        // Cancel Button
        addRenderableWidget(Button.builder(
                Component.literal("Cancel"),
                button -> onClose()
        ).bounds(
                PAD, bottom_inset,
                field_width, BTN_HEIGHT
        ).build());
    }

    private Component dropdownLabel() {
        return Component.literal(selectedMode
                + (dropdownOpen ? "  ▲" : "  ▼"));
    }

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

    @Override
    protected void extractBlurredBackground(final GuiGraphicsExtractor graphics) {
        //no blur
    }

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
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(
                font,
                title,
                PAD, PAD,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                "Layer name:",
                PAD, PAD + 20,
                0xFFCCCCCC,
                false
        );

        graphics.text(
                font,
                "Mode:",
                PAD, PAD + 60,
                0xFFCCCCCC,
                false
        );

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Decorative background widget. It is registered before the
     * interactive controls so that the controls render on top.
     */
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

            graphics.fill(
                    getX(), getY(),
                    getX() + width, getY() + height,
                    0xF0202020
            );
            // Simple border.
            graphics.fill(
                    getX(), getY(),
                    getX() + width, getY() + 1,
                    0xFF888888
            );
            graphics.fill(
                    getX(), getY() + height - 1,
                    getX() + width, getY() + height,
                    0xFF888888
            );
            graphics.fill(
                    getX(), getY(),
                    getX() + 1, getY() + height,
                    0xFF888888
            );
            graphics.fill(
                    getX() + width - 1, getY(),
                    getX() + width, getY() + height,
                    0xFF888888
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

//This uses the 26.1-era GuiGraphicsExtractor rendering API rather than the older GuiGraphics API. The background is a custom widget registered before the form controls, so it draws behind them. GitHub

        //2. Register a keybinding to open the form


//        5. Connect the Create button to your mod logic
//
//        Right now, submitForm() prints the form values to the development console and closes the screen. Replace its TODO section with your intended operation.
//
//        For example, if the form is configuring a custom machine, you might use the entered name and selected mode to configure that machine.
//
//        One important distinction: this implementation is client-side only. If submitting the form should modify server-side world data, create items, or affect a multiplayer server, send a validated payload to the server using Fabric networking rather than performing the operation only on the client.
//
//        For further reference, the official
//        Fabric custom screens guide
//
//        and
//        custom widgets guide
//
//        cover the rendering and interaction APIs.
