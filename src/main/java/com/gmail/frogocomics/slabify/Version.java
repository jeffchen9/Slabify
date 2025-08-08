/*
 *     A plugin for WorldPainter that adds slab and stair detail to terrain.
 *     Copyright (C) 2025  Jeff Chen
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.gmail.frogocomics.slabify;

import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for making the Maven project version number available to code.
 */
public final class Version {

  public static final String VERSION;

  static {
    Properties versionProps = new Properties();
    try {
      versionProps.load(
          Version.class.getResourceAsStream("/com.gmail.frogocomics.slabify.properties"));
      VERSION = versionProps.getProperty("com.gmail.frogocomics.slabify.version");
    } catch (IOException e) {
      throw new RuntimeException("I/O error loading version number from classpath", e);
    }
  }

  private Version() {
    // Prevent instantiation
  }
}
