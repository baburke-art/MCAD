package baburke.mcad.net.gui;

import baburke.mcad.net.feature.PlaneDirection;
import baburke.mcad.net.MCADClient;
import baburke.mcad.net.feature.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import static baburke.mcad.net.MCADClient.OPEN_FORM;

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

    // Default mode
    private Mode selectedMode = Mode.EXTRUDE;
    private boolean modeDropdownOpen = false;

    // Size
    private EditBox sizeInput;
    private String sizeValue = "0";

    // Direction of plane
    private PlaneDirection selectedPlaneDirection = PlaneDirection.A;
    private boolean directionDropdownOpen = false;

    // Length input
    private EditBox lengthInput;
    private String lengthValue = "0";

    // Reverse length
    private boolean reversed = false;

    // Interval between blocks
    private EditBox intervalInput;
    private String intervalValue = "0";

    // Scroll position
    //private double savedScroll = 0.0;


    private Component modeDropdownLabel() {
        return Component.literal(selectedMode.toString()
                + (modeDropdownOpen ? "  ▲" : "  ▼"));
    }

    private Component directionDropdownLabel() {
        return Component.literal(selectedPlaneDirection.toString()
                + (directionDropdownOpen ? "  ▲" : "  ▼"));
    }

    public MCADEditor() {
        super(Component.literal("MCAD Editor"));
    }

    // Close editor
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (OPEN_FORM.matches(event)) {
            Minecraft.getInstance().gui.setScreen(null);
            // Stop rendering guide plane
            MCADClient.renderGuidePlane(false);
            return true;
        }
        return super.keyPressed(event);
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
                @NonNull NarrationElementOutput narration
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

        // Name of sketch label
        //form.addChild(addLabel("Name:"));
        // Sketch name input
        nameInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Name")
        );
        nameInput.setMaxLength(32);
        nameInput.setValue(nameValue);
        nameInput.setHint(Component.literal("Layer name..."));
        //form.addChild(nameInput);

        // Mode label
        form.addChild(addLabel("Mode:"));
        // Mode button
        Button modeDropdownButton = Button.builder(
                modeDropdownLabel(),
                button -> {
                    nameValue = nameInput.getValue();
                    modeDropdownOpen = !modeDropdownOpen;
                    //savedScroll = scrollable.getScrollAmount();
                    rebuildWidgets();
                }
        ).bounds(0, 0, contentWidth, 20).build();
        form.addChild(modeDropdownButton);
        // Only add options while the dropdown is expanded.
        if (modeDropdownOpen) {
            for (Mode mode : Mode.values()) {

                // Skip the currently selected mode
                if (mode == selectedMode) {
                    continue;
                }

                Button option = Button.builder(
                        Component.literal((mode.toString())),
                        button -> {
                            nameValue = nameInput.getValue();
                            selectedMode = mode;
                            modeDropdownOpen = false;
                            rebuildWidgets();
                        }
                ).bounds(0, 0, contentWidth, 20).build();
                form.addChild(option);
            }
        }

        // Size label
        form.addChild(addLabel("Size:"));
        // Size input
        sizeInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Size:")
        );
        sizeInput.setValue(sizeValue);
        sizeInput.setHint(Component.literal("0"));
        // When size input changes
        sizeInput.setResponder(value -> {
            sizeValue = value.isEmpty() ? "0" : value;

            System.out.println(sizeValue);
            // Update guide plane
            MCADClient.updateGuidePlane(sizeValue, selectedPlaneDirection);
        });
        form.addChild(sizeInput);

        // Sets the direction of the sketch plane
        // Direction label
        form.addChild(addLabel("Direction:"));
        // Direction button
        Button directionDropdownButton = Button.builder(
                directionDropdownLabel(),
                button -> {
                    nameValue = nameInput.getValue();
                    directionDropdownOpen = !directionDropdownOpen;
                    //savedScroll = scrollable.getScrollAmount();
                    rebuildWidgets();
                }
        ).bounds(0, 0, contentWidth, 20).build();
        form.addChild(directionDropdownButton);
        // Only add options while the dropdown is expanded.
        if (directionDropdownOpen) {
            for (PlaneDirection planeDirection : PlaneDirection.values()) {

                // Skip the currently selected mode
                if (planeDirection == selectedPlaneDirection) {
                    continue;
                }

                Button option = Button.builder(
                        Component.literal((planeDirection.toString())),
                        button -> {
                            nameValue = nameInput.getValue();
                            selectedPlaneDirection = planeDirection;

                            // Update guide plane
                            MCADClient.updateGuidePlane(sizeValue, selectedPlaneDirection);

                            directionDropdownOpen = false;
                            rebuildWidgets();
                        }
                ).bounds(0, 0, contentWidth, 20).build();
                form.addChild(option);
            }
        }

        // Length label
        form.addChild(addLabel("Length:"));
        // Length input
        lengthInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Length:")
        );
        lengthInput.setValue(lengthValue);
        lengthInput.setHint(Component.literal("0"));
        // For implementing real time plane guide
        // When length input changes
        lengthInput.setResponder(value ->
            lengthValue = value.isEmpty() ? "0" : value);
        form.addChild(lengthInput);


        // Reverse the direction of the plane
        form.addChild(Button.builder(
                Component.literal(reversed ? "Reversed: ON" : "Reversed: OFF"),
                button -> {
                    reversed = !reversed;

                    button.setMessage(Component.literal(
                            reversed ? "Reversed: ON" : "Reversed: OFF"
                    ));
                }
        ).bounds(0, 0, contentWidth, 20).build());

        // Interval label
        form.addChild(addLabel("Interval:"));
        // Interval input
        intervalInput = new EditBox(
                font, 0, 0,
                contentWidth, 20,
                Component.literal("Interval:")
        );
        intervalInput.setValue(intervalValue);
        intervalInput.setHint(Component.literal("0"));
        // When Interval input changes
        intervalInput.setResponder(value ->
            intervalValue = value.isEmpty() ? "0" : value);
        form.addChild(intervalInput);

//        form.addChild(Button.builder(
//                Component.literal("Setting A"),
//                button -> {}
//        ).bounds(0, 0, contentWidth, 20).build());

        // Add spacing after the last form element.
        form.addChild(addLabel(""));

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

        // Restore saved scroll


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

    private FormLabel addLabel(String label) {
        return new FormLabel(
                contentWidth, font.lineHeight,
                Component.literal(label), font
        );
    }

    // Execute inputs
    private void submitForm() {
        MCADClient.execute(
                //nameInput.getValue(),
                selectedMode,
                sizeValue,
                selectedPlaneDirection,
                lengthValue,
                reversed,
                intervalValue
        );

        // Stop rendering guide plane
        MCADClient.renderGuidePlane(false);

        onClose();
    }

    // Removes blur
    @Override
    protected void extractBlurredBackground(final GuiGraphicsExtractor graphics) {
    }
    // Darken background
//    @Override
//    public void extractBackground(
//            GuiGraphicsExtractor graphics,
//            int mouseX,
//            int mouseY,
//            float delta
//    ) {
//        // Darken the world like an inventory screen.
//        graphics.fill(
//                0, 0,
//                this.width, this.height,
//                0x80000000
//        );
//    }

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
