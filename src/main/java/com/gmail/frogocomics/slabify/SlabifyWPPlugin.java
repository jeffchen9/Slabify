/*
 *     A plugin for WorldPainter that adds additional shape detail to terrain.
 *     Copyright (C) 2026  Jeff Chen
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gmail.frogocomics.slabify;

import com.gmail.frogocomics.slabify.gui.SlabCustomLayerEditor;
import com.gmail.frogocomics.slabify.layers.Slab;
import com.gmail.frogocomics.slabify.shape.Shapes;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.MainFrame;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.WPContext;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.LayerEditor;
import org.pepsoft.worldpainter.plugins.AbstractPlugin;
import org.pepsoft.worldpainter.plugins.CustomLayerProvider;
import org.pepsoft.worldpainter.plugins.LayerEditorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gmail.frogocomics.slabify.Version.VERSION;

/**
 * The main plugin class for Slabify.
 */
@SuppressWarnings("unused") // Instantiated by WorldPainter
public final class SlabifyWPPlugin extends AbstractPlugin implements CustomLayerProvider,
    LayerEditorProvider {

  private static final Logger logger = LoggerFactory.getLogger(SlabifyWPPlugin.class);
  private static final String NAME = "Slabify Plugin";
  private static final List<Class<? extends CustomLayer>> CUSTOM_LAYERS = Collections.singletonList(
      Slab.class);
  private static final String LATEST_RELEASE_API = "https://api.github.com/repos/jeffchen9/Slabify/releases/latest";
  private static final String LATEST_RELEASE_PAGE = "https://github.com/jeffchen9/Slabify/releases/latest";
  private static final String IGNORED_UPDATE_KEY = "com.gmail.frogocomics.slabify.ignoredUpdate.";

  public SlabifyWPPlugin() {
    super(NAME, VERSION);
    Shapes.init();
  }

  @Override
  public void init(WPContext context) {
    // Respect WorldPainter's own "Check for updates" preference
    if (Configuration.getInstance().isCheckForUpdates()) {
      Thread thread = new Thread(SlabifyWPPlugin::checkForUpdate, "Slabify update check");
      thread.setDaemon(true);
      thread.start();
    }
  }

  private static void checkForUpdate() {
    // Command-line tools such as the scripting tool also initialize plugins, but never create main
    // window, so they wait here until they exit without checking or showing anything
    try {
      while (MainFrame.getMainFrame() == null) {
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      return;
    }

    try (InputStream in = URI.create(LATEST_RELEASE_API).toURL().openStream()) {
      Matcher m = Pattern.compile("SlabifyPlugin-([0-9]+(?:\\.[0-9]+)*)\\.jar")
          .matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));

      if (m.find()) {
        String latest = m.group(1);
        logger.info("Current version: {}; latest version: {}", VERSION, latest);

        if (isNewer(latest, VERSION) && !Configuration.getInstance().isMessageDisplayed(IGNORED_UPDATE_KEY + latest)) {
          SwingUtilities.invokeLater(() -> showUpdateDialog(latest));
        }
      }
    } catch (IOException ignored) {
    }
  }

  private static void showUpdateDialog(String latest) {
    String[] options = {"Open release page", "Ask me next time", "Ignore"};
    int choice = JOptionPane.showOptionDialog(MainFrame.getMainFrame(),
        "A new version of Slabify is available.\nCurrent version: " + VERSION + "\nLatest version: " + latest, NAME,
        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

    if (choice == 0) {
      try {
        Desktop.getDesktop().browse(URI.create(LATEST_RELEASE_PAGE));
      } catch (IOException | UnsupportedOperationException e) {
        JOptionPane.showMessageDialog(MainFrame.getMainFrame(),
            "Could not open a browser. The release page is:\n" + LATEST_RELEASE_PAGE, NAME,
            JOptionPane.WARNING_MESSAGE);
      }
    } else if (choice == 2) {
      Configuration.getInstance().setMessageDisplayed(IGNORED_UPDATE_KEY + latest);
    }
  }

  /**
   * Check whether a version is newer than another, comparing each number in turn.
   *
   * @param latest  the version to check.
   * @param current the version to compare against. Anything after the numbers, such as {@code -SNAPSHOT}, is ignored.
   * @return {@code true} if {@code latest} is newer than {@code current}.
   */
  private static boolean isNewer(String latest, String current) {
    String[] l = latest.split("\\.");
    String[] c = current.split("\\D+");

    for (int i = 0; i < Math.max(l.length, c.length); i++) {
      int a = i < l.length ? Integer.parseInt(l[i]) : 0;
      int b = i < c.length ? Integer.parseInt(c[i]) : 0;

      if (a != b) {
        return a > b;
      }
    }

    return false;
  }

  @Override
  public List<Class<? extends CustomLayer>> getCustomLayers() {
    return CUSTOM_LAYERS;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <L extends Layer> LayerEditor<L> createLayerEditor(Platform platform, Class<L> layerType) {
    if (layerType == Slab.class) {
      return (LayerEditor<L>) new SlabCustomLayerEditor(platform);
    } else {
      return null;
    }
  }
}
