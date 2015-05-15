package com.smartbear.msazuresupport.utils;

import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class ApiSelectorDialog implements AutoCloseable {
    private final List<AzureApi.ApiInfo> apis;
    private final XFormDialog dialog;
    private final JList apiListBox;

    public ApiSelectorDialog(List<AzureApi.ApiInfo> apiList) {
        apis = apiList;

        dialog = ADialogBuilder.buildDialog(SelectAPIFromMsAzureForm.class);
        ListModel<String> listBoxModel = new AbstractListModel<String>() {
            @Override
            public int getSize() {
                return apis.size();
            }

            @Override
            public String getElementAt(int index) {
                return apis.get(index).name;
            }
        };
        apiListBox = new JList(listBoxModel);
        dialog.getFormField(SelectAPIFromMsAzureForm.NAME).setProperty("component", new JScrollPane(apiListBox));
        dialog.getFormField(SelectAPIFromMsAzureForm.NAME).setProperty("preferredSize", new Dimension(500, 150));
        dialog.getFormField(SelectAPIFromMsAzureForm.DESCRIPTION).setProperty("preferredSize", new Dimension(500, 50));
        dialog.setValue(SelectAPIFromMsAzureForm.DESCRIPTION, null);
        dialog.setValue(SelectAPIFromMsAzureForm.SPEC, null);

        apiListBox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] selected = apiListBox.getSelectedIndices();
                if (selected != null && selected.length == 1) {
                    int selectedNo = selected[0];
                    dialog.getFormField(SelectAPIFromMsAzureForm.DESCRIPTION).setValue(apis.get(selectedNo).description);
                    dialog.getFormField(SelectAPIFromMsAzureForm.SPEC).setValue(apis.get(selectedNo).path);
                } else {
                    dialog.getFormField(SelectAPIFromMsAzureForm.DESCRIPTION).setValue(null);
                    dialog.getFormField(SelectAPIFromMsAzureForm.SPEC).setValue(null);
                }
            }
        });
        apiListBox.setSelectedIndex(-1);

        dialog.getFormField(SelectAPIFromMsAzureForm.NAME).addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField formField) {
                int[] selected = apiListBox.getSelectedIndices();
                if (selected == null || selected.length == 0) {
                    return new ValidationMessage[]{new ValidationMessage("Please select at least one API specification to add.", formField)};
                } else {
                    return new ValidationMessage[0];
                }
            }
        });
    }

    public List<AzureApi.ApiInfo> getSelectedApi() {
        if (dialog.show()) {
            ArrayList<AzureApi.ApiInfo> result = new ArrayList<>();
            int[] selected = apiListBox.getSelectedIndices();
            for (int index : selected) {
                result.add(apis.get(index));
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        dialog.release();
    }

    @AForm(name = "Select API to Import", description = "Please select from the list which API specification(s) you want to import to the project.")
    public interface SelectAPIFromMsAzureForm {
        @AField(description = "API Name", type = AField.AFieldType.COMPONENT)
        public final static String NAME = "Name";

        @AField(description = "API Description", type = AField.AFieldType.INFORMATION)
        public final static String DESCRIPTION = "Description";

        @AField(description = "API Definition", type = AField.AFieldType.LABEL)
        public final static String SPEC = "Definition";
    }
}
