package ninja.crinkle.mod.client.gui.widgets;

import ninja.crinkle.mod.client.gui.managers.GuiManager;
import ninja.crinkle.mod.client.gui.properties.Rect;
import ninja.crinkle.mod.client.gui.properties.Sizing;
import ninja.crinkle.mod.client.gui.themes.ThemeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A reusable tabbed panel container. Internally composed of an HBoxContainer with a left VBoxContainer
 * (tab rail) and a right Container (content area using stack layout — only the active tab's content is visible).
 */
public class TabbedPanelContainer extends AbstractContainer {

    public record Entry(String id, Button tabButton, VBoxContainer content) {}

    // The outer rail container (styled, direct child of this widget)
    private final AbstractContainer tabRailOuter;
    // The inner container where tab buttons are actually added
    private final AbstractContainer tabButtonHost;
    private final Container contentArea;
    // Direct children used by arrange()
    private final AbstractContainer contentChild;
    private final List<Entry> tabs = new ArrayList<>();
    private int selectedIndex = -1;
    private final int tabWidth;
    private final int tabMargin;
    private final int contentMargin;

    protected TabbedPanelContainer(@NotNull Builder builder) {
        super(builder);
        this.tabWidth = builder.tabWidth();
        this.tabMargin = builder.tabMargin();
        this.contentMargin = builder.contentMargin();

        // Create the tab rail (left side, styled)
        VBoxContainer rail = new VBoxContainer.Builder(this)
                .name("tab_rail")
                .style("tab_rail")
                .separation(0)
                .horizontalSizing(Sizing.ShrinkBegin)
                .verticalSizing(Sizing.Fill)
                .pushAndReturn();
        tabRailOuter = rail;

        // If tabMargin > 0, insert a MarginContainer inside the rail
        // to provide padding around the button list
        if (tabMargin > 0) {
            MarginContainer railMargin = new MarginContainer.Builder(rail)
                    .margins(tabMargin)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();
            tabButtonHost = new VBoxContainer.Builder(railMargin)
                    .name("tab_button_list")
                    .separation(2)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();
        } else {
            rail.separation(2);
            tabButtonHost = rail;
        }

        // Create the content area (right side, stack layout)
        if (contentMargin > 0) {
            MarginContainer contentWrapper = new MarginContainer.Builder(this)
                    .margins(contentMargin)
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();
            contentArea = new Container.Builder(contentWrapper)
                    .name("tab_content_area")
                    .horizontalSizing(Sizing.Fill)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();
            contentChild = contentWrapper;
        } else {
            contentArea = new Container.Builder(this)
                    .name("tab_content_area")
                    .horizontalSizing(Sizing.Expand, Sizing.Fill)
                    .verticalSizing(Sizing.Fill)
                    .pushAndReturn();
            contentChild = contentArea;
        }
    }

    /**
     * Adds a new tab with the given id and label. Returns the content VBoxContainer for the caller to populate.
     */
    public VBoxContainer addTab(String id, String label) {
        int index = tabs.size();
        int btnHeight = appearance().font().lineHeight + 13;

        // Create tab button in the button host (inside the rail, possibly inside a margin)
        Button tabButton = new Button.Builder(tabButtonHost)
                .name("tab_btn_" + id)
                .text(label)
                .style(tabs.isEmpty() ? "tab_active" : "tab_inactive")
                .minSize(tabWidth - 4 - (tabMargin * 2), btnHeight)
                .horizontalSizing(Sizing.Fill)
                .onClick((e, w) -> selectTab(index))
                .pushAndReturn();

        // Create content container (initially hidden unless first tab)
        VBoxContainer content = new VBoxContainer.Builder(contentArea)
                .name("tab_content_" + id)
                .separation(5)
                .horizontalSizing(Sizing.Fill)
                .verticalSizing(Sizing.Fill)
                .visible(tabs.isEmpty())
                .pushAndReturn();

        Entry entry = new Entry(id, tabButton, content);
        tabs.add(entry);

        if (tabs.size() == 1) {
            selectedIndex = 0;
        }

        return content;
    }

    /**
     * Selects the tab at the given index, hiding all other tab contents and swapping button styles.
     */
    public void selectTab(int index) {
        if (index < 0 || index >= tabs.size() || index == selectedIndex) return;

        for (int i = 0; i < tabs.size(); i++) {
            Entry entry = tabs.get(i);
            boolean isSelected = (i == index);
            entry.content().visible(isSelected);
            entry.tabButton().style(ThemeRegistry.current().widgetTheme(isSelected ? "tab_active" : "tab_inactive"));
        }
        selectedIndex = index;
    }

    public int selectedIndex() {
        return selectedIndex;
    }

    public List<Entry> tabs() {
        return List.copyOf(tabs);
    }

    @Override
    public int minimumWidth() {
        int minW = super.minimumWidth();
        int childSum = tabRailOuter.minimumWidth() + contentChild.minimumWidth();
        return Math.max(minW, childSum);
    }

    @Override
    public int minimumHeight() {
        int minH = super.minimumHeight();
        return Math.max(minH, Math.max(tabRailOuter.minimumHeight(), contentChild.minimumHeight()));
    }

    @Override
    public void arrange() {
        if (children().isEmpty() || rect().equals(Rect.ZERO)) return;

        int railWidth = tabWidth;
        int contentWidth = rect().width() - railWidth;

        // Position direct children — if wrapped in MarginContainers,
        // they'll handle applying insets to their own children.
        tabRailOuter.setRect(new Rect(rect().x(), rect().y(), railWidth, rect().height()));
        contentChild.setRect(new Rect(rect().x() + railWidth, rect().y(), contentWidth, rect().height()));
    }

    @Override
    public AbstractWidget visualCopy(AbstractContainer newParent) {
        TabbedPanelContainer copy = new TabbedPanelContainer.Builder(newParent)
                .tabWidth(tabWidth).build();
        copy.copyVisualProperties(this);
        return copy;
    }

    public static class Builder extends AbstractContainerBuilder<Builder> {
        private int tabWidth = 100;
        private int tabMargin = 0;
        private int contentMargin = 0;

        public Builder(AbstractContainer container) {
            super(container);
            active(true);
        }

        public Builder(GuiManager manager) {
            super(manager);
            active(true);
        }

        public Builder tabWidth(int tabWidth) {
            this.tabWidth = tabWidth;
            return self();
        }

        public int tabWidth() {
            return tabWidth;
        }

        public Builder tabMargin(int tabMargin) {
            this.tabMargin = tabMargin;
            return self();
        }

        public int tabMargin() {
            return tabMargin;
        }

        public Builder contentMargin(int contentMargin) {
            this.contentMargin = contentMargin;
            return self();
        }

        public int contentMargin() {
            return contentMargin;
        }

        @Override
        public AbstractContainer push() {
            return parent().add(this);
        }

        @Override
        public TabbedPanelContainer pushAndReturn() {
            TabbedPanelContainer container = new TabbedPanelContainer(this);
            parent().add(container);
            return container;
        }

        @Override
        public TabbedPanelContainer build() {
            return new TabbedPanelContainer(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
