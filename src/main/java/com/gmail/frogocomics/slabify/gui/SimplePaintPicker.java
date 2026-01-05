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

package com.gmail.frogocomics.slabify.gui;

import org.pepsoft.worldpainter.layers.renderers.RendererPreviewer;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;

/**
 * Duplicate of {@link org.pepsoft.worldpainter.layers.renderers.PaintPicker} but only for a solid
 * color selection. Additionally, no support for transparency is provided.
 */
public final class SimplePaintPicker extends JPanel {

  private JButton buttonPickPaint;
  private RendererPreviewer rendererPreviewer;
  private Color color = Color.ORANGE;
  private float opacity;

  public SimplePaintPicker() {
    initComponents();
    updatePreview();
  }

  /**
   * Get the color.
   *
   * @return the color.
   */
  public Color getPaint() {
    return color;
  }

  /**
   * Set the color.
   *
   * @param paint the color.
   */
  public void setPaint(Color paint) {
    // Now add change listener
    // firePropertyChange("paint", color, paint);
    color = paint;
    updatePreview();
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    this.opacity = opacity;
    updatePreview();
  }

  @Override
  public int getBaseline(int width, int height) {
    return buttonPickPaint.getBaseline(width, height);
  }

  private void updatePreview() {
    rendererPreviewer.setColour(color);
    rendererPreviewer.setOpacity(opacity);
  }

  private void pickPaint() {
    Color previousColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (opacity * 255));
    Color selectedColor = JColorChooser.showDialog(this, "Choose a Color", previousColor);
    if (selectedColor != null) {
      setOpacity(selectedColor.getAlpha() / 255f);

      // Strip alpha
      selectedColor = new Color(selectedColor.getRGB());
      setPaint(selectedColor);
    }
  }

  private void initComponents() {
    rendererPreviewer = new RendererPreviewer();
    buttonPickPaint = new JButton();
    rendererPreviewer.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
    rendererPreviewer.setPreferredSize(new Dimension(64, 0));
    buttonPickPaint.setText("...");
    buttonPickPaint.addActionListener(e -> pickPaint());
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(
        layout.createSequentialGroup().addComponent(this.rendererPreviewer, -2, -1, -2)
            .addPreferredGap(
                ComponentPlacement.UNRELATED).addComponent(this.buttonPickPaint)));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addComponent(this.buttonPickPaint, -1, -1, 32767)
        .addComponent(this.rendererPreviewer, -1, -1, 32767));
  }
}
