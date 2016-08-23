package com.vaadin.spring.ui;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.spring.viewmenu.ViewMenu;
import com.vaadin.spring.viewmenu.ViewMenuLayout;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * A helper class with basic main layout with ViewMenu and ViewMenuLayout,
 * configures Navigator automatically. This way you'll get professional looking
 * basic application structure for free.
 *
 * In your own app, override this class and map it with CDIUI annotation.
 */
@UIScope
public class ViewMenuUI extends UI {
	private static final long serialVersionUID = -3886173714848731454L;

	@Autowired
	private SpringViewProvider viewProvider;

	@Autowired
	protected ViewMenuLayout viewMenuLayout;

	@Override
	protected void init(VaadinRequest request) {
		Navigator navigator = new Navigator(this, this.viewMenuLayout.getMainContent()) {
			private static final long serialVersionUID = -1883036290735427155L;

			@Override
			public void navigateTo(String navigationState) {
				try {
					super.navigateTo(navigationState);
					ViewMenuUI.this.viewMenuLayout	.getViewMenu()
													.navigateTo(navigationState);
				} catch (Exception e) {
					handleNavigationError(navigationState, e);
				}
			}

		};
		navigator.addProvider(this.viewProvider);
		this.viewMenuLayout.init();
		setContent(this.viewMenuLayout);
	}

	public ViewMenuLayout getViewMenuLayout() {
		return this.viewMenuLayout;
	}

	public CssLayout getContentLayout() {
		return this.viewMenuLayout.getMainContent();
	}

	public static ViewMenu getMenu() {
		return ((ViewMenuUI) UI.getCurrent())	.getViewMenuLayout()
												.getViewMenu();
	}

	/**
	 * Workaround for issue 1, related to vaadin issues: 13566, 14884
	 *
	 * @param navigationState
	 *            the view id that was requested
	 * @param e
	 *            the exception thrown by Navigator
	 */
	protected void handleNavigationError(String navigationState, Exception e) {
		Notification.show("The requested view (" + navigationState + ") was not available, "
				+ "entering default screen.", Notification.Type.WARNING_MESSAGE);
		getNavigator().navigateTo(this.viewProvider.getDefaultViewId());
		getSession().getErrorHandler()
					.error(new com.vaadin.server.ErrorEvent(e));
	}

}