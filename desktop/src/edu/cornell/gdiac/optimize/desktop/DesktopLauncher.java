package edu.cornell.gdiac.optimize.desktop;

import edu.cornell.gdiac.optimize.GDXRoot;
import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GDXAppSettings config = new GDXAppSettings();
		config.title = "TempoRary";
		config.width  = 1200;
		config.height = 800;
		config.fullscreen = false;
		config.resizable = true;
		new GDXApp(new GDXRoot(), config);
	}
}
