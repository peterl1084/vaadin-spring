package com.vaadin.spring.viewmenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.internal.Conventions;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * A helper to automatically create a menu from available Vaadin CDI view.
 * Listed views should be annotated with ViewMenuItem annotation to be listed
 * here, there you can also set icon, caption etc.
 *
 * You'll probably want something more sophisticated in your app, but this might
 * be handy prototyping small CRUD apps.
 *
 * By default the menu uses Valo themes responsive layout rules, but those can
 * easily be overridden.
 *
 */
@UIScope
@StyleSheet("viewmenu.css")
public class ViewMenu extends CssLayout {
	private static final long serialVersionUID = -7321278809483532737L;

	@Autowired
	private SpringViewProvider viewProvider;

	// We can have multiple views with the same view name, as long as they
	// belong to different UI subclasses
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ViewMenu.class);

	private final Header header = new Header(null).setHeaderLevel(3);

	private Button selectedButton;

	private final HashMap<String, Button> nameToButton = new HashMap<String, Button>();
	private Button active;
	private Component secondaryComponent;
	private CssLayout items;

	// @PostConstruct
	void init() {
		createHeader();

		final Button showMenu = new Button("Menu", new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				if (getStyleName().contains("valo-menu-visible")) {
					removeStyleName("valo-menu-visible");
				} else {
					addStyleName("valo-menu-visible");
				}
			}
		});
		showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
		showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
		showMenu.addStyleName("valo-menu-toggle");
		showMenu.setIcon(FontAwesome.LIST);
		addComponent(showMenu);

		this.items = new CssLayout(getAsLinkButtons(this.viewProvider.getRootBeanNames()));
		this.items.setPrimaryStyleName("valo-menuitems");
		addComponent(this.items);

		addAttachListener(new AttachListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void attach(AttachEvent event) {
				getUI().addStyleName("valo-menu-responsive");
				if (getMenuTitle() == null) {
					setMenuTitle(detectMenuTitle());
				}
				Navigator navigator = UI.getCurrent()
										.getNavigator();
				if (navigator != null) {
					String state = navigator.getState();
					if (state == null) {
						state = "";
					}
					Button b = ViewMenu.this.nameToButton.get(state);
					if (b != null) {
						emphasisAsSelected(b);
					}
				}
			}
		});
	}

	protected void createHeader() {
		setPrimaryStyleName("valo-menu");
		addStyleName("valo-menu-part");
		HorizontalLayout headercontent = new HorizontalLayout(this.header);
		headercontent.setMargin(false);
		for (Component component : headercontent) {
			headercontent.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
		}
		headercontent.setStyleName("valo-menu-title");
		addComponent(headercontent);
	}

	private Component[] getAsLinkButtons(List<String> beanNames) {
		ArrayList<Button> buttons = new ArrayList<Button>();
		for (String beanName : beanNames) {
			Class<?> viewClass = this.viewProvider.getTypeOfBeanName(beanName);
			SpringView annotation = this.viewProvider.getAnnotationOfBeanName(beanName);
			String viewId = Conventions.deriveMappingForView(viewClass, annotation);
			Button button = getButtonForViewId(viewId);
			button.removeStyleName("valo-menu-subtitle");

			buttons.add(button);
		}

		return buttons.toArray(new Button[0]);
	}

	protected Button getButtonFor(final Class<?> viewClass, SpringView annotation, final String viewId) {
		final Button button = new Button(viewId);
		button.setPrimaryStyleName("valo-menu-item");
		button.setIcon(getIconFor(annotation));
		button.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				final Navigator navigator = UI	.getCurrent()
												.getNavigator();
				navigator.navigateTo(viewId);
			}
		});
		return button;
	}

	protected void emphasisAsSelected(Button button) {
		if (this.selectedButton != null) {
			this.selectedButton.removeStyleName("selected");
		}
		button.addStyleName("selected");
		this.selectedButton = button;
	}

	protected Resource getIconFor(SpringView annotation) {
		if (annotation == null) {
			return FontAwesome.FILE;
		}
		return annotation.icon();
	}

	// protected String getNameFor(SpringViewA) {
	// ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
	// if (annotation != null && !annotation.title().isEmpty()) {
	// return annotation.title();
	// }
	// String simpleName = viewType.getSimpleName();
	// // remove trailing view
	// simpleName = simpleName.replaceAll("View$", "");
	// // decamelcase
	// simpleName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
	// simpleName), " ");
	// return simpleName;
	// }

	public void setActive(String viewId) {
		if (this.active != null) {
			this.active.setEnabled(true);
		}
		this.active = this.nameToButton.get(viewId);
		if (this.active != null) {
			this.active.setEnabled(false);
		}
	}

	public String getMenuTitle() {
		return this.header.getText();
	}

	public void setMenuTitle(String menuTitle) {
		this.header.setText(menuTitle);
	}

	private String detectMenuTitle() {
		// try to dig a sane default from Title annotation in UI or class name
		final Class<? extends UI> uiClass = getUI().getClass();
		Title title = uiClass.getAnnotation(Title.class);
		if (title != null) {
			return title.value();
		} else {
			String simpleName = uiClass.getSimpleName();
			return simpleName.replaceAll("UI", "");
		}
	}

	public void setSecondaryComponent(Component component) {
		if (this.secondaryComponent != component) {
			if (this.secondaryComponent != null) {
				removeComponent(this.secondaryComponent);
			}
			this.secondaryComponent = component;
			addComponent(component, 1);
		}
	}

	public void navigateTo(final String viewId) {
		navigateTo(viewId, true);
	}

	private void navigateTo(final String viewId, boolean navigate) {
		if (navigate) {
			removeStyleName("valo-menu-visible");
		}
		this.items.removeAllComponents();

		// if navigation has no parent => root
		if (this.viewProvider.getBeanNameOfViewName(viewId) == null) {
			this.items.addComponents(getAsLinkButtons(this.viewProvider.getRootBeanNames()));
			return;
		}

		List<String> children = this.viewProvider.getChildBeanNames(viewId);

		if (hasParentView(viewId) || children.size() > 0) {
			Button backwardsButton = getBackwardsButton(viewId);
			this.items.addComponent(backwardsButton);
		}

		Button button = getButtonForViewId(viewId);

		if (children.size() > 0) {
			button.addStyleName("valo-menu-subtitle");
			this.items.addComponent(button);
			this.items.addComponents(getAsLinkButtons(children));
		} else {
			if (hasParentView(viewId)) {
				navigateTo(this.viewProvider.getParentViewName(viewId), false);
			} else {
				this.items.addComponents(getAsLinkButtons(this.viewProvider.getRootBeanNames()));
			}
		}
		if (navigate) {
			emphasisAsSelected(button);
		}
	}

	private Button getBackwardsButton(final String viewId) {
		Button backwardsButton = new Button("back");
		backwardsButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				navigateTo(ViewMenu.this.viewProvider.getParentViewName(viewId), false);
			}
		});
		backwardsButton.setPrimaryStyleName("valo-menu-item");
		backwardsButton.setIcon(FontAwesome.ARROW_LEFT);
		return backwardsButton;
	}

	private boolean hasParentView(String viewName) {
		String beanName = this.viewProvider.getBeanNameOfViewName(viewName);
		if (beanName == null) {
			return false;
		}
		return !this.viewProvider	.getAnnotationOfBeanName(beanName)
									.parentName()
									.isEmpty();
	}

	private Button getButtonForViewId(String viewId) {
		Button button = this.nameToButton.get(viewId);
		if (button == null) {
			String beanName = this.viewProvider.getBeanNameOfViewName(viewId);
			Class<?> viewClass = this.viewProvider.getTypeOfBeanName(beanName);
			SpringView annotation = this.viewProvider.getAnnotationOfBeanName(beanName);
			button = getButtonFor(viewClass, annotation, viewId);
			this.nameToButton.put(viewId, button);
		}
		return button;
	}

}
