package com.hinaclient.hina.ui

import com.hinaclient.hina.MioHr
import com.hinaclient.hina.event.EventBus
import com.hinaclient.hina.event.EventListener
import com.hinaclient.hina.event.skia.EventSkiaDrawScene
import com.hinaclient.hina.mixin.mixins.accessors.MinecraftAccessor
import com.hinaclient.hina.ui.clickgui.Panel
import io.github.humbleui.skija.FilterTileMode
import io.github.humbleui.skija.ImageFilter
import io.github.humbleui.types.Rect
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

class ClickGuiScreen : Screen(Component.literal("ClickGUI")) {
    private var panel: Panel? = null
    private var cachedBackground: io.github.humbleui.skija.Image? = null
    private var guiMouseX = 0
    private var guiMouseY = 0

    companion object {
        private var INSTANCE: ClickGuiScreen? = null
        fun getInstance() = INSTANCE
    }

    init {
        INSTANCE = this
    }

    override fun init() {
        super.init()
        EventBus.INSTANCE.register(this)
        if (panel == null) {
            panel = Panel()
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiMouseX = mouseX
        guiMouseY = mouseY
    }

    @EventListener
    fun onSkiaRender(event: EventSkiaDrawScene) {
        if (this.minecraft.screen !== this) {
            EventBus.INSTANCE.unregister(this)
            return
        }

        val activePanel = panel ?: return
        AnimationUtils.tick()
        activePanel.update(guiMouseX, guiMouseY)
        activePanel.layout()

        cachedBackground?.close()
        cachedBackground = event.surface.makeImageSnapshot()

        val scale = minecraft.window.guiScale.toFloat()
        cachedBackground?.let { background ->
            ImageFilter.makeBlur(7f * scale, 7f * scale, FilterTileMode.CLAMP).use { blur ->
                io.github.humbleui.skija.Paint().setImageFilter(blur).use { paint ->
                event.canvas.saveLayer(Rect.makeWH(minecraft.window.width.toFloat(), minecraft.window.height.toFloat()), paint)
                event.canvas.drawImage(background, 0f, 0f)
                event.canvas.restore()
                }
            }
        }
        activePanel.render(event.canvas, guiMouseX, guiMouseY, null)
    }

    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        if (panel != null) {
            return panel!!.mouseClicked(event.x(), event.y(), event.button())
        }
        return super.mouseClicked(event, bl)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (panel != null) {
            panel!!.mouseReleased(event.x(), event.y(), event.button())
        }
        return super.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (panel != null) {
            return panel!!.mouseScrolled(mouseY, scrollY)
        }
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val handled = panel?.handleKeyPress(event.key()) ?: false
        if (handled) return true
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            closeGui()
            return true
        }
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val chr = if (event.codepointAsString().isNotEmpty()) event.codepointAsString()[0] else return false
        val handled = panel?.handleCharTyped(chr) ?: false
        if (handled) return true
        return super.charTyped(event)
    }

    override fun onClose() {
        closeGui()
    }

    fun closeGui() {
        EventBus.INSTANCE.unregister(this)
        cachedBackground?.close()
        cachedBackground = null
        panel?.resetDrag()
        if (minecraft.screen === this) (minecraft as MinecraftAccessor).`miohr$setScreenDirect`(null)
        val clickGuiModule = MioHr.INSTANCE.moduleManager.clickGuiModule
        if (clickGuiModule.isEnabled) clickGuiModule.isEnabled = false
    }

    override fun removed() {
        EventBus.INSTANCE.unregister(this)
        cachedBackground?.close()
        cachedBackground = null
        panel?.resetDrag()
        super.removed()
    }
}
