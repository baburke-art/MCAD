package baburke.mcad.net;

import baburke.mcad.net.feature.PlaneDirection;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;
import java.util.List;

public class PlaneGuideRenderer {

    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(MCAD.id("pipeline/guide_block"))
            .withDepthStencilState(Optional.empty())
            .build()
    );

    // Render states should be immutable, thread safe, and fast to create.
    private record WaypointRenderState(int x, int y, int z, float r, float g, float b, float a) { }

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final StagedVertexBuffer stagedBuffer = new StagedVertexBuffer(() -> "Waypoints Buffer", RenderType.SMALL_BUFFER_SIZE);

    private static boolean RENDER_ENABLED = true;

    public static void setRenderEnabled(boolean enabled) {
        RENDER_ENABLED = enabled;
    }

    public static boolean getRenderEnabled() {
        return RENDER_ENABLED;
    }

    private static final PlaneGuideRenderer INSTANCE =
            new PlaneGuideRenderer();

    public static void register(List<BlockPos> positions, PlaneDirection planeDirection) {
        LevelExtractionEvents.END_EXTRACTION.register(
                context -> INSTANCE.extractWaypoint(context, positions, planeDirection)
        );

        LevelRenderEvents.END_MAIN.register(
                INSTANCE::renderAndDrawWaypoint
        );
    }

    // Used in the drawing phase (extracted data)
    private List<WaypointRenderState> waypointStates = List.of();

    private void extractWaypoint(
            LevelExtractionContext context,
            List<BlockPos> positions,
            PlaneDirection planeDirection
    ) {
        if (!RENDER_ENABLED) {
            waypointStates = List.of();
            return;
        }

        // Only access the (immutable and thread safe) render state in the drawing phase.
        List<WaypointRenderState> cubes = new ArrayList<>();

        for (BlockPos pos : positions) {
            cubes.add(new WaypointRenderState(
                    pos.getX(), pos.getY(), pos.getZ(),
                    planeDirection.red(), planeDirection.green(), planeDirection.blue(), planeDirection.alpha()
            ));
        }
        this.waypointStates = List.copyOf(cubes);
    }

    private void renderAndDrawWaypoint(LevelRenderContext context
    ) {
        // If disabled stop
        if (!RENDER_ENABLED) {
            return;
        }

        RenderPipeline renderPipeline = PlaneGuideRenderer.FILLED_THROUGH_WALLS;
        VertexFormat formatBinding = renderPipeline.getVertexFormatBinding(0);

        assert formatBinding != null;

        PrimitiveTopology primitive = renderPipeline.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = stagedBuffer.appendDraw(formatBinding, primitive, primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        this.renderWaypoint(context, draw);

        stagedBuffer.upload();

        StagedVertexBuffer.ExecuteInfo info = stagedBuffer.getExecuteInfo(draw);

        if (info != null) {
            draw(Minecraft.getInstance(), info, renderPipeline);
        }

        stagedBuffer.endFrame();
    }

    private void renderWaypoint(LevelRenderContext context,
                                StagedVertexBuffer.Draw draw
    ) {
        if (this.waypointStates == null) {
            stagedBuffer.upload();
            return;
        }

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        final VertexConsumer builder = stagedBuffer.getVertexBuilder(draw);

        for (WaypointRenderState waypoint : this.waypointStates) {
            this.renderFilledBox(
                    matrices.last().pose(),
                    builder,
                    waypoint.x(), waypoint.y(), waypoint.z(),
                    waypoint.x() + 1, waypoint.y() + 1, waypoint.z() + 1,
                    waypoint.r(), waypoint.g(), waypoint.b(), waypoint.a()
            );
        }
        matrices.popPose();
    }

    private void renderFilledBox(Matrix4fc positionMatrix, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        // Front Face
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Back face
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        // Left face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Right face
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Top face
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Bottom face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    private static void draw(Minecraft client, StagedVertexBuffer.ExecuteInfo info, RenderPipeline pipeline) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();

        assert colorTexture != null;

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> MCAD.MOD_ID + " example render pipeline rendering", colorTexture, Optional.empty(), mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    public static void close() {
        stagedBuffer.close();
    }


}