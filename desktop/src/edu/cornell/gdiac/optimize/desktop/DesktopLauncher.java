package edu.cornell.gdiac.optimize.desktop;

import edu.cornell.gdiac.optimize.GDXRoot;
import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GDXAppSettings config = new GDXAppSettings();
		config.title = "Optimization";
		config.width  = 800;
		config.height = 600;
		config.fullscreen = false;
		config.resizable = true;
		new GDXApp(new GDXRoot(), config);
	}
}
