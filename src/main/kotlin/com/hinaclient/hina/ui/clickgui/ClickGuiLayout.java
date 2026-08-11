package com.hinaclient.hina.ui.clickgui;

import io.github.humbleui.types.Rect;

public final class ClickGuiLayout {
    public final Rect panel;
    public final Rect sidebar;
    public final Rect brand;
    public final Rect navigation;
    public final Rect footer;
    public final Rect workspace;
    public final Rect header;
    public final Rect search;
    public final Rect hudAction;
    public final Rect closeAction;
    public final Rect section;
    public final Rect moduleViewport;

    private ClickGuiLayout(Rect panel, Rect sidebar, Rect brand, Rect navigation, Rect footer,
                           Rect workspace, Rect header, Rect search, Rect hudAction, Rect closeAction,
                           Rect section, Rect moduleViewport) {
        this.panel = panel;
        this.sidebar = sidebar;
        this.brand = brand;
        this.navigation = navigation;
        this.footer = footer;
        this.workspace = workspace;
        this.header = header;
        this.search = search;
        this.hudAction = hudAction;
        this.closeAction = closeAction;
        this.section = section;
        this.moduleViewport = moduleViewport;
    }

    public static ClickGuiLayout calculate(float screenWidth, float screenHeight) {
        float panelWidth = screenWidth * ClickGuiMetrics.PANEL_WIDTH_RATIO;
        float panelHeight = screenHeight * ClickGuiMetrics.PANEL_HEIGHT_RATIO;
        float panelX = (screenWidth - panelWidth) / 2f;
        float panelY = (screenHeight - panelHeight) / 2f;
        Rect panel = Rect.makeXYWH(panelX, panelY, panelWidth, panelHeight);
        float sidebarWidth = Math.clamp(panelWidth * ClickGuiMetrics.SIDEBAR_RATIO,
                ClickGuiMetrics.SIDEBAR_MIN, ClickGuiMetrics.SIDEBAR_MAX);
        Rect sidebar = Rect.makeXYWH(panelX, panelY, sidebarWidth, panelHeight);
        Rect brand = Rect.makeXYWH(panelX + 12f, panelY + 15f, sidebarWidth - 24f, 50f);
        Rect footer = Rect.makeXYWH(panelX + 12f, panelY + panelHeight - 50f, sidebarWidth - 24f, 38f);
        Rect navigation = Rect.makeXYWH(panelX + 12f, panelY + 82f, sidebarWidth - 24f,
                Math.max(70f, footer.getTop() - panelY - 92f));
        float workspaceX = panelX + sidebarWidth;
        Rect workspace = Rect.makeXYWH(workspaceX, panelY, panelWidth - sidebarWidth, panelHeight);
        float pad = Math.min(ClickGuiMetrics.CONTENT_PADDING, workspace.getWidth() * 0.055f);
        float contentX = workspaceX + pad;
        float contentWidth = workspace.getWidth() - pad * 2f;
        Rect header = Rect.makeXYWH(contentX, panelY + 18f, contentWidth, ClickGuiMetrics.HEADER_HEIGHT);
        float actionSize = 30f;
        Rect closeAction = Rect.makeXYWH(contentX + contentWidth - actionSize, panelY + 17f, actionSize, actionSize);
        Rect hudAction = Rect.makeXYWH(closeAction.getLeft() - actionSize - 6f, panelY + 17f, actionSize, actionSize);
        Rect search = Rect.makeXYWH(contentX, header.getBottom(), contentWidth, ClickGuiMetrics.SEARCH_HEIGHT);
        Rect section = Rect.makeXYWH(contentX, search.getBottom() + 8f, contentWidth, ClickGuiMetrics.SECTION_HEIGHT);
        Rect moduleViewport = Rect.makeXYWH(contentX, section.getBottom(), contentWidth,
                Math.max(0f, panelY + panelHeight - pad - section.getBottom()));
        return new ClickGuiLayout(panel, sidebar, brand, navigation, footer, workspace, header,
                search, hudAction, closeAction, section, moduleViewport);
    }
}
