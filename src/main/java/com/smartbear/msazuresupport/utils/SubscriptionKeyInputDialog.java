package com.smartbear.msazuresupport.utils;

import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.smartbear.msazuresupport.Strings;
import com.smartbear.msazuresupport.entities.ApiInfo;
import com.smartbear.msazuresupport.entities.Subscription;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.util.List;

public class SubscriptionKeyInputDialog implements AutoCloseable {
    private final List<ApiInfo> apis;
    private final List<Subscription> subscriptions;
    private final XFormDialog dialog;
    private JTable apiTable;

    private class ApiInfoTableModel implements TableModel {

        @Override
        public int getRowCount() {
            return apis.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Strings.SubscriptionKeyDialog.NAME_COLUMN;
            } else if (columnIndex == 1) {
                return Strings.SubscriptionKeyDialog.KEY_COLUMN;
            } else {
                return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            } else if (columnIndex == 1) {
                return Subscription.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return apis.get(rowIndex).name;
            } else if (columnIndex == 1) {
                return apis.get(rowIndex).getSubscription();
            } else {
                return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1 && aValue instanceof Subscription) {
                apis.get(rowIndex).setSubscription((Subscription) aValue);
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            //nothing doing
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            //nothing doing
        }
    }

    public class SubscriptionComboBoxCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JComboBox comboBox;
        private final List<Subscription> items;

        SubscriptionComboBoxCellEditor(JComboBox comboBox, List<Subscription> items) {
            this.comboBox = comboBox;
            this.items = items;
        }

        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            comboBox.removeAllItems();
            String id = apis.get(row).id;
            for (Subscription subscription : items) {
                if (subscription.associatedWithApi(id)) {
                    comboBox.addItem(subscription);
                }
            }
            comboBox.setSelectedItem(value);

            return comboBox;
        }
    }

    public SubscriptionKeyInputDialog(List<ApiInfo> apis, List<Subscription> subscriptions) {
        this.apis = apis;
        this.subscriptions = subscriptions;
        dialog = ADialogBuilder.buildDialog(Form.class);

        apiTable = new JTable(new ApiInfoTableModel());

        ListSelectionModel selectionModel = apiTable.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel());

        TableColumn col = apiTable.getColumnModel().getColumn(1);
        col.setCellEditor(new SubscriptionComboBoxCellEditor(comboBox, this.subscriptions));

        apiTable.setRowHeight((int) comboBox.getPreferredSize().getHeight());

        dialog.getFormField(Form.NAME).setProperty("component", new JScrollPane(apiTable));
    }

    public boolean show() {
        boolean res = dialog.show();
        TableCellEditor editor = apiTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }
        return res;
    }

    @Override
    public void close() {
        dialog.release();
    }

    @AForm(name = Strings.SubscriptionKeyDialog.CAPTION, description = Strings.SubscriptionKeyDialog.DESCRIPTION)
    public interface Form {
        @AField(name = "###Name", description = "", type = AField.AFieldType.COMPONENT)
        public final static String NAME = "###Name";

        @AField(name = "###Comment", description = Strings.SubscriptionKeyDialog.REMARK, type = AField.AFieldType.LABEL)
        public final static String COMMENT = "###Comment";
    }
}
