package com.vaadin.spring.viewmenu;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.server.Responsive;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

/**
 * A simple "main layout" that can be used with ViewMenu and the responsive menu
 * implementation it uses. getContent() method returns the layout into which you
 * should place your actual main area, or configure it for your Navigator.
 */
@UIScope
public class ViewMenuLayout extends HorizontalLayout {
	private static final long serialVersionUID = -1785464973401661421L;

	@Autowired
	ViewMenu viewMenu;

	CssLayout content = new CssLayout();

	/**
	 * @return the layout to be used for the main content.
	 */
	public CssLayout getMainContent() {
		return this.content;
	}

	// @PostConstruct
	public void init() {
		setSpacing(false);
		setSizeFull();
		/*
		 * We are using some CSS magic built into Valo theme for reponsive menu.
		 * This adds hints necessary for some supported browsers.
		 */
		this.content.setPrimaryStyleName("valo-content");
		this.content.addStyleName("v-scrollable");
		this.content.setSizeFull();
		addComponents(this.viewMenu, this.content);
		setExpandRatio(this.content, 1);
		this.content.setWidth(100, Unit.PERCENTAGE);

		addAttachListener(new AttachListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void attach(AttachEvent event) {
				Responsive.makeResponsive(getUI());
			}
		});

		this.viewMenu.init();
	}

	public ViewMenu getViewMenu() {
		return this.viewMenu;
	}
}
