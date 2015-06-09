package com.smartbear.msazuresupport.utils;

import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;

public class SubscriptionKeyInputDialog implements AutoCloseable {
    private final List<AzureApi.ApiInfo> apis;
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
                return "API Name";
            } else if (columnIndex == 1) {
                return "Subscription Key";
            } else {
                return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
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
                return apis.get(rowIndex).getSubscriptionKey();
            } else {
                return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                apis.get(rowIndex).setSubscriptionKey(aValue.toString());
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

    public SubscriptionKeyInputDialog(List<AzureApi.ApiInfo> apis) {
        this.apis = apis;
        dialog = ADialogBuilder.buildDialog(Form.class);

        apiTable = new JTable(new ApiInfoTableModel());
        dialog.getFormField(Form.NAME).setProperty("component", new JScrollPane(apiTable));
    }

    public boolean show() {
        return dialog.show();
    }

    @Override
    public void close() {
        dialog.release();
    }

    @AForm(name = "Enter Subscription Key(s) for the selected API", description = "Please enter the Subscription Key for selected to import API.")
    public interface Form {
        @AField(name = "###Name", description = "", type = AField.AFieldType.COMPONENT)
        public final static String NAME = "###Name";

        @AField(name = "###Comment", description = "You can set the Subscription Key value later at the 'Custom properties' tab of the project.", type = AField.AFieldType.LABEL)
        public final static String COMMENT = "###Comment";
    }
}
