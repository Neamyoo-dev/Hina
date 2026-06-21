package com.hinaclient.hina.ui

import com.hinaclient.hina.event.EventBus
import com.hinaclient.hina.event.EventListener
import com.hinaclient.hina.event.skia.EventSkiaDrawScene
import com.hinaclient.hina.ui.clickgui.Panel
import com.hinaclient.hina.utils.shader.LiquidGlassShader
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

    companion object {
        private var INSTANCE: ClickGuiScreen? = null
        fun getInstance() = INSTANCE
    }

    init {
        INSTANCE = this
    }

    override fun init() {
        super.init()
        LiquidGlassShader.init()
        EventBus.INSTANCE.register(this)
        if (panel == null) {
            panel = Panel()
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {}

    @EventListener
    fun onSkiaRender(event: EventSkiaDrawScene) {
        if (this.minecraft.screen !== this) {
            EventBus.INSTANCE.unregister(this)
            return
        }

        if (cachedBackground == null) {
            cachedBackground = event.surface.makeImageSnapshot()
        }

        val glassShader = if (cachedBackground != null && panel != null) {
            LiquidGlassShader.makeShader(
                cachedBackground!!,
                Rect.makeXYWH(panel!!.x, panel!!.y, panel!!.width, panel!!.height)
            )
        } else null

        panel?.render(event.canvas, 0, 0, glassShader)
    }

    override fun mouseClicked(event: MouseButtonEvent, bl: Boolean): Boolean {
        if (panel != null) {
            val scaleX = minecraft.window.guiScale.toDouble()
            val scaleY = minecraft.window.guiScale.toDouble()
            return panel!!.mouseClicked(event.x() * scaleX, event.y() * scaleY, event.button())
        }
        return super.mouseClicked(event, bl)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        if (panel != null) {
            val scaleX = minecraft.window.guiScale.toDouble()
            val scaleY = minecraft.window.guiScale.toDouble()
            panel!!.mouseReleased(event.x() * scaleX, event.y() * scaleY, event.button())
        }
        return super.mouseReleased(event)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (panel != null) {
            val scaleY = minecraft.window.guiScale.toDouble()
            return panel!!.mouseScrolled(mouseY * scaleY, scrollY)
        }
        return false
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (panel?.isSearchFocused == true) {
                panel?.handleKeyPress(event.key())
                return true
            }
            minecraft.screen = null
            return true
        }
        val handled = panel?.handleKeyPress(event.key()) ?: false
        if (handled) return true
        return super.keyPressed(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        val chr = if (event.codepointAsString().isNotEmpty()) event.codepointAsString()[0] else return false
        val handled = panel?.handleCharTyped(chr) ?: false
        if (handled) return true
        return super.charTyped(event)
    }

    override fun onClose() {
        EventBus.INSTANCE.unregister(this)
        super.onClose()
    }

    override fun removed() {
        EventBus.INSTANCE.unregister(this)
        cachedBackground?.close()
        cachedBackground = null
        LiquidGlassShader.invalidateCache()
        panel?.resetDrag()
        super.removed()
    }
}
