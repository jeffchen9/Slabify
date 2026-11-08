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

import com.gmail.frogocomics.slabify.Constants;
import com.gmail.frogocomics.slabify.layers.Slab;
import com.gmail.frogocomics.slabify.layers.Slab.Interpolation;
import com.gmail.frogocomics.slabify.layers.SlabCustomLayerSettings;
import com.gmail.frogocomics.slabify.shape.HeadShape;
import com.gmail.frogocomics.slabify.shape.Shape;
import com.gmail.frogocomics.slabify.shape.Shape.Options;
import com.gmail.frogocomics.slabify.shape.Shapes;
import com.gmail.frogocomics.slabify.utils.Utils;
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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
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
  private final Map<String, Map<Options, JRadioButton>> shapeSelectionMap = new HashMap<>();
  // Material
  private JLabel materialLabel;
  private MixedMaterialChooser mixedMaterialSelector;
  // Replace non-solid blocks
  private JLabel replaceLabel;
  private JCheckBox replaceMaterialBox;
  // Shape
  private JLabel shapesLabel;
  private JButton shapesBox;
  private JPanel shapesRow;
  private JLabel shapesSummaryLabel;
  // Interpolation method
  private JLabel interpolationLabel;
  private JComboBox<Interpolation> interpolationBox;
  // Mimic underlying blocks
  private JLabel mimicLabel;
  private JCheckBox mimicBox;
  // Additive
  private JLabel additiveLabel;
  private JSpinner additiveSpinner;
  // Conquest
  private JLabel conquestLabel;
  private JCheckBox conquestBox;
  // Block mapping table
  private JTable mimicTable;
  private JScrollPane tableScrollPane;
  private JComponent grips;
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
  // Shapes
  private JPanel shapesPanel;
  private JCheckBox stackingCheckBox;
  // Shapes dialog status
  private JLabel resolutionLabel;
  private JLabel conquestWarningLabel;
  // Frame
  private JFrame frame;
  // Divider, and the editor width it sets (-1 until dragged)
  private JPanel divider;
  private int editorWidth = -1;
  // Dragged row, or -1
  private int draggedRow = -1;

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
      settingsChanged();
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

    // Keep the preview up to date with the settings as they are edited
    replaceMaterialBox.addItemListener(e -> settingsChanged());
    additiveSpinner.addChangeListener(e -> settingsChanged());
    interpolationBox.addActionListener(e -> settingsChanged());
    model.addTableModelListener(e -> settingsChanged());
    // The shapes summary also warns about shapes that need Conquest blocks
    conquestBox.addItemListener(e -> shapesChanged());
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
    additiveSpinner.setValue(layer.getHeight());
    conquestBox.setSelected(layer.allowConquest());
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
        String[] underlyingMaterial = Utils.withNamespace(entry.getKey()).split(":");

        if (underlyingMaterial.length == 1) {
          continue;
        }

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
    // The edited layer only changes on commit, so the preview gets a copy with the editor's settings
    Slab previewLayer = new Slab(nameField.getText(), layer.getMaterial());
    // Set afterwards, as the constructor registers its material, which may only happen on commit
    previewLayer.setMaterial(mixedMaterialSelector.getMaterial());
    saveSettings(previewLayer);
    return new SlabCustomLayerSettings(previewLayer);
  }

  @Override
  public boolean isCommitAvailable() {
    // WorldPainter's export treats same-named slab layers as equal, so only one of them would be exported
    return context.getAllLayers().stream().noneMatch(l -> !l.equals(layer) && l.getName().equals(nameField.getText()));
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
    layer.setHeight(((Number) additiveSpinner.getValue()).floatValue());
    layer.setAllowConquest(conquestBox.isSelected());
    layer.setMapping(getCurrentMapping());

    // Get from shapes dialog
    Map<String, Options> newShapes = new HashMap<>();
    for (Shape shape : Shapes.shapesList) {
      Options option = getSelectedOption(shape);

      if (option != null) {
        newShapes.put(shape.getName(), option);
      }
    }

    layer.setShapes(newShapes);
    layer.setStacking(stackingCheckBox.isSelected());
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

      Optional<Material> o1 = getMaterial(p1);
      Optional<Material> o2 = getMaterial(p2);

      // Key: underlying block, cleaned so that "Stone" matches the block in the world
      // Value: slab
      if (o1.isPresent() && o2.isPresent()) {
        mapping.put(o1.get().name, o2.get());
      }
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

    additiveLabel = new JLabel("Height increase:");
    additiveLabel.setToolTipText("Increase the height by this number");
    SpinnerNumberModel numModel = new SpinnerNumberModel(0, 0, 1.5, 0.1);
    additiveSpinner = new JSpinner(numModel);
    JSpinner.NumberEditor numEditor = new JSpinner.NumberEditor(additiveSpinner, "0.0");
    additiveSpinner.setEditor(numEditor);

    conquestLabel = new JLabel("Use Conquest blocks:");
    conquestLabel.setToolTipText("Checking this allows the use of blocks in the conquest namespace");
    conquestBox = new JCheckBox();

    shapesLabel = new JLabel("Allowed shapes:");
    shapesLabel.setToolTipText("Set allowed block shapes");
    shapesBox = new JButton("Edit");
    shapesSummaryLabel = new JLabel();
    shapesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    shapesRow.add(shapesBox);
    shapesRow.add(Box.createHorizontalStrut(6));
    shapesRow.add(shapesSummaryLabel);
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
                .addComponent(conquestLabel)
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
                .addComponent(additiveSpinner)
                .addComponent(conquestBox)
                .addComponent(shapesRow)
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
                .addComponent(additiveSpinner)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(conquestLabel)
                .addComponent(conquestBox)
            )
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(shapesLabel)
                .addComponent(shapesRow)
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
        .setCellEditor(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK, false));
    mimicTable.getColumnModel().getColumn(1)
        .setCellEditor(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK, true));
    mimicTable.getColumnModel().getColumn(0)
        .setCellRenderer(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK, false));
    mimicTable.getColumnModel().getColumn(1)
        .setCellRenderer(new CustomRendererEditor(namesArray, Constants.DEFAULT_BLOCK, true));
    mimicTable.getTableHeader().setReorderingAllowed(false);

    // Rows are reordered by dragging them by their grips into a gap between rows
    mimicTable.setDropMode(DropMode.INSERT_ROWS);
    mimicTable.setTransferHandler(new TransferHandler() {
      @Override
      public int getSourceActions(JComponent c) {
        return MOVE;
      }

      @Override
      protected Transferable createTransferable(JComponent c) {
        // The row is moved by its index, so a drag carries nothing, and cutting or copying transfers nothing
        return draggedRow == -1 ? null : new StringSelection("");
      }

      @Override
      public boolean canImport(TransferSupport support) {
        // Only rows dragged from this table can be dropped
        return support.isDrop() && draggedRow != -1;
      }

      @Override
      public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
          return false;
        }

        int row = model.moveRow(draggedRow, ((JTable.DropLocation) support.getDropLocation()).getRow());
        mimicTable.setRowSelectionInterval(row, row);
        return true;
      }

      @Override
      protected void exportDone(JComponent source, Transferable data, int action) {
        draggedRow = -1;
      }
    });

    tableScrollPane = new JScrollPane(mimicTable);

    // Grips down the side of the table, one for each row
    grips = new JComponent() {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension(18, mimicTable.getPreferredSize().height);
      }

      @Override
      protected void paintComponent(Graphics g) {
        g.setColor(mimicTable.isEnabled() ? mimicTable.getForeground() : UIManager.getColor("Label.disabledForeground"));

        for (int row = 0; row < mimicTable.getRowCount(); row++) {
          Rectangle cell = mimicTable.getCellRect(row, 0, true);
          int middle = cell.y + cell.height / 2;

          // Two columns of three dots
          for (int y = middle - 7; y <= middle + 3; y += 5) {
            g.fillRect(5, y, 3, 3);
            g.fillRect(10, y, 3, 3);
          }
        }
      }
    };
    grips.setToolTipText("Drag to reorder");
    MouseAdapter gripListener = new MouseAdapter() {
      private int pressedRow = -1;

      @Override
      public void mousePressed(MouseEvent e) {
        pressedRow = mimicTable.isEnabled() && SwingUtilities.isLeftMouseButton(e)
            ? mimicTable.rowAtPoint(e.getPoint()) : -1;

        if (pressedRow != -1) {
          // The editor would otherwise stay on the row that takes this one's place
          stopEditing();
          // Ctrl and Shift select several rows, as in other lists
          mimicTable.changeSelection(pressedRow, 0, e.isControlDown(), e.isShiftDown());
          // For the keyboard shortcuts
          mimicTable.requestFocusInWindow();
        }
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (pressedRow != -1) {
          draggedRow = pressedRow;
          pressedRow = -1;
          mimicTable.getTransferHandler().exportAsDrag(mimicTable,
              SwingUtilities.convertMouseEvent(grips, e, mimicTable), TransferHandler.MOVE);
        }
      }
    };
    grips.addMouseListener(gripListener);
    grips.addMouseMotionListener(gripListener);
    mimicTable.addPropertyChangeListener("enabled", e -> {
      grips.setCursor(mimicTable.isEnabled() ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR) : null);
      grips.repaint();
    });
    // Repainted too, as the viewport stretches the grips to its height, so a new row may not resize them
    model.addTableModelListener(e -> {
      grips.revalidate();
      grips.repaint();
    });
    tableScrollPane.setRowHeaderView(grips);

    // Operations on rows, from a context menu and keyboard shortcuts
    Action insertAboveAction = rowAction("Insert row above", null, () -> {
      int[] selected = mimicTable.getSelectedRows();
      insertRow(selected.length == 0 ? 0 : selected[0]);
    });
    Action insertBelowAction = rowAction("Insert row below", KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), () -> {
      int[] selected = mimicTable.getSelectedRows();
      insertRow(selected.length == 0 ? model.getRowCount() : selected[selected.length - 1] + 1);
    });
    Action duplicateAction = rowAction("Duplicate",
        KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), this::duplicateSelectedRows);
    Action deleteAction = rowAction("Delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), this::deleteSelectedRows);
    Action moveUpAction = rowAction("Move up", KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
        () -> moveSelectedRows(-1));
    Action moveDownAction = rowAction("Move down", KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
        () -> moveSelectedRows(1));
    Action clearAction = rowAction("Clear all...", null, this::clearRows);

    Runnable updateRowActions = () -> {
      int[] selected = mimicTable.getSelectedRows();
      boolean any = selected.length > 0;
      duplicateAction.setEnabled(any);
      deleteAction.setEnabled(any);
      moveUpAction.setEnabled(any && selected[0] > 0);
      moveDownAction.setEnabled(any && selected[selected.length - 1] < model.getRowCount() - 1);
      clearAction.setEnabled(model.getRowCount() > 0);
    };
    mimicTable.getSelectionModel().addListSelectionListener(e -> updateRowActions.run());
    model.addTableModelListener(e -> updateRowActions.run());
    updateRowActions.run();

    JPopupMenu rowMenu = new JPopupMenu();
    rowMenu.add(insertAboveAction);
    rowMenu.add(insertBelowAction);
    rowMenu.add(duplicateAction);
    rowMenu.add(deleteAction);
    rowMenu.addSeparator();
    rowMenu.add(moveUpAction);
    rowMenu.add(moveDownAction);
    rowMenu.addSeparator();
    rowMenu.add(clearAction);

    MouseAdapter rowMenuListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        showRowMenu(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        showRowMenu(e);
      }

      private void showRowMenu(MouseEvent e) {
        // Which of pressing and releasing opens a context menu depends on the platform
        if (!e.isPopupTrigger() || !mimicTable.isEnabled()) {
          return;
        }

        int row = mimicTable.rowAtPoint(SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), mimicTable));

        // A row that is not selected is selected instead, so that the menu acts on the row that was clicked
        if (row != -1 && !mimicTable.isRowSelected(row)) {
          stopEditing();
          mimicTable.setRowSelectionInterval(row, row);
        }

        rowMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    };
    mimicTable.addMouseListener(rowMenuListener);
    grips.addMouseListener(rowMenuListener);
    // The space below the rows
    tableScrollPane.getViewport().addMouseListener(rowMenuListener);

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
    removeButton.setToolTipText("Remove the selected rows");
    removeButton.setMargin(new Insets(2, 2, 2, 2));
    // The same as Delete in the context menu
    removeButton.addActionListener(deleteAction);

    // Popup menu for remove button
    JPopupMenu popup = new JPopupMenu();
    popup.add(clearAction);
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

    JPanel wrapperPanel = new JPanel(new BorderLayout());
    wrapperPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    wrapperPanel.add(topPanel, BorderLayout.CENTER);

    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.add(wrapperPanel, BorderLayout.NORTH);
    contentPanel.add(bottomPanel, BorderLayout.CENTER);

    // The divider spans the full height of the editor, beside everything else
    setLayout(new BorderLayout());
    createDivider();
    add(contentPanel, BorderLayout.CENTER);
    add(divider, BorderLayout.EAST);
  }

  // WorldPainter lays the editor out at its preferred width beside the preview, which dragging the divider sets
  private void createDivider() {
    divider = new JPanel(new BorderLayout());
    // Wider than the line, to make it easier to grab
    divider.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    divider.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER);
    divider.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
    divider.setToolTipText("Drag to resize");

    MouseAdapter dragHandler = new MouseAdapter() {
      private int startX;
      private int startWidth;

      @Override
      public void mousePressed(MouseEvent e) {
        // Screen coordinates, since the divider moves while it is dragged
        startX = e.getXOnScreen();
        startWidth = SlabCustomLayerEditor.this.getWidth();
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        setEditorWidth(startWidth + e.getXOnScreen() - startX);
      }
    };

    divider.addMouseListener(dragHandler);
    divider.addMouseMotionListener(dragHandler);
  }

  private void setEditorWidth(int width) {
    // Leave the rest of the dialog the minimum width that the preview and the gaps around it need
    Container contentPane = getRootPane().getContentPane();
    int maxWidth = contentPane.getWidth() - (contentPane.getMinimumSize().width - getPreferredSize().width);

    editorWidth = Math.max(getMinimumSize().width, Math.min(maxWidth, width));
    revalidate();
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    return editorWidth < 0 ? size : new Dimension(editorWidth, size.height);
  }

  private Action rowAction(String name, KeyStroke keyStroke, Runnable operation) {
    Action action = new AbstractAction(name) {
      @Override
      public void actionPerformed(ActionEvent e) {
        // Keys come from the table, which keeps the focus when typing opens a cell; they are then meant for the cell
        if (!mimicTable.isEnabled() || (e.getSource() == mimicTable && mimicTable.isEditing())) {
          return;
        }

        // The edited row may move
        stopEditing();
        operation.run();
      }
    };

    if (keyStroke != null) {
      action.putValue(Action.ACCELERATOR_KEY, keyStroke);
      // Only while the table itself has the focus, so that the keys still work as usual in a cell being edited
      mimicTable.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, name);
      mimicTable.getActionMap().put(name, action);
    }

    return action;
  }

  private void stopEditing() {
    if (mimicTable.isEditing()) {
      mimicTable.getCellEditor().stopCellEditing();
    }
  }

  private void insertRow(int index) {
    model.insertRow(index, new CustomRow());
    selectRows(index, index);
  }

  private void duplicateSelectedRows() {
    int[] selected = mimicTable.getSelectedRows();

    if (selected.length == 0) {
      return;
    }

    // The selected rows are all above the copies, so they keep their indices as the copies are inserted
    int index = selected[selected.length - 1] + 1;

    for (int i = 0; i < selected.length; i++) {
      model.insertRow(index + i, model.copyRow(selected[i]));
    }

    selectRows(index, index + selected.length - 1);
  }

  private void deleteSelectedRows() {
    int[] selected = mimicTable.getSelectedRows();

    if (selected.length == 0) {
      return;
    }

    // From the last, so that the rows still to be removed keep their indices
    for (int i = selected.length - 1; i >= 0; i--) {
      model.removeRow(selected[i]);
    }

    int index = Math.min(selected[0], model.getRowCount() - 1);

    if (index >= 0) {
      selectRows(index, index);
    }
  }

  // -1 moves up and 1 moves down; nothing moves if a selected row is already at that end
  private void moveSelectedRows(int direction) {
    int[] selected = mimicTable.getSelectedRows();

    if (selected.length == 0 || selected[0] + direction < 0
        || selected[selected.length - 1] + direction >= model.getRowCount()) {
      return;
    }

    // Starting from the end that they move towards, so that each row moves past a row that is not selected
    for (int i = 0; i < selected.length; i++) {
      int row = selected[direction < 0 ? i : selected.length - 1 - i];
      // The gap to move into is above the row before, or below the row after
      model.moveRow(row, direction < 0 ? row - 1 : row + 2);
    }

    mimicTable.clearSelection();

    for (int row : selected) {
      mimicTable.addRowSelectionInterval(row + direction, row + direction);
    }

    int end = selected[direction < 0 ? 0 : selected.length - 1] + direction;
    mimicTable.scrollRectToVisible(mimicTable.getCellRect(end, 0, true));
  }

  private void clearRows() {
    if (JOptionPane.showConfirmDialog(this, "Remove all rows of the block mapping?", "Clear All",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
      model.removeRows();
    }
  }

  private void selectRows(int first, int last) {
    mimicTable.setRowSelectionInterval(first, last);
    mimicTable.scrollRectToVisible(mimicTable.getCellRect(first, 0, true));
  }

  /**
   * Get the material for a block. Characters that are not allowed in block names are removed, and blocks with "slab"
   * in their name are made bottom slabs.
   *
   * @param p the namespace and name of the block. A {@code null} namespace is treated as Minecraft's.
   * @return the material, or nothing if the name is {@code null} or either part is empty after cleaning.
   */
  public Optional<Material> getMaterial(Pair<String, String> p) {
    String blockNamespace = p.getValue0();
    String block = p.getValue1();

    if (blockNamespace == null) {
      blockNamespace = Constants.MC_NAMESPACE;
    }

    if (block == null) {
      return Optional.empty();
    }

    blockNamespace = Utils.cleanId(blockNamespace);
    block = Utils.cleanId(block);

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
    // Controls can change while the editor is set up, before it has a context
    if (context != null) {
      context.settingsChanged();
    }
  }

  private void loadMappings(File location) {
    Map<String, Material> mappings = new LinkedHashMap<>();
    try {
      for (String[] parts : Utils.readCsv(location)) {
        if (parts.length == 2) {
          String underlying = Utils.withNamespace(parts[0]);
          String[] slab = Utils.withNamespace(parts[1]).split(":");

          if (underlying.split(":").length == 2 && slab.length == 2) {
            getMaterial(new Pair<>(slab[0], slab[1])).ifPresent(material -> mappings.put(underlying, material));
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

  private void createShapesDialog() {
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

    for (Shape shape : Shapes.shapesList) {
      JLabel label = new JLabel(shape.toString());

      // Bold the labels of non-vanilla shapes to indicate to the user the potential incompatibility
      if (!shape.isVanilla()) {
        label.setText(label.getText() + "*");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
      }

      shapesPanel.add(label, gbc);

      Map<Options, JRadioButton> rowMap = new EnumMap<>(Options.class);
      ButtonGroup group = new ButtonGroup();

      gbc.gridx++;

      for (Options option : Options.values()) {
        if (shape.getAvailableOptions().contains(option)) {
          JRadioButton button = new JRadioButton();
          group.add(button);

          // This is to account for the rare occurrence a layer was saved and new shapes are created. The default
          // option would be to disable the new shape, so the button corresponding to Options.DISABLE is selected.
          if (option == Options.DISABLE) {
            button.setSelected(true);
          }

          button.addItemListener(e -> shapesChanged());
          shapesPanel.add(button, gbc);
          rowMap.put(option, button);
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

    stackingCheckBox = new JCheckBox();
    stackingCheckBox.addItemListener(e -> shapesChanged());

    resolutionLabel = new JLabel();
    conquestWarningLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
    conquestWarningLabel.setBorder(new EmptyBorder(5, 0, 0, 0));
    conquestWarningLabel.setVisible(false);
  }

  private void updateShapesDialog(Map<String, Options> shapes) {
    for (Entry<String, Options> entry : shapes.entrySet()) {
      String k = entry.getKey();
      Options v = entry.getValue();

      if (shapeSelectionMap.containsKey(k)) {
        Map<Options, JRadioButton> rowMap = shapeSelectionMap.get(k);

        for (Entry<Options, JRadioButton> entry2 : rowMap.entrySet()) {
          Options k2 = entry2.getKey();
          JRadioButton v2 = entry2.getValue();
          v2.setSelected(v == k2);
        }
      }
    }

    stackingCheckBox.setSelected(layer.supportsStacking());
    // In case no selection changed, which would have updated it
    updateShapesSummary();
  }

  private void enableAllShapes(int maxResolution) {
    for (Shape shape : Shapes.shapesList) {
      Options finest = Options.DISABLE;

      // Heads are better chosen by hand
      if (!(shape instanceof HeadShape)) {
        for (Options option : shape.getAvailableOptions()) {
          int resolution = shape.getMinResolution(option);

          if (option != Options.DISABLE && resolution <= maxResolution
              && (finest == Options.DISABLE || resolution > shape.getMinResolution(finest))) {
            finest = option;
          }
        }
      }

      selectShapeOption(shape, finest);
    }
  }

  private void resetShapes() {
    for (Shape shape : Shapes.shapesList) {
      if (shape.getDefaultOption() != null) {
        selectShapeOption(shape, shape.getDefaultOption());
      }
    }
  }

  private void selectShapeOption(Shape shape, Options option) {
    JRadioButton button = shapeSelectionMap.get(shape.getName()).get(option);

    if (button != null) {
      button.setSelected(true);
    }
  }

  private Options getSelectedOption(Shape shape) {
    for (Entry<Options, JRadioButton> entry : shapeSelectionMap.get(shape.getName()).entrySet()) {
      if (entry.getValue().isSelected()) {
        return entry.getKey();
      }
    }

    return null;
  }

  private void shapesChanged() {
    updateShapesSummary();
    settingsChanged();
  }

  // The resolution is worked out as the exporter does
  private void updateShapesSummary() {
    int count = 0;
    int resolution = 1;
    boolean conquestShapes = false;

    for (Shape shape : Shapes.shapesList) {
      Options option = getSelectedOption(shape);

      if (option != null && option != Options.DISABLE) {
        count++;
        resolution = Math.max(resolution, shape.getMinResolution(option));
        conquestShapes |= !shape.isVanilla();
      }
    }

    resolutionLabel.setText("Export resolution: " + (count == 0 ? "none, as no shapes are enabled" : resolution + "×"));
    String summary = count == 0 ? "No shapes" : count + (count == 1 ? " shape, " : " shapes, ") + resolution + "×";

    // Without Conquest blocks the exporter skips Conquest shapes and stacking, but still counts them in the resolution
    List<String> problems = new ArrayList<>();

    if (!conquestBox.isSelected()) {
      if (conquestShapes) {
        problems.add("no Conquest blocks are placed, but Conquest shapes still raise the export resolution");
      }

      if (stackingCheckBox.isSelected()) {
        problems.add("shapes are not stacked");
      }
    }

    String warning = null;

    if (!problems.isEmpty()) {
      // A set width makes the text wrap, as the dialog is only as wide as the shapes
      warning = "<html><body style='width: 250px'>\"Use Conquest blocks\" is off, so:<br>&bull; "
          + String.join("<br>&bull; ", problems) + "</body></html>";
      summary += "; Conquest is off";
    }

    shapesSummaryLabel.setText(summary);
    shapesSummaryLabel.setToolTipText(warning);
    conquestWarningLabel.setText(warning);

    if (conquestWarningLabel.isVisible() != (warning != null)) {
      conquestWarningLabel.setVisible(warning != null);
      // The dialog cannot be resized, so it is fitted to the warning
      Window window = SwingUtilities.getWindowAncestor(conquestWarningLabel);

      if (window != null && window.isShowing()) {
        window.pack();
      }
    }
  }

  private JButton createEnableAllButton(Options option, int resolution) {
    JButton button = new JButton(option.toString());
    button.setToolTipText("Enable every shape but Head, each at its finest option up to "
        + option.toString().toLowerCase() + ", for an export at " + resolution + "× resolution");
    button.addActionListener(e -> enableAllShapes(resolution));
    return button;
  }

  private void openShapesDialog() {
    JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
    JDialog dialog = new JDialog(parentFrame, "Select allowed shapes", true);
    dialog.setResizable(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setLocationRelativeTo(parentFrame);

    JLabel label1 = new JLabel("Disable or set resolution for the different block shapes");
    label1.setBorder(new EmptyBorder(5, 10, 5, 10));
    dialog.add(label1, BorderLayout.NORTH);

    dialog.add(shapesPanel, BorderLayout.CENTER);

    JPanel southPanel = new JPanel();
    southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
    southPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

    // Presets
    JSeparator separator = new JSeparator();
    separator.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel presetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    presetRow.add(new JLabel("Enable all, up to:"));
    presetRow.add(createEnableAllButton(Options.EIGHTHS, 8));
    presetRow.add(createEnableAllButton(Options.QUARTERS, 4));
    presetRow.add(createEnableAllButton(Options.HALVES, 2));
    presetRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JButton resetButton = new JButton("Reset to defaults");
    resetButton.setToolTipText("Select the shapes that a new layer starts with");
    resetButton.addActionListener(e -> resetShapes());
    JPanel resetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    resetRow.add(resetButton);
    resetRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel checkboxRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    JLabel label3 = new JLabel("Enable Conquest shape stacking (slow)*");
    label3.setFont(label3.getFont().deriveFont(Font.BOLD));
    checkboxRow.add(label3);
    checkboxRow.add(stackingCheckBox);
    checkboxRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel label2 = new JLabel("* Conquest only");
    label2.setFont(label2.getFont().deriveFont(Font.BOLD));

    southPanel.add(separator);
    southPanel.add(Box.createVerticalStrut(5));
    southPanel.add(presetRow);
    southPanel.add(Box.createVerticalStrut(5));
    southPanel.add(resetRow);
    southPanel.add(Box.createVerticalStrut(5));
    southPanel.add(checkboxRow);
    southPanel.add(Box.createVerticalStrut(5));
    southPanel.add(resolutionLabel);
    southPanel.add(conquestWarningLabel);
    southPanel.add(Box.createVerticalStrut(5));
    southPanel.add(label2);

    dialog.add(southPanel, BorderLayout.SOUTH);

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

    /**
     * Insert a row.
     *
     * @param index the index of the new row.
     * @param row   the row to insert.
     */
    public void insertRow(int index, CustomRow row) {
      rows.add(index, row);
      fireTableRowsInserted(index, index);
    }

    /**
     * Get a copy of a row.
     *
     * @param i the index of the row.
     * @return the copy.
     */
    public CustomRow copyRow(int i) {
      CustomRow row = rows.get(i);
      return new CustomRow(row.getBlock(), row.getBlockNamespace(), row.getSlab(), row.getSlabNamespace());
    }

    /**
     * Remove a row.
     *
     * @param i the index of the row to remove.
     */
    public void removeRow(int i) {
      rows.remove(i);
      fireTableRowsDeleted(i, i);
    }

    /**
     * Move a row.
     *
     * @param from the index of the row to move.
     * @param to   the index of the gap to move it to, from 0 (above the first row) to the number of rows (below the
     *             last row).
     * @return the index of the row after the move.
     */
    public int moveRow(int from, int to) {
      // Taking the row out moves the gaps below it up by one
      int index = to > from ? to - 1 : to;

      if (index != from) {
        rows.add(index, rows.remove(from));
        fireTableRowsUpdated(Math.min(from, index), Math.max(from, index));
      }

      return index;
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
          case 0: {
            boolean changed = !Objects.equals(p.getValue0(), row.getBlockNamespace())
                || !Objects.equals(p.getValue1(), row.getBlock());
            row.setBlockNamespace(p.getValue0());
            row.setBlock(p.getValue1());

            // Only when the block changes, so that a slab cleared on purpose stays empty
            if (changed && fillSlab(row)) {
              // Both cells of the row changed
              fireTableRowsUpdated(rowIndex, rowIndex);
              return;
            }
            break;
          }
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

    // Only blocks in the block mapping are filled in, so nothing is filled in for a misspelt block
    private static boolean fillSlab(CustomRow row) {
      String slab = row.getSlab();
      String block = row.getBlock();

      if ((slab != null && !slab.trim().isEmpty()) || block == null || block.isEmpty()) {
        return false;
      }

      String namespace = row.getBlockNamespace() == null ? Constants.MC_NAMESPACE : row.getBlockNamespace();

      if (!Shapes.isAvailable(namespace + ":" + block)) {
        return false;
      }

      row.setSlabNamespace(namespace);
      row.setSlab(block);
      return true;
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

    /**
     * Create a new row.
     *
     * @param block          the block name.
     * @param blockNamespace the block namespace.
     * @param slab           the slab name.
     * @param slabNamespace  the slab namespace.
     */
    public CustomRow(String block, String blockNamespace, String slab, String slabNamespace) {
      this.block = block;
      this.blockNamespace = blockNamespace;
      this.slab = slab;
      this.slabNamespace = slabNamespace;
    }

    /**
     * Get the block name.
     *
     * @return the block name.
     */
    public String getBlock() {
      return block;
    }

    /**
     * Set the block name.
     *
     * @param block the block name.
     */
    public void setBlock(String block) {
      this.block = block;
    }

    /**
     * Get the block namespace.
     *
     * @return the block namespace.
     */
    public String getBlockNamespace() {
      return blockNamespace;
    }

    /**
     * Set the block namespace.
     *
     * @param blockNamespace the block namespace.
     */
    public void setBlockNamespace(String blockNamespace) {
      this.blockNamespace = blockNamespace;
    }

    /**
     * Get the slab name.
     *
     * @return the slab name.
     */
    public String getSlab() {
      return slab;
    }

    /**
     * Set the slab name.
     *
     * @param slab the slab name.
     */
    public void setSlab(String slab) {
      this.slab = slab;
    }

    /**
     * Get the slab namespace.
     *
     * @return the slab namespace.
     */
    public String getSlabNamespace() {
      return slabNamespace;
    }

    /**
     * Set the slab namespace.
     *
     * @param slabNamespace the slab namespace.
     */
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
    private final boolean slab;
    // Name field tint
    private final Color normalBackground;
    private final boolean normalOpaque;
    private final Color invalidBackground;
    // Why the name field is tinted, or null
    private String problem;

    /**
     * Create a new renderer and editor.
     *
     * @param blockNames   the block names to suggest.
     * @param defaultValue the block name to show when a cell has none.
     * @param slab         {@code true} if the cells are slabs, whose blocks must be in the block mapping.
     */
    public CustomRendererEditor(String[] blockNames, String defaultValue, boolean slab) {
      this.defaultValue = defaultValue;
      this.slab = slab;

      // Over a cell that is not being edited, the table shows the tooltip of the renderer's component
      panel = new JPanel(new BorderLayout()) {
        @Override
        public String getToolTipText(MouseEvent event) {
          return problem;
        }
      };
      // panel.setBorder(new EmptyBorder(5, 5, 5, 5));

      JPanel contentPanel = new JPanel(new GridBagLayout());

      namespaceField = new JComboBox<>(new String[]{Constants.MC_NAMESPACE, Constants.CQ_NAMESPACE});
      namespaceField.setEditable(true);
      namespaceField.setSelectedItem(Constants.MC_NAMESPACE);
      nameField = new JComboBox<String>(blockNames) {
        @Override
        protected void paintComponent(Graphics g) {
          super.paintComponent(g);

          // Some looks leave a gap between the text field and the arrow button, which is tinted with the text field
          if (problem != null) {
            Component text = getEditor().getEditorComponent();
            int x = text.getX() + text.getWidth();

            for (Component c : getComponents()) {
              if (c instanceof JButton && c.getX() > x) {
                g.setColor(invalidBackground);
                g.fillRect(x, text.getY(), c.getX() - x, text.getHeight());
              }
            }
          }
        }
      };
      nameField.setEditable(true);

      // Action listeners
      namespaceField.addActionListener(e -> stopCellEditing());
      nameField.addActionListener(e -> stopCellEditing());

      // Tint the name field whenever the text of either field changes, whether typed or set for a cell
      JTextField nameText = textField(nameField);
      normalBackground = nameText.getBackground();
      normalOpaque = nameText.isOpaque();
      // A fifth of the way to red: light red in a light theme and dark red in a dark one, so the text stays readable
      invalidBackground = new Color((normalBackground.getRed() * 4 + 255) / 5, normalBackground.getGreen() * 4 / 5,
          normalBackground.getBlue() * 4 / 5);
      DocumentListener tintUpdater = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          updateTint();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          updateTint();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          updateTint();
        }
      };
      textField(namespaceField).getDocument().addDocumentListener(tintUpdater);
      nameText.getDocument().addDocumentListener(tintUpdater);
      updateTint();

      for (JComboBox<String> field : List.of(namespaceField, nameField)) {
        Dimension size = new Dimension(0, field.getPreferredSize().height);
        field.setPreferredSize(size);
        field.setMinimumSize(size);
      }

      // As the fields can be narrow, let their lists be as wide as their items
      widenPopup(namespaceField);
      widenPopup(nameField);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.insets = new Insets(0, 2, 0, 0);
      gbc.weightx = 1;
      contentPanel.add(namespaceField, gbc);
      gbc.weightx = 0;
      contentPanel.add(new JLabel(":"), gbc);
      gbc.weightx = 1;
      gbc.insets = new Insets(0, 2, 0, 2);
      contentPanel.add(nameField, gbc);
      panel.add(contentPanel, BorderLayout.CENTER);
    }

    private static void widenPopup(JComboBox<?> comboBox) {
      comboBox.addPopupMenuListener(new PopupMenuListener() {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          // The popup sizes its list to the combo box after this event, so it is widened once it is showing
          SwingUtilities.invokeLater(() -> {
            Object child = comboBox.getAccessibleContext().getAccessibleChild(0);

            if (!(child instanceof BasicComboPopup) || !((BasicComboPopup) child).isVisible()) {
              return;
            }

            BasicComboPopup popup = (BasicComboPopup) child;
            JList<?> list = popup.getList();
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, list);

            if (scrollPane == null) {
              return;
            }

            Insets insets = scrollPane.getInsets();
            int width = list.getPreferredSize().width + scrollPane.getVerticalScrollBar().getPreferredSize().width
                + insets.left + insets.right;
            Dimension size = scrollPane.getPreferredSize();

            if (width > size.width) {
              size.width = width;
              scrollPane.setPreferredSize(size);
              scrollPane.setMaximumSize(size);
              // Clears the size cached by the popup, without which pack() would see no change
              scrollPane.revalidate();
              popup.pack();
            }
          });
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
      });
    }

    // Setting the text puts the caret at its end, which scrolls a narrow field to the end of its text
    private void showStart() {
      for (JComboBox<String> field : List.of(namespaceField, nameField)) {
        textField(field).setScrollOffset(0);
      }
    }

    // Tinted when the name is empty or, for a slab, not in the block mapping and so without shapes to export
    private void updateTint() {
      JTextField nameText = textField(nameField);
      String block = Utils.cleanId(nameText.getText());
      String newProblem = null;

      if (block.isEmpty()) {
        newProblem = "No block is entered";
      } else if (slab) {
        // Cleaned as when the mapping is saved, so that only blocks that would be left out are tinted
        String namespace = Utils.cleanId(textField(namespaceField).getText());
        String id = (namespace.isEmpty() ? Constants.MC_NAMESPACE : namespace) + ":" + block;

        if (!Shapes.isAvailable(id)) {
          newProblem = id + " is not in mappings.csv, so it has no shapes to place";
        }
      }

      boolean invalid = newProblem != null;
      // Some looks leave the field see-through to show the combo box behind it, so it is made opaque to show the tint
      nameText.setOpaque(invalid || normalOpaque);
      nameText.setBackground(invalid ? invalidBackground : normalBackground);

      if (!Objects.equals(problem, newProblem)) {
        boolean wasInvalid = problem != null;
        problem = newProblem;
        // Shown over the field while the cell is being edited
        nameText.setToolTipText(problem);

        if (wasInvalid != invalid) {
          // For the gap beside the arrow button, which the combo box paints
          nameField.repaint();
        }
      }
    }

    private static JTextField textField(JComboBox<?> field) {
      return (JTextField) field.getEditor().getEditorComponent();
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
        showStart();
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
        showStart();
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
      Object namespace = namespaceField.getSelectedItem();

      // An emptied namespace means Minecraft's
      if (namespace == null || namespace.toString().trim().isEmpty()) {
        namespace = Constants.MC_NAMESPACE;
      }

      return new Pair<>(namespace, nameField.getSelectedItem());
    }

    /**
     * Enable the namespace and name fields.
     */
    public void enable() {
      namespaceField.setEnabled(true);
      nameField.setEnabled(true);
    }

    /**
     * Disable the namespace and name fields.
     */
    public void disable() {
      namespaceField.setEnabled(false);
      nameField.setEnabled(false);
    }
  }
}