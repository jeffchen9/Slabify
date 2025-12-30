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

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.layers.Slab;
import com.gmail.frogocomics.slabify.layers.Slab.Interpolation;
import com.gmail.frogocomics.slabify.layers.SlabCustomLayerSettings;
import com.gmail.frogocomics.slabify.shape.Shape;
import com.gmail.frogocomics.slabify.shape.Shape.Options;
import com.gmail.frogocomics.slabify.shape.Shapes;
import org.javatuples.Pair;
import org.pepsoft.minecraft.Material;
import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.dynmap.DynmapPreviewer;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import static org.pepsoft.minecraft.Constants.MC_STONE;

public final class SlabCustomLayerEditor extends AbstractLayerEditor<Slab> {

  private final Platform platform;
  // DEBUG
  private final boolean enableBorders = false;

  // Material
  private JLabel materialLabel;
  private MixedMaterialChooser mixedMaterialSelector;
  // Replace non-solid blocks
  private JLabel replaceLabel;
  private JCheckBox replaceMaterialBox;
  // Shape
  private JLabel shapesLabel;
  private JButton shapesBox;
  // Interpolation method
  private JLabel interpolationLabel;
  private JComboBox<Interpolation> interpolationBox;
  // Mimic underlying blocks
  private JLabel mimicLabel;
  private JCheckBox mimicBox;
  // Additive
  private JLabel additiveLabel;
  private JCheckBox additiveBox;

  // Block mapping table
  private JTable mimicTable;
  private JScrollPane tableScrollPane;
  private CustomTableModel model;
  private JButton addButton;
  private JButton removeButton;
  private JButton loadButton;
  private JButton saveButton;
  private JButton previewButton;

  // Name
  private JLabel nameLabel;
  private JTextField nameField;
  // Layer color
  private JLabel paintLabel;
  private SimplePaintPicker paintPicker;

  /**
   * Creates new form SlabCustomLayerEditor
   *
   * @param platform the platform
   */
  public SlabCustomLayerEditor(Platform platform) {

    // Initialize components
    initComponents();
    this.platform = platform;

    // Name
    nameField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        settingsChanged();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        settingsChanged();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        settingsChanged();
      }
    });

    // Mimic underlying blocks
    mimicBox.addItemListener(e -> {
      boolean selected = e.getStateChange() == ItemEvent.SELECTED;
      mixedMaterialSelector.setEnabled(!selected);
      mimicTable.setEnabled(selected);

      // Buttons
      addButton.setEnabled(selected);
      removeButton.setEnabled(selected);
      loadButton.setEnabled(selected);
      saveButton.setEnabled(selected);
      previewButton.setEnabled(selected);
    });

    mimicTable.addPropertyChangeListener("enabled", evt -> {
      boolean oldValue = (boolean) evt.getOldValue();
      boolean newValue = (boolean) evt.getNewValue();

      if (!oldValue && newValue) {
        // Enabled
        for (int row = 0; row < mimicTable.getRowCount(); row++) {
          for (int column = 0; column < mimicTable.getColumnCount(); column++) {
            CustomRendererEditor editor = (CustomRendererEditor) mimicTable.getCellEditor(row,
                column);
            editor.enable();
          }
        }
      } else if (oldValue & !newValue) {
        // Disabled
        for (int row = 0; row < mimicTable.getRowCount(); row++) {
          for (int column = 0; column < mimicTable.getColumnCount(); column++) {
            CustomRendererEditor editor = (CustomRendererEditor) mimicTable.getCellEditor(row,
                column);
            editor.disable();
          }
        }
        mimicTable.clearSelection();
      }
    });
    mimicTable.setEnabled(false);

    // Material
    mixedMaterialSelector.setPlatform(platform);
    mixedMaterialSelector.addPropertyChangeListener("material", event -> settingsChanged());

    // Shape selection
    shapesBox.addActionListener(e -> openShapesDialog());
  }

  @Override
  public Slab createLayer() {
    Material stoneSlabMaterial = Material.get(MC_STONE);
    return new Slab("My Slab", MixedMaterial.create(platform, stoneSlabMaterial));
  }

  @Override
  public void setLayer(Slab layer) {
    super.setLayer(layer);
    reset();
  }

  @Override
  public void commit() {
    if (!isCommitAvailable()) {
      throw new IllegalStateException("Settings invalid or incomplete");
    }
    // Make sure the material is registered, in case it's new
    layer.setMaterial(
        MixedMaterialManager.getInstance().register(mixedMaterialSelector.getMaterial()));
    saveSettings(layer);
  }

  @Override
  public void reset() {
    nameField.setText(layer.getName());
    paintPicker.setPaint((Color) layer.getPaint());
    paintPicker.setOpacity(layer.getOpacity());
    mixedMaterialSelector.setMaterial(layer.getMaterial());
    replaceMaterialBox.setSelected(layer.replacesNonSolidBlocks());
    mimicBox.setSelected(layer.mimicsTerrain());
    updateMimicTable(layer.getMapping());
    updateShapesDialog(layer.getShapes());
    additiveBox.setSelected(layer.isAddHalf());
    interpolationBox.setSelectedItem(layer.getInterpolation());

    addButton.setEnabled(layer.mimicsTerrain());
    removeButton.setEnabled(layer.mimicsTerrain());
    loadButton.setEnabled(layer.mimicsTerrain());
    saveButton.setEnabled(layer.mimicsTerrain());
    previewButton.setEnabled(layer.mimicsTerrain());

    settingsChanged();
  }

  private void updateMimicTable(Map<String, Material> mapping) {
    // Deal with the mapping
    CustomTableModel tableModel = (CustomTableModel) mimicTable.getModel();

    // Remove rows
    tableModel.removeRows();

    // Add empty rows
    if (mapping.isEmpty()) {
      tableModel.addRow();
    } else {
      tableModel.addRows(mapping.size());

      int row = 0;

      // Populate rows
      for (Entry<String, Material> entry : mapping.entrySet()) {
        String[] underlyingMaterial = entry.getKey().split(":");
        Material slabMaterial = entry.getValue();

        tableModel.setValueAt(
            new Pair<>(underlyingMaterial[0], underlyingMaterial[1]),
            row, 0);
        tableModel.setValueAt(new Pair<>(slabMaterial.namespace, slabMaterial.simpleName), row, 1);

        row++;
      }
    }
  }

  @Override
  public ExporterSettings getSettings() {
    return new SlabCustomLayerSettings(layer);
  }

  @Override
  public boolean isCommitAvailable() {
    // Check whether the configuration currently selected by the user is valid and could be written to the layer
    return true;
  }

  @Override
  public void setContext(LayerEditorContext context) {
    super.setContext(context);
    mixedMaterialSelector.setColourScheme(context.getColourScheme());
    mixedMaterialSelector.setExtendedBlockIds(context.isExtendedBlockIds());
  }

  private void saveSettings(Slab layer) {
    layer.setName(nameField.getText());
    layer.setPaint(paintPicker.getPaint());
    layer.setOpacity(paintPicker.getOpacity());
    layer.setReplaceNonSolidBlocks(replaceMaterialBox.isSelected());
    layer.setMimic(mimicBox.isSelected());
    layer.setAddHalf(additiveBox.isSelected());
    layer.setMapping(getCurrentMapping());

    // Get from shapes dialog
    Map<String, Options> newShapes = new HashMap<>();
    for (Entry<String, Map<Options, JCheckBox>> entry : shapeSelectionMap.entrySet()) {
      Map<Options, JCheckBox> v = entry.getValue();

      for (Entry<Options, JCheckBox> entry2 : v.entrySet()) {
        if (entry2.getValue().isSelected()) {
          newShapes.put(entry.getKey(), entry2.getKey());
        }
      }
    }

    layer.setShapes(newShapes);
    layer.setInterpolation((Interpolation) interpolationBox.getSelectedItem());
  }

  private Map<String, Material> getCurrentMapping() {
    // Deal with the mapping
    Map<String, Material> mapping = new LinkedHashMap<>();
    TableModel tableModel = mimicTable.getModel();
    for (int i = 0; i < mimicTable.getRowCount(); i++) {
      @SuppressWarnings("unchecked")
      Pair<String, String> p1 = (Pair<String, String>) tableModel.getValueAt(i, 0);
      @SuppressWarnings("unchecked")
      Pair<String, String> p2 = (Pair<String, String>) tableModel.getValueAt(i, 1);

      Optional<Material> o2 = getMaterial(p2);

      // Key: underlying block
      // Value: slab
      o2.ifPresent(material -> mapping.put(p1.getValue0() + ":" + p1.getValue1(), material));
    }

    return mapping;
  }

  private void initComponents() {

    materialLabel = new JLabel("Material:");
    materialLabel.setToolTipText(
        "Full blocks will be automatically be converted to slabs on export");
    mixedMaterialSelector = new MixedMaterialChooser();

    replaceLabel = new JLabel("Replace non-solid blocks:");
    replaceLabel.setToolTipText("Any blocks which are not solid will be replaced with slabs");
    replaceMaterialBox = new JCheckBox();

    mimicLabel = new JLabel("Mimic underlying blocks:");
    mimicLabel.setToolTipText("Places the slab variant of the underlying block, where possible");
    mimicBox = new JCheckBox();

    additiveLabel = new JLabel("Additive:");
    additiveLabel.setToolTipText("Only add to the terrain");
    additiveBox = new JCheckBox();

    shapesLabel = new JLabel("Allowed shapes:");
    shapesLabel.setToolTipText("Set allowed block shapes");
    shapesBox = new JButton("Edit");
    createShapesDialog();

    interpolationLabel = new JLabel("Interpolation method:");
    interpolationLabel.setToolTipText("The interpolation method used to add extra detail");
    interpolationBox = new JComboBox<>(Interpolation.values());
    interpolationBox.setSelectedIndex(0);

    nameLabel = new JLabel("Name:");
    nameField = new JTextField();

    paintLabel = new JLabel("Paint:");
    paintPicker = new SimplePaintPicker();

    JPanel topPanel = new JPanel();
    GroupLayout layout = new GroupLayout(topPanel);
    topPanel.setLayout(layout);

    layout.setHorizontalGroup(
        layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(nameLabel)
                .addComponent(paintLabel)
                .addComponent(materialLabel)
                .addComponent(replaceLabel)
                .addComponent(mimicLabel)
                .addComponent(additiveLabel)
                .addComponent(shapesLabel)
                .addComponent(interpolationLabel)
            )
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE)
                .addComponent(paintPicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE)
                .addComponent(mixedMaterialSelector, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(replaceMaterialBox)
                .addComponent(mimicBox)
                .addComponent(additiveBox)
                .addComponent(shapesBox)
                .addComponent(interpolationBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE)
            )
    );

    layout.setVerticalGroup(
        layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(nameLabel)
                .addComponent(nameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(paintLabel)
                .addComponent(paintPicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(materialLabel)
                .addComponent(mixedMaterialSelector, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(replaceLabel)
                .addComponent(replaceMaterialBox)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(mimicLabel)
                .addComponent(mimicBox)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(additiveLabel)
                .addComponent(additiveBox)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(shapesLabel)
                .addComponent(shapesBox)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(interpolationLabel)
                .addComponent(interpolationBox)
            )
    );

    model = new CustomTableModel();
    mimicTable = new JTable(model);
    mimicTable.setRowHeight(25);

    List<String> names = new ArrayList<>(
        Material.getAllSimpleNamesForNamespace(Constants.MC_NAMESPACE));
    names.sort(Collator.getInstance());
    String[] namesArray = names.toArray(new String[0]);

    mimicTable.getColumnModel().getColumn(0)
        .setCellEditor(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK));
    mimicTable.getColumnModel().getColumn(1)
        .setCellEditor(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK));
    mimicTable.getColumnModel().getColumn(0)
        .setCellRenderer(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK));
    mimicTable.getColumnModel().getColumn(1)
        .setCellRenderer(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK));

    if (enableBorders) {
      mimicTable.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
    }

    // mimicTable.setPreferredSize(new Dimension(500, mimicTable.getPreferredSize().height));
    mimicTable.getTableHeader().setReorderingAllowed(false);

    tableScrollPane = new JScrollPane(mimicTable);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

    // Add button
    addButton = new JButton();
    addButton.setIcon(
        new ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_add.png")));
    addButton.setToolTipText("Add a new row");
    addButton.setMargin(new Insets(2, 2, 2, 2));
    addButton.addActionListener(e -> model.addRow());

    // Remove button
    removeButton = new JButton();
    removeButton.setIcon(
        new ImageIcon(getClass().getResource("/org/pepsoft/worldpainter/icons/brick_delete.png")));
    removeButton.setToolTipText("Remove a row");
    removeButton.setMargin(new Insets(2, 2, 2, 2));
    removeButton.addActionListener(e -> {
      int selectedRow = mimicTable.getSelectedRow();

      if (selectedRow != -1) {
        // There is a row selected, remove it!
        model.removeRow(selectedRow);

        if (model.getRowCount() > 0) {
          mimicTable.changeSelection(model.getRowCount() - 1, 0, false, false);
          mimicTable.changeSelection(model.getRowCount() - 1, 1, false, true);
        }
      }
    });

    // Popup menu for remove button
    JPopupMenu popup = new JPopupMenu();
    JMenuItem clearItem = new JMenuItem("Clear");
    popup.add(clearItem);
    clearItem.addActionListener(e -> model.removeRows());
    removeButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          popup.show(e.getComponent(), e.getX(), e.getY());
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          popup.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });

    // Load button
    loadButton = new JButton();
    loadButton.setIcon(new ImageIcon(
        getClass().getResource("/com/gmail/frogocomics/slabify/icons/table_add.png")));
    loadButton.setToolTipText("Load a saved mapping");
    loadButton.setMargin(new Insets(2, 2, 2, 2));
    loadButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
      fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
      int result = fileChooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        loadMappings(fileChooser.getSelectedFile());
      }
    });

    // Save button
    saveButton = new JButton();
    saveButton.setIcon(new ImageIcon(
        getClass().getResource("/com/gmail/frogocomics/slabify/icons/table_save.png")));
    saveButton.setToolTipText("Save the current mapping");
    saveButton.setMargin(new Insets(2, 2, 2, 2));
    saveButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
      fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
      int result = fileChooser.showSaveDialog(this);
      if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        // If selected file does not have a .csv extention, add it
        if (!selectedFile.getName().endsWith(".csv")) {
          selectedFile = new File(selectedFile.getAbsoluteFile() + ".csv");
        }

        saveMappings(selectedFile);
      }
    });

    // Preview button
    previewButton = new JButton();
    previewButton.setIcon(
        new ImageIcon(getClass().getResource("/com/gmail/frogocomics/slabify/icons/eye.png")));
    previewButton.setToolTipText("View the mapping");
    previewButton.setMargin(new Insets(2, 2, 2, 2));
    previewButton.addActionListener(e -> openPreviewDialog());

    buttonPanel.add(addButton);
    buttonPanel.add(Box.createVerticalStrut(4));
    buttonPanel.add(removeButton);
    buttonPanel.add(Box.createVerticalStrut(4));
    buttonPanel.add(loadButton);
    buttonPanel.add(Box.createVerticalStrut(4));
    buttonPanel.add(saveButton);
    buttonPanel.add(Box.createVerticalStrut(4));
    buttonPanel.add(previewButton);

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout(5, 0));
    bottomPanel.add(tableScrollPane, BorderLayout.CENTER);
    bottomPanel.add(buttonPanel, BorderLayout.EAST);

    setLayout(new BorderLayout());

    JPanel wrapperPanel = new JPanel(new BorderLayout());
    wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    wrapperPanel.add(topPanel, BorderLayout.CENTER);
    add(wrapperPanel, BorderLayout.NORTH);
    add(bottomPanel, BorderLayout.CENTER);
  }

  public Optional<Material> getMaterial(Pair<String, String> p) {
    String blockNamespace = p.getValue0();
    String block = p.getValue1();

    if (blockNamespace == null) {
      blockNamespace = Constants.MC_NAMESPACE;
    }

    if (block == null) {
      return Optional.empty();
    }

    blockNamespace = blockNamespace.toLowerCase().replaceAll("[^a-z_]]", "");
    block = block.toLowerCase().replaceAll("[^a-z_]]", "");

    if (!block.isEmpty() && !blockNamespace.isEmpty()) {
      String materialName = blockNamespace + ":" + block;

      if (materialName.contains("slab")) {
        return Optional.of(Material.get(materialName, org.pepsoft.minecraft.Constants.MC_TYPE, "bottom"));
      } else {
        return Optional.of(Material.getPrototype(materialName));
      }
    }

    return Optional.empty();
  }


  private void settingsChanged() {
    context.settingsChanged();
  }

  private void loadMappings(File location) {
    Map<String, Material> mappings = new LinkedHashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(location))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
          String[] p1 = parts[0].split(":");
          String[] p2 = parts[1].split(":");

          if (p1.length <= 2 && p2.length <= 2) {

            String underlyingNamespace;
            String underlyingName;
            String slabNamespace;
            String slabName;

            if (p1.length == 1) {
              underlyingNamespace = Constants.MC_NAMESPACE;
              underlyingName = p1[0];
            } else {
              underlyingNamespace = p1[0];
              underlyingName = p1[1];
            }

            if (p2.length == 1) {
              slabNamespace = Constants.MC_NAMESPACE;
              slabName = p2[0];
            } else {
              slabNamespace = p2[0];
              slabName = p2[1];
            }

            Optional<Material> optSlab = getMaterial(new Pair<>(slabNamespace, slabName));

            optSlab.ifPresent(
                material -> mappings.put(underlyingNamespace + ":" + underlyingName, material));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    updateMimicTable(mappings);
  }

  private void saveMappings(File location) {
    // Note that this does not update the layer but merely outputs the current mappings to a file.
    Map<String, Material> mapping = getCurrentMapping();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(location))) {
      for (Entry<String, Material> entry : mapping.entrySet()) {
        String underlyingMaterial = entry.getKey();
        Material slabMaterial = entry.getValue();
        writer.write(underlyingMaterial + ","
            + slabMaterial.namespace + ":" + slabMaterial.simpleName);
        writer.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JPanel shapesPanel;
  private final Map<String, Map<Options, JCheckBox>> shapeSelectionMap = new HashMap<>();

  private void createShapesDialog() {
    if (shapesPanel == null) {
      shapesPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 10, 5, 10);
      gbc.fill = GridBagConstraints.BOTH;

      gbc.gridy = 0;
      gbc.gridx = 1;

      for (Options option : Options.values()) {
        shapesPanel.add(new JLabel(option.toString()), gbc);
        gbc.gridx++;
      }

      gbc.gridx = 0;
      gbc.gridy++;

      for (Shape shape : Shapes.SHAPES.values()) {
        JLabel label = new JLabel(shape.toString());

        // Bold the labels of non-vanilla shapes to indicate to the user the potential incompatibility
        if (!shape.isVanilla()) {
          label.setText(label.getText() + "*");
          label.setFont(label.getFont().deriveFont(Font.BOLD));
        }

        shapesPanel.add(label, gbc);

        Map<Options, JCheckBox> rowMap = new EnumMap<>(Options.class);
        ButtonGroup group = new ButtonGroup();

        gbc.gridx++;

        for (Options option : Options.values()) {
          if (shape.getAvailableOptions().contains(option)) {
            JCheckBox checkBox = new JCheckBox();
            group.add(checkBox);

            // This is to account for the rare occurrence a layer was saved and new shapes are created. The default
            // option would be to disable the new shape, so the checkbox corresponding to Options.DISABLE is selected.
            if (option == Options.DISABLE) {
              checkBox.setSelected(true);
            }

            shapesPanel.add(checkBox, gbc);
            rowMap.put(option, checkBox);
          } else {
            // Empty placeholder keeps grid aligned
            shapesPanel.add(Box.createHorizontalStrut(16), gbc);
          }

          gbc.gridx++;
        }

        shapeSelectionMap.put(shape.getName(), rowMap);

        gbc.gridx = 0;
        gbc.gridy++;
      }
    }
  }

  private void updateShapesDialog(Map<String, Options> shapes) {

    if (shapesPanel != null) {
      for (Entry<String, Options> entry : shapes.entrySet()) {
        String k = entry.getKey();
        Options v = entry.getValue();

        if (shapeSelectionMap.containsKey(k)) {
          Map<Options, JCheckBox> rowMap = shapeSelectionMap.get(k);

          for (Entry<Options, JCheckBox> entry2 : rowMap.entrySet()) {
            Options k2 = entry2.getKey();
            JCheckBox v2 = entry2.getValue();
            v2.setSelected(v == k2);
          }
        }
      }
    }
  }

  private void openShapesDialog() {
    JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
    JDialog dialog = new JDialog(parentFrame, "Select allowed shapes", true);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(parentFrame);

    JLabel label1 = new JLabel("Disable or set resolution for the different block shapes");
    label1.setBorder(new EmptyBorder(5, 10, 5, 10));
    dialog.add(label1, BorderLayout.NORTH);

    dialog.add(shapesPanel, BorderLayout.CENTER);

    JLabel label2 = new JLabel("* Conquest only");
    label2.setFont(label2.getFont().deriveFont(Font.BOLD));
    label2.setBorder(new EmptyBorder(5, 10, 5, 10));
    dialog.add(label2, BorderLayout.SOUTH);

    dialog.pack();
    dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    dialog.setVisible(true);
  }

  private void openPreviewDialog() {
    JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
    JDialog dialog = new JDialog(parentFrame, "Preview Window", true);
    dialog.setSize(800, 600);
    dialog.setLocationRelativeTo(parentFrame);
    DynmapPreviewer viewer = new DynmapPreviewer();
    TileFactory tileFactory = TileFactoryFactory.createNoiseTileFactory(0L, Terrain.GRASS,
        DefaultPlugin.JAVA_ANVIL_1_15.minZ, DefaultPlugin.JAVA_ANVIL_1_15.standardMaxHeight, 58, 62,
        false, true, 20, 1);
    org.pepsoft.worldpainter.Dimension dimension = (new World2(DefaultPlugin.JAVA_ANVIL_1_15, 0,
        tileFactory)).getDimension(
        Anchor.NORMAL_DETAIL);

    if (!getCurrentMapping().isEmpty()) {
      viewer.setObject(new SlabPreviewObject(getCurrentMapping()), dimension);
      dialog.add(viewer);
      dialog.setVisible(true);
    }
  }

  private static class CustomTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Underlying Block", "Slab"};
    private final List<CustomRow> rows = new ArrayList<>();

    /**
     * Add a new blank row.
     */
    public void addRow() {
      addRow(new CustomRow());
    }

    /**
     * Add multiple empty rows.
     *
     * @param n the number of rows to add.
     */
    public void addRows(int n) {
      for (int i = 0; i < n; i++) {
        addRow();
      }
    }

    /**
     * Add a row.
     *
     * @param row the row to add.
     */
    public void addRow(CustomRow row) {
      rows.add(row);
      fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    public void removeRow(int i) {
      rows.remove(i);
      fireTableRowsDeleted(i, i);
    }

    /**
     * Remove all rows.
     */
    public void removeRows() {
      int length = rows.size();

      if (length > 0) {
        rows.clear();
        fireTableRowsDeleted(0, length - 1);
      }
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      CustomRow row = rows.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return new Pair<>(row.getBlockNamespace(), row.getBlock());
        case 1:
          return new Pair<>(row.getSlabNamespace(), row.getSlab());
        default:
          throw new IllegalArgumentException("Invalid column index");
      }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
      if (rowIndex >= 0 && rowIndex < getRowCount() && columnIndex >= 0
          && columnIndex < getColumnCount()) {
        CustomRow row = rows.get(rowIndex);

        if (!(value instanceof Pair)) {
          throw new IllegalArgumentException("Input must be a pair");
        }

        @SuppressWarnings("unchecked")
        Pair<String, String> p = (Pair<String, String>) value;

        switch (columnIndex) {
          case 0:
            row.setBlockNamespace(p.getValue0());
            row.setBlock(p.getValue1());
            break;
          case 1:
            row.setSlabNamespace(p.getValue0());
            row.setSlab(p.getValue1());
            break;
          default:
            throw new IllegalArgumentException("Invalid column index");
        }

        fireTableCellUpdated(rowIndex, columnIndex);
      }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }
  }

  private static class CustomRow {

    private String block;
    private String blockNamespace;
    private String slab;
    private String slabNamespace;

    /**
     * Create an empty row.
     */
    public CustomRow() {
      this("", Constants.MC_NAMESPACE, "", Constants.MC_NAMESPACE);
    }

    public CustomRow(String block, String blockNamespace, String slab, String slabNamespace) {
      this.block = block;
      this.blockNamespace = blockNamespace;
      this.slab = slab;
      this.slabNamespace = slabNamespace;
    }

    public String getBlock() {
      return block;
    }

    public void setBlock(String block) {
      this.block = block;
    }

    public String getBlockNamespace() {
      return blockNamespace;
    }

    public void setBlockNamespace(String blockNamespace) {
      this.blockNamespace = blockNamespace;
    }

    public String getSlab() {
      return slab;
    }

    public void setSlab(String slab) {
      this.slab = slab;
    }

    public String getSlabNamespace() {
      return slabNamespace;
    }

    public void setSlabNamespace(String slabNamespace) {
      this.slabNamespace = slabNamespace;
    }
  }

  private static class CustomRendererEditor extends AbstractCellEditor implements TableCellEditor,
      TableCellRenderer {

    private final JPanel panel;
    private final JComboBox<String> namespaceField;
    private final JComboBox<String> nameField;
    private final String defaultValue;

    public CustomRendererEditor(String[] blockNames, String defaultValue) {
      this.defaultValue = defaultValue;

      panel = new JPanel(new BorderLayout());
      // panel.setBorder(new EmptyBorder(5, 5, 5, 5));

      JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));

      namespaceField = new JComboBox<>(new String[]{Constants.MC_NAMESPACE});
      namespaceField.setEditable(true);
      namespaceField.setSelectedItem(Constants.MC_NAMESPACE);
      nameField = new JComboBox<>(blockNames);
      nameField.setEditable(true);

      // Action listeners
      namespaceField.addActionListener(e -> stopCellEditing());
      nameField.addActionListener(e -> stopCellEditing());

      // Sizes
      namespaceField.setPreferredSize(new Dimension(100, namespaceField.getPreferredSize().height));
      nameField.setPreferredSize(new Dimension(100, nameField.getPreferredSize().height));

      contentPanel.add(namespaceField);
      contentPanel.add(new JLabel(":"));
      contentPanel.add(nameField);
      panel.add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {

      if (value instanceof Pair) {
        @SuppressWarnings("unchecked")
        Pair<String, String> p = (Pair<String, String>) value;
        namespaceField.setSelectedItem(
            p.getValue0() == null ? Constants.MC_NAMESPACE : p.getValue0());
        nameField.setSelectedItem(p.getValue1() == null ? defaultValue : p.getValue1());
      }

      if (isSelected) {
        panel.setBackground(table.getSelectionBackground());
      } else {
        panel.setBackground(table.getBackground());
      }

      return panel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {

      if (value instanceof Pair) {
        @SuppressWarnings("unchecked")
        Pair<String, String> p = (Pair<String, String>) value;
        namespaceField.setSelectedItem(
            p.getValue0() == null ? Constants.MC_NAMESPACE : p.getValue0());
        nameField.setSelectedItem(p.getValue1() == null ? defaultValue : p.getValue1());
      }

      if (isSelected) {
        panel.setBackground(table.getSelectionBackground());
      } else {
        panel.setBackground(table.getBackground());
      }

      return panel;
    }

    @Override
    public Object getCellEditorValue() {
      return new Pair<>(namespaceField.getSelectedItem(), nameField.getSelectedItem());
    }

    public void enable() {
      namespaceField.setEnabled(true);
      nameField.setEnabled(true);
    }

    public void disable() {
      namespaceField.setEnabled(false);
      nameField.setEnabled(false);
    }
  }
}