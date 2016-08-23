package com.vaadin.spring.viewmenu;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Label whose content is wrapped in an H1,H2,H3.. element.
 *
 * Uses Jsoup for sanitation. Only text by default.
 */
public class Header extends Label {
	private static final long serialVersionUID = 1L;
	
	private String text;
    private int headerLevel = 1;

    protected Whitelist getWhitelist() {
        return Whitelist.none();
    }

    /**
     *
     * @param whitelist the whitelist used for sanitizing the header text
     * @return the object itself for further configuration
     * @deprecated Whitelist is not serializable. If using e.g. clustering,
     * override the getter method for whitelist instead.
     */
    @Deprecated
    public Header setWhitelist(Whitelist whitelist) {
        markAsDirty();
        return this;
    }

    public String getText() {
        return text;
    }

    public Header setText(String text) {
        this.text = text;
        markAsDirty();
        return this;
    }

    public int getHeaderLevel() {
        return headerLevel;
    }

    public Header setHeaderLevel(int headerLevel) {
        if (headerLevel < 1 || headerLevel > 6) {
            throw new IllegalArgumentException("Header levels 1-6 supported");
        }
        this.headerLevel = headerLevel;
        markAsDirty();
        return this;
    }

    public Header(String headerText) {
        text = headerText;
    }

    @Override
    public void setValue(String newStringValue) {
        setText(newStringValue);
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        render();
        super.beforeClientResponse(initial);
    }

    private void render() {
        if (text != null) {
            setContentMode(ContentMode.HTML);
            StringBuilder sb = new StringBuilder("<h");
            sb.append(headerLevel);
            sb.append(">");
            sb.append(Jsoup.clean(text, getWhitelist()));
            sb.append("</h");
            sb.append(headerLevel);
            sb.append(">");
            super.setValue(sb.toString());
            text = null;
        }
    }

    public Header withStyleName(String styleName) {
        setStyleName(styleName);
        return this;
    }

}