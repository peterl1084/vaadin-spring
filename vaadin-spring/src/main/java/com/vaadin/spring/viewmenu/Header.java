/*
 * Copyright 2015 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	 * @param whitelist
	 *            the whitelist used for sanitizing the header text
	 * @return the object itself for further configuration
	 * @deprecated Whitelist is not serializable. If using e.g. clustering,
	 *             override the getter method for whitelist instead.
	 */
	@Deprecated
	public Header setWhitelist(Whitelist whitelist) {
		markAsDirty();
		return this;
	}

	public String getText() {
		return this.text;
	}

	public Header setText(String text) {
		this.text = text;
		markAsDirty();
		return this;
	}

	public int getHeaderLevel() {
		return this.headerLevel;
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
		this.text = headerText;
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
		if (this.text != null) {
			setContentMode(ContentMode.HTML);
			StringBuilder sb = new StringBuilder("<h");
			sb.append(this.headerLevel);
			sb.append(">");
			sb.append(Jsoup.clean(this.text, getWhitelist()));
			sb.append("</h");
			sb.append(this.headerLevel);
			sb.append(">");
			super.setValue(sb.toString());
			this.text = null;
		}
	}

	public Header withStyleName(String styleName) {
		setStyleName(styleName);
		return this;
	}

}