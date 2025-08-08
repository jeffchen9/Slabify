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

package com.gmail.frogocomics.slabify.gui;

import com.gmail.frogocomics.slabify.HeightmapLayer;
import com.gmail.frogocomics.slabify.HeightmapManager;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileFilter;
import org.pepsoft.worldpainter.Configuration;

/**
 * Custom component for adding new heightmap or selecting an existing heightmap from
 * the {@link HeightmapManager}.
 */
public final class ImageRefChooser extends JPanel {

  private HeightmapLayer parent;
  private JButton buttonAdd;
  private JButton buttonRemove;
  private JComboBox<File> comboBox;

  public ImageRefChooser() {
    initComponents();
    initComboBox(null);
    setControlStates();
  }

  public File getHeightmapLocation() {
    return (File) comboBox.getSelectedItem();
  }

  public void setHeightmapLocation(File location) {

    File oldValue = getHeightmapLocation();

    if (location == null) {
      comboBox.setSelectedIndex(-1);
    } else {
      if (((DefaultComboBoxModel<?>) comboBox.getModel()).getIndexOf(location) == -1) {
        initComboBox(location);
      }
      comboBox.setSelectedItem(location);
    }

    setControlStates();
    firePropertyChange("location", oldValue, location);
  }

  public void refresh() {
    File selectedLocation = getHeightmapLocation();
    initComboBox(selectedLocation);
    comboBox.setSelectedItem(selectedLocation);
  }

  @Override
  public int getBaseline(int width, int height) {
    return comboBox.getBaseline(width, height);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    setControlStates();
  }

  void setLayerParent(HeightmapLayer parent) {
    this.parent = parent;
  }

  private void initComboBox(File unlistedLocation) {
    HeightmapManager.getInstance().register(unlistedLocation, parent);
    comboBox.setModel(new DefaultComboBoxModel<>(HeightmapManager.getInstance().getHeightmaps().toArray(new File[0])));
  }

  private void setControlStates() {
    comboBox.setEnabled(isEnabled());
    buttonAdd.setEnabled(isEnabled());
    buttonRemove.setEnabled(isEnabled() && comboBox.getSelectedItem() != null);
  }

  private void initComponents() {
    comboBox = new JComboBox<>();
    comboBox.addActionListener(this::jComboBox1ActionPerformed);
    comboBox.setRenderer(new CustomComboBoxRenderer(150));

    buttonAdd = new JButton();
    buttonAdd.setIcon(new ImageIcon(Objects.requireNonNull(
        this.getClass().getResource("/org/pepsoft/worldpainter/icons/brick_add.png"))));
    buttonAdd.setToolTipText("Add an image reference");
    buttonAdd.setMargin(new Insets(2, 2, 2, 2));
    buttonAdd.addActionListener(this::buttonAddActionPerformed);

    buttonRemove = new JButton();
    buttonRemove.setIcon(new ImageIcon(Objects.requireNonNull(
        this.getClass().getResource("/org/pepsoft/worldpainter/icons/brick_delete.png"))));
    buttonRemove.setToolTipText("Remove an image reference");
    buttonRemove.setMargin(new Insets(2, 2, 2, 2));
    buttonRemove.addActionListener(this::buttonRemoveActionPerformed);

    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(this.comboBox, -2, -1, -2).addPreferredGap(
        ComponentPlacement.RELATED).addComponent(this.buttonAdd).addPreferredGap(ComponentPlacement.RELATED).addComponent(this.buttonRemove)));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.buttonRemove).addComponent(this.comboBox, -2, -1, -2).addComponent(this.buttonAdd));
  }

  private void jComboBox1ActionPerformed(ActionEvent e) {
    setControlStates();
    firePropertyChange("image", null, comboBox.getSelectedItem());
  }

  private void buttonAddActionPerformed(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser();

    // Set file filter to only accept image files
    fileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        if (f.isDirectory()) {
          return true;
        }

        String fileName = f.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif");
      }

      @Override
      public String getDescription() {
        return "Image Files (.jpg, .jpeg, .png, .gif)";
      }
    });

    // Show dialog
    fileChooser.setCurrentDirectory(Configuration.getInstance().getHeightMapsDirectory());
    int result = fileChooser.showOpenDialog(this);

    // Process result
    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();

      boolean exists = false;

      for (File ref : HeightmapManager.getInstance().getHeightmaps()) {
        if (ref.equals(selectedFile)) {
          exists = true;
          break;
        }
      }

      if (!exists) {
        comboBox.addItem(selectedFile);
        comboBox.setSelectedItem(selectedFile);

        // Register image ref
        HeightmapManager.getInstance().register(selectedFile, parent);

        Configuration.getInstance().setHeightMapsDirectory(selectedFile.getParentFile());
      }
    }
  }

  private void buttonRemoveActionPerformed(ActionEvent e) {
    File heightmapLocation = (File) comboBox.getSelectedItem();

    if (heightmapLocation != null) {
      comboBox.removeItemAt(comboBox.getSelectedIndex());

      HeightmapManager.getInstance().deregister(heightmapLocation);
    }

  }

  // Custom renderer class
  static class CustomComboBoxRenderer extends DefaultListCellRenderer {
    private final int maxWidth;

    public CustomComboBoxRenderer(int maxWidth) {
      this.maxWidth = maxWidth;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      // Value is equal to null when the combo box is initially created
      if (value instanceof File) {
        String text = ((File) value).getName();

        // Truncate the text if it exceeds the maximum width
        FontMetrics metrics = label.getFontMetrics(label.getFont());
        if (metrics.stringWidth(text) > maxWidth) {
          text = truncateTextToFitWidth(text, metrics, maxWidth);
        }

        label.setText(text);
      }
      return label;
    }

    private String truncateTextToFitWidth(String text, FontMetrics metrics, int maxWidth) {
      String ellipsis = "...";

      for (int i = text.length() - 1; i >= 0; i--) {
        String truncatedText = text.substring(0, i) + ellipsis;
        if (metrics.stringWidth(truncatedText) <= maxWidth) {
          return truncatedText;
        }
      }
      return ellipsis; // If nothing else fits
    }
  }
}
